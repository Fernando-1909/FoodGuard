package com.example.foodguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodguard.data.FoodDatabase
import com.example.foodguard.data.FoodItem
import com.example.foodguard.data.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    val allActiveItems: LiveData<List<FoodItem>>

    init {
        val foodDao = FoodDatabase.getDatabase(application).foodDao()
        repository = FoodRepository(foodDao)
        allActiveItems = repository.allActiveItems.asLiveData()
    }

    fun insert(foodItem: FoodItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(foodItem)
    }

    fun update(foodItem: FoodItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(foodItem)
    }

    fun delete(foodItem: FoodItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(foodItem)
    }

    fun markAsConsumed(foodItemId: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.markAsConsumed(foodItemId)
    }

    fun getItemsNearExpiration(daysThreshold: Int): LiveData<List<FoodItem>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysThreshold)
        val threshold = calendar.timeInMillis
        return repository.getItemsNearExpiration(threshold).asLiveData()
    }
}
