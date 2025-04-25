package com.anhq.smartalarm.features.addalarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.addalarm.AddAlarmRoute
import kotlinx.serialization.Serializable

@Serializable
data object EditAlarmRoute

fun NavController.navigateToAddAlarm(navOptions: NavOptions? = null) {
    navigate(
        route = EditAlarmRoute, navOptions = navOptions
    )
}

fun NavGraphBuilder.addAlarmScreen(
    onCancelClick: () -> Unit,
    onAddClick: () -> Unit
) {
    composable<EditAlarmRoute> {
        AddAlarmRoute(
            onCancelClick = onCancelClick,
            onAddClick = onAddClick
        )
    }
}