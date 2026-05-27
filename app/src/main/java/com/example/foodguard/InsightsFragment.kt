package com.example.foodguard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodguard.databinding.FragmentInsightsBinding
import com.example.foodguard.viewmodel.FoodViewModel
import java.text.NumberFormat
import java.util.*

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FoodViewModel by activityViewModels()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPeriodChips()
        setupGoalButton()
        setupHistoryButton()
        observeData()
    }

    private fun setupHistoryButton() {
        binding.btnEconomyHistory.setOnClickListener {
            val intent = android.content.Intent(requireContext(), HistoryActivity::class.java)
            intent.putExtra("HISTORY_TYPE", "ECONOMY")
            startActivity(intent)
        }
    }

    private fun setupPeriodChips() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()

            // Update visual state for all chips
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as com.google.android.material.chip.Chip
                if (chip.id == checkedId) {
                    chip.setChipBackgroundColorResource(R.color.primary_green)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    chip.chipStrokeWidth = 0f
                } else {
                    chip.setChipBackgroundColorResource(R.color.white)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black))
                    chip.chipStrokeWidth = resources.displayMetrics.density * 1f
                    chip.setChipStrokeColorResource(R.color.divider_gray)
                }
            }

            val period = when (checkedId) {
                R.id.chipWeek -> "week"
                R.id.chipMonth -> "month"
                R.id.chipThreeMonths -> "three_months"
                else -> "week"
            }
            viewModel.setFilterPeriod(period)
        }
    }

    private fun setupGoalButton() {
        binding.btnEditGoal.setOnClickListener {
            showEditGoalDialog()
        }
    }

    private fun observeData() {
        // Observe user for goals
        viewModel.userFlow.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateGoalUI(it, viewModel.insightData.value)
            }
        }

        // Observe data from ViewModel
        viewModel.insightData.observe(viewLifecycleOwner) { stats ->
            val consumed = stats.consumedCount
            val expired = stats.expiredCount
            val total = consumed + expired

            binding.tvConsumedBarValue.text = consumed.toString()
            binding.tvWastedBarValue.text = expired.toString()
            binding.tvInsightsSavingsValue.text = currencyFormat.format(stats.estimatedSavings)

            if (total > 0) {
                val efficiency = (consumed.toDouble() / total * 100).toInt()
                binding.tvEfficiencyRate.text = "$efficiency%"

                // Simple bar scaling logic
                val maxHeight = 100 // dp
                val consumedHeight = (consumed.toDouble() / total * maxHeight).toInt().coerceAtLeast(20)
                val wastedHeight = (expired.toDouble() / total * maxHeight).toInt().coerceAtLeast(20)

                binding.barConsumed.layoutParams.height = (consumedHeight * resources.displayMetrics.density).toInt()
                binding.barConsumed.requestLayout()

                binding.barWasted.layoutParams.height = (wastedHeight * resources.displayMetrics.density).toInt()
                binding.barWasted.requestLayout()
            } else {
                binding.tvEfficiencyRate.text = "100%"
                
                // Reset bars
                val density = resources.displayMetrics.density
                binding.barConsumed.layoutParams.height = (40 * density).toInt()
                binding.barWasted.layoutParams.height = (40 * density).toInt()
                binding.barConsumed.requestLayout()
                binding.barWasted.requestLayout()
            }

            // Update category waste with real data
            updateWastedCategories(binding.layoutWastedCategories, stats.wastedCategories)
            
            // Update Goal UI with new stats
            viewModel.userFlow.value?.let { updateGoalUI(it, stats) }

            // Update trend
            if (stats.savingsTrend >= 0) {
                binding.tvSavingsTrend.text = "+${stats.savingsTrend}%"
                binding.tvSavingsTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_green))
            } else {
                binding.tvSavingsTrend.text = "${stats.savingsTrend}%"
                binding.tvSavingsTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_red))
            }

            // Update suggestions based on waste
            updateSuggestions(stats.wastedCategories)
        }
    }

    private fun updateSuggestions(categories: List<FoodViewModel.WastedCategory>) {
        if (categories.isEmpty()) {
            binding.ivSuggestionIcon.setImageResource(R.drawable.ic_apple)
            binding.tvSuggestionTitle.text = "Tudo sob controle!"
            binding.tvSuggestionDescription.text = "Você não teve desperdícios significativos neste período. Continue assim!"
            return
        }

        val topWaste = categories.first()
        binding.tvSuggestionTitle.text = "Atenção com ${topWaste.name}"
        
        when (topWaste.name) {
            "Carnes" -> {
                binding.ivSuggestionIcon.setImageResource(R.drawable.ic_meat)
                binding.tvSuggestionDescription.text = "Você desperdiçou ${topWaste.count} itens de carne. Tente congelar porções menores para aumentar a durabilidade."
            }
            "Frutas" -> {
                binding.ivSuggestionIcon.setImageResource(R.drawable.ic_apple)
                binding.tvSuggestionDescription.text = "Frutas representam seu maior desperdício (${topWaste.count} itens). Considere comprar frutas em diferentes estágios de maturação."
            }
            "Legumes", "Verduras" -> {
                binding.ivSuggestionIcon.setImageResource(R.drawable.ic_carrot)
                binding.tvSuggestionDescription.text = "Houve desperdício de ${topWaste.count} vegetais. Armazene-os em gavetas úmidas ou use potes herméticos."
            }
            "Padaria" -> {
                binding.ivSuggestionIcon.setImageResource(R.drawable.ic_bread)
                binding.tvSuggestionDescription.text = "Itens de padaria estragaram (${topWaste.count} itens). Pães podem ser congelados e aquecidos na hora do consumo."
            }
            else -> {
                binding.ivSuggestionIcon.setImageResource(R.drawable.ic_apple)
                binding.tvSuggestionDescription.text = "A categoria ${topWaste.name} teve ${topWaste.count} itens desperdiçados. Reveja a frequência de compra desses itens."
            }
        }
    }

    private fun updateGoalUI(user: com.example.foodguard.data.User, stats: FoodViewModel.InsightStats?) {
        val periodLabel = when (viewModel.filterPeriod.value) {
            "week" -> "Semanal"
            "month" -> "Mensal"
            "three_months" -> "Trimestral"
            else -> ""
        }

        var hasAnyGoal = false

        // Savings Goal
        if (user.savingsGoal > 0 && user.savingsGoalPeriod == viewModel.filterPeriod.value) {
            hasAnyGoal = true
            binding.cardSavingsGoal.visibility = View.VISIBLE
            val currentSavings = stats?.estimatedSavings ?: 0.0
            val progress = ((currentSavings / user.savingsGoal) * 100).toInt().coerceIn(0, 100)
            
            binding.tvSavingsGoalTitle.text = "Meta de Economia $periodLabel"
            binding.tvSavingsGoalProgress.text = "$progress%"
            binding.pbSavingsGoal.progress = progress
            binding.tvSavingsGoalDescription.text = "Economizado ${currencyFormat.format(currentSavings.coerceAtLeast(0.0))} de ${currencyFormat.format(user.savingsGoal)}"
        } else {
            binding.cardSavingsGoal.visibility = View.GONE
        }

        // Waste Goal
        if (user.wasteCountGoal > 0 && user.wasteGoalPeriod == viewModel.filterPeriod.value) {
            hasAnyGoal = true
            binding.cardWasteGoal.visibility = View.VISIBLE
            val currentWaste = stats?.expiredCount ?: 0
            
            // O progresso representa quanto do "limite" já foi atingido.
            // Se o limite é 5 e desperdiçou 1, o progresso é 20%.
            val progress = if (user.wasteCountGoal > 0) 
                ((currentWaste.toDouble() / user.wasteCountGoal) * 100).toInt().coerceIn(0, 100)
                else 0
            
            binding.tvWasteGoalTitle.text = "Limite de Desperdício $periodLabel"
            binding.tvWasteGoalProgress.text = "$progress%"
            binding.pbWasteGoal.progress = progress
            
            // Descrição mais clara: "1 de 5 itens desperdiçados"
            binding.tvWasteGoalDescription.text = "$currentWaste de ${user.wasteCountGoal} itens desperdiçados"
            
            // A barra de desperdício agora fica sempre vermelha
            binding.pbWasteGoal.progressTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.status_red)
            )
            binding.pbWasteGoal.progressBackgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor("#FFEBEE") // Um tom de vermelho bem claro para o fundo
            )
        } else {
            binding.cardWasteGoal.visibility = View.GONE
        }

        binding.cardEmptyGoal.visibility = if (hasAnyGoal) View.GONE else View.VISIBLE
        binding.btnEditGoal.text = if (hasAnyGoal) "Editar metas" else "Configurar metas"
    }

    private fun showEditGoalDialog() {
        val user = viewModel.userFlow.value ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)
        
        val etSavingsGoal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSavingsGoal)
        val etSavingsPeriod = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.etSavingsPeriod)
        val etWasteGoal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWasteGoal)
        val etWastePeriod = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.etWastePeriod)

        val periods = arrayOf("Semanal", "Mensal", "Trimestral")
        val periodValues = arrayOf("week", "month", "three_months")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, periods)
        
        etSavingsPeriod.setAdapter(adapter)
        etWastePeriod.setAdapter(adapter)

        // Set current values
        if (user.savingsGoal > 0) etSavingsGoal.setText(user.savingsGoal.toString())
        val currentSavingsPeriodIndex = periodValues.indexOf(user.savingsGoalPeriod).coerceAtLeast(0)
        etSavingsPeriod.setText(periods[currentSavingsPeriodIndex], false)

        if (user.wasteCountGoal > 0) etWasteGoal.setText(user.wasteCountGoal.toString())
        val currentWastePeriodIndex = periodValues.indexOf(user.wasteGoalPeriod).coerceAtLeast(0)
        etWastePeriod.setText(periods[currentWastePeriodIndex], false)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Configurar Metas")
            .setMessage("Você pode definir uma meta de economia, um limite de desperdício ou ambos.")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val savings = etSavingsGoal.text.toString().toDoubleOrNull() ?: 0.0
                val selectedSavingsPeriod = periodValues[periods.indexOf(etSavingsPeriod.text.toString()).coerceAtLeast(0)]
                
                val waste = etWasteGoal.text.toString().toIntOrNull() ?: 0
                val selectedWastePeriod = periodValues[periods.indexOf(etWastePeriod.text.toString()).coerceAtLeast(0)]
                
                viewModel.updateGoals(savings, selectedSavingsPeriod, waste, selectedWastePeriod)
                Toast.makeText(context, "Metas atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateWastedCategories(container: LinearLayout, categories: List<FoodViewModel.WastedCategory>) {
        container.removeAllViews()
        if (categories.isEmpty()) {
            val emptyText = TextView(context).apply {
                text = "Nenhum desperdício registrado no período"
                setPadding(0, 16, 0, 16)
                alpha = 0.6f
            }
            container.addView(emptyText)
            return
        }

        for (data in categories) {
            val itemView = layoutInflater.inflate(R.layout.item_wasted_category, container, false)
            itemView.findViewById<TextView>(R.id.tvCategoryName).text = data.name
            itemView.findViewById<TextView>(R.id.tvWastedCount).text = "${data.count} ${if (data.count == 1) "item" else "itens"}"
            itemView.findViewById<TextView>(R.id.tvWastedItemsList).text = "${data.items.joinToString(", ")} desperdiçados"
            
            val icon = itemView.findViewById<ImageView>(R.id.ivCategoryIcon)
            when (data.name) {
                "Carnes" -> icon.setImageResource(R.drawable.ic_meat)
                "Frutas" -> icon.setImageResource(R.drawable.ic_apple)
                "Legumes", "Verduras" -> icon.setImageResource(R.drawable.ic_carrot)
                "Padaria" -> icon.setImageResource(R.drawable.ic_bread)
                else -> icon.setImageResource(R.drawable.ic_apple)
            }

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
