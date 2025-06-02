package com.anhq.smartalarm.core.utils

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

data class AlarmSound(
    val uri: Uri,
    val title: String
)

class AlarmSoundManager(private val context: Context) {

    fun getAllAlarmSounds(): List<AlarmSound> {
        val alarmSounds = mutableListOf<AlarmSound>()
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = ringtoneManager.cursor

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(cursor.position)
            alarmSounds.add(AlarmSound(uri, title))
        }

        return alarmSounds
    }
} 