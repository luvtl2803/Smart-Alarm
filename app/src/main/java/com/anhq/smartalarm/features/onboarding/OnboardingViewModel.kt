package com.anhq.smartalarm.features.onboarding

import android.app.AppOpsManager
import android.app.Application
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application: Application,
    private val preferenceHelper: PreferenceHelper
) : AndroidViewModel(application) {
    fun setFirstRun(isFirstRun: Boolean) {
        preferenceHelper.isFirstRun = isFirstRun
    }

    fun setSleepTime(hour: Int, minute: Int) {
        preferenceHelper.sleepTimeHour = hour
        preferenceHelper.sleepTimeMinute = minute
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        preferenceHelper.isNotificationPermissionGranted = granted
    }

    fun setUsageStatsPermissionGranted(granted: Boolean) {
        preferenceHelper.isUsageStatsPermissionGranted = granted
    }

    fun hasNotificationPermission(): Boolean {
        return preferenceHelper.isNotificationPermissionGranted
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = getApplication<Application>().getSystemService(Application.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                getApplication<Application>().packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                getApplication<Application>().packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}