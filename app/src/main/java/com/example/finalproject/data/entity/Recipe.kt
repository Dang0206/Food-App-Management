package com.example.finalproject.data.entity

data class Recipe(
    val title: String,
    val description: String,
    val cookingTime: String,
    val ingredients: List<String>,
    val steps: List<String>
)
