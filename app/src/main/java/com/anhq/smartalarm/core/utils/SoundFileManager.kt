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
        private const val SOUND_DIR = "custom_sounds"
    }

    private val soundDir: File
        get() = File(context.filesDir, SOUND_DIR).apply { mkdirs() }

    fun copyTimerSoundToInternal(uri: Uri): String? {
        return try {
            val filename = getFileName(uri) ?: "timer_sound.mp3"

            soundDir.listFiles()?.forEach { it.delete() }

            val newFile = File(soundDir, filename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(newFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(newFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying timer sound file", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayName = cursor.getColumnIndex("_display_name")
                if (displayName != -1) {
                    return cursor.getString(displayName)
                }
            }
        }

        uri.path?.let { path ->
            return path.substring(path.lastIndexOf('/') + 1)
        }

        return null
    }

    fun deleteTimerSound() {
        try {
            soundDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting timer sound files", e)
        }
    }
} 