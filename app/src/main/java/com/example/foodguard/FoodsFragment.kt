package com.example.foodguard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.adapter.FoodAdapter
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.snackbar.Snackbar

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

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFoodItems)
        adapter = FoodAdapter { foodItem ->
            viewModel.markAsConsumed(foodItem.id)
            Snackbar.make(view, "${foodItem.name} consumido!", Snackbar.LENGTH_LONG)
                .setAction("Desfazer") {
                    viewModel.update(foodItem.copy(isConsumed = false))
                }.show()
        }
        recyclerView.adapter = adapter

        viewModel.allActiveItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }
}
