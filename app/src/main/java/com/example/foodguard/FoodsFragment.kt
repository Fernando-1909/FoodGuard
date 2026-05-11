package com.example.foodguard

import android.content.Intent
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
import com.google.android.material.button.MaterialButton

class FoodsFragment : Fragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_foods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvExpiringCount = view.findViewById<TextView>(R.id.tvExpiringCount)
        val btnAddFood = view.findViewById<MaterialButton>(R.id.btnAddFood)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFoodItems)

        adapter = FoodAdapter { foodItem ->
            val intent = Intent(requireContext(), FoodDetailActivity::class.java)
            intent.putExtra("food_item", foodItem)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        btnAddFood.setOnClickListener {
            val dialog = AddFoodDialogFragment()
            dialog.show(parentFragmentManager, "AddFoodDialog")
        }

        viewModel.allActiveItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            
            // Calculate expiring count (within 3 days)
            val currentTime = System.currentTimeMillis()
            val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
            val expiringCount = items.count { it.expirationDate - currentTime <= threeDaysMillis && it.expirationDate >= currentTime }
            tvExpiringCount.text = expiringCount.toString()
        }
    }
}
