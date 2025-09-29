package com.example.rouneboundmagic

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.IOException

class IntroActivity : AppCompatActivity() {

    private lateinit var backgroundImage: ImageView
    private lateinit var wizardAura: ImageView
    private lateinit var elfAura: ImageView
    private lateinit var blackWizard: ImageView
    private lateinit var elfGuardian: ImageView
    private lateinit var gemRow: LinearLayout
    private lateinit var redGem: ImageView
    private lateinit var blueGem: ImageView
    private lateinit var turquoiseGem: ImageView
    private lateinit var greenGem: ImageView
    private lateinit var subtitleText: TextView
    private lateinit var startButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private val assetBitmaps = mutableMapOf<String, Bitmap>()
    private val glowAnimators = mutableMapOf<View, ValueAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        setContentView(R.layout.activity_intro)

        backgroundImage = findViewById(R.id.backgroundImage)
        wizardAura = findViewById(R.id.wizardAura)
        elfAura = findViewById(R.id.elfAura)
        blackWizard = findViewById(R.id.blackWizard)
        elfGuardian = findViewById(R.id.elfGuardian)
        gemRow = findViewById(R.id.gemRow)
        redGem = findViewById(R.id.redGem)
        blueGem = findViewById(R.id.blueGem)
        turquoiseGem = findViewById(R.id.turquoiseGem)
        greenGem = findViewById(R.id.greenGem)
        subtitleText = findViewById(R.id.subtitleText)
        startButton = findViewById(R.id.startButton)

        loadBackground()
        loadCharacterAssets()

        startButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        startScene(0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
        glowAnimators.values.forEach(ValueAnimator::cancel)
        glowAnimators.clear()
        assetBitmaps.values.forEach(Bitmap::recycle)
        assetBitmaps.clear()
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun loadBackground() {
        loadBitmap("intro/MysticalTempleRuins")?.let { bitmap ->
            backgroundImage.setImageDrawable(BitmapDrawable(resources, bitmap))
        }
    }

    private fun loadCharacterAssets() {
        loadBitmap("puzzle/red_gem.png")?.let(redGem::setImageBitmap)
        loadBitmap("puzzle/blue_gem.png")?.let(blueGem::setImageBitmap)
        loadBitmap("puzzle/green_gem.png")?.let(greenGem::setImageBitmap)
        loadBitmap("puzzle/turquoise.png")?.let(turquoiseGem::setImageBitmap)
        loadBitmap("puzzle/black_wizard.png")?.let(blackWizard::setImageBitmap)
        loadBitmap("puzzle/elf.png")?.let(elfGuardian::setImageBitmap)
    }

    private fun loadBitmap(path: String): Bitmap? {
        return assetBitmaps[path] ?: run {
            val bitmap = try {
                assets.open(path).use(BitmapFactory::decodeStream)
            } catch (io: IOException) {
                null
            }
            if (bitmap != null) {
                assetBitmaps[path] = bitmap
            }
            bitmap
        }
    }

    private fun startScene(index: Int) {
        handler.removeCallbacksAndMessages(null)
        when (index) {
            0 -> playSceneOne()
            1 -> playSceneTwo()
            2 -> playSceneThree()
            3 -> playSceneFour()
        }
    }

    private fun playSceneOne() {
        showSubtitle("The world was once bound by the elemental runes — Fire, Water, Air, and Earth — that kept the balance of magic alive.")
        prepareGemsForScene()
        playAudio(R.raw.a1) {
            startScene(1)
        }
        handler.postDelayed({ revealGem(redGem) }, 3000L)
        handler.postDelayed({ revealGem(blueGem) }, 4000L)
        handler.postDelayed({ revealGem(turquoiseGem) }, 5000L)
        handler.postDelayed({ revealGem(greenGem) }, 6000L)
    }

    private fun playSceneTwo() {
        stopGlow(redGem)
        stopGlow(blueGem)
        stopGlow(turquoiseGem)
        stopGlow(greenGem)
        fadeOutView(redGem)
        fadeOutView(blueGem)
        fadeOutView(turquoiseGem)
        fadeOutView(greenGem)

        showSubtitle("But balance is a chain meant to be broken… and I, the Black Wizard, will forge a new world from the ashes.")
        fadeInCharacter(blackWizard, wizardAura)

        playAudio(R.raw.a2) {
            startScene(2)
        }
    }

    private fun playSceneThree() {
        fadeOutCharacter(blackWizard, wizardAura)
        showSubtitle("Yet hope remains. A lone guardian rises, chosen by the runes themselves, to stand against the growing darkness.")
        fadeInCharacter(elfGuardian, elfAura)

        playAudio(R.raw.a3) {
            startScene(3)
        }
    }

    private fun playSceneFour() {
        val offset = resources.displayMetrics.widthPixels * 0.28f

        showSubtitle("The battle of Fire, Water, Air, and Earth has begun!")

        showFinalCharacter(blackWizard, wizardAura, offset)
        showFinalCharacter(elfGuardian, elfAura, -offset)

        prepareGemsForScene()
        revealGem(redGem)
        revealGem(blueGem)
        revealGem(turquoiseGem)
        revealGem(greenGem)

        playAudio(R.raw.a4) {
            startButton.visibility = View.VISIBLE
            startButton.alpha = 0f
            startButton.animate().alpha(1f).setDuration(600L).start()
        }
    }

    private fun prepareGemsForScene() {
        gemRow.visibility = View.VISIBLE
        listOf(redGem, blueGem, turquoiseGem, greenGem).forEach { gem ->
            gem.visibility = View.VISIBLE
            gem.alpha = 0f
            gem.scaleX = 1f
            gem.scaleY = 1f
        }
    }

    private fun revealGem(gem: ImageView) {
        gem.animate().cancel()
        gem.visibility = View.VISIBLE
        gem.alpha = 0f
        gem.animate()
            .alpha(1f)
            .setDuration(500L)
            .withEndAction { startGlow(gem) }
            .start()
    }

    private fun fadeOutView(view: View) {
        view.animate().cancel()
        view.animate()
            .alpha(0f)
            .setDuration(400L)
            .withEndAction {
                view.visibility = View.GONE
            }
            .start()
    }

    private fun fadeInCharacter(character: ImageView, aura: ImageView) {
        character.animate().cancel()
        aura.animate().cancel()

        character.translationX = 0f
        aura.translationX = 0f

        aura.visibility = View.VISIBLE
        aura.alpha = 0f
        aura.animate()
            .alpha(1f)
            .setDuration(600L)
            .withEndAction { startGlow(aura) }
            .start()

        character.visibility = View.VISIBLE
        character.alpha = 0f
        character.animate()
            .alpha(1f)
            .setDuration(600L)
            .start()
    }

    private fun fadeOutCharacter(character: ImageView, aura: ImageView) {
        stopGlow(aura)
        stopGlow(character)
        character.animate().cancel()
        aura.animate().cancel()

        character.animate()
            .alpha(0f)
            .setDuration(500L)
            .withEndAction {
                character.visibility = View.GONE
            }
            .start()

        aura.animate()
            .alpha(0f)
            .setDuration(500L)
            .withEndAction {
                aura.visibility = View.GONE
            }
            .start()
    }

    private fun showFinalCharacter(character: ImageView, aura: ImageView, translationX: Float) {
        stopGlow(aura)
        stopGlow(character)
        character.animate().cancel()
        aura.animate().cancel()

        aura.visibility = View.VISIBLE
        aura.alpha = 0f
        aura.translationX = translationX
        aura.animate()
            .alpha(1f)
            .setDuration(600L)
            .withEndAction { startGlow(aura) }
            .start()

        character.visibility = View.VISIBLE
        character.alpha = 0f
        character.translationX = translationX
        character.animate()
            .alpha(1f)
            .setDuration(600L)
            .start()
    }

    private fun showSubtitle(text: String) {
        subtitleText.visibility = View.VISIBLE
        subtitleText.animate().cancel()
        subtitleText.alpha = 0f
        subtitleText.text = text
        subtitleText.animate().alpha(1f).setDuration(400L).start()
    }

    private fun playAudio(resId: Int, onComplete: () -> Unit) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            setOnCompletionListener {
                onComplete()
            }
            start()
        }
    }

    private fun startGlow(view: View) {
        stopGlow(view)
        val animator = ValueAnimator.ofFloat(1f, 1.1f, 1f).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { valueAnimator ->
                val scale = valueAnimator.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
        }
        animator.start()
        glowAnimators[view] = animator
    }

    private fun stopGlow(view: View) {
        glowAnimators.remove(view)?.cancel()
        view.scaleX = 1f
        view.scaleY = 1f
    }
}
