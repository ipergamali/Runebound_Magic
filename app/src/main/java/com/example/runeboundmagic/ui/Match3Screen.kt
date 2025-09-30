package com.example.runeboundmagic.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

@Composable
fun Match3Screen(
    heroOption: HeroOption,
    heroName: String,
    onExitBattle: () -> Unit,
    viewModel: Match3ViewModel = viewModel()
) {
    BackHandler(onBack = onExitBattle)

    val grid by viewModel.grid.collectAsState()
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showCharacters by remember { mutableStateOf(false) }
    var showBoard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showCharacters = true
        delay(420)
        showBoard = true
    }

    val displayedHeroName = if (heroName.isBlank()) {
        stringResource(id = heroOption.displayNameRes)
    } else {
        heroName
    }
    val heroSubtitle = stringResource(id = heroOption.displayNameRes)
    val enemyName = stringResource(id = R.string.match3_enemy_name)
    val enemySubtitle = stringResource(id = R.string.match3_enemy_subtitle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050A12))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.match3_title),
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                    .clickable(onClick = onExitBattle)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.match3_exit_battle),
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        AnimatedVisibility(
            visible = showCharacters,
            enter = fadeIn(animationSpec = tween(420)) + slideInHorizontally { fullWidth -> -fullWidth / 3 }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BattlePortrait(
                    name = displayedHeroName,
                    subtitle = heroSubtitle,
                    painterPath = heroOption.assetPath,
                    nameColor = Color.White
                )
                Text(
                    text = stringResource(id = R.string.match3_vs),
                    color = Color(0xFFFFD700),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                BattlePortrait(
                    name = enemyName,
                    subtitle = enemySubtitle,
                    painterPath = "characters/black_mage.png",
                    nameColor = Color(0xFFE57373)
                )
            }
        }

        AnimatedVisibility(
            visible = showBoard,
            enter = fadeIn(animationSpec = tween(360)) + slideInVertically { fullHeight -> fullHeight / 4 }
        ) {
            RuneBoard(
                grid = grid,
                selectedCell = selectedCell,
                onCellClick = { x, y ->
                    val current = selectedCell
                    if (current == null) {
                        selectedCell = x to y
                    } else {
                        if (current != x to y) {
                            viewModel.swapRunes(current.first, current.second, x, y)
                        }
                        selectedCell = null
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.match3_hint),
            color = Color(0xFFE0F2FF),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BattlePortrait(
    name: String,
    subtitle: String,
    painterPath: String,
    nameColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name,
            color = nameColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .background(Color(0x330B111A))
        ) {
            Image(
                painter = rememberAssetPainter(painterPath),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RuneBoard(
    grid: List<List<RuneType>>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        grid.forEachIndexed { y, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { x, rune ->
                    val isSelected = selectedCell?.let { it.first == x && it.second == y } == true
                    RuneTile(
                        rune = rune,
                        selected = isSelected,
                        onClick = { onCellClick(x, y) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuneTile(
    rune: RuneType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFFFFD700) else Color(0x22000000)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x330B111A))
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAssetPainter(rune.assetPath),
            contentDescription = rune.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

enum class RuneType(val assetPath: String) {
    FIRE("puzzle/red_gem.png"),
    WATER("puzzle/blue_gem.png"),
    AIR("puzzle/turquoise.png"),
    EARTH("puzzle/green_gem.png")
}

class Match3ViewModel : ViewModel() {
    private val gridSize = 6
    private val _grid = MutableStateFlow(generateGrid())
    val grid: StateFlow<List<List<RuneType>>> = _grid

    private fun generateGrid(): List<List<RuneType>> {
        return List(gridSize) {
            List(gridSize) {
                RuneType.values()[Random.nextInt(RuneType.values().size)]
            }
        }
    }

    fun swapRunes(x1: Int, y1: Int, x2: Int, y2: Int) {
        _grid.update { current ->
            val newGrid = current.map { it.toMutableList() }.toMutableList()
            val temp = newGrid[y1][x1]
            newGrid[y1][x1] = newGrid[y2][x2]
            newGrid[y2][x2] = temp
            newGrid
        }
    }
}
