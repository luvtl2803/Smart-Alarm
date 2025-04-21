package com.anhq.smartalarm.features.alarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.alarm.AlarmRoute
import kotlinx.serialization.Serializable

@Serializable
data object AlarmRoute

fun NavController.navigateToAlarm(navOptions: NavOptions? = null) = navigate(
    route = AlarmRoute, navOptions = navOptions
)

fun NavGraphBuilder.alarmScreen(
    navController: NavController
) {
    composable<AlarmRoute> {
        AlarmRoute(
            navController = navController
        )
    }
}
