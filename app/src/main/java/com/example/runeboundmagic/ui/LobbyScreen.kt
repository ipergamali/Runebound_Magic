package com.example.runeboundmagic.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoView
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch

@Composable
fun LobbyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSelectHero: () -> Unit,
    onStartBattle: (HeroOption, String) -> Unit,
    viewModel: HeroChoiceViewModel = lobbyViewModel(),
) {
    val context = LocalContext.current
    val backgroundPainter = rememberAssetPainter("lobby/Game_Lobby.png")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val carouselBringIntoView = remember { BringIntoViewRequester() }

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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = backgroundPainter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp)
                    .padding(bottom = 140.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LobbyHeader(lastChoice = lastChoice)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.select_hero),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color(0xFFE8F5FF),
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )

                        HeroCarousel(
                            modifier = Modifier
                                .bringIntoViewRequester(carouselBringIntoView)
                                .offset(y = (-32).dp),
                            heroes = heroes,
                            selectedHero = selectedHero,
                            onHeroSelected = { selectedHero = it }
                        )

                        val heroLabel = stringResource(id = selectedHero.displayNameRes)
                        val heroDescription = stringResource(id = selectedHero.descriptionRes)

                        Text(
                            text = heroLabel,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFF2CFF8F),
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = heroDescription,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFFE8F5FF),
                                lineHeight = 22.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = {
                        Text(text = stringResource(id = R.string.hero_name_hint))
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color(0xFF2CFF8F),
                        focusedIndicatorColor = Color(0xFF38B6FF),
                        unfocusedContainerColor = Color(0x33000814),
                        focusedContainerColor = Color(0x33000814),
                        cursorColor = Color(0xFF38B6FF)
                    )
                )
            }

            TransparentHotZone(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
                    .size(width = 160.dp, height = 76.dp),
                label = stringResource(id = R.string.lobby_back)
            ) {
                onBack()
            }

            TransparentHotZone(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(width = 180.dp, height = 88.dp),
                label = stringResource(id = R.string.lobby_select_hero)
            ) {
                scope.launch { carouselBringIntoView.bringIntoView() }
                onSelectHero()
            }

            TransparentHotZone(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .size(width = 168.dp, height = 80.dp),
                label = stringResource(id = R.string.lobby_start_battle)
            ) {
                val trimmedName = playerName.trim()
                if (trimmedName.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.error_empty_hero_name)
                        )
                    }
                    return@TransparentHotZone
                }

                val heroLabel = stringResource(id = selectedHero.displayNameRes)

                viewModel.saveHeroChoice(
                    playerName = trimmedName,
                    heroType = selectedHero.toHeroType(),
                    heroName = heroLabel
                )

                onStartBattle(selectedHero, trimmedName)

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
        }
    }
}

@Composable
private fun LobbyHeader(lastChoice: HeroChoiceEntity?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(id = R.string.lobby_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF2CFF8F),
                fontWeight = FontWeight.Bold,
                shadow = null
            )
        )
        Text(
            text = stringResource(id = R.string.lobby_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFB2D9FF))
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
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0x332CFF8F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
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
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFE8F5FF))
                        )
                        Text(
                            text = stringResource(id = R.string.lobby_last_saved_time, formattedTime),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8FA3BF))
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
    onHeroSelected: (HeroOption) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(heroes) { hero ->
            val painter = rememberAssetPainter(hero.assetPath)
            val isSelected = hero == selectedHero
            val targetScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.9f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "heroScale${hero.name}"
            )

            HeroCarouselCard(
                hero = hero,
                painter = painter,
                scale = targetScale,
                isSelected = isSelected,
                onClick = { onHeroSelected(hero) }
            )
        }
    }
}

@Composable
private fun HeroCarouselCard(
    hero: HeroOption,
    painter: androidx.compose.ui.graphics.painter.Painter,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(176.dp)
            .height(244.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.8f + (scale - 0.9f).coerceIn(0f, 0.2f)
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Color(0x552CFF8F) else Color(0x33000814)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 10.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painter,
                    contentDescription = stringResource(id = hero.displayNameRes),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = stringResource(id = hero.displayNameRes),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFE8F5FF),
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun TransparentHotZone(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .semantics { contentDescription = label }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}
