package com.example.foodguard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.adapter.FoodAdapter
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class NotificationsFragment : Fragment() {

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvNotifications)
        adapter = FoodAdapter { foodItem ->
            viewModel.markAsConsumed(foodItem.id)
            Snackbar.make(view, "${foodItem.name} removido das notificações.", Snackbar.LENGTH_LONG)
                .setAction("Desfazer") {
                    viewModel.update(foodItem.copy(isConsumed = false))
                }.show()
        }
        recyclerView.adapter = adapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        
        // Inicialmente mostra todos os itens que estão vencendo (ex: nos próximos 3 dias)
        observeNearExpiration(3)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> observeAll()
                    1 -> observeNearExpiration(3)
                    2 -> observeExpired()
                    3 -> showTips()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeAll() {
        viewModel.allActiveItems.removeObservers(viewLifecycleOwner)
        viewModel.allActiveItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun observeNearExpiration(days: Int) {
        viewModel.allActiveItems.removeObservers(viewLifecycleOwner)
        viewModel.getItemsNearExpiration(days).observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun observeExpired() {
        viewModel.allActiveItems.removeObservers(viewLifecycleOwner)
        // threshold = 0 para itens já vencidos
        viewModel.getItemsNearExpiration(0).observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun showTips() {
        viewModel.allActiveItems.removeObservers(viewLifecycleOwner)
        // TODO: Implementar dicas de conservação reais
        adapter.submitList(emptyList())
    }
}
