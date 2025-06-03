package com.anhq.smartalarm.features.addalarm.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.addalarm.AddAlarmRoute
import kotlinx.serialization.Serializable

@Serializable
data object AddAlarmRoute

fun NavController.navigateToAddAlarm(navOptions: NavOptions? = null) {
    navigate(
        route = AddAlarmRoute, navOptions = navOptions
    )
}

fun NavGraphBuilder.addAlarmScreen(
    onCancelClick: () -> Unit,
    onAddClick: () -> Unit
) {
    composable<AddAlarmRoute> (
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        }
    ) {
        AddAlarmRoute(
            onCancelClick = onCancelClick,
            onAddClick = onAddClick
        )
    }
}