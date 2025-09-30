package com.example.runeboundmagic.ui

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runeboundmagic.HeroChoiceViewModel
import com.example.runeboundmagic.HeroChoiceViewModelFactory
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.R
import com.example.runeboundmagic.data.local.HeroChoiceDatabase
import com.example.runeboundmagic.data.local.HeroChoiceEntity
import com.example.runeboundmagic.toHeroOption
import com.example.runeboundmagic.toHeroType
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun LobbyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSelectHero: () -> Unit,
    onStartBattle: (HeroOption, String) -> Unit,
    onOpenCodex: () -> Unit,
    onLobbyShown: () -> Unit = {},
    isA4Playing: Boolean = false,
    viewModel: HeroChoiceViewModel = lobbyViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { onLobbyShown() }

    val heroes = remember { HeroOption.values().toList() }
    var selectedHero by rememberSaveable { mutableStateOf(heroes.first()) }
    var playerName by rememberSaveable { mutableStateOf("") }

    val lastChoice by viewModel.getLastChoice().collectAsState(initial = null)

    LaunchedEffect(lastChoice) {
        lastChoice?.let { choice ->
            selectedHero = choice.heroType.toHeroOption()
            if (playerName.isBlank()) {
                playerName = choice.playerName
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF45533C),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF45533C))
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = stringResource(id = R.string.lobby_back),
                        color = Color(0xFFE8F5FF),
                        fontSize = 13.sp
                    )
                }
                TextButton(onClick = onOpenCodex) {
                    Text(
                        text = stringResource(id = R.string.codex_open_button),
                        color = Color(0xFF38B6FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            LobbyHeader(
                lastChoice = lastChoice,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text(text = stringResource(id = R.string.hero_name_hint)) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color(0xFF00FF88),
                    focusedIndicatorColor = Color(0xFF38B6FF),
                    unfocusedContainerColor = Color(0x33000814),
                    focusedContainerColor = Color(0x33000814),
                    cursorColor = Color(0xFF38B6FF),
                    focusedLabelColor = Color(0xFF38B6FF),
                    unfocusedLabelColor = Color(0xFFB2D9FF),
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HeroCarousel(
                    heroes = heroes,
                    selectedHero = selectedHero,
                    isA4Playing = isA4Playing,
                    onHeroSelected = { hero -> selectedHero = hero }
                )
            }

            val selectedHeroLabel = stringResource(id = selectedHero.displayNameRes)

            Button(
                onClick = onSelectHero,
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38B6FF),
                    contentColor = Color.Black
                )
            ) {
                Text(text = stringResource(id = R.string.lobby_select_hero))
            }

            Button(
                onClick = {
                    val trimmedName = playerName.trim()
                    if (trimmedName.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.error_empty_hero_name)
                            )
                        }
                        return@Button
                    }

                    viewModel.saveHeroChoice(
                        playerName = trimmedName,
                        heroType = selectedHero.toHeroType(),
                        heroName = selectedHeroLabel
                    )

                    onStartBattle(selectedHero, trimmedName)

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.lobby_selection_saved,
                                trimmedName,
                                selectedHeroLabel
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF88),
                    contentColor = Color(0xFF002B1C)
                )
            ) {
                Text(text = stringResource(id = R.string.lobby_start_battle))
            }
        }
    }
}

@Composable
private fun LobbyHeader(
    lastChoice: HeroChoiceEntity?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.lobby_title),
            color = Color(0xFF00FF88),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = stringResource(id = R.string.lobby_subtitle),
            color = Color(0xFFCFD9D4),
            fontSize = 14.sp
        )

        AnimatedVisibility(
            visible = lastChoice != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 220)) +
                scaleOut(targetScale = 0.9f, animationSpec = tween(durationMillis = 220))
        ) {
            lastChoice?.let { choice ->
                ElevatedCard(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0x332CFF8F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val heroOption = choice.heroType.toHeroOption()
                        val heroLabel = stringResource(id = heroOption.displayNameRes)
                        val formattedTime = remember(choice.timestamp) {
                            DateFormat.getDateTimeInstance().format(Date(choice.timestamp))
                        }
                        Text(
                            text = stringResource(
                                id = R.string.lobby_last_choice,
                                choice.playerName,
                                heroLabel
                            ),
                            color = Color(0xFFE8F5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(id = R.string.lobby_last_saved_time, formattedTime),
                            color = Color(0xFF8FA3BF),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCarousel(
    modifier: Modifier = Modifier,
    heroes: List<HeroOption>,
    selectedHero: HeroOption,
    isA4Playing: Boolean,
    onHeroSelected: (HeroOption) -> Unit
) {
    if (heroes.isEmpty()) {
        return
    }

    val listState = rememberLazyListState()
    var lastInteractionTimestamp by remember { mutableLongStateOf(0L) }

    LaunchedEffect(selectedHero, heroes) {
        val index = heroes.indexOf(selectedHero)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(isA4Playing, selectedHero, heroes, lastInteractionTimestamp) {
        if (!isA4Playing || heroes.isEmpty()) {
            return@LaunchedEffect
        }
        while (isActive && isA4Playing) {
            delay(4_000)
            if (!isActive || !isA4Playing) break
            val now = SystemClock.elapsedRealtime()
            if (now - lastInteractionTimestamp < 6_000L) {
                continue
            }
            val currentIndex = heroes.indexOf(selectedHero)
            if (currentIndex == -1) {
                continue
            }
            val nextHero = heroes[(currentIndex + 1) % heroes.size]
            onHeroSelected(nextHero)
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(heroes) { hero ->
            val isSelected = hero == selectedHero
            val targetScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.9f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "heroScale${hero.name}"
            )

            HeroCarouselCard(
                hero = hero,
                scale = targetScale,
                isSelected = isSelected,
                onClick = {
                    lastInteractionTimestamp = SystemClock.elapsedRealtime()
                    onHeroSelected(hero)
                }
            )
        }
    }
}

@Composable
private fun HeroCarouselCard(
    hero: HeroOption,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(196.dp)
            .height(268.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.85f + (scale - 0.9f).coerceIn(0f, 0.15f)
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Color(0xFF2F4F3A) else Color(0x33212B1C)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 10.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Crossfade(
                    targetState = hero.assetPath,
                    animationSpec = tween(durationMillis = 300),
                    label = "heroAsset${hero.name}"
                ) { assetPath ->
                    Image(
                        painter = rememberAssetPainter(assetPath),
                        contentDescription = stringResource(id = hero.displayNameRes),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(
                text = stringResource(id = hero.displayNameRes),
                color = Color(0xFF00FF88),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(id = hero.descriptionRes),
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun lobbyViewModel(): HeroChoiceViewModel {
    val context = LocalContext.current
    val database = remember { HeroChoiceDatabase.getInstance(context) }
    val factory = remember { HeroChoiceViewModelFactory(database.heroChoiceDao()) }
    return viewModel(factory = factory)
}
