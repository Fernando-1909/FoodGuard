package com.example.foodguard.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    fun getAllActiveItems(userId: String): Flow<List<FoodItem>> = foodDao.getAllActiveItems(userId)

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

    fun getNearExpirationItems(userId: String, currentTime: Long, threshold: Long): Flow<List<FoodItem>> {
        return foodDao.getNearExpirationItems(userId, currentTime, threshold)
    }

    fun getExpiredItems(userId: String, currentTime: Long): Flow<List<FoodItem>> {
        return foodDao.getExpiredItems(userId, currentTime)
    }

    fun getConsumedCount(userId: String): Flow<Int> = foodDao.getConsumedCount(userId)
    
    fun getExpiredCount(userId: String, currentTime: Long): Flow<Int> = foodDao.getExpiredCount(userId, currentTime)
    
    fun getConsumedItems(userId: String): Flow<List<FoodItem>> = foodDao.getConsumedItems(userId)

    fun getItemsWithReminders(userId: String): Flow<List<FoodItem>> = foodDao.getItemsWithReminders(userId)
}
