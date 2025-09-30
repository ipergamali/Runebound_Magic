package com.example.runeboundmagic.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.runeboundmagic.R

class BackgroundMusicController(private val context: Context) : DefaultLifecycleObserver {
    private var mediaPlayer: MediaPlayer? = null
    private var shouldResumeOnStart = false

    fun startOrResume() {
        shouldResumeOnStart = true
        val player = ensurePlayer()
        if (!player.isPlaying) {
            player.start()
        }
    }

    fun stop() {
        shouldResumeOnStart = false
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }

    override fun onStart(owner: LifecycleOwner) {
        if (shouldResumeOnStart) {
            val player = ensurePlayer()
            if (!player.isPlaying) {
                player.start()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
    }

    private fun ensurePlayer(): MediaPlayer {
        mediaPlayer?.let { return it }
        val assetDescriptor = context.resources.openRawResourceFd(R.raw.soundtrack)
        val player = MediaPlayer()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player.setDataSource(assetDescriptor.fileDescriptor, assetDescriptor.startOffset, assetDescriptor.length)
        assetDescriptor.close()
        player.isLooping = true
        player.prepare()
        player.setVolume(1f, 1f)
        mediaPlayer = player
        return player
    }
}
