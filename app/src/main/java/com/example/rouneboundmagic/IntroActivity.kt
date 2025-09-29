package com.example.rouneboundmagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class IntroActivity : AppCompatActivity() {

    private lateinit var heroCardView: ImageView
    private lateinit var heroNameView: TextView
    private lateinit var heroDescriptionView: TextView
    private lateinit var villainCardView: ImageView
    private lateinit var continueButton: Button
    private lateinit var selectedHero: HeroOption

    private var heroBitmap: Bitmap? = null
    private var villainBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_intro)

        heroCardView = findViewById(R.id.heroCard)
        heroNameView = findViewById(R.id.heroName)
        heroDescriptionView = findViewById(R.id.heroDescription)
        villainCardView = findViewById(R.id.villainCard)
        continueButton = findViewById(R.id.continueButton)

        selectedHero = HeroOption.fromName(intent.getStringExtra(EXTRA_SELECTED_HERO))
        bindHero(selectedHero)
        bindVillain()

        continueButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_SELECTED_HERO, selectedHero.name)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heroBitmap?.recycle()
        heroBitmap = null
        villainBitmap?.recycle()
        villainBitmap = null
    }

    private fun bindHero(hero: HeroOption) {
        heroNameView.text = getString(hero.displayNameRes)
        heroDescriptionView.text = getString(hero.descriptionRes)
        heroBitmap = loadBitmap(hero.assetPath)
        heroBitmap?.let { heroCardView.setImageDrawable(BitmapDrawable(resources, it)) }
    }

    private fun bindVillain() {
        villainBitmap = loadBitmap(VILLAIN_ASSET_PATH)
        villainBitmap?.let { villainCardView.setImageDrawable(BitmapDrawable(resources, it)) }
    }

    private fun loadBitmap(path: String): Bitmap? {
        return runCatching {
            assets.open(path).use(BitmapFactory::decodeStream)
        }.getOrNull()
    }

    companion object {
        const val EXTRA_SELECTED_HERO = "selected_hero"
        private const val VILLAIN_ASSET_PATH = "characters/black_mage.png"
    }
}
