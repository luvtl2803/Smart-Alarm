package com.anhq.smartalarm.features.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(
    route = HomeRoute, navOptions = navOptions
)

fun NavGraphBuilder.homeScreen(
) {
    composable<HomeRoute> {
        HomeRoute()
    }
}