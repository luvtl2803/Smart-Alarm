package com.anhq.smartalarm.app.navigation

import com.anhq.smartalarm.R
import com.anhq.smartalarm.features.alarm.navigation.AlarmRoute
import com.anhq.smartalarm.features.timer.navigation.TimerRoute
import com.anhq.smartalarm.features.setting.navigation.SettingRoute
import com.anhq.smartalarm.features.statistics.navigation.StatisticsRoute
import kotlin.reflect.KClass


enum class TopLevelDestination(
    val labelId: Int,
    val iconId: Int,
    val route: KClass<*>,
) {
    ALARM(
        labelId = R.string.nav_alarm,
        iconId = R.drawable.ic_clock,
        route = AlarmRoute::class
    ),
    TIMER(
        labelId = R.string.nav_timer,
        iconId = R.drawable.ic_timer,
        route = TimerRoute::class,
    ),
    STATISTICS(
        labelId = R.string.nav_statistics,
        iconId = R.drawable.ic_streak,
        route = StatisticsRoute::class
    ),

    SETTINGS(
        labelId = R.string.nav_settings,
        iconId = R.drawable.ic_user,
        route = SettingRoute::class
    )
}