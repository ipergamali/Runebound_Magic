package com.example.rouneboundmagic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var startGameButton: Button
    private var hasShownButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        videoView = findViewById(R.id.introVideoView)
        startGameButton = findViewById(R.id.startGameButton)

        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.intro}")
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            videoView.start()
        }
        videoView.setOnCompletionListener { showStartButton() }
        videoView.setOnErrorListener { _, _, _ ->
            showStartButton()
            true
        }
        videoView.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            }
            showStartButton()
        }

        startGameButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showStartButton() {
        if (hasShownButton) return

        hasShownButton = true
        if (videoView.isPlaying) {
            videoView.stopPlayback()
        }
        startGameButton.visibility = View.VISIBLE
    }
}
