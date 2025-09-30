package com.example.runeboundmagic.ui

import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
            IntroSceneDefinition(
                kind = IntroSceneKind.RUNE_PROLOGUE,
                audioRes = R.raw.a1,
                subtitle = "The world was once bound by the elemental runes — Fire, Water, Air, Earth — that kept the balance of magic alive."
            ),
            IntroSceneDefinition(
                kind = IntroSceneKind.BLACK_MAGE,
                audioRes = R.raw.a2,
                subtitle = "But balance is a chain meant to be broken… and I, the Black Wizard, will forge a new world from the ashes!"
            ),
            IntroSceneDefinition(
                kind = IntroSceneKind.MYSTICAL_PRIESTESS,
                audioRes = R.raw.a3,
                subtitle = "Yet hope remains. A lone guardian rises, chosen by the runes themselves, to stand against the growing darkness."
            ),
            IntroSceneDefinition(
                kind = IntroSceneKind.FINAL_CONFRONTATION,
                audioRes = R.raw.a4,
                subtitle = "The battle of runes begins. Match their power, wield their magic, and decide the fate of the realm."
            )
        )
    }
    var currentSceneIndex by rememberSaveable { mutableIntStateOf(0) }
    var highlightedWords by remember { mutableStateOf(emptySet<String>()) }
    var runeVisibility by remember { mutableStateOf(RuneVisibility()) }
    var showStartGameButton by rememberSaveable { mutableStateOf(false) }
    var showRuneLogo by rememberSaveable { mutableStateOf(false) }

    val signatureFont = FontFamily.Cursive

    val templePainter = rememberAssetPainter("intro/MysticalTempleRuins")
    val magePainter = rememberAssetPainter("characters/black_mage.png")
    val priestessPainter = rememberAssetPainter("characters/mystical_priestess.png")
    val fireRunePainter = rememberAssetPainter("puzzle/red_gem.png")
    val waterRunePainter = rememberAssetPainter("puzzle/blue_gem.png")
    val airRunePainter = rememberAssetPainter("puzzle/turquoise.png")
    val earthRunePainter = rememberAssetPainter("puzzle/green_gem.png")
    val logoPainter = painterResource(id = R.drawable.logo)

    val currentScene = scenes[currentSceneIndex]
    val isFinalScene = currentScene.kind == IntroSceneKind.FINAL_CONFRONTATION

    DisposableEffect(currentSceneIndex) {
        val player = MediaPlayer.create(context, currentScene.audioRes)
        if (player == null) {
            showStartGameButton = true
            return@DisposableEffect onDispose { }
        }
        var released = false
        fun releasePlayer() {
            if (released) return
            released = true
            player.setOnCompletionListener(null)
            runCatching { player.release() }
        }
        val listener = MediaPlayer.OnCompletionListener {
            if (currentSceneIndex < scenes.lastIndex) {
                currentSceneIndex += 1
            } else {
                showStartGameButton = true
            }
            releasePlayer()
        }
        player.setOnCompletionListener(listener)
        player.start()
        onDispose {
            releasePlayer()
        }
    }

    LaunchedEffect(currentSceneIndex) {
        highlightedWords = emptySet()
        runeVisibility = RuneVisibility()
        showStartGameButton = false
        when (currentScene.kind) {
            IntroSceneKind.RUNE_PROLOGUE -> {
                showRuneLogo = false
                var visibility = RuneVisibility()
                var words = emptySet<String>()
                delay(3_000)
                visibility = visibility.copy(fire = true)
                runeVisibility = visibility
                words = words + "Fire"
                highlightedWords = words
                delay(1_000)
                visibility = visibility.copy(water = true)
                runeVisibility = visibility
                words = words + "Water"
                highlightedWords = words
                delay(1_000)
                visibility = visibility.copy(air = true)
                runeVisibility = visibility
                words = words + "Air"
                highlightedWords = words
                delay(1_000)
                visibility = visibility.copy(earth = true)
                runeVisibility = visibility
                words = words + "Earth"
                highlightedWords = words
                delay(600)
                showRuneLogo = true
            }
            else -> showRuneLogo = true
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
                    fireRunePainter = fireRunePainter,
                    waterRunePainter = waterRunePainter,
                    airRunePainter = airRunePainter,
                    earthRunePainter = earthRunePainter,
                    logoPainter = logoPainter,
                    runeVisibility = runeVisibility,
                    showLogo = showRuneLogo
                )
                1 -> BlackMageScene(
                    magePainter = magePainter
                )
                2 -> MysticalPriestessScene(
                    priestessPainter = priestessPainter
                )
                else -> FinalClashScene(
                    magePainter = magePainter,
                    priestessPainter = priestessPainter,
                    fireRunePainter = fireRunePainter,
                    waterRunePainter = waterRunePainter,
                    airRunePainter = airRunePainter,
                    earthRunePainter = earthRunePainter
                )
            }
        }

        val subtitleModifier = if (isFinalScene) {
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 380.dp)
        } else {
            Modifier
                .align(Alignment.TopCenter)
                .padding(start = 24.dp, end = 24.dp, top = 48.dp)
        }
        IntroSubtitleOverlay(
            kind = currentScene.kind,
            subtitle = currentScene.subtitle,
            fontFamily = signatureFont,
            highlightedWords = highlightedWords,
            modifier = subtitleModifier
        )

        AnimatedVisibility(
            visible = showStartGameButton,
            enter = fadeIn(animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.88f, animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 220)),
            modifier = if (isFinalScene) {
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 200.dp)
            } else {
                Modifier.align(Alignment.Center)
            }
        ) {
            Button(
                onClick = onIntroFinished,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2CFF8F),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.intro_start_game),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = signatureFont,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    )
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
    @RawRes val audioRes: Int,
    val subtitle: String
)

private data class RuneVisibility(
    val fire: Boolean = false,
    val water: Boolean = false,
    val air: Boolean = false,
    val earth: Boolean = false
)

@Composable
private fun IntroSubtitleOverlay(
    kind: IntroSceneKind,
    subtitle: String,
    fontFamily: FontFamily,
    highlightedWords: Set<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = kind,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            label = "subtitle"
        ) { targetKind ->
            when (targetKind) {
                IntroSceneKind.RUNE_PROLOGUE -> RuneNarrationText(
                    fontFamily = fontFamily,
                    highlightedWords = highlightedWords,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> SceneSubtitleText(
                    text = subtitle,
                    fontFamily = fontFamily,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SceneSubtitleText(
    text: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xB0050A12))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        style = androidx.compose.ui.text.TextStyle(
            fontFamily = fontFamily,
            color = Color(0xFFEAF7FF),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            shadow = Shadow(color = Color(0x99000000), blurRadius = 18f)
        )
    )
}

@Composable
private fun RunePrologueScene(
    backgroundPainter: Painter,
    fireRunePainter: Painter,
    waterRunePainter: Painter,
    airRunePainter: Painter,
    earthRunePainter: Painter,
    logoPainter: Painter,
    runeVisibility: RuneVisibility,
    showLogo: Boolean,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            RuneCircle(
                logoPainter = logoPainter,
                fireRunePainter = fireRunePainter,
                waterRunePainter = waterRunePainter,
                airRunePainter = airRunePainter,
                earthRunePainter = earthRunePainter,
                runeVisibility = runeVisibility,
                showLogo = showLogo
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
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xB0050A12))
            .padding(horizontal = 24.dp, vertical = 18.dp),
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
private fun RuneCircle(
    logoPainter: Painter,
    fireRunePainter: Painter,
    waterRunePainter: Painter,
    airRunePainter: Painter,
    earthRunePainter: Painter,
    runeVisibility: RuneVisibility,
    showLogo: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x442CFF8F),
                            Color(0x22040015),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x332CFF8F),
                            Color.Transparent
                        )
                    )
                )
        )
        AnimatedVisibility(
            visible = showLogo,
            enter = fadeIn(animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            Image(
                painter = logoPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(136.dp)
                    .graphicsLayer { this.alpha = 0.95f }
            )
        }
        RuneOrb(
            painter = fireRunePainter,
            glowColor = Color(0xFFFF7043),
            alignment = Alignment.TopCenter,
            visible = runeVisibility.fire
        )
        RuneOrb(
            painter = waterRunePainter,
            glowColor = Color(0xFF4FC3F7),
            alignment = Alignment.BottomCenter,
            visible = runeVisibility.water
        )
        RuneOrb(
            painter = airRunePainter,
            glowColor = Color(0xFFA5F0FF),
            alignment = Alignment.CenterStart,
            visible = runeVisibility.air
        )
        RuneOrb(
            painter = earthRunePainter,
            glowColor = Color(0xFF8BC34A),
            alignment = Alignment.CenterEnd,
            visible = runeVisibility.earth
        )
    }
}

@Composable
private fun BoxScope.RuneOrb(
    painter: Painter,
    glowColor: Color,
    alignment: Alignment,
    visible: Boolean,
    size: Float = 78f
) {
    val targetAlpha = if (visible) 1f else 0f
    val targetScale = if (visible) 1f else 0.8f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "runeOrbAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "runeOrbScale"
    )

    Box(modifier = Modifier.align(alignment)) {
        Box(
            modifier = Modifier
                .size(size.dp * 1.6f)
                .graphicsLayer { this.alpha = alpha * 0.75f }
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

@Composable
private fun BlackMageScene(
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
    }
}

@Composable
private fun MysticalPriestessScene(
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
    }
}

@Composable
private fun FinalClashScene(
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
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CharacterPortraitCard(
                painter = priestessPainter,
                contentDescription = null,
                glowColor = Color(0xFF7C8DFF),
                backgroundTint = Color(0xFF0B0F1F),
                scale = cardScale,
                alpha = contentAlpha,
                modifier = Modifier
                    .height(320.dp)
            )
            CharacterPortraitCard(
                painter = magePainter,
                contentDescription = null,
                glowColor = Color(0xFF2CFF8F),
                backgroundTint = Color(0xFF04110A),
                scale = cardScale,
                alpha = contentAlpha,
                modifier = Modifier
                    .height(320.dp)
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
            contentScale = ContentScale.Fit,
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

