package com.example.finalproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_notification",
    foreignKeys = [
        ForeignKey(
            entity = Food::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("foodId")]
)
data class FoodNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodId: Long,
    val isReminderSent: Boolean = false,
    val isExpiryNotificationSent: Boolean = false,
    val customNotifyTime: Long? = null,
    val scheduledReminderTime: Long? = null,
    val scheduledExpiryTime: Long? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)