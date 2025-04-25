package com.anhq.smartalarm.features.editalarm.navigation

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
    composable<EditAlarmRoute> {
        EditAlarmRoute(
            onCancelClick = onCancelClick,
            onUpdateClick = onUpdateClick
        )
    }
}