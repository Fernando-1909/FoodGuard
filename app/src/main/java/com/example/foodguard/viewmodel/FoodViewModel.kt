package com.example.foodguard.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.foodguard.data.FoodDatabase
import com.example.foodguard.data.FoodItem
import com.example.foodguard.data.FoodRepository
import com.example.foodguard.data.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val userManager = UserManager(application)
    
    private val _currentUserId = MutableStateFlow(userManager.getUserEmail() ?: "")
    private val _notificationFilter = MutableStateFlow(0) // 0: All, 1: Near, 2: Expired

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

    /**
     * FÓRMULA DE ECONOMIA ESTIMADA DO MÊS:
     * Economia = (Valor dos itens consumidos no prazo ou em estoque) - (Valor dos itens vencidos ou descartados).
     * 
     * Se um item vence ou é descartado, seu valor é SUBTRAÍDO da economia total.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlySavings: LiveData<Double> = _currentUserId.flatMapLatest { userId ->
        val calendar = Calendar.getInstance()
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis
        
        // Emite o tempo atual a cada minuto para reavaliar vencimentos em tempo real
        val ticker = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(60000) 
            }
        }.onStart { emit(System.currentTimeMillis()) }

        ticker.flatMapLatest { now ->
            repository.getPurchasedItemsInRange(userId, startOfMonth, endOfMonth).map { items ->
                items.sumOf { item ->
                    val isExpired = item.expirationDate < now
                    val consumedOnTime = item.isConsumed && (item.consumedDate ?: 0L) <= item.expirationDate
                    val activeAndNotExpired = !item.isConsumed && !item.isDiscarded && !isExpired
                    
                    val price = item.price ?: 0.0
                    when {
                        consumedOnTime || activeAndNotExpired -> price
                        isExpired || item.isDiscarded -> -price
                        else -> 0.0
                    }
                }
            }
        }
    }.asLiveData()

    init {
        val foodDao = FoodDatabase.getDatabase(application).foodDao()
        repository = FoodRepository(foodDao)
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
        repository.markAsConsumed(foodItemId, System.currentTimeMillis())
    }

    fun markAsDiscarded(foodItemId: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.markAsDiscarded(foodItemId, System.currentTimeMillis())
    }

    fun clearAllData() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}
