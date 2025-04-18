package com.anhq.smartalarm.app.navigation

import com.anhq.smartalarm.R
import com.anhq.smartalarm.features.alarm.navigation.AlarmRoute
import com.anhq.smartalarm.features.home.navigation.HomeRoute
import com.anhq.smartalarm.features.statistics.navigation.StatisticsRoute
import kotlin.reflect.KClass


enum class TopLevelDestination(
    val labelId: Int,
    val iconId: Int,
    val route: KClass<*>,
) {
    HOME(
        labelId = R.string.home,
        iconId = R.drawable.ic_home,
        route = HomeRoute::class,
    ),
    STATISTICS(
        labelId = R.string.streak,
        iconId = R.drawable.ic_streak,
        route = StatisticsRoute::class
    ),
    ALARM(
        labelId = R.string.alarm,
        iconId = R.drawable.ic_clock,
        route = AlarmRoute::class
    ),
//    USER(
//        labelId = R.string.user,
//        selectedIconId = R.drawable.ic_user,
//        unSelectedIconId = R.drawable.ic_user,
//        route = NewTaskRoute::class
//    )
}