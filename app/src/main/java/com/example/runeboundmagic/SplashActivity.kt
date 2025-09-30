package com.example.runeboundmagic

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.runeboundmagic.audio.BackgroundMusicController
import com.example.runeboundmagic.ui.RuneboundMagicApp

class SplashActivity : ComponentActivity() {
    private val backgroundMusicController by lazy { BackgroundMusicController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        lifecycle.addObserver(backgroundMusicController)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RuneboundMagicApp(backgroundMusicController)
        }
    }
}
