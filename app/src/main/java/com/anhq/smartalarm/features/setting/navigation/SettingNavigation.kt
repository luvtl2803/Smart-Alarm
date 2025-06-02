package com.anhq.smartalarm.features.setting.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.setting.SettingRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingRoute

fun NavController.navigateToSetting(navOptions: NavOptions? = null) = navigate(
    route = SettingRoute, navOptions = navOptions
)

fun NavGraphBuilder.settingScreen(
) {
    composable<SettingRoute> {
        SettingRoute()
    }
}