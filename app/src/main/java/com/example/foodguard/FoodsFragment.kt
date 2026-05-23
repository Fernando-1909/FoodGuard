package com.example.foodguard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.adapter.FoodAdapter
import com.example.foodguard.viewmodel.FoodViewModel
import java.text.NumberFormat
import java.util.*

class FoodsFragment : Fragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var adapter: FoodAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_foods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvExpiringCount = view.findViewById<TextView>(R.id.tvExpiringCount)
        val tvEstimatedSavings = view.findViewById<TextView>(R.id.tvEstimatedSavings)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFoodItems)

        adapter = FoodAdapter { foodItem ->
            val intent = Intent(requireContext(), FoodDetailActivity::class.java)
            intent.putExtra("food_item", foodItem)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Observer para os alimentos ativos da lista
        viewModel.allActiveItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            
            // Calcula contagem de itens vencendo (em até 3 dias)
            val currentTime = System.currentTimeMillis()
            val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
            val expiringCount = items.count { it.expirationDate - currentTime <= threeDaysMillis && it.expirationDate >= currentTime }
            tvExpiringCount.text = expiringCount.toString()
        }

        // Observer para a economia mensal com lógica de cor para valores negativos
        viewModel.monthlySavings.observe(viewLifecycleOwner) { savings ->
            val value = savings ?: 0.0
            tvEstimatedSavings.text = currencyFormat.format(value)
            
            if (value < 0) {
                tvEstimatedSavings.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_red))
            } else {
                tvEstimatedSavings.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black))
            }
        }
    }
}
