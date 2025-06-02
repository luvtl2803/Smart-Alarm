package com.anhq.smartalarm.features.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.onboarding.OnboardingScreen

const val OnboardingRoute = "onboarding_route"

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    this.navigate(OnboardingRoute, navOptions)
}

fun NavGraphBuilder.onboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    composable(route = OnboardingRoute) {
        OnboardingScreen(onOnboardingComplete = onOnboardingComplete)
    }
} 