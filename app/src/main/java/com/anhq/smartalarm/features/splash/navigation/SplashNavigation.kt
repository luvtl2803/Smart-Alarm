package com.anhq.smartalarm.features.splash.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.splash.SplashScreen

const val SplashRoute = "splash_route"

fun NavController.navigateToSplash(navOptions: NavOptions? = null) {
    this.navigate(SplashRoute, navOptions)
}

fun NavGraphBuilder.splashScreen(
    onSplashComplete: () -> Unit
) {
    composable(route = SplashRoute) {
        SplashScreen(onSplashComplete = onSplashComplete)
    }
} 