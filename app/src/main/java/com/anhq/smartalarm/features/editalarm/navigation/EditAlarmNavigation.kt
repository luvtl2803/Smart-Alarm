package com.anhq.smartalarm.features.editalarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.core.model.AlarmSetType
import com.anhq.smartalarm.features.editalarm.EditAlarmRoute
import kotlinx.serialization.Serializable

@Serializable
data class EditAlarmRoute(
    val id: Int,
    val type: AlarmSetType
)

fun NavController.navigateToEditAlarm(id: Int, type: AlarmSetType, navOptions: NavOptions? = null)  {
    val route = EditAlarmRoute(id, type)
    navigate(
        route = route, navOptions = navOptions
    )
}

fun NavGraphBuilder.editAlarmScreen(
) {
    composable<EditAlarmRoute> {
        EditAlarmRoute()
    }
}