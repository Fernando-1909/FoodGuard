package com.example.foodguard

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foodguard.data.FoodItem
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FoodDetailActivity : AppCompatActivity() {

    private val viewModel: FoodViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_food_detail)

        val foodItem = intent.getParcelableExtra<FoodItem>("food_item") ?: return finish()

        setupToolbar()
        setupFoodHeader(foodItem)
        setupWarningCard(foodItem)
        setupInfoSection(foodItem)
        setupRecommendations(foodItem)
        setupActions(foodItem)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupFoodHeader(item: FoodItem) {
        findViewById<TextView>(R.id.tvDetailName).text = item.name
        findViewById<TextView>(R.id.tvDetailCategory).text = item.category ?: "Geral"
    }

    private fun setupWarningCard(item: FoodItem) {
        val tvWarningTitle = findViewById<TextView>(R.id.tvWarningTitle)
        val tvWarningSubtitle = findViewById<TextView>(R.id.tvWarningSubtitle)
        val cardWarning = findViewById<MaterialCardView>(R.id.cardWarning)

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
        // Quantity
        val layoutQuantity = findViewById<android.view.View>(R.id.layoutQuantity)
        layoutQuantity.findViewById<TextView>(R.id.tvInfoLabel).text = "Quantidade"
        layoutQuantity.findViewById<TextView>(R.id.tvInfoValue).text = item.quantity ?: "Não informada"
        layoutQuantity.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_menu_sort_by_size)

        // Location
        val layoutLocation = findViewById<android.view.View>(R.id.layoutLocation)
        layoutLocation.findViewById<TextView>(R.id.tvInfoLabel).text = "Local de armazenamento"
        layoutLocation.findViewById<TextView>(R.id.tvInfoValue).text = item.storageLocation ?: "Geladeira"
        layoutLocation.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_dialog_map)

        // Purchase Date
        val layoutPurchase = findViewById<android.view.View>(R.id.layoutPurchaseDate)
        layoutPurchase.findViewById<TextView>(R.id.tvInfoLabel).text = "Data de compra"
        layoutPurchase.findViewById<TextView>(R.id.tvInfoValue).text = dateFormat.format(Date(item.purchaseDate))
        layoutPurchase.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_menu_my_calendar)

        // Expiry Date
        val layoutExpiry = findViewById<android.view.View>(R.id.layoutExpiryDate)
        layoutExpiry.findViewById<TextView>(R.id.tvInfoLabel).text = "Data de validade"
        layoutExpiry.findViewById<TextView>(R.id.tvInfoValue).text = dateFormat.format(Date(item.expirationDate))
        layoutExpiry.findViewById<ImageView>(R.id.ivInfoIcon).setImageResource(android.R.drawable.ic_menu_today)
    }

    private fun setupRecommendations(item: FoodItem) {
        findViewById<TextView>(R.id.tvConservationTips).text = item.conservationTips ?: "Nenhuma recomendação disponível."
        findViewById<TextView>(R.id.tvConsumptionSuggestions).text = item.consumptionSuggestions ?: "Nenhuma sugestão disponível."
    }

    private fun setupActions(item: FoodItem) {
        findViewById<MaterialButton>(R.id.btnMarkConsumed).setOnClickListener {
            viewModel.markAsConsumed(item.id)
            finish()
        }

        findViewById<MaterialButton>(R.id.btnDiscard).setOnClickListener {
            viewModel.delete(item)
            finish()
        }
    }

    private fun getDaysLeft(expirationTime: Long): Long {
        val diff = expirationTime - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}
