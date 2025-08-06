package com.example.finalproject.presentation.screen.groupfood

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.entity.GroupFood
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GroupFoodViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val groupFoodDao = AppDatabase.getDatabase(application).groupFoodDao()
    private val foodDao = AppDatabase.getDatabase(application).foodDao()

    var state by mutableStateOf(GroupFoodState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadGroupFoods()
    }

    private fun loadGroupFoods() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)

                // Combine group foods with their food counts
                combine(
                    groupFoodDao.getAllGroupFood(),
                    foodDao.getAllFood()
                ) { groups, foods ->
                    val foodCounts = foods
                        .filter { it.deletedDate == null }
                        .groupBy { it.groupFoodId }
                        .mapValues { it.value.size }
                        .filterKeys { it != null }
                        .mapKeys { it.key!! }

                    groups to foodCounts
                }.collect { (groups, counts) ->
                    state = state.copy(
                        groupFoods = groups,
                        foodCounts = counts,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: GroupFoodEvent) {
        when (event) {
            is GroupFoodEvent.OnNameChange -> {
                state = state.copy(name = event.name)
            }

            is GroupFoodEvent.OnIconChange -> {
                state = state.copy(icon = event.icon)
            }

            is GroupFoodEvent.OnSaveGroupFood -> {
                viewModelScope.launch {
                    try {
                        val groupFood = GroupFood(
                            name = state.name,
                            icon = state.icon,
                            createdBy = null,
                            updatedBy = null,
                            deletedDate = null,
                            deletedBy = null
                        )
                        groupFoodDao.insert(groupFood)
                        _eventFlow.emit(UiEvent.SaveGroupFood)
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't save category"))
                    }
                }
            }

            is GroupFoodEvent.OnDeleteGroupFood -> {
                viewModelScope.launch {
                    try {
                        groupFoodDao.delete(event.groupFood)
                        _eventFlow.emit(UiEvent.ShowSuccess("Category deleted successfully"))
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't delete category"))
                    }
                }
            }

            is GroupFoodEvent.OnSoftDeleteGroupFood -> {
                viewModelScope.launch {
                    try {
                        groupFoodDao.softDelete(event.id, System.currentTimeMillis(), 0)
                        _eventFlow.emit(UiEvent.ShowSuccess("Category removed"))
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't remove category"))
                    }
                }
            }

            is GroupFoodEvent.GetGroupFoodById -> {
                viewModelScope.launch {
                    try {
                        val groupFood = groupFoodDao.getGroupFoodById(event.id)
                        groupFood?.let {
                            state = state.copy(
                                id = it.id,
                                name = it.name,
                                icon = it.icon ?: ""
                            )
                        }
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't load category"))
                    }
                }
            }

            GroupFoodEvent.LoadAllGroupFoods -> loadGroupFoods()

            is GroupFoodEvent.OnUpdateGroupFood -> {
                viewModelScope.launch {
                    try {
                        // Update the group food
                        groupFoodDao.update(event.groupFood)

                        // Reset state to avoid storing old data
                        state = GroupFoodState()

                        // Reload the list
                        loadGroupFoods()

                        // Emit success event
                        _eventFlow.emit(UiEvent.ShowSuccess("Category updated successfully"))
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowError(e.message ?: "Couldn't update category"))
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class ShowSuccess(val message: String) : UiEvent()
        data object SaveGroupFood : UiEvent()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupFoodViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GroupFoodViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}