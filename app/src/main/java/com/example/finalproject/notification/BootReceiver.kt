package com.example.finalproject.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, rescheduling AlarmManager notifications")

            // Use goAsync() for long-running operations
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Reschedule all notifications using AlarmManager
                    AlarmManagerNotificationService.rescheduleAllNotifications(context)
                    Log.d(TAG, "Successfully rescheduled all notifications")
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling notifications: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}