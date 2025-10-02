package com.example.runeboundmagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.runeboundmagic.codex.HeroCodexActivity
import com.example.runeboundmagic.data.local.HeroChoiceDatabase
import com.example.runeboundmagic.data.local.HeroChoiceEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StartGameActivity : AppCompatActivity() {
    private var lobbyBackgroundBitmap: Bitmap? = null
    private var selectedHero: HeroOption? = null
    private var currentHeroIndex: Int = 0
    private var selectedHeroName: String? = null
    private val heroes: List<HeroOption> = HeroOption.values().toList()
    private lateinit var heroChoiceViewModel: HeroChoiceViewModel
    private lateinit var heroNameInputLayout: TextInputLayout
    private lateinit var heroNameInput: TextInputEditText
    private lateinit var confirmSelectionButton: MaterialButton
    private lateinit var startBattleButton: MaterialButton
    private lateinit var openCodexButton: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var selectedHeroLabel: TextView
    private lateinit var heroNameLabel: TextView
    private lateinit var heroDescriptionLabel: TextView
    private lateinit var heroCarousel: ViewPager2
    private lateinit var heroAdapter: HeroCarouselAdapter
    private lateinit var nextHeroButton: MaterialButton
    private lateinit var previousHeroButton: MaterialButton

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

        heroChoiceViewModel = ViewModelProvider(
            this,
            HeroChoiceViewModelFactory(
                HeroChoiceDatabase.getInstance(applicationContext).heroChoiceDao()
            )
        )[HeroChoiceViewModel::class.java]

        val backgroundView: ImageView = findViewById(R.id.lobbyBackground)
        lobbyBackgroundBitmap = loadLobbyBackground()
        lobbyBackgroundBitmap?.let(backgroundView::setImageBitmap)

        heroNameLabel = findViewById(R.id.heroName)
        heroDescriptionLabel = findViewById(R.id.heroDescription)
        selectedHeroLabel = findViewById(R.id.selectedHeroLabel)
        heroNameInputLayout = findViewById(R.id.heroNameInputLayout)
        heroNameInput = findViewById(R.id.heroNameInput)
        confirmSelectionButton = findViewById(R.id.selectHeroButton)
        startBattleButton = findViewById(R.id.startBattleButton)
        openCodexButton = findViewById(R.id.openCodexButton)
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

        openCodexButton.isEnabled = false

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

        val restoredHero = savedInstanceState?.getString(KEY_SELECTED_HERO)?.let(HeroOption::fromName)
        selectedHeroName = savedInstanceState?.getString(KEY_SELECTED_HERO_NAME)
        if (restoredHero != null && !selectedHeroName.isNullOrBlank()) {
            setSelectedHero(restoredHero, selectedHeroName!!)
        }

        heroNameInput.doAfterTextChanged { text ->
            if (!text.isNullOrBlank()) {
                heroNameInputLayout.error = null
            }
        }

        confirmSelectionButton.setOnClickListener {
            val hero = heroes[currentHeroIndex]
            val customName = heroNameInput.text?.toString()?.trim().orEmpty()
            if (customName.isBlank()) {
                heroNameInputLayout.error = getString(R.string.error_empty_hero_name)
                return@setOnClickListener
            }

            setSelectedHero(hero, customName)
            heroChoiceViewModel.saveHeroChoice(
                playerName = "Player1",
                heroType = hero.toHeroType(),
                heroName = customName
            )

            Toast.makeText(
                this,
                getString(R.string.lobby_selection_saved, customName, getString(hero.displayNameRes)),
                Toast.LENGTH_SHORT
            ).show()
        }

        openCodexButton.setOnClickListener {
            val hero = selectedHero
            if (hero == null) {
                Toast.makeText(this, R.string.hero_codex_missing_selection, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val resolvedName = selectedHeroName
                ?: heroNameInput.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() }
                ?: getString(hero.displayNameRes)

            val intent = HeroCodexActivity.createIntent(
                context = this,
                heroOption = hero,
                heroName = resolvedName
            )
            startActivity(intent)
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                heroChoiceViewModel.getLastChoice().collectLatest(::applySavedChoice)
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
        outState.putString(KEY_SELECTED_HERO_NAME, selectedHeroName)
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
        confirmSelectionButton.text = getString(
            R.string.lobby_confirm_selection_with_choice,
            getString(hero.displayNameRes)
        )
        val hasSelection = selectedHero != null
        startBattleButton.isEnabled = hasSelection
        openCodexButton.isEnabled = hasSelection
    }

    private fun setSelectedHero(hero: HeroOption, customName: String) {
        selectedHero = hero
        selectedHeroName = customName
        confirmSelectionButton.isEnabled = true
        startBattleButton.isEnabled = true
        openCodexButton.isEnabled = true
        selectedHeroLabel.text = getString(
            R.string.lobby_selected_custom_hero,
            customName,
            getString(hero.displayNameRes)
        )
        if (heroNameInput.text?.toString() != customName) {
            heroNameInput.setText(customName)
            heroNameInput.setSelection(customName.length)
        }
        heroNameInputLayout.error = null
    }

    private fun applySavedChoice(choice: HeroChoiceEntity?) {
        if (choice == null) {
            selectedHeroLabel.text = getString(R.string.lobby_select_prompt)
            selectedHero = null
            selectedHeroName = null
            startBattleButton.isEnabled = false
            openCodexButton.isEnabled = false
            if (heroNameInput.text?.isNotEmpty() == true) {
                heroNameInput.setText("")
            }
            return
        }

        val heroOption = choice.heroType.toHeroOption()
        val heroIndex = heroes.indexOf(heroOption)
        if (heroIndex >= 0 && heroCarousel.currentItem != heroIndex) {
            heroCarousel.setCurrentItem(heroIndex, false)
            currentHeroIndex = heroIndex
            updateHeroPreview()
        }
        setSelectedHero(heroOption, choice.heroName)
    }

    companion object {
        private const val LOBBY_BACKGROUND_ASSET = "lobby/Game_Lobby.png"
        private const val KEY_SELECTED_HERO = "key_selected_hero"
        private const val KEY_CURRENT_HERO_INDEX = "key_current_hero_index"
        private const val KEY_SELECTED_HERO_NAME = "key_selected_hero_name"
    }
}
