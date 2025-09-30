package com.example.runeboundmagic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.runeboundmagic.ui.IntroScreen

class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            IntroScreen(
                onIntroFinished = {
                    startActivity(Intent(this, StartGameActivity::class.java))
                    finish()
                }
            )
        }
    }
}
