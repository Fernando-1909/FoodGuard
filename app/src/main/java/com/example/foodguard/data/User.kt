package com.example.foodguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,
    val name: String,
    val password: String,
    val profileImageUri: String? = null,
    val savingsGoal: Double = 0.0,
    val wasteCountGoal: Int = 0
)
