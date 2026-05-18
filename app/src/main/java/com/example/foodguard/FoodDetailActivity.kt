package com.example.foodguard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.foodguard.data.ConservationAI
import com.example.foodguard.data.FoodItem
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FoodDetailActivity : AppCompatActivity() {

    private val viewModel: FoodViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private lateinit var currentFoodItem: FoodItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Força o modo claro independente do sistema
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_food_detail)

        val foodItem = if (android.os.Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("food_item", FoodItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("food_item")
        } ?: return finish()
        
        currentFoodItem = foodItem

        setupToolbar()
        updateUI(currentFoodItem)
        setupActions()

        // Observe changes if any update happens
        viewModel.allActiveItems.observe(this) { items ->
            val updated = items.find { it.id == currentFoodItem.id }
            if (updated != null) {
                currentFoodItem = updated
                updateUI(currentFoodItem)
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun updateUI(item: FoodItem) {
        setupFoodHeader(item)
        setupWarningCard(item)
        setupInfoSection(item)
        setupRecommendations(item)
        
        // Show reminder info if exists
        val btnReminder = findViewById<MaterialButton>(R.id.btnSetReminder)
        if (item.reminderTimestamp != null) {
            btnReminder.text = "Lembrete: ${dateTimeFormat.format(Date(item.reminderTimestamp))}"
            btnReminder.setIconResource(android.R.drawable.ic_lock_idle_alarm)
        } else {
            btnReminder.text = "Definir lembrete"
            btnReminder.setIconResource(android.R.drawable.ic_lock_silent_mode_off)
        }
    }

    private fun setupFoodHeader(item: FoodItem) {
        findViewById<TextView>(R.id.tvDetailName).text = item.name
        findViewById<TextView>(R.id.tvDetailCategory).text = item.category ?: "Geral"
    }

    private fun setupWarningCard(item: FoodItem) {
        val tvWarningTitle = findViewById<TextView>(R.id.tvWarningTitle)
        val tvWarningSubtitle = findViewById<TextView>(R.id.tvWarningSubtitle)
        val cardWarning = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardWarning)

        val daysLeft = getDaysLeft(item.expirationDate)
        
        when {
            daysLeft < 0 -> {
                tvWarningTitle.text = "Vencido"
                tvWarningSubtitle.text = "Este item passou da validade em ${dateFormat.format(Date(item.expirationDate))}"
                cardWarning.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_red))
            }
            daysLeft == 0L -> {
                tvWarningTitle.text = "Vence hoje"
                tvWarningSubtitle.text = "Consuma este item ainda hoje!"
                cardWarning.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_orange))
            }
            else -> {
                tvWarningTitle.text = "Vence em $daysLeft dias"
                tvWarningSubtitle.text = "Consuma ou congele até ${dateFormat.format(Date(item.expirationDate))}"
                cardWarning.setCardBackgroundColor(ContextCompat.getColor(this, R.color.badge_black))
            }
        }
    }

    private fun setupInfoSection(item: FoodItem) {
        val layoutQuantity = findViewById<View>(R.id.layoutQuantity)
        layoutQuantity.findViewById<TextView>(R.id.tvInfoLabel).text = "Quantidade"
        layoutQuantity.findViewById<TextView>(R.id.tvInfoValue).text = item.quantity ?: "Não informada"
        layoutQuantity.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_menu_sort_by_size)

        val layoutLocation = findViewById<View>(R.id.layoutLocation)
        layoutLocation.findViewById<TextView>(R.id.tvInfoLabel).text = "Local de armazenamento"
        layoutLocation.findViewById<TextView>(R.id.tvInfoValue).text = item.storageLocation ?: "Geladeira"
        layoutLocation.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_dialog_map)

        val layoutPurchase = findViewById<View>(R.id.layoutPurchaseDate)
        layoutPurchase.findViewById<TextView>(R.id.tvInfoLabel).text = "Data de compra"
        layoutPurchase.findViewById<TextView>(R.id.tvInfoValue).text = dateFormat.format(Date(item.purchaseDate))
        layoutPurchase.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_menu_my_calendar)

        val layoutExpiry = findViewById<View>(R.id.layoutExpiryDate)
        layoutExpiry.findViewById<TextView>(R.id.tvInfoLabel).text = "Data de validade"
        layoutExpiry.findViewById<TextView>(R.id.tvInfoValue).text = dateFormat.format(Date(item.expirationDate))
        layoutExpiry.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_menu_today)
    }

    private fun setupRecommendations(item: FoodItem) {
        val tipsView = findViewById<TextView>(R.id.tvConservationTips)
        
        // Se o item já tiver dicas personalizadas salvas no banco, usamos elas.
        // Caso contrário, a IA gera as dicas baseadas no nome/categoria.
        if (!item.conservationTips.isNullOrBlank()) {
            tipsView.text = item.conservationTips
        } else {
            lifecycleScope.launch {
                try {
                    val aiTips = ConservationAI.getTips(item.name, item.category)
                    tipsView.text = aiTips.joinToString("\n\n• ", prefix = "• ")
                } catch (e: Exception) {
                    tipsView.text = "Dicas não disponíveis no momento."
                }
            }
        }
    }

    private fun setupActions() {
        findViewById<MaterialButton>(R.id.btnMarkConsumed).setOnClickListener {
            viewModel.markAsConsumed(currentFoodItem.id)
            Toast.makeText(this, "Item consumido!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.btnDiscard).setOnClickListener {
            viewModel.delete(currentFoodItem)
            Toast.makeText(this, "Item descartado!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<MaterialButton>(R.id.btnEditInfo).setOnClickListener {
            val dialog = AddFoodDialogFragment.newInstance(currentFoodItem)
            dialog.show(supportFragmentManager, "EditFoodDialog")
        }

        findViewById<MaterialButton>(R.id.btnAdjustQuantity).setOnClickListener {
            showAdjustQuantityDialog()
        }

        findViewById<MaterialButton>(R.id.btnSetReminder).setOnClickListener {
            if (currentFoodItem.reminderTimestamp != null) {
                showRemoveReminderDialog()
            } else {
                showReminderPicker()
            }
        }
    }

    private fun showRemoveReminderDialog() {
        AlertDialog.Builder(this)
            .setTitle("Remover Lembrete")
            .setMessage("Deseja remover o lembrete definido?")
            .setPositiveButton("Sim") { _, _ ->
                val updated = currentFoodItem.copy(reminderTimestamp = null)
                viewModel.update(updated)
                Toast.makeText(this, "Lembrete removido", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun showAdjustQuantityDialog() {
        val input = TextInputEditText(this)
        input.setText(currentFoodItem.quantity)
        input.hint = "Ex: 500g, 2 unidades"

        AlertDialog.Builder(this)
            .setTitle("Ajustar Quantidade")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val newQty = input.text.toString()
                val updated = currentFoodItem.copy(quantity = newQty)
                viewModel.update(updated)
                Toast.makeText(this, "Quantidade atualizada!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showReminderPicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                
                val reminderTime = calendar.timeInMillis
                val updated = currentFoodItem.copy(reminderTimestamp = reminderTime)
                viewModel.update(updated)
                
                Toast.makeText(this, "Lembrete definido para: ${dateTimeFormat.format(calendar.time)}", Toast.LENGTH_LONG).show()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getDaysLeft(expirationTime: Long): Long {
        val diff = expirationTime - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}
