package com.example.foodguard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.chip.ChipGroup
import java.text.NumberFormat
import java.util.*

class InsightsFragment : Fragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_insights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val barConsumed = view.findViewById<View>(R.id.barConsumed)
        val barWasted = view.findViewById<View>(R.id.barWasted)
        val tvConsumedValue = view.findViewById<TextView>(R.id.tvConsumedBarValue)
        val tvWastedValue = view.findViewById<TextView>(R.id.tvWastedBarValue)
        val tvEfficiency = view.findViewById<TextView>(R.id.tvEfficiencyRate)
        val tvSavings = view.findViewById<TextView>(R.id.tvInsightsSavingsValue)
        val layoutWastedCategories = view.findViewById<LinearLayout>(R.id.layoutWastedCategories)
        val chipGroupPeriod = view.findViewById<ChipGroup>(R.id.chipGroupPeriod)

        chipGroupPeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            val period = when (checkedIds.firstOrNull()) {
                R.id.chipWeek -> "week"
                R.id.chipMonth -> "month"
                R.id.chipThreeMonths -> "three_months"
                else -> "week"
            }
            viewModel.setFilterPeriod(period)
        }

        // Observe data from ViewModel
        viewModel.insightData.observe(viewLifecycleOwner) { stats ->
            val consumed = stats.consumedCount
            val expired = stats.expiredCount
            val total = consumed + expired

            tvConsumedValue.text = consumed.toString()
            tvWastedValue.text = expired.toString()
            tvSavings.text = currencyFormat.format(stats.estimatedSavings)

            if (total > 0) {
                val efficiency = (consumed.toDouble() / total * 100).toInt()
                tvEfficiency.text = "$efficiency%"

                // Simple bar scaling logic
                val maxHeight = 100 // dp
                val consumedHeight = (consumed.toDouble() / total * maxHeight).toInt().coerceAtLeast(20)
                val wastedHeight = (expired.toDouble() / total * maxHeight).toInt().coerceAtLeast(20)

                val consumedParams = barConsumed.layoutParams
                consumedParams.height = (consumedHeight * resources.displayMetrics.density).toInt()
                barConsumed.layoutParams = consumedParams

                val wastedParams = barWasted.layoutParams
                wastedParams.height = (wastedHeight * resources.displayMetrics.density).toInt()
                barWasted.layoutParams = wastedParams
            } else {
                tvEfficiency.text = "100%"
                
                // Reset bars
                val density = resources.displayMetrics.density
                barConsumed.layoutParams.height = (40 * density).toInt()
                barWasted.layoutParams.height = (40 * density).toInt()
                barConsumed.requestLayout()
                barWasted.requestLayout()
            }
        }

        val tvGoalTitle = view.findViewById<TextView>(R.id.tvGoalTitle)
        val tvGoalProgress = view.findViewById<TextView>(R.id.tvGoalProgress)
        val pbGoal = view.findViewById<ProgressBar>(R.id.pbGoal)
        val tvGoalDescription = view.findViewById<TextView>(R.id.tvGoalDescription)
        val btnEditGoal = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEditGoal)

        btnEditGoal.setOnClickListener {
            showEditGoalDialog()
        }

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

            tvConsumedValue.text = consumed.toString()
            tvWastedValue.text = expired.toString()
            tvSavings.text = currencyFormat.format(stats.estimatedSavings)

            if (total > 0) {
                val efficiency = (consumed.toDouble() / total * 100).toInt()
                tvEfficiency.text = "$efficiency%"

                // Simple bar scaling logic
                val maxHeight = 100 // dp
                val consumedHeight = (consumed.toDouble() / total * maxHeight).toInt().coerceAtLeast(20)
                val wastedHeight = (expired.toDouble() / total * maxHeight).toInt().coerceAtLeast(20)

                val consumedParams = barConsumed.layoutParams
                consumedParams.height = (consumedHeight * resources.displayMetrics.density).toInt()
                barConsumed.layoutParams = consumedParams

                val wastedParams = barWasted.layoutParams
                wastedParams.height = (wastedHeight * resources.displayMetrics.density).toInt()
                barWasted.layoutParams = wastedParams
            } else {
                tvEfficiency.text = "100%"
                
                // Reset bars
                val density = resources.displayMetrics.density
                barConsumed.layoutParams.height = (40 * density).toInt()
                barWasted.layoutParams.height = (40 * density).toInt()
                barConsumed.requestLayout()
                barWasted.requestLayout()
            }

            // Update category waste with real data
            updateWastedCategories(layoutWastedCategories, stats.wastedCategories)
            
            // Update Goal UI with new stats
            viewModel.userFlow.value?.let { updateGoalUI(it, stats) }

            // Update trend
            val tvSavingsTrend = view.findViewById<TextView>(R.id.tvSavingsTrend)
            if (stats.savingsTrend >= 0) {
                tvSavingsTrend.text = "+${stats.savingsTrend}%"
                tvSavingsTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_green))
            } else {
                tvSavingsTrend.text = "${stats.savingsTrend}%"
                tvSavingsTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_red))
            }
        }
    }

    private fun updateGoalUI(user: com.example.foodguard.data.User, stats: FoodViewModel.InsightStats?) {
        val view = view ?: return
        val tvGoalTitle = view.findViewById<TextView>(R.id.tvGoalTitle)
        val tvGoalProgress = view.findViewById<TextView>(R.id.tvGoalProgress)
        val pbGoal = view.findViewById<ProgressBar>(R.id.pbGoal)
        val tvGoalDescription = view.findViewById<TextView>(R.id.tvGoalDescription)

        if (user.savingsGoal > 0) {
            val currentSavings = stats?.estimatedSavings ?: 0.0
            val progress = ((currentSavings / user.savingsGoal) * 100).toInt().coerceIn(0, 100)
            
            tvGoalTitle.text = "Meta de Economia"
            tvGoalProgress.text = "$progress%"
            pbGoal.progress = progress
            tvGoalDescription.text = "Economizado ${currencyFormat.format(currentSavings)} de ${currencyFormat.format(user.savingsGoal)}"
        } else if (user.wasteCountGoal > 0) {
            val currentWaste = stats?.expiredCount ?: 0
            val remaining = (user.wasteCountGoal - currentWaste).coerceAtLeast(0)
            val progress = if (user.wasteCountGoal > 0) 
                ((remaining.toDouble() / user.wasteCountGoal) * 100).toInt().coerceIn(0, 100)
                else 100
            
            tvGoalTitle.text = "Limite de Desperdício"
            tvGoalProgress.text = "$progress%"
            pbGoal.progress = progress
            tvGoalDescription.text = "Você ainda pode desperdiçar $remaining de ${user.wasteCountGoal} itens"
        } else {
            tvGoalTitle.text = "Nenhuma meta definida"
            tvGoalProgress.text = "0%"
            pbGoal.progress = 0
            tvGoalDescription.text = "Clique em editar meta para definir um objetivo"
        }
    }

    private fun showEditGoalDialog() {
        val user = viewModel.userFlow.value ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)
        val etSavingsGoal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSavingsGoal)
        val etWasteGoal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etWasteGoal)

        if (user.savingsGoal > 0) etSavingsGoal.setText(user.savingsGoal.toString())
        if (user.wasteCountGoal > 0) etWasteGoal.setText(user.wasteCountGoal.toString())

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar Meta")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val savings = etSavingsGoal.text.toString().toDoubleOrNull() ?: 0.0
                val waste = etWasteGoal.text.toString().toIntOrNull() ?: 0
                viewModel.updateGoals(savings, waste)
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
                else -> icon.setImageResource(android.R.drawable.ic_dialog_info)
            }

            container.addView(itemView)
        }
    }
}
