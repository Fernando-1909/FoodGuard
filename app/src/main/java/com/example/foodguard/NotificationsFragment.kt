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
import com.google.android.material.tabs.TabLayout

class NotificationsFragment : Fragment() {

    private val viewModel: FoodViewModel by activityViewModels()
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
        
        // Observa a lista única de notificações do ViewModel
        viewModel.notificationItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setNotificationFilter(0) // Todos
                    1 -> viewModel.setNotificationFilter(1) // Próximos do vencimento
                    2 -> viewModel.setNotificationFilter(2) // Vencidos
                    3 -> adapter.submitList(emptyList()) // Dicas (ainda não implementado)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Define o filtro inicial baseado na aba selecionada
        viewModel.setNotificationFilter(tabLayout.selectedTabPosition)
    }
}
