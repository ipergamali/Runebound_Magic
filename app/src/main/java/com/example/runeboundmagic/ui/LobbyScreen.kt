package com.example.runeboundmagic.ui

import android.os.SystemClock
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
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
import com.example.runeboundmagic.LobbyInteractionEvent
import com.example.runeboundmagic.R
import com.example.runeboundmagic.data.local.HeroChoiceDatabase
import com.example.runeboundmagic.toHeroOption
import com.example.runeboundmagic.toHeroType
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

    val heroes = remember {
        listOf(
            HeroOption.MAGE,
            HeroOption.MYSTICAL_PRIESTESS,
            HeroOption.RANGER,
            HeroOption.WARRIOR
        )
    }
    var selectedHero by rememberSaveable { mutableStateOf(heroes.first()) }
    var playerName by rememberSaveable { mutableStateOf("") }
    var lastSavedSignature by remember { mutableStateOf<Pair<String, String>?>(null) }

    val lastChoice by viewModel.getLastChoice().collectAsState(initial = null)

    LaunchedEffect(lastChoice) {
        lastChoice?.let { choice ->
            selectedHero = choice.heroType.toHeroOption()
            if (playerName.isBlank()) {
                playerName = choice.playerName
            }
            lastSavedSignature = choice.playerName.trim() to choice.heroType.name
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val selectedHeroLabel = stringResource(id = selectedHero.displayNameRes)
        val selectedHeroDescription = stringResource(id = selectedHero.descriptionRes)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val density = LocalDensity.current
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()

            val selectHeroSize = 220.dp to 60.dp
            val backSize = 120.dp to 50.dp
            val startBattleSize = 140.dp to 50.dp
            val codexSize = 220.dp to 48.dp

            val selectHeroWidthPx = with(density) { selectHeroSize.first.toPx() }
            val selectHeroHeightPx = with(density) { selectHeroSize.second.toPx() }
            val backWidthPx = with(density) { backSize.first.toPx() }
            val backHeightPx = with(density) { backSize.second.toPx() }
            val startBattleWidthPx = with(density) { startBattleSize.first.toPx() }
            val startBattleHeightPx = with(density) { startBattleSize.second.toPx() }
            val codexWidthPx = with(density) { codexSize.first.toPx() }
            val codexHeightPx = with(density) { codexSize.second.toPx() }

            val selectHeroOffsetX = with(density) {
                (screenWidthPx * 0.5f - selectHeroWidthPx / 2f).toDp()
            }
            val selectHeroOffsetY = with(density) {
                (screenHeightPx * 0.87f - selectHeroHeightPx / 2f).toDp()
            }
            val backOffsetX = with(density) {
                (screenWidthPx * 0.18f - backWidthPx / 2f).toDp()
            }
            val backOffsetY = with(density) {
                (screenHeightPx * 0.94f - backHeightPx / 2f).toDp()
            }
            val startBattleOffsetX = with(density) {
                (screenWidthPx * 0.82f - startBattleWidthPx / 2f).toDp()
            }
            val startBattleOffsetY = with(density) {
                (screenHeightPx * 0.94f - startBattleHeightPx / 2f).toDp()
            }
            val codexOffsetX = with(density) {
                (screenWidthPx * 0.5f - codexWidthPx / 2f).toDp()
            }
            val codexOffsetY = with(density) {
                (screenHeightPx * 0.98f - codexHeightPx / 2f).toDp()
            }

            val extraBottomSpacePx = with(density) { 16.dp.toPx() }
            val selectHeroTopPx = screenHeightPx * 0.87f - selectHeroHeightPx / 2f
            val bottomPadding = with(density) {
                (screenHeightPx - selectHeroTopPx + extraBottomSpacePx)
                    .coerceAtLeast(0f)
                    .toDp()
            }

            val selectHeroInteraction = remember { MutableInteractionSource() }
            val backInteraction = remember { MutableInteractionSource() }
            val startBattleInteraction = remember { MutableInteractionSource() }
            val codexInteraction = remember { MutableInteractionSource() }

            Image(
                painter = rememberAssetPainter("lobby/Game_Lobby.png"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x880B111A))
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                LobbyHeader(
                    heroName = selectedHeroLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = bottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeroCarousel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    heroes = heroes,
                    selectedHero = selectedHero,
                    isA4Playing = isA4Playing,
                    onHeroSelected = { hero ->
                        selectedHero = hero
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.HERO_SELECTED,
                            heroType = hero.toHeroType(),
                            heroDisplayName = context.getString(hero.displayNameRes),
                            heroNameInput = playerName
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = selectedHeroLabel,
                    color = Color(0xFFF0C977),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = selectedHeroDescription,
                    color = Color(0xFFE8F5FF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { newValue ->
                        playerName = newValue
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.HERO_NAME_CHANGED,
                            heroType = selectedHero.toHeroType(),
                            heroDisplayName = context.getString(selectedHero.displayNameRes),
                            heroNameInput = newValue
                        )
                    },
                    placeholder = { Text(text = stringResource(id = R.string.hero_name_hint)) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color(0xFFE0D299),
                        focusedIndicatorColor = Color(0xFFF0C977),
                        unfocusedContainerColor = Color(0x330B111A),
                        focusedContainerColor = Color(0x330B111A),
                        cursorColor = Color(0xFFF0C977),
                        focusedLabelColor = Color(0xFFF0C977),
                        unfocusedLabelColor = Color(0xFFE8F5FF),
                    )
                )
            }

            val selectionSignature = remember(selectedHero, playerName) {
                playerName.trim() to selectedHero.name
            }

            fun persistSelection(trimmedName: String, heroLabel: String) {
                viewModel.saveHeroChoice(
                    playerName = trimmedName,
                    heroType = selectedHero.toHeroType(),
                    heroName = heroLabel
                )
                lastSavedSignature = trimmedName to selectedHero.name
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.lobby_selection_saved,
                            trimmedName,
                            heroLabel
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = selectHeroOffsetX, y = selectHeroOffsetY)
                    .size(selectHeroSize.first, selectHeroSize.second)
                    .clickable(
                        indication = null,
                        interactionSource = selectHeroInteraction
                    ) {
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.SELECT_HERO_CLICKED,
                            heroType = selectedHero.toHeroType(),
                            heroDisplayName = selectedHeroLabel,
                            heroNameInput = playerName
                        )

                        val trimmedName = playerName.trim()
                        if (trimmedName.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.error_empty_hero_name)
                                )
                            }
                            return@clickable
                        }

                        if (lastSavedSignature != selectionSignature) {
                            persistSelection(trimmedName, selectedHeroLabel)
                        }

                        onSelectHero()
                    }
            )

            Box(
                modifier = Modifier
                    .offset(x = backOffsetX, y = backOffsetY)
                    .size(backSize.first, backSize.second)
                    .clickable(
                        indication = null,
                        interactionSource = backInteraction
                    ) {
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.BACK_CLICKED,
                            heroType = selectedHero.toHeroType(),
                            heroDisplayName = selectedHeroLabel,
                            heroNameInput = playerName
                        )
                        onBack()
                    }
            )

            Box(
                modifier = Modifier
                    .offset(x = startBattleOffsetX, y = startBattleOffsetY)
                    .size(startBattleSize.first, startBattleSize.second)
                    .clickable(
                        indication = null,
                        interactionSource = startBattleInteraction
                    ) {
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.START_BATTLE_CLICKED,
                            heroType = selectedHero.toHeroType(),
                            heroDisplayName = selectedHeroLabel,
                            heroNameInput = playerName
                        )

                        val trimmedName = playerName.trim()
                        if (trimmedName.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.error_empty_hero_name)
                                )
                            }
                            return@clickable
                        }

                        if (lastSavedSignature != selectionSignature) {
                            persistSelection(trimmedName, selectedHeroLabel)
                        }

                        onStartBattle(selectedHero, trimmedName)
                    }
            )

            Box(
                modifier = Modifier
                    .offset(x = codexOffsetX, y = codexOffsetY)
                    .size(codexSize.first, codexSize.second)
                    .clickable(
                        indication = null,
                        interactionSource = codexInteraction
                    ) {
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.CODEX_CLICKED,
                            heroType = selectedHero.toHeroType(),
                            heroDisplayName = selectedHeroLabel,
                            heroNameInput = playerName
                        )
                        onOpenCodex()
                    }
            )
        }
    }
}

@Composable
private fun LobbyHeader(
    heroName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(id = R.string.lobby_title),
            color = Color(0xFFF0C977),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        Text(
            text = stringResource(id = R.string.lobby_subtitle),
            color = Color(0xFFE8F5FF),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.lobby_selected_hero, heroName),
            color = Color(0xFFFFE7A7),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
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
