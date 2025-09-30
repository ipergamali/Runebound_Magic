package com.example.runeboundmagic.ui

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberAssetPainter(assetPath: String): Painter {
    val context = LocalContext.current
    val painterState = produceState<Painter>(initialValue = ColorPainter(Color.Transparent), context, assetPath) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()?.let { bitmap ->
                        BitmapPainter(bitmap)
                    }
                }
            }.getOrNull() ?: ColorPainter(Color.Transparent)
        }
    }
    return painterState.value
}
