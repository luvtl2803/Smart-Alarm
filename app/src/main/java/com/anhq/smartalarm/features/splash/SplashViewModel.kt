package com.anhq.smartalarm.features.splash

import androidx.lifecycle.ViewModel
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    fun isFirstRun(): Boolean {
        return preferenceHelper.isFirstRun
    }
}
