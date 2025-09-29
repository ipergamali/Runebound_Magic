package com.example.rouneboundmagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class CharacterSelectionActivity : AppCompatActivity() {

    private val heroBitmaps = mutableMapOf<String, Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_character_selection)

        setupHeroCard(
            imageId = R.id.warriorCard,
            labelId = R.id.warriorLabel,
            hero = HeroOption.WARRIOR
        )
        setupHeroCard(
            imageId = R.id.mageCard,
            labelId = R.id.mageLabel,
            hero = HeroOption.MAGE
        )
        setupHeroCard(
            imageId = R.id.priestessCard,
            labelId = R.id.priestessLabel,
            hero = HeroOption.MYSTICAL_PRIESTESS
        )
        setupHeroCard(
            imageId = R.id.rangerCard,
            labelId = R.id.rangerLabel,
            hero = HeroOption.RANGER
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        heroBitmaps.values.forEach(Bitmap::recycle)
        heroBitmaps.clear()
    }

    private fun setupHeroCard(imageId: Int, labelId: Int, hero: HeroOption) {
        val imageView: ImageView = findViewById(imageId)
        val labelView: TextView = findViewById(labelId)

        labelView.text = getString(hero.displayNameRes)
        imageView.setImageBitmap(loadBitmap(hero.assetPath))
        imageView.setOnClickListener { onHeroSelected(hero) }
    }

    private fun onHeroSelected(hero: HeroOption) {
        val intent = Intent(this, IntroActivity::class.java)
        intent.putExtra(IntroActivity.EXTRA_SELECTED_HERO, hero.name)
        startActivity(intent)
    }

    private fun loadBitmap(path: String): Bitmap? {
        return heroBitmaps[path] ?: run {
            val bitmap = runCatching {
                assets.open(path).use(BitmapFactory::decodeStream)
            }.getOrNull()
            if (bitmap != null) {
                heroBitmaps[path] = bitmap
            }
            bitmap
        }
    }
}
