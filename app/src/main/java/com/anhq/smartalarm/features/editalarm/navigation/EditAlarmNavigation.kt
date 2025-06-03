package com.anhq.smartalarm.features.editalarm.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.editalarm.EditAlarmRoute
import kotlinx.serialization.Serializable

@Serializable
data class EditAlarmRoute(
    val id: Int
)

fun NavController.navigateToEditAlarm(id: Int, navOptions: NavOptions? = null) {
    val route = EditAlarmRoute(id)
    navigate(
        route = route, navOptions = navOptions
    )
}

fun NavGraphBuilder.editAlarmScreen(
    onCancelClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    composable<EditAlarmRoute> (
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
        EditAlarmRoute(
            onCancelClick = onCancelClick,
            onSaveClick = onUpdateClick
        )
    }
}