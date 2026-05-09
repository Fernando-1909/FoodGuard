package com.example.foodguard.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    val allActiveItems: Flow<List<FoodItem>> = foodDao.getAllActiveItems()

    suspend fun insert(foodItem: FoodItem) {
        foodDao.insert(foodItem)
    }

    suspend fun update(foodItem: FoodItem) {
        foodDao.update(foodItem)
    }

    suspend fun delete(foodItem: FoodItem) {
        foodDao.delete(foodItem)
    }

    suspend fun markAsConsumed(foodItemId: Long) {
        foodDao.markAsConsumed(foodItemId)
    }

    fun getItemsNearExpiration(threshold: Long): Flow<List<FoodItem>> {
        return foodDao.getItemsNearExpiration(threshold)
    }
}
