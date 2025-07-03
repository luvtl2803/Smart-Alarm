package com.anhq.smartalarm.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SoundFileManager"
        private const val TIMER_SOUND_DIR = "timer_sounds"
        private const val ALARM_SOUND_DIR = "alarm_sounds"
    }

    private val timerSoundDir: File
        get() = File(context.filesDir, TIMER_SOUND_DIR).apply { mkdirs() }

    private val alarmSoundDir: File
        get() = File(context.filesDir, ALARM_SOUND_DIR).apply { mkdirs() }

    fun copyTimerSoundToInternal(uri: Uri): String? {
        return try {
            val filename = getFileName(uri) ?: "timer_sound.mp3"
            val newFile = File(timerSoundDir, filename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(newFile, false).use { output ->
                    input.copyTo(output)
                }
            }

            if (!newFile.exists() || newFile.length() == 0L) {
                Log.e(TAG, "Failed to copy timer sound file or file is empty")
                return null
            }

            Uri.fromFile(newFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying timer sound file", e)
            null
        }
    }

    fun copyAlarmSoundToInternal(uri: Uri): String? {
        return try {
            val filename = getFileName(uri) ?: "alarm_sound.mp3"
            val newFile = File(alarmSoundDir, filename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(newFile, false).use { output ->
                    input.copyTo(output)
                }
            }

            if (!newFile.exists() || newFile.length() == 0L) {
                Log.e(TAG, "Failed to copy alarm sound file or file is empty")
                return null
            }

            Uri.fromFile(newFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying alarm sound file", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameColumn = cursor.getColumnIndex("_display_name")
                if (displayNameColumn != -1) {
                    return cursor.getString(displayNameColumn)
                }
            }
        }

        uri.path?.let { path ->
            return path.substringAfterLast('/')
        }

        return null
    }

    fun deleteTimerSound() {
        try {
            timerSoundDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting timer sound files", e)
        }
    }

    fun deleteAlarmSound() {
        try {
            alarmSoundDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting alarm sound files", e)
        }
    }
}
