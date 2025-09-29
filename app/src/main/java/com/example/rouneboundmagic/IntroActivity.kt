package com.example.rouneboundmagic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton

class IntroActivity : AppCompatActivity() {

    private lateinit var startButton: MaterialButton
    private lateinit var backgroundView: ImageView
    private lateinit var runeFireView: ImageView
    private lateinit var runeWaterView: ImageView
    private lateinit var runeAirView: ImageView
    private lateinit var runeEarthView: ImageView
    private lateinit var mageGroup: View
    private lateinit var magePortrait: ImageView
    private lateinit var priestessGroup: View
    private lateinit var priestessPortrait: ImageView

    private val handler = Handler(Looper.getMainLooper())
    private val scheduledTasks = mutableListOf<Runnable>()
    private val glowAnimators = mutableListOf<android.animation.ValueAnimator>()
    private val activeBitmaps = mutableListOf<Bitmap>()
    private val narrationQueue = ArrayDeque(listOf(R.raw.a1, R.raw.a2, R.raw.a3, R.raw.a4))
    private var mediaPlayer: MediaPlayer? = null
    private var introCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_intro)

        bindViews()
        loadArtwork()
        val alreadyFinished = prepareInitialState(savedInstanceState)
        setupStartButton()
        if (!alreadyFinished) {
            scheduleRuneReveals()
            playNextNarration()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_MAGE_VISIBLE, mageGroup.isVisible)
        outState.putBoolean(KEY_PRIESTESS_VISIBLE, priestessGroup.isVisible)
        outState.putBoolean(KEY_INTRO_FINISHED, introCompleted)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelScheduledTasks()
        releaseNarration()
        releaseBitmaps()
        glowAnimators.forEach { it.cancel() }
        glowAnimators.clear()
    }

    private fun bindViews() {
        backgroundView = findViewById(R.id.introBackground)
        runeFireView = findViewById(R.id.runeFire)
        runeWaterView = findViewById(R.id.runeWater)
        runeAirView = findViewById(R.id.runeAir)
        runeEarthView = findViewById(R.id.runeEarth)
        mageGroup = findViewById(R.id.mageContainer)
        magePortrait = findViewById(R.id.magePortrait)
        priestessGroup = findViewById(R.id.priestessContainer)
        priestessPortrait = findViewById(R.id.priestessPortrait)
        startButton = findViewById(R.id.startGameButton)
    }

    private fun loadArtwork() {
        backgroundView.setImageBitmap(loadBitmap("intro/MysticalTempleRuins"))
        runeFireView.setImageBitmap(loadBitmap("puzzle/red_gem.png"))
        runeWaterView.setImageBitmap(loadBitmap("puzzle/blue_gem.png"))
        runeAirView.setImageBitmap(loadBitmap("puzzle/turquoise.png"))
        runeEarthView.setImageBitmap(loadBitmap("puzzle/green_gem.png"))
        magePortrait.setImageBitmap(loadBitmap("characters/black_mage.png"))
        priestessPortrait.setImageBitmap(loadBitmap("characters/mystical_priestess.png"))
    }

    private fun prepareInitialState(savedInstanceState: Bundle?): Boolean {
        introCompleted = savedInstanceState?.getBoolean(KEY_INTRO_FINISHED) == true
        mageGroup.isVisible = savedInstanceState?.getBoolean(KEY_MAGE_VISIBLE) == true
        priestessGroup.isVisible = savedInstanceState?.getBoolean(KEY_PRIESTESS_VISIBLE) == true
        val runeViews = listOf(runeFireView, runeWaterView, runeAirView, runeEarthView)
        runeViews.forEach { view ->
            view.scaleX = 0.6f
            view.scaleY = 0.6f
            view.alpha = if (introCompleted) 1f else 0f
            if (!introCompleted) {
                view.isVisible = false
            } else {
                view.isVisible = true
                view.scaleX = 1f
                view.scaleY = 1f
            }
        }
        startButton.isVisible = introCompleted
        startButton.alpha = if (introCompleted) 1f else 0f
        startButton.isEnabled = introCompleted
        if (introCompleted) {
            startRuneGlow(runeFireView)
            startRuneGlow(runeWaterView)
            startRuneGlow(runeAirView)
            startRuneGlow(runeEarthView)
        }
        return introCompleted
    }

    private fun setupStartButton() {
        startButton.setOnClickListener {
            startActivity(Intent(this, StartGameActivity::class.java))
            finish()
        }
    }

    private fun scheduleRuneReveals() {
        schedule(3_000L) { revealRune(runeFireView) }
        schedule(4_000L) { revealRune(runeWaterView) }
        schedule(5_000L) { revealRune(runeAirView) }
        schedule(6_000L) { revealRune(runeEarthView) }
    }

    private fun schedule(delayMillis: Long, block: () -> Unit) {
        val runnable = Runnable(block)
        scheduledTasks += runnable
        handler.postDelayed(runnable, delayMillis)
    }

    private fun revealRune(target: ImageView) {
        target.isVisible = true
        target.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(320L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun playNextNarration() {
        val nextRes = narrationQueue.removeFirstOrNull() ?: run {
            onNarrationFinished()
            return
        }
        releaseNarration()
        mediaPlayer = MediaPlayer.create(this, nextRes).apply {
            setOnCompletionListener {
                playNextNarration()
            }
            start()
        }
        when (nextRes) {
            R.raw.a2 -> showBlackMage()
            R.raw.a3 -> showPriestess()
            R.raw.a4 -> showFinale()
        }
    }

    private fun showBlackMage() {
        mageGroup.isVisible = true
        mageGroup.alpha = 0f
        mageGroup.scaleX = 0.85f
        mageGroup.scaleY = 0.85f
        mageGroup.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(420L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showPriestess() {
        priestessGroup.isVisible = true
        priestessGroup.alpha = 0f
        priestessGroup.animate()
            .alpha(1f)
            .setDuration(420L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showFinale() {
        mageGroup.isVisible = true
        priestessGroup.isVisible = true
        startRuneGlow(runeFireView)
        startRuneGlow(runeWaterView)
        startRuneGlow(runeAirView)
        startRuneGlow(runeEarthView)
    }

    private fun startRuneGlow(target: ImageView) {
        target.isVisible = true
        val animator = android.animation.ValueAnimator.ofFloat(0.85f, 1.05f).apply {
            duration = 900L
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.REVERSE
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                target.scaleX = scale
                target.scaleY = scale
                target.alpha = 0.6f + (scale - 0.85f) * 1.2f
            }
            start()
        }
        glowAnimators += animator
    }

    private fun onNarrationFinished() {
        introCompleted = true
        startButton.isVisible = true
        startButton.alpha = 0f
        startButton.animate()
            .alpha(1f)
            .setDuration(450L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { startButton.isEnabled = true }
            .start()
    }

    private fun releaseNarration() {
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun cancelScheduledTasks() {
        scheduledTasks.forEach(handler::removeCallbacks)
        scheduledTasks.clear()
    }

    private fun releaseBitmaps() {
        activeBitmaps.forEach(Bitmap::recycle)
        activeBitmaps.clear()
    }

    private fun loadBitmap(path: String): Bitmap? {
        val bitmap = runCatching {
            assets.open(path).use(BitmapFactory::decodeStream)
        }.getOrNull()
        bitmap?.let(activeBitmaps::add)
        return bitmap
    }

    companion object {
        private const val KEY_MAGE_VISIBLE = "mage_visible"
        private const val KEY_PRIESTESS_VISIBLE = "priestess_visible"
        private const val KEY_INTRO_FINISHED = "intro_finished"
    }
}
