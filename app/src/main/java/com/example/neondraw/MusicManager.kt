package com.example.neondraw

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

data class MusicTrack(
    val id: Int,
    val title: String,
    val fileName: String,
    val resId: Int
)

object MusicManager {
    private const val TAG = "MusicManager"
    private const val PREF_NAME = "neon_music_pref"
    private const val KEY_SELECTED_TRACK = "selected_track_id"
    private const val KEY_IS_MUTED = "is_muted"

    val TRACKS = listOf(
        MusicTrack(1, "Bài 1", "1.mp3", R.raw.music_1),
        MusicTrack(2, "Bài 2", "22222.mp3", R.raw.music_22222),
        MusicTrack(3, "Bài 3", "333.mp3", R.raw.music_333),
        MusicTrack(4, "Bài 4", "44.mp3", R.raw.music_44),
        MusicTrack(5, "Bài 5", "5.mp3", R.raw.music_5),
        MusicTrack(6, "Bài 6", "6.mp3", R.raw.music_6)
    )

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackId: Int = 1
    private var isMuted: Boolean = false
    private var isInitialized = false

    var onStateChanged: ((track: MusicTrack, isMuted: Boolean) -> Unit)? = null

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        currentTrackId = prefs.getInt(KEY_SELECTED_TRACK, 1)
        isMuted = prefs.getBoolean(KEY_IS_MUTED, false)

        if (!isMuted) {
            playTrack(context, currentTrackId)
        } else {
            notifyState()
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

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                val afd = context.resources.openRawResourceFd(track.resId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                setVolume(1.0f, 1.0f) // 100% full scale - matches phone media volume 1:1!
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing track: ${e.message}")
        }
        notifyState()
    }

    fun nextTrack(context: Context): MusicTrack {
        val nextIndex = (TRACKS.indexOfFirst { it.id == currentTrackId } + 1) % TRACKS.size
        val next = TRACKS[nextIndex]
        playTrack(context, next.id)
        return next
    }

    fun toggleMute(context: Context): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                Log.e(TAG, "pause error: ${e.message}")
            }
        } else {
            if (mediaPlayer == null) {
                playTrack(context, currentTrackId)
            } else {
                try {
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    playTrack(context, currentTrackId)
                }
            }
        }
        savePrefs(context)
        notifyState()
        return isMuted
    }

    fun setMute(context: Context, mute: Boolean) {
        if (isMuted == mute) return
        isMuted = mute
        if (isMuted) {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                Log.e(TAG, "pause error: ${e.message}")
            }
        } else {
            if (mediaPlayer == null) {
                playTrack(context, currentTrackId)
            } else {
                try {
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    playTrack(context, currentTrackId)
                }
            }
        }
        savePrefs(context)
        notifyState()
    }

    fun getSystemMediaVolume(context: Context): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun getSystemMaxMediaVolume(context: Context): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun setSystemMediaVolume(context: Context, volumeIndex: Int) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex.coerceIn(0, max), 0)
    }

    fun isMuted(): Boolean = isMuted
    fun getCurrentTrack(): MusicTrack = TRACKS.find { it.id == currentTrackId } ?: TRACKS.first()
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

    private fun notifyState() {
        onStateChanged?.invoke(getCurrentTrack(), isMuted)
    }

    private fun savePrefs(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_SELECTED_TRACK, currentTrackId)
            .putBoolean(KEY_IS_MUTED, isMuted)
            .apply()
    }
}
