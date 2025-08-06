package com.example.finalproject.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.entity.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmNotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmNotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received for food notification")

        val foodId = intent.getLongExtra("food_id", -1)
        val foodName = intent.getStringExtra("food_name") ?: "Unknown Food"
        val isExpired = intent.getBooleanExtra("is_expired", false)
        val note = intent.getStringExtra("note")

        if (foodId == -1L) {
            Log.e(TAG, "Invalid food ID received in alarm")
            return
        }

        Log.d(TAG, "Processing notification for: $foodName (ID: $foodId, isExpired: $isExpired)")

        // Use goAsync() to handle long-running operations
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Verify the food still exists and is not deleted
                val database = AppDatabase.getDatabase(context)
                val foodDao = database.foodDao()
                val food = foodDao.getFoodById(foodId)

                if (food != null && food.deletedDate == null) {
                    // Create Food object for notification
                    val foodForNotification = Food(
                        id = foodId,
                        name = foodName,
                        expiryDate = food.expiryDate,
                        remindBefore = food.remindBefore,
                        notifyTime = food.notifyTime,
                        remindOptionId = food.remindOptionId,
                        groupFoodId = food.groupFoodId,
                        note = note ?: food.note,
                        createdDate = food.createdDate,
                        createdBy = food.createdBy,
                        updatedDate = food.updatedDate,
                        updatedBy = food.updatedBy,
                        deletedDate = food.deletedDate,
                        deletedBy = food.deletedBy
                    )

                    // Show the notification
                    NotificationHelper.showExpiryNotification(
                        context,
                        foodForNotification,
                        isExpired
                    )

                    // Mark notification as sent in database
                    AlarmManagerNotificationService.markNotificationSent(
                        context,
                        foodId,
                        isExpired
                    )

                    Log.d(TAG, "Successfully showed notification for: $foodName")
                } else {
                    Log.w(TAG, "Food not found or deleted, skipping notification: $foodName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing alarm notification: ${e.message}", e)
            } finally {
                // Always finish the broadcast
                pendingResult.finish()
            }
        }
    }
}