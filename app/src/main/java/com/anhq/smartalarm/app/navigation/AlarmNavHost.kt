package com.anhq.smartalarm.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.anhq.smartalarm.app.ui.AlarmAppState
import com.anhq.smartalarm.features.addalarm.navigation.addAlarmScreen
import com.anhq.smartalarm.features.alarm.navigation.alarmScreen
import com.anhq.smartalarm.features.editalarm.navigation.editAlarmScreen
import com.anhq.smartalarm.features.home.navigation.homeScreen
import com.anhq.smartalarm.features.home.navigation.navigateToHome
import com.anhq.smartalarm.features.onboarding.navigation.navigateToOnboarding
import com.anhq.smartalarm.features.onboarding.navigation.onboardingScreen
import com.anhq.smartalarm.features.setting.navigation.settingScreen
import com.anhq.smartalarm.features.splash.navigation.SplashRoute
import com.anhq.smartalarm.features.splash.navigation.splashScreen
import com.anhq.smartalarm.features.statistics.navigation.statisticsScreen

@Composable
fun AlarmNavHost(
    appState: AlarmAppState, modifier: Modifier = Modifier
) {
    val navController = appState.navController
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = SplashRoute,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            ExitTransition.None
        }

    ) {
        splashScreen(
            navController = navController
        )

        onboardingScreen(
            navController = navController
        )

        homeScreen()

        statisticsScreen()

        settingScreen()

        alarmScreen(
            navController = navController
        )

        addAlarmScreen(
            onCancelClick = {
                navController.popBackStack()
            },
            onAddClick = {
                navController.popBackStack()
            }
        )

        editAlarmScreen(
            onUpdateClick = {
                navController.popBackStack()
            },
            onCancelClick = {
                navController.popBackStack()
            }
        )
    }
}