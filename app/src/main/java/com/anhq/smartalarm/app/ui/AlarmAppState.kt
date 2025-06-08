package com.anhq.smartalarm.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.anhq.smartalarm.app.navigation.TopLevelDestination
import com.anhq.smartalarm.features.alarm.navigation.navigateToAlarm
import com.anhq.smartalarm.features.timer.navigation.TimerRoute
import com.anhq.smartalarm.features.timer.navigation.navigateToTimer
import com.anhq.smartalarm.features.setting.navigation.navigateToSetting
import com.anhq.smartalarm.features.statistics.navigation.navigateToStatistics

@Composable
fun rememberAlarmAppState(
    navController: NavHostController = rememberNavController()
): AlarmAppState {
    return remember(
        navController
    ) {
        AlarmAppState(
            navController = navController
        )
    }
}

@Stable
class AlarmAppState(
    val navController: NavHostController
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    private val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val currentDestination = currentDestination
            return remember(currentDestination) {
                TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                    currentDestination?.hasRoute(route = topLevelDestination.route) ?: false
                }
            }
        }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo<TimerRoute> {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        when (topLevelDestination) {
            TopLevelDestination.ALARM -> {
                navController.navigateToAlarm(topLevelNavOptions)
            }
            TopLevelDestination.TIMER -> {
                navController.navigateToTimer(topLevelNavOptions)
            }
            TopLevelDestination.STATISTICS -> {
                navController.navigateToStatistics(topLevelNavOptions)
            }
            TopLevelDestination.SETTINGS -> {
                navController.navigateToSetting(topLevelNavOptions)
            }
        }
    }
}