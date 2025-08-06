package com.example.finalproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groupFood")
data class GroupFood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val icon: String?,
    val name: String,
    val createdDate: Long = System.currentTimeMillis(),
    val createdBy: Long?,
    val updatedDate: Long = System.currentTimeMillis(),
    val updatedBy: Long?,
    val deletedDate: Long?,
    val deletedBy: Long?
)
