package com.example.foodguard.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val expirationDate: Long,
    val purchaseDate: Long = System.currentTimeMillis(),
    val category: String? = null,
    val quantity: String? = null,
    val storageLocation: String? = "Geladeira",
    val isConsumed: Boolean = false,
    val conservationTips: String? = null,
    val reminderTimestamp: Long? = null
) : Parcelable
