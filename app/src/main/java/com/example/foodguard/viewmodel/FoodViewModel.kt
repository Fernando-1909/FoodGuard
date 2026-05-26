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
    private val _filterPeriod = MutableStateFlow("week") // week, month, three_months

    private val _currentUser = MutableStateFlow<com.example.foodguard.data.User?>(null)
    val currentUser: StateFlow<com.example.foodguard.data.User?> = _currentUser.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val userFlow: LiveData<com.example.foodguard.data.User?> = _currentUserId.flatMapLatest { email ->
        if (email.isEmpty()) flowOf(null)
        else FoodDatabase.getDatabase(getApplication()).userDao().getUserByEmailFlow(email)
    }.asLiveData()

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
                    val wasWasted = item.isDiscarded || (isExpired && !item.isConsumed)
                    
                    val price = item.price ?: 0.0
                    when {
                        consumedOnTime || activeAndNotExpired -> price
                        wasWasted -> -price
                        else -> 0.0
                    }
                }
            }
        }
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val insightData: LiveData<InsightStats> = combine(_currentUserId, _filterPeriod) { userId, period ->
        userId to period
    }.flatMapLatest { (userId, period) ->
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val endTime = System.currentTimeMillis()
        val startTime = when (period) {
            "week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.timeInMillis
            }
            "month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            "three_months" -> {
                calendar.add(Calendar.MONTH, -2)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            else -> 0L
        }

        // Previous period logic for trend
        val prevCalendar = Calendar.getInstance()
        prevCalendar.timeInMillis = startTime
        when (period) {
            "week" -> prevCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            "month" -> prevCalendar.add(Calendar.MONTH, -1)
            "three_months" -> prevCalendar.add(Calendar.MONTH, -3)
        }
        val prevStartTime = prevCalendar.timeInMillis
        val prevEndTime = startTime - 1

        combine(
            repository.getConsumedItemsInRange(userId, startTime, endTime),
            repository.getExpiredItemsInRange(userId, startTime, endTime, now),
            repository.getActiveNotExpired(userId, now),
            repository.getPurchasedItemsInRange(userId, startTime, endTime),
            repository.getConsumedItemsInRange(userId, prevStartTime, prevEndTime),
            repository.getExpiredItemsInRange(userId, prevStartTime, prevEndTime, now)
        ) { flows ->
            val consumed = flows[0] as List<FoodItem>
            val expired = flows[1] as List<FoodItem>
            val active = flows[2] as List<FoodItem>
            // flows[3] is purchased, not used here
            val prevConsumed = flows[4] as List<FoodItem>
            val prevExpired = flows[5] as List<FoodItem>

            val consumedCount = consumed.size
            val expiredCount = expired.size
            
            // Economia: valor dos itens consumidos no prazo + valor dos itens ativos - valor dos itens desperdiçados
            val savings = (consumed.sumOf { if ((it.consumedDate ?: 0) <= it.expirationDate) (it.price ?: 0.0) else 0.0 }) +
                         (active.sumOf { it.price ?: 0.0 }) -
                         (expired.sumOf { it.price ?: 0.0 })

            // Previous month savings calculation
            val prevSavings = (prevConsumed.sumOf { if ((it.consumedDate ?: 0) <= it.expirationDate) (it.price ?: 0.0) else 0.0 }) -
                             (prevExpired.sumOf { it.price ?: 0.0 })

            val trend = if (prevSavings > 0) {
                ((savings - prevSavings) / prevSavings * 100).toInt()
            } else if (savings > 0) {
                100
            } else {
                0
            }

            val wastedCats = expired.groupBy { it.category ?: "Outros" }
                .map { (cat, items) ->
                    WastedCategory(cat, items.map { it.name }, items.size)
                }.sortedByDescending { it.count }

            InsightStats(consumedCount, expiredCount, savings, wastedCats, trend)
        }
    }.asLiveData()

    data class InsightStats(
        val consumedCount: Int,
        val expiredCount: Int,
        val estimatedSavings: Double,
        val wastedCategories: List<WastedCategory> = emptyList(),
        val savingsTrend: Int = 0
    )

    data class WastedCategory(
        val name: String,
        val items: List<String>,
        val count: Int
    )

    init {
        val database = FoodDatabase.getDatabase(application)
        val foodDao = database.foodDao()
        repository = FoodRepository(foodDao)
        _currentUserId.value = userManager.getUserEmail() ?: ""
        
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val email = userManager.getUserEmail()
        if (email != null) {
            viewModelScope.launch {
                val user = FoodDatabase.getDatabase(getApplication()).userDao().getUserByEmail(email)
                _currentUser.value = user
            }
        }
    }

    fun updateUser(user: com.example.foodguard.data.User) = viewModelScope.launch(Dispatchers.IO) {
        FoodDatabase.getDatabase(getApplication()).userDao().update(user)
        _currentUser.value = user
    }

    fun setNotificationFilter(filter: Int) {
        _notificationFilter.value = filter
    }

    fun setFilterPeriod(period: String) {
        _filterPeriod.value = period
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

    fun updateGoals(savingsGoal: Double, wasteCountGoal: Int) = viewModelScope.launch(Dispatchers.IO) {
        val user = _currentUser.value ?: return@launch
        val updatedUser = user.copy(savingsGoal = savingsGoal, wasteCountGoal = wasteCountGoal)
        updateUser(updatedUser)
    }
}
