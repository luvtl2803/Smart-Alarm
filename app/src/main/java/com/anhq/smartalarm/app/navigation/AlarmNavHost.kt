package com.anhq.smartalarm.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.anhq.smartalarm.app.ui.AlarmAppState
import com.anhq.smartalarm.features.alarm.navigation.alarmScreen
import com.anhq.smartalarm.features.home.navigation.HomeRoute
import com.anhq.smartalarm.features.home.navigation.homeScreen
import com.anhq.smartalarm.features.statistics.navigation.statisticsScreen

@Composable
fun AlarmNavHost(
    appState: AlarmAppState, modifier: Modifier = Modifier
) {
    val navController = appState.navController
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = HomeRoute,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }

    ) {
        homeScreen()
        statisticsScreen()
        alarmScreen()
    }
}