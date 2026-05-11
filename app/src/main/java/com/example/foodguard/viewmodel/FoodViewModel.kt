package com.example.foodguard.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.foodguard.data.FoodDatabase
import com.example.foodguard.data.FoodItem
import com.example.foodguard.data.FoodRepository
import com.example.foodguard.data.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val userManager = UserManager(application)
    
    private val _currentUserId = MutableStateFlow(userManager.getUserEmail() ?: "")
    private val _notificationFilter = MutableStateFlow(0) // 0: All, 1: Near, 2: Expired, 3: Reminders

    @OptIn(ExperimentalCoroutinesApi::class)
    val allActiveItems: LiveData<List<FoodItem>> = _currentUserId.flatMapLatest { userId ->
        repository.getAllActiveItems(userId)
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notificationItems: LiveData<List<FoodItem>> = combine(_currentUserId, _notificationFilter) { userId, filter ->
        userId to filter
    }.flatMapLatest { (userId, filter) ->
        val currentTime = System.currentTimeMillis()
        when (filter) {
            1 -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 3)
                repository.getNearExpirationItems(userId, currentTime, cal.timeInMillis)
            }
            2 -> repository.getExpiredItems(userId, currentTime)
            3 -> repository.getItemsWithReminders(userId)
            else -> repository.getAllActiveItems(userId)
        }
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val consumedCount: LiveData<Int> = _currentUserId.flatMapLatest { userId ->
        repository.getConsumedCount(userId)
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val consumedItems: LiveData<List<FoodItem>> = _currentUserId.flatMapLatest { userId ->
        repository.getConsumedItems(userId)
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val expiredCount: LiveData<Int> = _currentUserId.flatMapLatest { userId ->
        repository.getExpiredCount(userId, System.currentTimeMillis())
    }.asLiveData()

    init {
        val foodDao = FoodDatabase.getDatabase(application).foodDao()
        repository = FoodRepository(foodDao)
        // Refresh user ID on init to be sure
        _currentUserId.value = userManager.getUserEmail() ?: ""
    }

    fun setNotificationFilter(filter: Int) {
        _notificationFilter.value = filter
    }

    fun getCurrentUserId(): String {
        val email = userManager.getUserEmail() ?: ""
        if (_currentUserId.value != email) {
            _currentUserId.value = email
        }
        return email
    }

    fun insert(foodItem: FoodItem) = viewModelScope.launch(Dispatchers.IO) {
        val currentId = getCurrentUserId()
        val itemWithUser = foodItem.copy(userId = currentId)
        repository.insert(itemWithUser)
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
}
