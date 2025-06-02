package com.anhq.smartalarm.core.model

import com.anhq.smartalarm.core.database.model.AlarmEntity

fun Alarm.toAlarmEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        hour = hour,
        minute = minute,
        isActive = isActive,
        isVibrate = isVibrate,
        selectedDays = selectedDays,
        label = label,
        gameType = gameType,
        soundUri = soundUri
    )
}

fun AlarmEntity.mapToAlarm(): Alarm {
    return Alarm(
        id = id,
        hour = hour,
        minute = minute,
        isActive = isActive,
        isVibrate = isVibrate,
        selectedDays = selectedDays,
        label = label,
        gameType = gameType,
        soundUri = soundUri
    )
}

fun List<AlarmEntity>.mapToAlarms(): List<Alarm> {
    return map { it.mapToAlarm() }
} 