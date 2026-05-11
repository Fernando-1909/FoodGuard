package com.example.foodguard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodguard.R
import com.example.foodguard.data.FoodItem
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FoodAdapter(private val onItemClick: (FoodItem) -> Unit) : 
    ListAdapter<FoodItem, FoodAdapter.FoodViewHolder>(FoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FoodViewHolder(
        itemView: View, 
        private val onItemClick: (FoodItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvStorage: TextView = itemView.findViewById(R.id.tvStorageInfo)
        private val tvDate: TextView = itemView.findViewById(R.id.tvExpirationDate)
        private val tvDays: TextView = itemView.findViewById(R.id.tvDaysLeft)
        private val cardDays: MaterialCardView = itemView.findViewById(R.id.cardDays)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(item: FoodItem) {
            tvName.text = item.name
            tvStorage.text = "${item.storageLocation ?: "Geladeira"} • ${item.quantity ?: ""}"
            tvDate.text = dateFormat.format(Date(item.expirationDate))
            
            val daysLeft = getDaysLeft(item.expirationDate)
            tvDays.text = when {
                daysLeft < 0 -> "Vencido"
                daysLeft == 0L -> "Vence hoje"
                else -> "$daysLeft dias"
            }

            // Status color logic
            when {
                daysLeft < 0 -> {
                    cardDays.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.status_red))
                    tvDays.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                daysLeft <= 3 -> {
                    cardDays.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.badge_black))
                    tvDays.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                else -> {
                    cardDays.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.badge_gray))
                    tvDays.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_black))
                }
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun getDaysLeft(expirationTime: Long): Long {
            val diff = expirationTime - System.currentTimeMillis()
            return TimeUnit.MILLISECONDS.toDays(diff)
        }
    }

    class FoodDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean = oldItem == newItem
    }
}
