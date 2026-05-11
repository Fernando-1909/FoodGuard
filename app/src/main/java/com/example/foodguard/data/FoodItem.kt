package com.example.foodguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // ID do usuário proprietário do item
    val name: String,
    val expirationDate: Long, // Almacenado como timestamp
    val category: String? = null,
    val quantity: String? = null,
    val isConsumed: Boolean = false
)
