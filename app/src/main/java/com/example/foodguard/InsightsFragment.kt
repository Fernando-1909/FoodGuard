package com.example.foodguard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.adapter.FoodAdapter
import com.example.foodguard.viewmodel.FoodViewModel

class InsightsFragment : Fragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_insights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvConsumedCount = view.findViewById<TextView>(R.id.tvConsumedCount)
        val tvExpiredCount = view.findViewById<TextView>(R.id.tvExpiredCount)
        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)

        adapter = FoodAdapter { /* No action needed in history list or maybe restore? */ }
        rvHistory.adapter = adapter

        viewModel.consumedCount.observe(viewLifecycleOwner) { count ->
            tvConsumedCount.text = count?.toString() ?: "0"
        }

        viewModel.expiredCount.observe(viewLifecycleOwner) { count ->
            tvExpiredCount.text = count?.toString() ?: "0"
        }

        viewModel.consumedItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }
}
