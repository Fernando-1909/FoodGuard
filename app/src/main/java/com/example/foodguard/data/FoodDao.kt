package com.example.foodguard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND isDiscarded = 0 ORDER BY expirationDate ASC")
    fun getAllActiveItems(userId: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("UPDATE food_items SET isConsumed = 1, consumedDate = :consumedDate WHERE id = :foodItemId")
    suspend fun markAsConsumed(foodItemId: Long, consumedDate: Long)

    @Query("UPDATE food_items SET isDiscarded = 1, discardedDate = :discardedDate WHERE id = :foodItemId")
    suspend fun markAsDiscarded(foodItemId: Long, discardedDate: Long)

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND isDiscarded = 0 AND expirationDate < :currentTime ORDER BY expirationDate ASC")
    fun getExpiredItems(userId: String, currentTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND isDiscarded = 0 AND expirationDate >= :currentTime AND expirationDate <= :threshold ORDER BY expirationDate ASC")
    fun getNearExpirationItems(userId: String, currentTime: Long, threshold: Long): Flow<List<FoodItem>>

    @Query("SELECT COUNT(*) FROM food_items WHERE userId = :userId AND isConsumed = 1")
    fun getConsumedCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM food_items WHERE userId = :userId AND isConsumed = 0 AND isDiscarded = 0 AND expirationDate < :currentTime")
    fun getExpiredCount(userId: String, currentTime: Long): Flow<Int>
    
    @Query("SELECT * FROM food_items WHERE userId = :userId ORDER BY purchaseDate DESC")
    fun getAllItemsOfUser(userId: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 1 ORDER BY consumedDate DESC")
    fun getConsumedItems(userId: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND purchaseDate >= :startTime AND purchaseDate <= :endTime")
    fun getPurchasedItemsInRange(userId: String, startTime: Long, endTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 1 AND consumedDate >= :startTime AND consumedDate <= :endTime")
    fun getConsumedItemsInRange(userId: String, startTime: Long, endTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 1 AND consumedDate >= :startTime AND consumedDate <= :endTime AND consumedDate <= expirationDate")
    fun getConsumedOnTimeInRange(userId: String, startTime: Long, endTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND isDiscarded = 0 AND expirationDate >= :currentTime")
    fun getActiveNotExpired(userId: String, currentTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND (" +
           "(isDiscarded = 1 AND discardedDate >= :startTime AND discardedDate <= :endTime) OR " +
           "(expirationDate >= :startTime AND expirationDate <= :endTime AND expirationDate <= :currentTime AND (consumedDate IS NULL OR consumedDate > expirationDate))" +
           ")")
    fun getExpiredItemsInRange(userId: String, startTime: Long, endTime: Long, currentTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND isDiscarded = 0 AND reminderTimestamp IS NOT NULL ORDER BY reminderTimestamp ASC")
    fun getItemsWithReminders(userId: String): Flow<List<FoodItem>>

    @Query("DELETE FROM food_items")
    suspend fun deleteAll()
}
