package com.example.finalproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "food",
    foreignKeys = [
        ForeignKey(
            entity = GroupFood::class,
            parentColumns = ["id"],
            childColumns = ["groupFoodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val expiryDate: Long?,
    val remindBefore: Int?,
    val notifyTime: Long?,
    val remindOptionId: Int? = null,
    val groupFoodId: Long?,
    val note: String?,
    val createdDate: Long = System.currentTimeMillis(),
    val createdBy: Long?,
    val updatedDate: Long = System.currentTimeMillis(),
    val updatedBy: Long?,
    val deletedDate: Long?,
    val deletedBy: Long?
)