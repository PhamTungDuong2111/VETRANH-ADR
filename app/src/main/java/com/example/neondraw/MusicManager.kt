package com.example.neondraw

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.Log

data class MusicTrack(
    val id: Int,
    val name: String,
    val resId: Int
)

object MusicManager {
    private const val TAG = "MusicManager"
    private const val PREF_NAME = "neon_music_pref"
    private const val KEY_SELECTED_TRACK = "selected_track_id"
    private const val KEY_IS_MUTED = "is_muted"
    private const val KEY_VOLUME = "volume_level"

    val TRACKS = listOf(
        MusicTrack(1, "Track 1 (1.mp3)", R.raw.music_1),
        MusicTrack(2, "Track 2 (22222.mp3)", R.raw.music_22222),
        MusicTrack(3, "Track 3 (333.mp3)", R.raw.music_333),
        MusicTrack(4, "Track 4 (44.mp3)", R.raw.music_44),
        MusicTrack(5, "Track 5 (5.mp3)", R.raw.music_5),
        MusicTrack(6, "Track 6 (6.mp3)", R.raw.music_6)
    )

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackId: Int = 1
    private var isMuted: Boolean = false
    private var volume: Float = 0.8f
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        currentTrackId = prefs.getInt(KEY_SELECTED_TRACK, 1)
        isMuted = prefs.getBoolean(KEY_IS_MUTED, false)
        volume = prefs.getFloat(KEY_VOLUME, 0.8f)

        if (!isMuted) {
            playTrack(context, currentTrackId)
        }
    }

    fun playTrack(context: Context, trackId: Int) {
        val track = TRACKS.find { it.id == trackId } ?: TRACKS.first()
        currentTrackId = trackId
        isMuted = false
        savePrefs(context)

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            mediaPlayer = MediaPlayer.create(context.applicationContext, track.resId)?.apply {
                isLooping = true
                setVolume(volume, volume)
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing track: ${e.message}")
        }
    }

    fun toggleMute(context: Context): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            mediaPlayer?.pause()
        } else {
            if (mediaPlayer == null) {
                playTrack(context, currentTrackId)
            } else {
                mediaPlayer?.start()
            }
        }
        savePrefs(context)
        return isMuted
    }

    fun setMute(context: Context, mute: Boolean) {
        isMuted = mute
        if (isMuted) {
            mediaPlayer?.pause()
        } else {
            if (mediaPlayer == null) {
                playTrack(context, currentTrackId)
            } else {
                mediaPlayer?.start()
            }
        }
        savePrefs(context)
    }

    fun setVolume(context: Context, vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(volume, volume)
        savePrefs(context)
    }

    fun getVolume(): Float = volume
    fun isMuted(): Boolean = isMuted
    fun getCurrentTrackId(): Int = currentTrackId

    fun onPause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onPause error: ${e.message}")
        }
    }

    fun onResume(context: Context) {
        try {
            if (!isMuted) {
                if (mediaPlayer == null) {
                    playTrack(context, currentTrackId)
                } else if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onResume error: ${e.message}")
        }
    }

    fun release() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "release error: ${e.message}")
        }
    }

    private fun savePrefs(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_SELECTED_TRACK, currentTrackId)
            .putBoolean(KEY_IS_MUTED, isMuted)
            .putFloat(KEY_VOLUME, volume)
            .apply()
    }
}
