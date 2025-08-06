package com.example.finalproject.data.dao

import androidx.room.*
import com.example.finalproject.data.entity.FoodNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodNotificationDao {

    @Insert
    suspend fun insert(notification: FoodNotification): Long

    @Update
    suspend fun update(notification: FoodNotification)

    @Delete
    suspend fun delete(notification: FoodNotification)


    @Query("SELECT * FROM food_notification WHERE foodId = :foodId")
    suspend fun getByFoodId(foodId: Long): FoodNotification?

    @Query("SELECT * FROM food_notification WHERE scheduledReminderTime <= :currentTime AND isReminderSent = 0")
    suspend fun getDueReminders(currentTime: Long): List<FoodNotification>

    @Query("SELECT * FROM food_notification WHERE scheduledExpiryTime <= :currentTime AND isExpiryNotificationSent = 0")
    suspend fun getDueExpiryNotifications(currentTime: Long): List<FoodNotification>

    @Query("UPDATE food_notification SET isReminderSent = 1, updatedDate = :timestamp WHERE id = :id")
    suspend fun markReminderSent(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE food_notification SET isExpiryNotificationSent = 1, updatedDate = :timestamp WHERE id = :id")
    suspend fun markExpiryNotificationSent(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM food_notification WHERE foodId = :foodId")
    suspend fun deleteByFoodId(foodId: Long)

    @Query("SELECT fen.* FROM food_notification fen JOIN food f ON fen.foodId = f.id WHERE f.deletedDate IS NULL")
    fun getAllActiveNotifications(): Flow<List<FoodNotification>>

    @Query("SELECT fen.* FROM food_notification fen JOIN food f ON fen.foodId = f.id WHERE f.deletedDate IS NULL AND f.expiryDate <= :futureTime AND f.expiryDate > :currentTime")
    suspend fun getNotificationsForFoodsExpiringBefore(currentTime: Long, futureTime: Long): List<FoodNotification>
}