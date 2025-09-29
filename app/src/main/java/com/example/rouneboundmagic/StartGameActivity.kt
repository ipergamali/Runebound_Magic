package com.example.rouneboundmagic

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class StartGameActivity : AppCompatActivity() {
    private var lobbyBackgroundBitmap: Bitmap? = null
    private var selectedHero: HeroOption? = null
    private lateinit var selectHeroButton: View
    private lateinit var startBattleButton: View
    private lateinit var selectedHeroLabel: TextView

    private val heroSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val heroName = result.data?.getStringExtra(CharacterSelectionActivity.EXTRA_SELECTED_HERO)
            val hero = HeroOption.fromName(heroName)
            setSelectedHero(hero)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_start_game)

        val backgroundView: ImageView = findViewById(R.id.lobbyBackground)
        lobbyBackgroundBitmap = loadLobbyBackground()
        lobbyBackgroundBitmap?.let(backgroundView::setImageBitmap)

        selectHeroButton = findViewById(R.id.selectHeroButton)
        startBattleButton = findViewById(R.id.startBattleButton)
        selectedHeroLabel = findViewById(R.id.selectedHeroLabel)

        selectedHero = savedInstanceState?.getString(KEY_SELECTED_HERO)?.let(HeroOption::fromName)
        selectedHero?.let(::setSelectedHero)

        selectHeroButton.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            selectedHero?.let { intent.putExtra(CharacterSelectionActivity.EXTRA_SELECTED_HERO, it.name) }
            heroSelectionLauncher.launch(intent)
        }

        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }

        startBattleButton.setOnClickListener {
            val hero = selectedHero
            if (hero == null) {
                Toast.makeText(this, R.string.lobby_select_prompt, Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_SELECTED_HERO, hero.name)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lobbyBackgroundBitmap?.recycle()
        lobbyBackgroundBitmap = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SELECTED_HERO, selectedHero?.name)
    }

    private fun loadLobbyBackground(): Bitmap? {
        return runCatching {
            assets.open(LOBBY_BACKGROUND_ASSET).use(BitmapFactory::decodeStream)
        }.getOrNull()
    }

    private fun setSelectedHero(hero: HeroOption) {
        selectedHero = hero
        selectHeroButton.isEnabled = true
        startBattleButton.isEnabled = true
        selectedHeroLabel.text = getString(R.string.lobby_selected_hero, getString(hero.displayNameRes))
        if (selectHeroButton is TextView) {
            (selectHeroButton as TextView).text = getString(R.string.lobby_select_hero_with_choice, getString(hero.displayNameRes))
        }
    }

    companion object {
        private const val LOBBY_BACKGROUND_ASSET = "lobby/Game_Lobby.png"
        private const val KEY_SELECTED_HERO = "key_selected_hero"
    }
}
