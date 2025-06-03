package com.anhq.smartalarm.features.splash.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.splash.SplashRoute
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

fun NavGraphBuilder.splashScreen(
    navController: NavController
) {
    composable<SplashRoute> {
        SplashRoute(navController)
    }
}