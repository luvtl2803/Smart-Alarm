package com.anhq.smartalarm.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
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
            // Get original filename from uri
            val filename = getFileName(uri) ?: "timer_sound.mp3"
            
            // Delete old files in the directory
            soundDir.listFiles()?.forEach { it.delete() }
            
            // Create new file with original name
            val newFile = File(soundDir, filename)
            
            // Copy file content
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
        // Try to get filename from content provider
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayName = cursor.getColumnIndex("_display_name")
                if (displayName != -1) {
                    return cursor.getString(displayName)
                }
            }
        }

        // If content provider doesn't provide filename, try to get it from path
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