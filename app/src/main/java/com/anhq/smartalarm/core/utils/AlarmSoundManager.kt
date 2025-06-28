package com.anhq.smartalarm.core.utils

import android.content.Context
import android.database.Cursor
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

    fun getAlarmTitleFromUri(uri: Uri): String {
        return try {
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone?.getTitle(context)

            if (title.isNullOrBlank() || title == uri.toString() || title.startsWith("msf:")) {
                getFileNameFromUri(context, uri)
            } else {
                title
            }
        } catch (e: Exception) {
            "Tùy chỉnh"
        }
    }


    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = cursor.getColumnIndexOpenableDisplayName()
                if (cursor.moveToFirst() && nameIndex != -1) {
                    return cursor.getString(nameIndex)
                }
            }
            "Tùy chỉnh"
        } catch (e: Exception) {
            "Tùy chỉnh"
        }
    }

    private fun Cursor.getColumnIndexOpenableDisplayName(): Int {
        return getColumnIndex("_display_name")
    }

} 