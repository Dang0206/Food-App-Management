package com.example.finalproject.presentation.screen.groupfood

import com.example.finalproject.data.entity.GroupFood

sealed class GroupFoodEvent {
    data class OnNameChange(val name: String): GroupFoodEvent()
    data class OnIconChange(val icon: String): GroupFoodEvent()
    data object OnSaveGroupFood: GroupFoodEvent()
    data class OnDeleteGroupFood(val groupFood: GroupFood): GroupFoodEvent()
    data class OnUpdateGroupFood(val groupFood: GroupFood): GroupFoodEvent()
    data class OnSoftDeleteGroupFood(val id: Long): GroupFoodEvent()
    data class GetGroupFoodById(val id: Long): GroupFoodEvent()
    data object LoadAllGroupFoods: GroupFoodEvent()
}