package com.example.rouneboundmagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class StartGameActivity : AppCompatActivity() {
    private var lobbyBackgroundBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_start_game)

        val backgroundView: ImageView = findViewById(R.id.lobbyBackground)
        lobbyBackgroundBitmap = loadLobbyBackground()
        lobbyBackgroundBitmap?.let(backgroundView::setImageBitmap)

        findViewById<View>(R.id.selectHeroHotspot).setOnClickListener {
            startActivity(Intent(this, CharacterSelectionActivity::class.java))
        }

        findViewById<View>(R.id.backHotspot).setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.startBattleHotspot).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lobbyBackgroundBitmap?.recycle()
        lobbyBackgroundBitmap = null
    }

    private fun loadLobbyBackground(): Bitmap? {
        return runCatching {
            assets.open(LOBBY_BACKGROUND_ASSET).use(BitmapFactory::decodeStream)
        }.getOrNull()
    }

    companion object {
        private const val LOBBY_BACKGROUND_ASSET = "lobby/Game_Lobby.png"
    }
}
