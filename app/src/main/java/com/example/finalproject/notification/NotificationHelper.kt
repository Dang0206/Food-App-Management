package com.example.finalproject.notification
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.data.entity.Food

object NotificationHelper {
    private const val CHANNEL_ID = "food_reminder_channel"
    private const val CHANNEL_NAME = "Food Reminder Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for food reminders and expirations"

    // Create notification channel
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Show a food expiry notification
    fun showExpiryNotification(context: Context, food: Food, isExpired: Boolean = false) {
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return // Return early if permission not granted
            }
        }

        // Create an intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create the notification content based on whether food is expired or about to expire
        val title = if (isExpired) {
            "Expired food!"
        } else {
            "Food nearing expiration date!"
        }

        val message = if (isExpired) {
            "${food.name} Expired!"
        } else {
            "${food.name} food nearing expiration date. Please check!"
        }

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification with a unique ID based on the food ID
        try {
            Log.d("NotificationHelper", "Showing notification for ${food.name}")
            NotificationManagerCompat.from(context).notify(food.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Permission denied: ${e.message}")
            e.printStackTrace()
        }
    }
}