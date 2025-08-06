package com.example.finalproject.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.entity.Food
import com.example.finalproject.data.entity.FoodNotification
import java.util.Calendar

object AlarmManagerNotificationService {
    private const val TAG = "AlarmManagerNotService"

    // Request codes for different types of alarms
    private const val REMINDER_REQUEST_CODE_BASE = 10000
    private const val EXPIRY_REQUEST_CODE_BASE = 20000

    suspend fun scheduleNotificationsForFood(context: Context, food: Food) {
        val foodId = food.id
        val expiryDate = food.expiryDate ?: return
        val remindBefore = food.remindBefore ?: 0
        val notifyTime = food.notifyTime

        Log.d(TAG, "Scheduling AlarmManager notification for food: ${food.name}")

        val database = AppDatabase.getDatabase(context)
        val notificationDao = database.foodNotificationDao()

        // Calculate notification times
        val scheduledExpiryTime = calculateNotificationTime(expiryDate, 0, notifyTime)
        val scheduledReminderTime = if (remindBefore > 0) {
            calculateNotificationTime(expiryDate, remindBefore, notifyTime)
        } else null

        Log.d(TAG, "Scheduled times - Expiry: $scheduledExpiryTime, Reminder: $scheduledReminderTime")

        // Check if a notification record already exists
        val existingNotification = notificationDao.getByFoodId(foodId)

        if (existingNotification != null) {
            // Cancel existing alarms before updating
            cancelAlarmsForFood(context, foodId)

            // Update existing record
            val updatedNotification = existingNotification.copy(
                customNotifyTime = notifyTime,
                scheduledReminderTime = scheduledReminderTime,
                scheduledExpiryTime = scheduledExpiryTime,
                isReminderSent = if (scheduledReminderTime != existingNotification.scheduledReminderTime) false else existingNotification.isReminderSent,
                isExpiryNotificationSent = if (scheduledExpiryTime != existingNotification.scheduledExpiryTime) false else existingNotification.isExpiryNotificationSent,
                updatedDate = System.currentTimeMillis()
            )
            notificationDao.update(updatedNotification)
        } else {
            // Create new record
            val newNotification = FoodNotification(
                foodId = foodId,
                customNotifyTime = notifyTime,
                scheduledReminderTime = scheduledReminderTime,
                scheduledExpiryTime = scheduledExpiryTime
            )
            notificationDao.insert(newNotification)
        }

        // Schedule AlarmManager alarms
        scheduleAlarms(context, food, scheduledReminderTime, scheduledExpiryTime)
    }


    private fun scheduleAlarms(
        context: Context,
        food: Food,
        reminderTime: Long?,
        expiryTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTime = System.currentTimeMillis()

        // Schedule reminder alarm if needed
        if (reminderTime != null && reminderTime > currentTime) {
            val reminderIntent = createNotificationIntent(
                context,
                food,
                false, // isExpired = false for reminder
                REMINDER_REQUEST_CODE_BASE + food.id.toInt()
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        reminderIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        reminderIntent
                    )
                }
                Log.d(TAG, "Scheduled reminder alarm for ${food.name} at $reminderTime")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule reminder alarm: ${e.message}")
            }
        }

        // Schedule expiry alarm if in the future
        if (expiryTime > currentTime) {
            val expiryIntent = createNotificationIntent(
                context,
                food,
                true, // isExpired = true for expiry
                EXPIRY_REQUEST_CODE_BASE + food.id.toInt()
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        expiryTime,
                        expiryIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        expiryTime,
                        expiryIntent
                    )
                }
                Log.d(TAG, "Scheduled expiry alarm for ${food.name} at $expiryTime")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule expiry alarm: ${e.message}")
            }
        }
    }


    private fun createNotificationIntent(
        context: Context,
        food: Food,
        isExpired: Boolean,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
            putExtra("food_id", food.id)
            putExtra("food_name", food.name)
            putExtra("is_expired", isExpired)
            putExtra("note", food.note)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun calculateNotificationTime(
        expiryDate: Long,
        daysOffset: Int,
        notifyTime: Long?
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = expiryDate

            // Subtract the specified number of days
            add(Calendar.DAY_OF_YEAR, -daysOffset)

            // Set the time of day
            if (notifyTime != null) {
                val hour = (notifyTime / 100).toInt()
                val minute = (notifyTime % 100).toInt()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            } else {
                // Default to 8:00 AM if no time specified
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
            }
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

    suspend fun cancelNotificationsForFood(context: Context, foodId: Long) {
        // Cancel AlarmManager alarms
        cancelAlarmsForFood(context, foodId)

        // Remove from database
        val database = AppDatabase.getDatabase(context)
        database.foodNotificationDao().deleteByFoodId(foodId)

        Log.d(TAG, "Cancelled all notifications for food ID: $foodId")
    }

    private fun cancelAlarmsForFood(context: Context, foodId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel reminder alarm
        val reminderIntent = Intent(context, AlarmNotificationReceiver::class.java)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE_BASE + foodId.toInt(),
            reminderIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (reminderPendingIntent != null) {
            alarmManager.cancel(reminderPendingIntent)
            reminderPendingIntent.cancel()
            Log.d(TAG, "Cancelled reminder alarm for food ID: $foodId")
        }

        // Cancel expiry alarm
        val expiryIntent = Intent(context, AlarmNotificationReceiver::class.java)
        val expiryPendingIntent = PendingIntent.getBroadcast(
            context,
            EXPIRY_REQUEST_CODE_BASE + foodId.toInt(),
            expiryIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (expiryPendingIntent != null) {
            alarmManager.cancel(expiryPendingIntent)
            expiryPendingIntent.cancel()
            Log.d(TAG, "Cancelled expiry alarm for food ID: $foodId")
        }
    }

    suspend fun rescheduleAllNotifications(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val foodDao = database.foodDao()
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "Rescheduling all upcoming notifications after reboot")

        foodDao.getAllFood().collect { allFoods ->
            val upcomingFoods = allFoods.filter {
                val expiryDate = it.expiryDate ?: Long.MAX_VALUE
                it.deletedDate == null && expiryDate > currentTime
            }

            Log.d(TAG, "Found ${upcomingFoods.size} foods with upcoming expiry dates")

            for (food in upcomingFoods) {
                try {
                    scheduleNotificationsForFood(context, food)
                    Log.d(TAG, "Rescheduled notifications for: ${food.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule notifications for ${food.name}: ${e.message}")
                }
            }
        }
    }

    suspend fun markNotificationSent(context: Context, foodId: Long, isExpired: Boolean) {
        val database = AppDatabase.getDatabase(context)
        val notificationDao = database.foodNotificationDao()

        val notification = notificationDao.getByFoodId(foodId)
        notification?.let {
            if (isExpired) {
                notificationDao.markExpiryNotificationSent(it.id)
                Log.d(TAG, "Marked expiry notification as sent for food ID: $foodId")
            } else {
                notificationDao.markReminderSent(it.id)
                Log.d(TAG, "Marked reminder notification as sent for food ID: $foodId")
            }
        }
    }
}