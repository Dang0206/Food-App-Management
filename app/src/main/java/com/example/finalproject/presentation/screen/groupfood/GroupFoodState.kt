package com.example.finalproject.presentation.screen.groupfood

import com.example.finalproject.data.entity.GroupFood

data class GroupFoodState(
    val id: Long = 0,
    val name: String = "",
    val icon: String = "",
    val groupFoods: List<GroupFood> = emptyList(),
    val foodCounts: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)