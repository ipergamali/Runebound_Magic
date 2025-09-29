package com.example.rouneboundmagic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.Keep
import com.google.androidgamesdk.GameActivity
import java.io.IOException

class MainActivity : GameActivity() {
    private lateinit var selectionOverlay: ImageView
    private lateinit var overlayContainer: FrameLayout
    private val overlaySizeFallbackPx = 120
    private val overlayScaleFactor = 1.15f
    private var overlayBitmap: Bitmap? = null

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_SELECTED_HERO = "selected_hero"

        init {
                System.loadLibrary("rouneboundmagic")
        }
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setupSelectionOverlay()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    private fun setupSelectionOverlay() {
        overlayContainer = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
        }

        selectionOverlay = ImageView(this).apply {
            visibility = View.GONE
            alpha = 0f
            scaleX = 1f
            scaleY = 1f
            setBackgroundColor(Color.TRANSPARENT)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        overlayBitmap = loadOverlayBitmap().also { bitmap ->
            if (bitmap != null) {
                selectionOverlay.setImageBitmap(bitmap)
            } else {
                Log.w(
                    TAG,
                    "Circle overlay image (assets/puzzle/circle.png) not found. Selection overlay will be hidden."
                )
            }
        }

        overlayContainer.addView(selectionOverlay)
        addContentView(
            overlayContainer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun hideSystemUi() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    @Keep
    fun onRuneSelected(centerX: Float, centerY: Float, sizePx: Float) {
        runOnUiThread {
            overlayBitmap ?: run {
                selectionOverlay.visibility = View.GONE
                return@runOnUiThread
            }

            val targetSize = if (sizePx > 0f) sizePx.toInt() else overlaySizeFallbackPx
            val halfSize = targetSize / 2
            val params = FrameLayout.LayoutParams(targetSize, targetSize)
            params.leftMargin = (centerX - halfSize).toInt()
            params.topMargin = (centerY - halfSize).toInt()
            selectionOverlay.layoutParams = params

            selectionOverlay.visibility = View.VISIBLE
            selectionOverlay.alpha = 0f
            selectionOverlay.scaleX = 0.7f
            selectionOverlay.scaleY = 0.7f
            selectionOverlay.animate().cancel()
            selectionOverlay.animate()
                .alpha(1f)
                .scaleX(overlayScaleFactor)
                .scaleY(overlayScaleFactor)
                .setDuration(180L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    selectionOverlay.animate()
                        .alpha(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(200L)
                        .setDuration(220L)
                        .withEndAction { selectionOverlay.visibility = View.GONE }
                        .start()
                }
                .start()
        }
    }

    private fun loadOverlayBitmap(): Bitmap? {
        return try {
            assets.open("puzzle/circle.png").use(BitmapFactory::decodeStream)
        } catch (io: IOException) {
            Log.w(TAG, "Failed to load circle overlay image from assets.", io)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        selectionOverlay.animate().cancel()
        overlayBitmap?.recycle()
        overlayBitmap = null
    }
}