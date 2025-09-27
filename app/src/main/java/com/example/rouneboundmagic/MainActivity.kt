package com.example.rouneboundmagic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.Keep
import com.google.androidgamesdk.GameActivity

class MainActivity : GameActivity() {
    private lateinit var selectionOverlay: ImageView
    private lateinit var overlayContainer: FrameLayout
    private val overlaySizeFallbackPx = 120
    private val overlayFadeDurationMs = 150L
    private val selectionBitmap: Bitmap? by lazy { loadSelectionBitmap() }

    companion object {
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
            elevation = 10f
        }

        selectionOverlay = ImageView(this).apply {
            visibility = View.GONE
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
            alpha = 0f
            scaleType = ImageView.ScaleType.FIT_XY
            selectionBitmap?.let { setImageBitmap(it) }
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
            val targetSize = if (sizePx > 0f) sizePx.toInt() else overlaySizeFallbackPx
            val halfSize = targetSize / 2
            val params = FrameLayout.LayoutParams(targetSize, targetSize)
            params.leftMargin = (centerX - halfSize).toInt()
            params.topMargin = (centerY - halfSize).toInt()
            selectionOverlay.layoutParams = params
            if (selectionOverlay.visibility != View.VISIBLE) {
                selectionOverlay.alpha = 0f
                selectionOverlay.visibility = View.VISIBLE
            }
            selectionOverlay.animate().cancel()
            selectionOverlay.animate()
                .alpha(1f)
                .setDuration(overlayFadeDurationMs)
                .setListener(null)
                .start()
        }
    }

    @Keep
    fun onRuneDeselected() {
        runOnUiThread {
            if (selectionOverlay.visibility != View.VISIBLE) {
                selectionOverlay.visibility = View.GONE
                selectionOverlay.alpha = 0f
                return@runOnUiThread
            }

            selectionOverlay.animate().cancel()
            selectionOverlay.animate()
                .alpha(0f)
                .setDuration(overlayFadeDurationMs)
                .setListener(object : AnimatorListenerAdapter() {
                    private var cancelled = false

                    override fun onAnimationCancel(animation: Animator) {
                        cancelled = true
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (!cancelled) {
                            selectionOverlay.visibility = View.GONE
                        }
                    }
                })
                .start()
        }
    }

    private fun loadSelectionBitmap(): Bitmap? = runCatching {
        assets.open("puzzle/circle.png").use { input ->
            BitmapFactory.decodeStream(input)
        }
    }.getOrNull()
}
