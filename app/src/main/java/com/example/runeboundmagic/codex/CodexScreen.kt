package com.example.runeboundmagic.codex

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.runeboundmagic.R
import java.util.Locale

@Composable
fun CodexScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: CodexViewModel = rememberCodexViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF060B23),
        topBar = {
            CodexTopBar(
                searchQuery = uiState.filter.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onBack = onBack
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF38B6FF))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CodexContent(
                    uiState = uiState,
                    onCategorySelected = viewModel::onCategorySelected,
                    onEntrySelected = viewModel::onEntrySelected,
                    onToggleFavorite = viewModel::toggleFavorite
                )

                CodexDetailOverlay(
                    entry = uiState.selectedEntry,
                    isFavorite = uiState.selectedEntry?.let { uiState.favorites.contains(it.id) } == true,
                    onClose = { viewModel.onEntrySelected(null) },
                    onToggleFavorite = { entry ->
                        viewModel.toggleFavorite(entry)
                        if (entry.category == CodexCategory.HERO) {
                            viewModel.saveProgress(
                                heroId = entry.id,
                                level = 1,
                                inventory = buildHeroInventory(entry)
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun buildHeroInventory(entry: CodexEntry): HeroInventory {
    val cleanedId = entry.id.ifBlank {
        entry.name.lowercase(Locale.ROOT).replace("\s+".toRegex(), "_")
    }
    val idForLore = entry.id.ifBlank { entry.name }
    val heroCardLore = when (idForLore.lowercase(Locale.ROOT)) {
        "sora" -> "Η Sora είναι ανιχνεύτρια των ουρανών που συνδυάζει τεχνολογία και αρχαία ρούνια. Η κάρτα της υπενθυμίζει στον παίκτη ότι κάθε αποστολή απαιτεί στρατηγική, ευελιξία και μια εφεδρική επιλογή για την ίδια την κάρτα του ήρωα."
        else -> entry.description.ifBlank {
            "Η κάρτα του ήρωα λειτουργεί ως γρήγορη αναφορά για τις ικανότητες και τα σημεία ισχύος του."
        }
    }
    val heroCardName = entry.name.ifBlank { "Άγνωστος Ήρωας" }
    val heroCardSlot = HeroCardSlot(
        id = "${cleanedId}_hero_card",
        name = "Κάρτα Ήρωα $heroCardName",
        lore = heroCardLore
    )
    return HeroInventory(
        heroCard = heroCardSlot,
        equipment = emptyList()
    )
}

@Composable
private fun CodexTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF060B23))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color(0xFF38B6FF)
                )
            }
            Text(
                text = stringResource(id = R.string.codex_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF10163A),
                unfocusedContainerColor = Color(0xFF10163A),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLeadingIconColor = Color(0xFF38B6FF),
                unfocusedLeadingIconColor = Color(0xFF38B6FF)
            ),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.codex_search_placeholder),
                    color = Color(0xFF9FA8DA)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF38B6FF)
                )
            }
        )
    }
}

@Composable
private fun CodexContent(
    uiState: CodexUiState,
    onCategorySelected: (CodexCategory?) -> Unit,
    onEntrySelected: (CodexEntry) -> Unit,
    onToggleFavorite: (CodexEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryChips(
            selectedCategory = uiState.filter.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Text(
            text = stringResource(id = R.string.codex_results, uiState.filteredEntries.size),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9FA8DA))
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.filteredEntries, key = { it.id }) { entry ->
                CodexCard(
                    entry = entry,
                    isFavorite = uiState.favorites.contains(entry.id),
                    onClick = { onEntrySelected(entry) },
                    onFavorite = { onToggleFavorite(entry) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: CodexCategory?,
    onCategorySelected: (CodexCategory?) -> Unit
) {
    val categories = remember { listOf(null) + CodexCategory.values().toList() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val label = when (category) {
                null -> stringResource(id = R.string.codex_all)
                CodexCategory.HERO -> stringResource(id = R.string.codex_category_heroes)
                CodexCategory.CARD -> stringResource(id = R.string.codex_category_cards)
                CodexCategory.CAMPAIGN -> stringResource(id = R.string.codex_category_campaigns)
                CodexCategory.LORE -> stringResource(id = R.string.codex_category_lore)
            }
            val selected = category == selectedCategory
            AssistChip(
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = label,
                        color = if (selected) Color.Black else Color.White,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) Color(0xFF38B6FF) else Color(0xFF10163A)
                )
            )
        }
    }
}

@Composable
private fun CodexCard(
    entry: CodexEntry,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10163A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = entry.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC060B23))
                        )
                    )
            )
            IconButton(
                onClick = onFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = Color(0xFF38B6FF)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                entry.rarity?.let { rarity ->
                    Text(
                        text = rarity,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9FA8DA))
                    )
                }
            }
        }
    }
}

@Composable
private fun CodexDetailOverlay(
    entry: CodexEntry?,
    isFavorite: Boolean,
    onClose: () -> Unit,
    onToggleFavorite: (CodexEntry) -> Unit
) {
    AnimatedVisibility(
        visible = entry != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        entry?.let { codexEntry ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC060B23)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF10163A)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = codexEntry.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color(0xFF38B6FF)
                                )
                            }
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(codexEntry.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = codexEntry.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color(0xFF060B23), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = codexEntry.description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCFD8FF)),
                            textAlign = TextAlign.Start
                        )

                        if (codexEntry.abilities.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(id = R.string.codex_abilities),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color(0xFF38B6FF),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                codexEntry.abilities.forEach { ability ->
                                    Text(
                                        text = "• $ability",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                    )
                                }
                            }
                        }

                        codexEntry.role?.let { role ->
                            Text(
                                text = stringResource(id = R.string.codex_role, role),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.codex_category_label,
                                    codexEntry.category.name.lowercase().replaceFirstChar { it.titlecase() }
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF9FA8DA))
                            )

                            IconButton(onClick = { onToggleFavorite(codexEntry) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCodexViewModel(): CodexViewModel {
    val context = LocalContext.current
    val repository = remember { FirestoreCodexRepository() }
    val playerId = remember { context.packageName } // Απλό σταθερό id για demo
    return viewModel(factory = CodexViewModelFactory(repository, playerId))
}
