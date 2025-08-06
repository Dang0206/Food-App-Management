package com.example.finalproject.presentation.screen.food

import com.example.finalproject.data.entity.Food
import com.example.finalproject.data.entity.GroupFood

data class FoodState(
    val id: Long = 0,
    val name: String = "",
    val expiryDate: Long? = null,
    val remindBefore: Int? = null,
    val notifyTime: Long? = null,
    val remindOptionId: Int? = 1,
    val groupFoodId: Long? = null,
    val note: String? = null,
    val foods: List<Food> = emptyList(),
    val groupFoods: List<GroupFood> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)


