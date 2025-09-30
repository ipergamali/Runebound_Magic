package com.example.runeboundmagic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * Απλή κατάσταση αναπαραγωγής για να γνωρίζουμε αν παίζει το τελικό clip (a4).
 */
class MediaState {
    var isA4Playing by mutableStateOf(false)
        internal set
}

private val FinaleMediaIds = setOf("a4.mp3", "a4.mp4", "a4")

private fun isFinaleMedia(mediaId: String?): Boolean = mediaId != null && mediaId in FinaleMediaIds

/**
 * Παρακολουθεί το [Player] και ενημερώνει την [MediaState] μόνο όταν παίζει το a4 clip.
 */
fun Player.attachA4Observer(mediaState: MediaState) {
    addListener(object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val isCurrentA4 = isFinaleMedia(mediaItem?.mediaId)
            mediaState.isA4Playing = isCurrentA4 && this@attachA4Observer.isPlaying
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val isCurrentA4 = isFinaleMedia(currentMediaItem?.mediaId)
            mediaState.isA4Playing = isPlaying && isCurrentA4
        }
    })
}
