package com.example.runeboundmagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2

class StartGameActivity : AppCompatActivity() {
    private var lobbyBackgroundBitmap: Bitmap? = null
    private var selectedHero: HeroOption? = null
    private var currentHeroIndex: Int = 0
    private val heroes: List<HeroOption> = HeroOption.values().toList()
    private lateinit var selectHeroButton: View
    private lateinit var startBattleButton: View
    private lateinit var backButton: View
    private lateinit var selectedHeroLabel: TextView
    private lateinit var heroNameLabel: TextView
    private lateinit var heroDescriptionLabel: TextView
    private lateinit var heroCarousel: ViewPager2
    private lateinit var heroAdapter: HeroCarouselAdapter
    private lateinit var nextHeroButton: View
    private lateinit var previousHeroButton: View

    private val heroBitmaps: MutableMap<HeroOption, Bitmap?> = mutableMapOf()
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentHeroIndex = position
            updateHeroPreview()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_start_game)

        val backgroundView: ImageView = findViewById(R.id.lobbyBackground)
        lobbyBackgroundBitmap = loadLobbyBackground()
        lobbyBackgroundBitmap?.let(backgroundView::setImageBitmap)

        heroNameLabel = findViewById(R.id.heroName)
        heroDescriptionLabel = findViewById(R.id.heroDescription)
        selectedHeroLabel = findViewById(R.id.selectedHeroLabel)
        selectHeroButton = findViewById(R.id.selectHeroButton)
        startBattleButton = findViewById(R.id.startBattleButton)
        backButton = findViewById(R.id.backButton)
        heroCarousel = findViewById(R.id.heroCarousel)
        nextHeroButton = findViewById(R.id.nextHeroButton)
        previousHeroButton = findViewById(R.id.previousHeroButton)

        loadHeroBitmaps()
        heroAdapter = HeroCarouselAdapter(heroes, heroBitmaps)
        heroCarousel.adapter = heroAdapter
        heroCarousel.offscreenPageLimit = heroes.size
        heroCarousel.clipToPadding = false
        heroCarousel.clipChildren = false
        heroCarousel.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.alpha = 1f - (0.35f * absPos)
            val scale = 0.85f + (1f - absPos) * 0.15f
            page.scaleY = scale
            page.scaleX = scale
            page.translationX = -page.width * 0.08f * position
        }
        heroCarousel.registerOnPageChangeCallback(pageChangeCallback)

        nextHeroButton.setOnClickListener {
            if (heroAdapter.itemCount == 0) return@setOnClickListener
            val next = (heroCarousel.currentItem + 1) % heroAdapter.itemCount
            heroCarousel.setCurrentItem(next, true)
        }
        previousHeroButton.setOnClickListener {
            if (heroAdapter.itemCount == 0) return@setOnClickListener
            val previous = if (heroCarousel.currentItem - 1 < 0) {
                heroAdapter.itemCount - 1
            } else {
                heroCarousel.currentItem - 1
            }
            heroCarousel.setCurrentItem(previous, true)
        }

        currentHeroIndex = savedInstanceState?.getInt(KEY_CURRENT_HERO_INDEX) ?: 0
        heroCarousel.setCurrentItem(currentHeroIndex, false)
        updateHeroPreview()

        selectedHero = savedInstanceState?.getString(KEY_SELECTED_HERO)?.let(HeroOption::fromName)
        selectedHero?.let(::setSelectedHero)

        selectHeroButton.setOnClickListener {
            val hero = heroes[currentHeroIndex]
            setSelectedHero(hero)
            Toast.makeText(
                this,
                getString(R.string.lobby_selected_hero, getString(hero.displayNameRes)),
                Toast.LENGTH_SHORT
            ).show()
        }

        backButton.setOnClickListener {
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
        heroCarousel.unregisterOnPageChangeCallback(pageChangeCallback)
        lobbyBackgroundBitmap?.recycle()
        lobbyBackgroundBitmap = null
        heroBitmaps.values.filterNotNull().forEach(Bitmap::recycle)
        heroBitmaps.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SELECTED_HERO, selectedHero?.name)
        outState.putInt(KEY_CURRENT_HERO_INDEX, heroCarousel.currentItem)
    }

    private fun loadLobbyBackground(): Bitmap? {
        return runCatching {
            assets.open(LOBBY_BACKGROUND_ASSET).use(BitmapFactory::decodeStream)
        }.getOrNull()
    }

    private fun loadHeroBitmaps() {
        heroBitmaps.clear()
        heroes.forEach { hero ->
            val bitmap = runCatching {
                assets.open(hero.assetPath).use(BitmapFactory::decodeStream)
            }.getOrNull()
            heroBitmaps[hero] = bitmap
        }
    }

    private fun updateHeroPreview() {
        val safeIndex = currentHeroIndex.coerceIn(0, heroes.lastIndex)
        val hero = heroes[safeIndex]
        currentHeroIndex = safeIndex
        heroNameLabel.setText(hero.displayNameRes)
        heroDescriptionLabel.setText(hero.descriptionRes)
        if (selectHeroButton is TextView) {
            (selectHeroButton as TextView).text = getString(
                R.string.lobby_select_hero_with_choice,
                getString(hero.displayNameRes)
            )
        }
        startBattleButton.isEnabled = selectedHero != null
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
        private const val KEY_CURRENT_HERO_INDEX = "key_current_hero_index"
    }
}
