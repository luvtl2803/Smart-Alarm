package com.anhq.smartalarm.core.utils

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class PermissionsHelper(
    private val activity: ComponentActivity,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>
) {
    private val alarmManager by lazy { activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    companion object {
        @RequiresApi(Build.VERSION_CODES.Q)
        private val RUNTIME_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.FOREGROUND_SERVICE
                )
            } else {
                arrayOf(
                    Manifest.permission.FOREGROUND_SERVICE
                )
            }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val PERMISSION_EXPLANATIONS = mapOf(
            Manifest.permission.POST_NOTIFICATIONS to "Show alarm notifications",
            Manifest.permission.SCHEDULE_EXACT_ALARM to "Ensure alarms trigger at the exact time you set",
            Manifest.permission.FOREGROUND_SERVICE to "Track data in the background"
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasRequiredPermissions(): Boolean {
        val standardPermissionsGranted = RUNTIME_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            standardPermissionsGranted && alarmManager.canScheduleExactAlarms()
        } else {
            standardPermissionsGranted
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getPermissionsNeedingExplanation(): List<String> {
        return RUNTIME_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED &&
                    activity.shouldShowRequestPermissionRationale(permission)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getMissingRuntimePermissions(): List<String> {
        return RUNTIME_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun needsExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            !alarmManager.canScheduleExactAlarms()
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getMissingPermissions(): List<String> {
        val missingStandardPermissions = getMissingRuntimePermissions()
        return if (needsExactAlarmPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                missingStandardPermissions + Manifest.permission.SCHEDULE_EXACT_ALARM
            } else {
                TODO("VERSION.SDK_INT < S")
            }
        } else {
            missingStandardPermissions
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getPermissionExplanation(permission: String): String {
        return PERMISSION_EXPLANATIONS[permission]
            ?: "This permission is required for app functionality"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestRuntimePermissions(callback: (Boolean) -> Unit) {
        val missingPermissions = getMissingRuntimePermissions()
        if (missingPermissions.isEmpty()) {
            callback(true)
            return
        }
        permissionLauncher.launch(missingPermissions.toTypedArray())
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            activity.startActivity(intent)
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return if (permission == Manifest.permission.SCHEDULE_EXACT_ALARM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}