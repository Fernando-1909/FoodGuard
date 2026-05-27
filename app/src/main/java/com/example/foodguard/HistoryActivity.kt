package com.example.foodguard

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.data.FoodItem
import com.example.foodguard.data.UserManager
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: FoodViewModel
    private lateinit var adapter: HistoryAdapter
    private lateinit var userManager: UserManager
    private var historyType: String = "ALL" // "ALL" or "ECONOMY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        userManager = UserManager(this)
        historyType = intent.getStringExtra("HISTORY_TYPE") ?: "ALL"
        
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarHistory)
        toolbar.title = if (historyType == "ECONOMY") "Histórico de Economia" else "Histórico de Alimentos"
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // FoodViewModel é um AndroidViewModel, não precisa de factory customizada se usar a padrão
        viewModel = ViewModelProvider(this)[FoodViewModel::class.java]

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutHistory)
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        adapter = HistoryAdapter()
        rvHistory.adapter = adapter

        if (historyType == "ECONOMY") {
            tabLayout.visibility = View.VISIBLE
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val period = when (tab?.position) {
                        0 -> "week"
                        1 -> "month"
                        2 -> "three_months"
                        else -> "week"
                    }
                    observeEconomyData(period)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
            observeEconomyData("week")
        } else {
            tabLayout.visibility = View.GONE
            viewModel.allItems.observe(this) { items ->
                adapter.submitList(items, false)
            }
        }
    }

    private fun observeEconomyData(period: String) {
        val calendar = Calendar.getInstance()
        val endTime = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = when (period) {
            "week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.timeInMillis
            }
            "month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            "three_months" -> {
                calendar.add(Calendar.MONTH, -2)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            else -> 0L
        }

        viewModel.allItems.observe(this) { items ->
            val filtered = items.filter { item ->
                val date = item.consumedDate ?: item.discardedDate ?: item.expirationDate
                date in startTime..endTime && (item.isConsumed || item.isDiscarded || item.expirationDate < endTime)
            }.sortedByDescending { it.consumedDate ?: it.discardedDate ?: it.expirationDate }
            adapter.submitList(filtered, true)
        }
    }

    class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
        private var items = listOf<FoodItem>()
        private var isEconomyMode = false
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun submitList(newList: List<FoodItem>, economyMode: Boolean) {
            items = newList
            isEconomyMode = economyMode
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_food, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, isEconomyMode, dateFormat)
        }

        override fun getItemCount() = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivIcon = view.findViewById<ImageView>(R.id.ivHistoryIcon)
            private val tvName = view.findViewById<TextView>(R.id.tvHistoryName)
            private val tvStatus = view.findViewById<TextView>(R.id.tvHistoryStatus)
            private val tvPrice = view.findViewById<TextView>(R.id.tvHistoryPrice)
            private val tvType = view.findViewById<TextView>(R.id.tvHistoryType)

            fun bind(item: FoodItem, isEconomy: Boolean, dateFormat: SimpleDateFormat) {
                tvName?.text = item.name
                val price = item.price ?: 0.0
                tvPrice?.text = String.format(Locale("pt", "BR"), "R$ %.2f", price)

                ivIcon?.setImageResource(when(item.category) {
                    "Frutas" -> R.drawable.ic_apple
                    "Vegetais", "Legumes", "Verduras" -> R.drawable.ic_carrot
                    "Carnes" -> R.drawable.ic_meat
                    "Padaria" -> R.drawable.ic_bread
                    else -> R.drawable.ic_apple
                })

                val context = itemView.context
                if (item.isConsumed) {
                    val date = item.consumedDate?.let { dateFormat.format(Date(it)) } ?: ""
                    tvStatus?.text = context.getString(R.string.history_consumed, date)
                    tvStatus?.setTextColor(context.getColor(R.color.text_gray))
                    tvPrice?.setTextColor(context.getColor(R.color.primary_green))
                    tvType?.text = context.getString(R.string.history_type_savings)
                    ivIcon?.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.primary_light_green))
                } else if (item.isDiscarded || (item.expirationDate < System.currentTimeMillis())) {
                    val date = item.discardedDate?.let { dateFormat.format(Date(it)) } ?: dateFormat.format(Date(item.expirationDate))
                    val statusRes = if (item.isDiscarded) R.string.history_discarded else R.string.history_expired
                    tvStatus?.text = context.getString(statusRes, date)
                    tvStatus?.setTextColor(context.getColor(R.color.status_red))
                    tvPrice?.setTextColor(context.getColor(R.color.status_red))
                    tvType?.text = context.getString(R.string.history_type_loss)
                    ivIcon?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                } else if (item.lastModifiedDate != null && item.lastModifiedDate > item.purchaseDate) {
                    val date = dateFormat.format(Date(item.lastModifiedDate))
                    tvStatus?.text = context.getString(R.string.history_edited, date)
                    tvStatus?.setTextColor(context.getColor(R.color.text_gray))
                    tvPrice?.setTextColor(context.getColor(R.color.primary_black))
                    tvType?.text = context.getString(R.string.history_type_stock)
                    ivIcon?.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.background_gray))
                } else {
                    val date = dateFormat.format(Date(item.purchaseDate))
                    tvStatus?.text = context.getString(R.string.history_added, date)
                    tvStatus?.setTextColor(context.getColor(R.color.text_gray))
                    tvPrice?.setTextColor(context.getColor(R.color.primary_black))
                    tvType?.text = context.getString(R.string.history_type_stock)
                    ivIcon?.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.background_gray))
                }
            }
        }
    }
}
