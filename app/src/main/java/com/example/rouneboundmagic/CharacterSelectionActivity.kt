package com.example.rouneboundmagic

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CharacterSelectionActivity : AppCompatActivity() {

    private val heroBitmaps = mutableMapOf<String, Bitmap>()
    private lateinit var confirmButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private val heroCards = mutableMapOf<HeroOption, MaterialCardView>()
    private var selectedHero: HeroOption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_character_selection)

        confirmButton = findViewById(R.id.confirmSelectionButton)
        cancelButton = findViewById(R.id.cancelSelectionButton)

        confirmButton.isEnabled = false
        confirmButton.setOnClickListener { returnSelection() }
        cancelButton.setOnClickListener { finish() }

        setupHeroCard(R.id.cardWarrior, R.id.heroImageWarrior, R.id.heroLabelWarrior, HeroOption.WARRIOR)
        setupHeroCard(R.id.cardMage, R.id.heroImageMage, R.id.heroLabelMage, HeroOption.MAGE)
        setupHeroCard(R.id.cardPriestess, R.id.heroImagePriestess, R.id.heroLabelPriestess, HeroOption.MYSTICAL_PRIESTESS)
        setupHeroCard(R.id.cardRanger, R.id.heroImageRanger, R.id.heroLabelRanger, HeroOption.RANGER)

        selectedHero = savedInstanceState?.getString(KEY_SELECTED_HERO)?.let(HeroOption::fromName)
            ?: intent.getStringExtra(EXTRA_SELECTED_HERO)?.let(HeroOption::fromName)
        selectedHero?.let { highlightSelection(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SELECTED_HERO, selectedHero?.name)
    }

    override fun onDestroy() {
        super.onDestroy()
        heroBitmaps.values.forEach(Bitmap::recycle)
        heroBitmaps.clear()
    }

    private fun setupHeroCard(cardId: Int, imageId: Int, labelId: Int, hero: HeroOption) {
        val card: MaterialCardView = findViewById(cardId)
        val heroImage: ImageView = findViewById(imageId)
        val heroLabel: TextView = findViewById(labelId)

        heroImage.setImageBitmap(loadBitmap(hero.assetPath))
        heroLabel.text = getString(hero.displayNameRes)

        card.setOnClickListener {
            highlightSelection(hero)
        }
        heroCards[hero] = card
    }

    private fun highlightSelection(hero: HeroOption) {
        selectedHero = hero
        heroCards.forEach { (option, card) ->
            val isSelected = option == hero
            card.strokeWidth = if (isSelected) {
                resources.getDimensionPixelSize(R.dimen.hero_card_stroke_width)
            } else {
                0
            }
            card.strokeColor = if (isSelected) {
                getColor(R.color.teal_200)
            } else {
                getColor(android.R.color.transparent)
            }
            card.cardElevation = if (isSelected) 12f else 4f
        }
        confirmButton.isEnabled = true
    }

    private fun returnSelection() {
        val hero = selectedHero ?: return
        setResult(
            Activity.RESULT_OK,
            android.content.Intent().putExtra(EXTRA_SELECTED_HERO, hero.name)
        )
        finish()
    }

    private fun loadBitmap(path: String): Bitmap? {
        heroBitmaps[path]?.let { return it }
        val bitmap = runCatching {
            assets.open(path).use(BitmapFactory::decodeStream)
        }.getOrNull()
        if (bitmap != null) {
            heroBitmaps[path] = bitmap
        }
        return bitmap
    }

    companion object {
        const val EXTRA_SELECTED_HERO = "selected_hero"
        private const val KEY_SELECTED_HERO = "key_selected_hero"
    }
}
