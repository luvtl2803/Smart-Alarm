package com.anhq.smartalarm.core.designsystem.theme

import androidx.lifecycle.ViewModel
import com.anhq.smartalarm.core.model.ThemeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {
    val themeState: StateFlow<ThemeOption> = themeManager.themeState
} 