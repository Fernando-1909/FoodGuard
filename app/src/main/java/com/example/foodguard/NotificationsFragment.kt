package com.example.foodguard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.adapter.FoodAdapter
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var adapter: FoodAdapter
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

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
            showNotificationOptions(foodItem)
        }
        recyclerView.adapter = adapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        
        viewModel.notificationItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setNotificationFilter(0) // Todos
                    1 -> viewModel.setNotificationFilter(1) // Próximos do vencimento
                    2 -> viewModel.setNotificationFilter(2) // Vencidos
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showNotificationOptions(foodItem: com.example.foodguard.data.FoodItem) {
        val reminderText = if (foodItem.reminderTimestamp != null) {
            "Lembrete agendado para: ${dateTimeFormat.format(Date(foodItem.reminderTimestamp))}"
        } else {
            "Este item não possui um lembrete personalizado definido."
        }

        AlertDialog.Builder(requireContext())
            .setTitle(foodItem.name)
            .setMessage(reminderText)
            .setNeutralButton("Ver Detalhes") { _, _ ->
                val intent = Intent(requireContext(), FoodDetailActivity::class.java)
                intent.putExtra("food_item", foodItem)
                startActivity(intent)
            }
            .setNegativeButton("Excluir Lembrete") { _, _ ->
                if (foodItem.reminderTimestamp != null) {
                    viewModel.update(foodItem.copy(reminderTimestamp = null))
                    Toast.makeText(context, "Lembrete removido!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Não há lembrete para remover.", Toast.LENGTH_SHORT).show()
                }
            }
            .setPositiveButton("Fechar", null)
            .show()
    }
}
