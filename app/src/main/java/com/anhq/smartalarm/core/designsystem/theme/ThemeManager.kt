package com.anhq.smartalarm.core.designsystem.theme

import com.anhq.smartalarm.core.model.ThemeOption
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val preferenceHelper: PreferenceHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _themeState = MutableStateFlow(ThemeOption.SYSTEM)
    val themeState: StateFlow<ThemeOption> = _themeState.asStateFlow()

    init {
        loadThemeSetting()
    }

    private fun loadThemeSetting() {
        scope.launch {
            preferenceHelper.settingsFlow.collect { settings ->
                _themeState.update { settings.theme }
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _themeState.update { theme }
    }
} 