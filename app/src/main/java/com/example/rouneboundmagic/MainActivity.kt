package com.example.rouneboundmagic

import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.annotation.Keep
import com.google.androidgamesdk.GameActivity

class MainActivity : GameActivity() {
    private lateinit var selectionOverlay: VideoView
    private lateinit var overlayContainer: FrameLayout
    private var overlayVideoPrepared = false
    private val overlaySizeFallbackPx = 120
    private val circleVideoUri: Uri? by lazy { resolveCircleVideoUri() }

    companion object {
        private const val TAG = "MainActivity"

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

        selectionOverlay = VideoView(this).apply {
            visibility = View.GONE
            setBackgroundColor(Color.TRANSPARENT)
            setZOrderOnTop(true)
            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
                overlayVideoPrepared = true
            }
            setOnCompletionListener {
                visibility = View.GONE
            }
            setOnErrorListener { _: MediaPlayer?, _, _ ->
                visibility = View.GONE
                true
            }
            setOnTouchListener { _, _ -> false }
            circleVideoUri?.let(this::setVideoURI) ?: run {
                overlayVideoPrepared = false
                Log.w(TAG, "Missing circle selection overlay resource. Overlay animation disabled.")
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
            circleVideoUri ?: run {
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
            if (overlayVideoPrepared) {
                selectionOverlay.pause()
                selectionOverlay.seekTo(0)
            }
            selectionOverlay.start()
        }
    }

    private fun resolveCircleVideoUri(): Uri? {
        val resourceId = resources.getIdentifier("circle", "raw", packageName)
        return if (resourceId != 0) {
            Uri.parse("android.resource://$packageName/$resourceId")
        } else {
            Log.w(TAG, "Circle overlay video (res/raw/circle.*) not found. Selection overlay will be hidden.")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        selectionOverlay.stopPlayback()
    }
}