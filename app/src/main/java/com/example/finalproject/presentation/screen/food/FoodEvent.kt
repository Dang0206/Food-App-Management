package com.example.finalproject.presentation.screen.food

import com.example.finalproject.data.entity.Food

sealed class FoodEvent {
    data class OnNameChange(val name: String): FoodEvent()
    data class OnExpiryDateChange(val date: Long): FoodEvent()
    data class OnRemindBeforeChange(val days: Int): FoodEvent()
    data class OnRemindOptionIdChange(val optionId: Int): FoodEvent()
    data class OnNotifyTimeChange(val time: Long): FoodEvent()
    data class OnGroupFoodIdChange(val groupId: Long): FoodEvent()
    data class OnNoteChange(val note: String): FoodEvent()
    data object OnSaveFood: FoodEvent()
    data class OnDeleteFood(val food: Food): FoodEvent()
    data class OnUpdateFood(val food: Food): FoodEvent()
    data class OnSoftDeleteFood(val id: Long): FoodEvent()
    data class GetFoodById(val id: Long): FoodEvent()
    data class GetFoodsByGroupId(val groupId: Long): FoodEvent()
    data object LoadAllFoods: FoodEvent()
    data object LoadAllGroupFoods: FoodEvent()
    data object ResetFormState: FoodEvent()
}



