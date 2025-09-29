package com.example.rouneboundmagic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class StartGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_start_game)

        val startPlayButton: Button = findViewById(R.id.startPlayButton)
        startPlayButton.setOnClickListener {
            startActivity(Intent(this, CharacterSelectionActivity::class.java))
        }
    }
}
