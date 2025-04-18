package com.anhq.smartalarm.core.model

data class AlarmCount(
    val dayOfWeek: DayOfWeek,
    val alarmCount: Int
)

enum class DayOfWeek(val label: String) {
    SUN("Sun"),
    MON("Mon"),
    TUE("Tue"),
    WED("Wed"),
    THU("Thu"),
    FRI("Fri"),
    SAT("Sat");
}
