package com.example.foodguard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.R
import com.example.foodguard.data.FoodItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodAdapter(private val onConsumeClick: (FoodItem) -> Unit) : 
    ListAdapter<FoodItem, FoodAdapter.FoodViewHolder>(FoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view, onConsumeClick)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FoodViewHolder(
        itemView: View, 
        private val onConsumeClick: (FoodItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvExpirationDate)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val btnConsume: ImageButton = itemView.findViewById(R.id.btnConsume)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(item: FoodItem) {
            tvName.text = item.name
            tvDate.text = "Vence em: ${dateFormat.format(Date(item.expirationDate))}"
            tvCategory.text = item.category ?: "Geral"
            
            // Check if expired
            if (item.expirationDate < System.currentTimeMillis()) {
                tvDate.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            } else {
                tvDate.setTextColor(itemView.context.getColor(R.color.text_gray))
            }

            btnConsume.setOnClickListener {
                onConsumeClick(item)
            }
        }
    }

    class FoodDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
}
