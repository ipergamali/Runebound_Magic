package com.example.runeboundmagic.ui

import android.graphics.Typeface
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.example.runeboundmagic.R
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    modifier: Modifier = Modifier,
    onIntroFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val scenes = remember {
        listOf(
            IntroSceneDefinition(IntroSceneKind.RUNE_PROLOGUE, R.raw.a1),
            IntroSceneDefinition(IntroSceneKind.BLACK_MAGE, R.raw.a2),
            IntroSceneDefinition(IntroSceneKind.MYSTICAL_PRIESTESS, R.raw.a3),
            IntroSceneDefinition(IntroSceneKind.FINAL_CONFRONTATION, R.raw.a4)
        )
    }
    var currentSceneIndex by rememberSaveable { mutableIntStateOf(0) }
    var highlightedWords by remember { mutableStateOf(emptySet<String>()) }

    val signatureFont = rememberSignatureFont()

    val templePainter = rememberAssetPainter("intro/MysticalTempleRuins")
    val magePainter = rememberAssetPainter("characters/black_mage.png")
    val priestessPainter = rememberAssetPainter("characters/mystical_priestess.png")
    val fireRunePainter = rememberAssetPainter("puzzle/red_gem.png")
    val waterRunePainter = rememberAssetPainter("puzzle/blue_gem.png")
    val airRunePainter = rememberAssetPainter("puzzle/turquoise.png")
    val earthRunePainter = rememberAssetPainter("puzzle/green_gem.png")

    val currentScene = scenes[currentSceneIndex]

    DisposableEffect(currentSceneIndex) {
        val player = MediaPlayer.create(context, currentScene.audioRes)
        if (player == null) {
            onIntroFinished()
            return@DisposableEffect onDispose { }
        }
        val listener = MediaPlayer.OnCompletionListener {
            if (currentSceneIndex < scenes.lastIndex) {
                currentSceneIndex += 1
            } else {
                onIntroFinished()
            }
        }
        player.setOnCompletionListener(listener)
        player.start()
        onDispose {
            player.setOnCompletionListener(null)
            runCatching { player.stop() }
            player.release()
        }
    }

    LaunchedEffect(currentSceneIndex) {
        highlightedWords = emptySet()
        when (currentScene.kind) {
            IntroSceneKind.RUNE_PROLOGUE -> {
                delay(3_000)
                highlightedWords = setOf("Fire")
                delay(1_000)
                highlightedWords = highlightedWords + "Water"
                delay(1_000)
                highlightedWords = highlightedWords + "Air"
                delay(1_000)
                highlightedWords = highlightedWords + "Earth"
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Crossfade(
            targetState = currentSceneIndex,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            label = "introScene"
        ) { sceneIndex ->
            when (sceneIndex) {
                0 -> RunePrologueScene(
                    backgroundPainter = templePainter,
                    fontFamily = signatureFont,
                    highlightedWords = highlightedWords
                )
                1 -> BlackMageScene(
                    fontFamily = signatureFont,
                    magePainter = magePainter
                )
                2 -> MysticalPriestessScene(
                    fontFamily = signatureFont,
                    priestessPainter = priestessPainter
                )
                else -> FinalClashScene(
                    fontFamily = signatureFont,
                    magePainter = magePainter,
                    priestessPainter = priestessPainter,
                    fireRunePainter = fireRunePainter,
                    waterRunePainter = waterRunePainter,
                    airRunePainter = airRunePainter,
                    earthRunePainter = earthRunePainter
                )
            }
        }
    }
}

private enum class IntroSceneKind {
    RUNE_PROLOGUE,
    BLACK_MAGE,
    MYSTICAL_PRIESTESS,
    FINAL_CONFRONTATION
}

private data class IntroSceneDefinition(
    val kind: IntroSceneKind,
    @RawRes val audioRes: Int
)

@Composable
private fun RunePrologueScene(
    backgroundPainter: Painter,
    fontFamily: FontFamily,
    highlightedWords: Set<String>,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = backgroundPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA040015))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RuneNarrationText(
                fontFamily = fontFamily,
                highlightedWords = highlightedWords,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RuneNarrationText(
    fontFamily: FontFamily,
    highlightedWords: Set<String>,
    modifier: Modifier = Modifier
) {
    val fireGlow by animateFloatAsState(
        targetValue = if (highlightedWords.contains("Fire")) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "fireGlow"
    )
    val waterGlow by animateFloatAsState(
        targetValue = if (highlightedWords.contains("Water")) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "waterGlow"
    )
    val airGlow by animateFloatAsState(
        targetValue = if (highlightedWords.contains("Air")) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "airGlow"
    )
    val earthGlow by animateFloatAsState(
        targetValue = if (highlightedWords.contains("Earth")) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "earthGlow"
    )

    val baseStyle = androidx.compose.ui.text.TextStyle(
        fontFamily = fontFamily,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = Color(0xFFE5F3FF),
        shadow = Shadow(color = Color(0x33000000), blurRadius = 12f)
    )

    val fireStyle = runeSpanStyle(Color(0xFFFF7043), fireGlow)
    val waterStyle = runeSpanStyle(Color(0xFF4FC3F7), waterGlow)
    val airStyle = runeSpanStyle(Color(0xFFA5F0FF), airGlow)
    val earthStyle = runeSpanStyle(Color(0xFF8BC34A), earthGlow)

    val text = buildAnnotatedString {
        append("The world was once bound by the elemental runes — ")
        withStyle(fireStyle) { append("Fire") }
        append(", ")
        withStyle(waterStyle) { append("Water") }
        append(", ")
        withStyle(airStyle) { append("Air") }
        append(", ")
        withStyle(earthStyle) { append("Earth") }
        append(" — that kept the balance of magic alive.")
    }

    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = baseStyle
    )
}

private fun runeSpanStyle(color: Color, glow: Float): SpanStyle {
    val intensity = glow.coerceIn(0f, 1f)
    val glowAlpha = 0.25f + 0.6f * intensity
    return SpanStyle(
        color = color.copy(alpha = 0.45f + 0.55f * intensity),
        shadow = Shadow(
            color = color.copy(alpha = glowAlpha),
            blurRadius = 30f * (0.3f + intensity * 0.7f),
            offset = Offset.Zero
        )
    )
}

@Composable
private fun BlackMageScene(
    fontFamily: FontFamily,
    magePainter: Painter,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "mageScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "mageAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x992CFF8F),
                            Color(0x66002112),
                            Color(0xFF010206)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xAA01040A))
        )
        CharacterPortraitCard(
            painter = magePainter,
            contentDescription = null,
            glowColor = Color(0xFF2CFF8F),
            backgroundTint = Color(0xFF04110A),
            scale = scale,
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.Center)
                .height(360.dp)
                .padding(horizontal = 24.dp)
        )
        Text(
            text = "But balance is a chain meant to be broken… and I, the Black Wizard, will forge a new world from the ashes!",
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = fontFamily,
                color = Color(0xFFEFFEF5),
                fontSize = 26.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                shadow = Shadow(color = Color(0x882CFF8F), blurRadius = 18f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .graphicsLayer { this.alpha = alpha }
        )
    }
}

@Composable
private fun MysticalPriestessScene(
    fontFamily: FontFamily,
    priestessPainter: Painter,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "priestessScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "priestessAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xAA6D77FF),
                            Color(0x66182448),
                            Color(0xFF050616)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88040A17))
        )
        CharacterPortraitCard(
            painter = priestessPainter,
            contentDescription = null,
            glowColor = Color(0xFF6D77FF),
            backgroundTint = Color(0xFF0B0F1F),
            scale = scale,
            alpha = alpha,
            modifier = Modifier
                .align(Alignment.Center)
                .height(360.dp)
                .padding(horizontal = 24.dp)
        )
        Text(
            text = "Yet hope remains. A lone guardian rises, chosen by the runes themselves, to stand against the growing darkness.",
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = fontFamily,
                color = Color(0xFFE7EBFF),
                fontSize = 26.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                shadow = Shadow(color = Color(0x886D77FF), blurRadius = 18f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .graphicsLayer { this.alpha = alpha }
        )
    }
}

@Composable
private fun FinalClashScene(
    fontFamily: FontFamily,
    magePainter: Painter,
    priestessPainter: Painter,
    fireRunePainter: Painter,
    waterRunePainter: Painter,
    airRunePainter: Painter,
    earthRunePainter: Painter,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "finalScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "finalAlpha"
    )

    val pulse = rememberInfiniteTransition(label = "runePulse")
    val runeScale by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "runeScale"
    )
    val runeAlpha by pulse.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "runeAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF13173C),
                            Color(0xFF050714),
                            Color(0xFF0C1F1A)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CharacterPortraitCard(
                painter = priestessPainter,
                contentDescription = null,
                glowColor = Color(0xFF7C8DFF),
                backgroundTint = Color(0xFF0B0F1F),
                scale = cardScale,
                alpha = contentAlpha,
                modifier = Modifier
                    .fillMaxHeight(0.72f)
                    .padding(end = 12.dp)
            )
            CharacterPortraitCard(
                painter = magePainter,
                contentDescription = null,
                glowColor = Color(0xFF2CFF8F),
                backgroundTint = Color(0xFF04110A),
                scale = cardScale,
                alpha = contentAlpha,
                modifier = Modifier
                    .fillMaxHeight(0.72f)
                    .padding(start = 12.dp)
            )
        }
        RuneCluster(
            fireRunePainter = fireRunePainter,
            waterRunePainter = waterRunePainter,
            airRunePainter = airRunePainter,
            earthRunePainter = earthRunePainter,
            scale = runeScale,
            alpha = runeAlpha,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            text = "The battle of runes begins. Match their power, wield their magic, and decide the fate of the realm.",
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = fontFamily,
                color = Color(0xFFE4F9FF),
                fontSize = 24.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                shadow = Shadow(color = Color(0x882CFF8F), blurRadius = 18f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 32.dp, end = 32.dp, bottom = 28.dp)
                .graphicsLayer { this.alpha = contentAlpha }
        )
    }
}

@Composable
private fun RuneCluster(
    fireRunePainter: Painter,
    waterRunePainter: Painter,
    airRunePainter: Painter,
    earthRunePainter: Painter,
    scale: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(220.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0x332CFF8F))
        )
        Image(
            painter = fireRunePainter,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(72.dp)
        )
        Image(
            painter = waterRunePainter,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(72.dp)
        )
        Image(
            painter = airRunePainter,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(72.dp)
        )
        Image(
            painter = earthRunePainter,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(72.dp)
        )
    }
}

@Composable
private fun CharacterPortraitCard(
    painter: Painter,
    contentDescription: String?,
    glowColor: Color,
    backgroundTint: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    alpha: Float = 1f
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .aspectRatio(0.68f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(glowColor.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundTint.copy(alpha = 0.95f),
                            backgroundTint.copy(alpha = 0.75f)
                        )
                    )
                )
        )
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 2.dp,
                    color = glowColor.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(28.dp)
                )
        )
    }
}

@Composable
private fun rememberSignatureFont(): FontFamily {
    val context = LocalContext.current
    return remember {
        runCatching {
            FontFamily(Typeface.createFromAsset(context.assets, "fonts/WhisperingSignature.ttf"))
        }.getOrElse { FontFamily.Default }
    }
}
