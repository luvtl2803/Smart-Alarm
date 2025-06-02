package com.anhq.smartalarm.core.database.model

import com.anhq.smartalarm.core.model.Alarm

fun AlarmEntity.toAlarm(): Alarm {
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

fun Alarm.toEntity(): AlarmEntity {
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