package com.example.foodguard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.adapter.FoodAdapter
import com.example.foodguard.data.FoodItem
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var adapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rvFoodItems)
        adapter = FoodAdapter()
        recyclerView.adapter = adapter

        // Observar dados do ViewModel
        viewModel.allActiveItems.observe(this) { items ->
            adapter.submitList(items)
        }

        // Configurar FAB para adicionar item de teste
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 7) // Vence em 1 semana

            val newItem = FoodItem(
                name = "Item de Teste ${System.currentTimeMillis() % 1000}",
                expirationDate = calendar.timeInMillis,
                category = "Geral",
                quantity = "1 unid"
            )
            viewModel.insert(newItem)
        }
    }
}
