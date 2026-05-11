package com.example.foodguard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 ORDER BY expirationDate ASC")
    fun getAllActiveItems(userId: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("UPDATE food_items SET isConsumed = 1 WHERE id = :foodItemId")
    suspend fun markAsConsumed(foodItemId: Long)

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND expirationDate < :currentTime ORDER BY expirationDate ASC")
    fun getExpiredItems(userId: String, currentTime: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 0 AND expirationDate >= :currentTime AND expirationDate <= :threshold ORDER BY expirationDate ASC")
    fun getNearExpirationItems(userId: String, currentTime: Long, threshold: Long): Flow<List<FoodItem>>

    @Query("SELECT COUNT(*) FROM food_items WHERE userId = :userId AND isConsumed = 1")
    fun getConsumedCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM food_items WHERE userId = :userId AND isConsumed = 0 AND expirationDate < :currentTime")
    fun getExpiredCount(userId: String, currentTime: Long): Flow<Int>
    
    @Query("SELECT * FROM food_items WHERE userId = :userId AND isConsumed = 1 ORDER BY id DESC")
    fun getConsumedItems(userId: String): Flow<List<FoodItem>>
}
