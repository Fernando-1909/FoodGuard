package com.example.foodguard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_items WHERE isConsumed = 0 ORDER BY expirationDate ASC")
    fun getAllActiveItems(): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("UPDATE food_items SET isConsumed = 1 WHERE id = :foodItemId")
    suspend fun markAsConsumed(foodItemId: Long)

    @Query("SELECT * FROM food_items WHERE isConsumed = 0 AND expirationDate <= :threshold ORDER BY expirationDate ASC")
    fun getItemsNearExpiration(threshold: Long): Flow<List<FoodItem>>
}
