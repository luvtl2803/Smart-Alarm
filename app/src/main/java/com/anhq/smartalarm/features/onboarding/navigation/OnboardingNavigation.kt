package com.anhq.smartalarm.features.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.onboarding.OnboardingRoute
import com.anhq.smartalarm.features.onboarding.OnboardingScreen
import com.anhq.smartalarm.features.splash.SplashRoute
import com.anhq.smartalarm.features.splash.navigation.SplashRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) = navigate(
    route = OnboardingRoute, navOptions = navOptions
)

fun NavGraphBuilder.onboardingScreen(
    navController: NavController
) {
    composable<OnboardingRoute> {
        OnboardingRoute(
            navController = navController
        )
    }
}