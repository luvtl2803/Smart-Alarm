package com.anhq.smartalarm.features.timer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.timer.TimerRoute
import kotlinx.serialization.Serializable

@Serializable
data object TimerRoute

fun NavController.navigateToTimer(navOptions: NavOptions? = null) = navigate(
    route = TimerRoute, navOptions = navOptions
)

fun NavGraphBuilder.timerScreen(
) {
    composable<TimerRoute> {
        TimerRoute()
    }
}