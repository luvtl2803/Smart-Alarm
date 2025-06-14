package com.anhq.smartalarm.features.onboarding

import androidx.lifecycle.ViewModel
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    fun setFirstRun(isFirstRun: Boolean) {
        preferenceHelper.isFirstRun = isFirstRun
    }
}