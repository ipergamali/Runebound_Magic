package com.example.runeboundmagic.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.snapshotFlow
import kotlinx.coroutines.launch

@Composable
fun LobbyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onStartBattle: (HeroOption, String) -> Unit,
    onOpenCodex: () -> Unit,
    onLobbyShown: () -> Unit = {},
    viewModel: HeroChoiceViewModel = lobbyViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { onLobbyShown() }

    val heroes = remember { HeroOption.entries.toList() }
    var selectedHeroIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = selectedHeroIndex,
        pageCount = { heroes.size }
    )
    var playerName by rememberSaveable { mutableStateOf("") }
    var lastSavedSignature by remember { mutableStateOf<Pair<String, String>?>(null) }

    val lastChoice by viewModel.getLastChoice().collectAsState(initial = null)

    LaunchedEffect(lastChoice) {
        lastChoice?.let { choice ->
            val hero = choice.heroType.toHeroOption()
            val heroIndex = heroes.indexOf(hero).coerceAtLeast(0)
            if (heroIndex >= 0) {
                selectedHeroIndex = heroIndex
            }
            if (playerName.isBlank()) {
                playerName = choice.playerName
            }
            lastSavedSignature = choice.playerName.trim() to choice.heroType.name
        }
    }

    LaunchedEffect(selectedHeroIndex) {
        if (pagerState.currentPage != selectedHeroIndex || pagerState.targetPage != selectedHeroIndex) {
            pagerState.animateScrollToPage(selectedHeroIndex)
        }
    }

    var lastLoggedPage by remember { mutableIntStateOf(-1) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                if (page in heroes.indices) {
                    if (selectedHeroIndex != page) {
                        selectedHeroIndex = page
                    }
                    if (lastLoggedPage != page) {
                        lastLoggedPage = page
                        val hero = heroes[page]
                        viewModel.logInteraction(
                            event = LobbyInteractionEvent.HERO_SELECTED,
                            heroType = hero.toHeroType(),
                            heroDisplayName = context.getString(hero.displayNameRes),
                            heroNameInput = playerName
                        )
                    }
                }
            }
    }

    val selectedHero = heroes[selectedHeroIndex.coerceIn(heroes.indices)]

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

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(top = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeroInfoHeader(
                    heroName = selectedHeroLabel,
                    heroTrait = selectedHeroDescription,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = bottomPadding)
                    .offset(y = (-72).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeroCarousel(
                    heroes = heroes,
                    pagerState = pagerState,
                    selectedIndex = selectedHeroIndex,
                    onHeroSelected = { index ->
                        if (index in heroes.indices) {
                            selectedHeroIndex = index
                        }
                    }
                )

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
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.hero_name_hint),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.width(240.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFFFD700),
                        unfocusedIndicatorColor = Color(0xB3FFD700),
                        focusedContainerColor = Color(0x330B111A),
                        unfocusedContainerColor = Color(0x330B111A),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f)
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
private fun HeroInfoHeader(
    heroName: String,
    heroTrait: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = heroName,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = heroTrait,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFFC0C0C0)
        )
    }
}

@Composable
private fun HeroSelectionCard(
    hero: HeroOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) Color(0xFFFFD700) else Color(0x66FFFFFF)
    Box(
        modifier = modifier
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAssetPainter(hero.assetPath),
            contentDescription = stringResource(id = hero.displayNameRes),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroCarousel(
    heroes: List<HeroOption>,
    pagerState: PagerState,
    selectedIndex: Int,
    onHeroSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(180.dp),
        beyondBoundsPageCount = 1,
        contentPadding = PaddingValues(horizontal = 64.dp),
        pageSpacing = 20.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
    ) { page ->
        val hero = heroes[page]
        val isSelected = page == selectedIndex
        HeroSelectionCard(
            hero = hero,
            selected = isSelected,
            onClick = { onHeroSelected(page) },
            modifier = Modifier
                .height(220.dp)
                .width(160.dp)
        )
    }
}

@Composable
private fun lobbyViewModel(): HeroChoiceViewModel {
    val context = LocalContext.current
    val database = remember { HeroChoiceDatabase.getInstance(context) }
    val factory = remember { HeroChoiceViewModelFactory(database.heroChoiceDao()) }
    return viewModel(factory = factory)
}
