package com.anhq.smartalarm.features.statistics.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.anhq.smartalarm.features.statistics.StatisticsRoute
import kotlinx.serialization.Serializable

@Serializable
data object StatisticsRoute

fun NavController.navigateToStatistics(navOptions: NavOptions? = null) = navigate(
    route = StatisticsRoute, navOptions = navOptions
)

fun NavGraphBuilder.statisticsScreen(
) {
    composable<StatisticsRoute> {
        StatisticsRoute()
    }
}