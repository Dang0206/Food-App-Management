package com.example.finalproject.presentation.screen.food

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.entity.Food
import com.example.finalproject.notification.AlarmManagerNotificationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class FoodViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val TAG = "FoodViewModel"
    private val foodDao = AppDatabase.getDatabase(application).foodDao()
    private val groupFoodDao = AppDatabase.getDatabase(application).groupFoodDao()

    var state by mutableStateOf(FoodState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentJob: Job? = null

    init {
        loadFoods()
        loadGroupFoods()
    }

    private fun loadFoods() {
        // Cancel any existing job
        currentJob?.cancel()

        currentJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Loading all foods")
                state = state.copy(isLoading = true)
                foodDao.getAllFood().collect { foods ->
                    Log.d(TAG, "Loaded ${foods.size} foods")
                    state = state.copy(
                        foods = foods,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading foods: ${e.message}")
                state = state.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun loadFoodsByGroup(groupId: Long) {
        // Cancel any existing job
        currentJob?.cancel()

        Log.d(TAG, "Starting loadFoodsByGroup for groupId: $groupId")

        currentJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Loading foods for group: $groupId")
                state = state.copy(isLoading = true)

                // Get foods by group - Using collect() to OBSERVE CHANGES
                foodDao.getFoodByGroupId(groupId).collect { foods ->
                    Log.d(TAG, "Raw query result: ${foods.size} foods found")
                    foods.forEach { food ->
                        Log.d(TAG, "Food found: ${food.name}, GroupId: ${food.groupFoodId}, ID: ${food.id}")
                    }

                    // Verify filtering is working
                    val filteredFoods = foods.filter { it.groupFoodId == groupId }
                    Log.d(TAG, "After filtering: ${filteredFoods.size} foods for group $groupId")

                    state = state.copy(
                        foods = foods,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading foods for group $groupId: ${e.message}")
                state = state.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun loadGroupFoods() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading group foods")
                groupFoodDao.getAllGroupFood().collect { groupFoods ->
                    Log.d(TAG, "Loaded ${groupFoods.size} group foods")
                    groupFoods.forEach { group ->
                        Log.d(TAG, "Group: ${group.name} (ID: ${group.id})")
                    }
                    state = state.copy(groupFoods = groupFoods)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading group foods: ${e.message}")
                _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't load group foods"))
            }
        }
    }

    /**
     * Reset form state - useful when navigating between screens
     */
    fun resetFormState() {
        Log.d(TAG, "Resetting form state")
        state = state.copy(
            id = 0,
            name = "",
            expiryDate = null,
            remindBefore = null,
            notifyTime = null,
            remindOptionId = 1,
            groupFoodId = null,
            note = null,
            // Don't reset foods and groupFoods to save data
        )
    }

    fun onEvent(event: FoodEvent) {
        when (event) {
            is FoodEvent.OnNameChange -> {
                state = state.copy(name = event.name)
            }

            is FoodEvent.OnExpiryDateChange -> {
                state = state.copy(expiryDate = event.date)
            }

            is FoodEvent.OnRemindBeforeChange -> {
                state = state.copy(remindBefore = event.days)
                val option = RemindBeforeOption.getByDays(event.days)
                state = state.copy(remindOptionId = option.id)
            }

            is FoodEvent.OnRemindOptionIdChange -> {
                state = state.copy(remindOptionId = event.optionId)
                val option = RemindBeforeOption.getById(event.optionId)
                if (option != RemindBeforeOption.OTHER) {
                    state = state.copy(remindBefore = option.days)
                }
            }

            is FoodEvent.OnNotifyTimeChange -> {
                state = state.copy(notifyTime = event.time)
            }

            is FoodEvent.OnGroupFoodIdChange -> {
                state = state.copy(groupFoodId = event.groupId)
            }

            is FoodEvent.OnNoteChange -> {
                state = state.copy(note = event.note)
            }



            is FoodEvent.OnSaveFood -> {
                viewModelScope.launch {
                    try {
                        val food = Food(
                            name = state.name,
                            expiryDate = state.expiryDate,
                            remindBefore = state.remindBefore,
                            notifyTime = state.notifyTime,
                            remindOptionId = state.remindOptionId,
                            groupFoodId = state.groupFoodId,
                            note = state.note,
                            createdBy = null,
                            updatedBy = null,
                            deletedDate = null,
                            deletedBy = null
                        )
                        val insertedId = foodDao.insert(food)

                        val insertedFood = food.copy(id = insertedId)

                        // Use AlarmManager for precise notifications
                        AlarmManagerNotificationService.scheduleNotificationsForFood(getApplication(), insertedFood)

                        // Reset form state after successful save
                        resetFormState()

                        _eventFlow.emit(UiEvent.SaveFood)
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't save food"))
                    }
                }
            }

            is FoodEvent.OnDeleteFood -> {
                viewModelScope.launch {
                    try {
                        foodDao.delete(event.food)
                        AlarmManagerNotificationService.cancelNotificationsForFood(getApplication(), event.food.id)
                        _eventFlow.emit(UiEvent.ShowSuccess("Food deleted successfully"))
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't delete food"))
                    }
                }
            }

            is FoodEvent.OnSoftDeleteFood -> {
                viewModelScope.launch {
                    try {
                        foodDao.softDelete(event.id, System.currentTimeMillis(), 0)
                        AlarmManagerNotificationService.cancelNotificationsForFood(getApplication(), event.id)
                        _eventFlow.emit(UiEvent.ShowSuccess("Food removed"))
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't remove food"))
                    }
                }
            }

            is FoodEvent.GetFoodById -> {
                viewModelScope.launch {
                    try {
                        val food = foodDao.getFoodById(event.id)
                        food?.let {
                            state = state.copy(
                                id = it.id,
                                name = it.name,
                                expiryDate = it.expiryDate,
                                remindBefore = it.remindBefore,
                                remindOptionId = it.remindOptionId ?: RemindBeforeOption.getByDays(it.remindBefore).id,
                                notifyTime = it.notifyTime,
                                groupFoodId = it.groupFoodId,
                                note = it.note

                            )
                        }
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't load food"))
                    }
                }
            }

            is FoodEvent.GetFoodsByGroupId -> {
                Log.d(TAG, "Event: GetFoodsByGroupId(${event.groupId})")
                loadFoodsByGroup(event.groupId)
            }

            FoodEvent.LoadAllFoods -> {
                Log.d(TAG, "Event: LoadAllFoods")
                loadFoods()
            }

            FoodEvent.LoadAllGroupFoods -> {
                Log.d(TAG, "Event: LoadAllGroupFoods")
                loadGroupFoods()
            }

            is FoodEvent.OnUpdateFood -> {
                viewModelScope.launch {
                    try {
                        foodDao.update(event.food)

                        // Use AlarmManager to reschedule notifications for updated food
                        AlarmManagerNotificationService.scheduleNotificationsForFood(getApplication(), event.food)

                        // Reset form state after successful update
                        resetFormState()

                        // Reload foods list
                        loadFoods()

                        _eventFlow.emit(UiEvent.ShowSuccess("Food updated successfully"))
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't update food"))
                    }
                }
            }

            FoodEvent.ResetFormState -> {
                Log.d(TAG, "Event: ResetFormState")
                resetFormState()
            }
        }
    }

    /**
     * Schedule all notifications using AlarmManager
     */
    fun scheduleAllNotifications() {
        viewModelScope.launch {
            try {
                AlarmManagerNotificationService.rescheduleAllNotifications(getApplication())
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't schedule notifications"))
            }
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class ShowSuccess(val message: String) : UiEvent()
        data object SaveFood : UiEvent()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FoodViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}