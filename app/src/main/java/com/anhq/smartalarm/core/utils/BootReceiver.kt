package com.anhq.smartalarm.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.anhq.smartalarm.core.service.RestoreAlarmsService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                Log.d(TAG, "Device booted in locked state")
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_USER_UNLOCKED -> {
                Log.d(TAG, "Device unlocked, starting restore service")
                val serviceIntent = Intent(context, RestoreAlarmsService::class.java)
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
} 