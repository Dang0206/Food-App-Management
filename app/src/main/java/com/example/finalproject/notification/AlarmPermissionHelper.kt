package com.example.finalproject.notification

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

object AlarmPermissionHelper {

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Always available on older versions
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }
}

@Composable
fun ExactAlarmPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Permission Required") },
            text = {
                Text("This app needs permission to schedule exact alarms for food expiry notifications. Please enable 'Alarms & reminders' in the next screen.")
            },
            confirmButton = {
                Button(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
fun BatteryOptimizationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Battery Optimization") },
            text = {
                Text("To ensure notifications work reliably, please disable battery optimization for this app in the next screen.")
            },
            confirmButton = {
                Button(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Skip")
                }
            }
        )
    }
}

@Composable
fun AlarmPermissionManager() {
    val context = LocalContext.current
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var permissionsChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!permissionsChecked) {
            // Check exact alarm permission
            if (!AlarmPermissionHelper.canScheduleExactAlarms(context)) {
                showExactAlarmDialog = true
            }
            // Check battery optimization
            else if (!AlarmPermissionHelper.isIgnoringBatteryOptimizations(context)) {
                showBatteryDialog = true
            }
            permissionsChecked = true
        }
    }

    ExactAlarmPermissionDialog(
        showDialog = showExactAlarmDialog,
        onDismiss = {
            showExactAlarmDialog = false
            // Check battery optimization after exact alarm dialog
            if (!AlarmPermissionHelper.isIgnoringBatteryOptimizations(context)) {
                showBatteryDialog = true
            }
        },
        onOpenSettings = {
            AlarmPermissionHelper.openExactAlarmSettings(context)
            showExactAlarmDialog = false
        }
    )

    BatteryOptimizationDialog(
        showDialog = showBatteryDialog,
        onDismiss = { showBatteryDialog = false },
        onOpenSettings = {
            AlarmPermissionHelper.openBatteryOptimizationSettings(context)
            showBatteryDialog = false
        }
    )
}