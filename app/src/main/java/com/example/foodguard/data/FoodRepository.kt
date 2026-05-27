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

    suspend fun markAsConsumed(foodItemId: Long, consumedDate: Long) {
        foodDao.markAsConsumed(foodItemId, consumedDate)
    }

    suspend fun markAsDiscarded(foodItemId: Long, discardedDate: Long) {
        foodDao.markAsDiscarded(foodItemId, discardedDate)
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

    fun getPurchasedItemsInRange(userId: String, startTime: Long, endTime: Long): Flow<List<FoodItem>> {
        return foodDao.getPurchasedItemsInRange(userId, startTime, endTime)
    }

    fun getConsumedItemsInRange(userId: String, startTime: Long, endTime: Long): Flow<List<FoodItem>> {
        return foodDao.getConsumedItemsInRange(userId, startTime, endTime)
    }

    fun getConsumedOnTimeInRange(userId: String, startTime: Long, endTime: Long): Flow<List<FoodItem>> {
        return foodDao.getConsumedOnTimeInRange(userId, startTime, endTime)
    }

    fun getActiveNotExpired(userId: String, currentTime: Long): Flow<List<FoodItem>> {
        return foodDao.getActiveNotExpired(userId, currentTime)
    }

    fun getExpiredItemsInRange(userId: String, startTime: Long, endTime: Long, currentTime: Long): Flow<List<FoodItem>> {
        return foodDao.getExpiredItemsInRange(userId, startTime, endTime, currentTime)
    }

    fun getAllItemsOfUser(userId: String): Flow<List<FoodItem>> = foodDao.getAllItemsOfUser(userId)

    fun getItemsWithReminders(userId: String): Flow<List<FoodItem>> = foodDao.getItemsWithReminders(userId)

    suspend fun deleteAll() {
        foodDao.deleteAll()
    }
}
