package com.example.runeboundmagic.battleprep

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.R
import com.example.runeboundmagic.inventory.Item
import com.example.runeboundmagic.ui.rememberAssetPainter

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun BattlePreparationScreen(
    heroOption: HeroOption,
    heroName: String,
    onBack: () -> Unit,
    onStartBattle: (HeroOption, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = stringResource(id = heroOption.displayNameRes)
    val heroLore = stringResource(id = heroOption.descriptionRes)
    val viewModel = battlePreparationViewModel(heroOption, heroName, heroLore)
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by rememberSaveable { mutableStateOf<InventoryCategoryInfo?>(null) }

    LaunchedEffect(uiState.isBackpackOpen) {
        if (!uiState.isBackpackOpen) {
            selectedCategory = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF090F1F)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1533), Color(0xFF131D45))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HeroHeader(
                    onBack = onBack,
                    title = stringResource(id = R.string.battle_prep_title),
                    heroLabel = displayName
                )
                HeroCardSection(uiState = uiState)
                InventorySummary(uiState = uiState)
            }

            InventoryToggle(
                isOpen = uiState.isBackpackOpen,
                onToggle = viewModel::toggleBackpack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 24.dp)
            )

            AnimatedVisibility(
                visible = uiState.isBackpackOpen,
                enter = slideInVertically { -it / 2 } + fadeIn(),
                exit = slideOutVertically { -it / 2 } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 96.dp, end = 24.dp)
            ) {
                InventoryOverlay(
                    uiState = uiState,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onSlotClick = viewModel::selectSlot
                )
            }

            StartBattleSection(
                heroOption = heroOption,
                heroName = uiState.heroCard?.hero?.name ?: heroName.ifBlank { displayName },
                onStartBattle = onStartBattle,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )


            val selectedItem = uiState.selectedItem
            if (selectedItem != null) {
                ItemDetailsDialog(item = selectedItem, onDismiss = viewModel::dismissItemDetails)
            }

            uiState.errorMessage?.let { message ->
                ErrorMessage(message = message, modifier = Modifier.align(Alignment.BottomStart))
            }
        }
    }
}

@Composable
private fun HeroHeader(onBack: () -> Unit, title: String, heroLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
            Text(
                text = heroLabel,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9FA8DA))
            )
        }
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun HeroCardSection(uiState: BattlePreparationUiState) {
    val heroCard = uiState.heroCard
    if (heroCard == null) {
        LoadingCard()
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111A3A)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Image(
                    painter = rememberAssetPainter(assetPath = heroCard.hero.cardImage),
                    contentDescription = heroCard.hero.name,
                    modifier = Modifier
                        .size(140.dp)
                        .border(2.dp, heroCard.rarity.color, RoundedCornerShape(18.dp))
                        .padding(4.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = heroCard.hero.name,
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = heroCard.heroDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFB0BEC5))
                    )
                    RarityChip(rarity = heroCard.rarity)
                    ClassInfo(heroClassMetadata = heroCard.heroClassMetadata)
                }
            }
            StatsRow(baseStats = heroCard.baseStats)
        }
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        color = Color(0x33111A3A),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = stringResource(id = R.string.loading), color = Color.White)
        }
    }
}

@Composable
private fun RarityChip(rarity: RarityMetadata) {
    Surface(
        color = rarity.color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, rarity.color)
    ) {
        Text(
            text = rarity.displayName,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ClassInfo(heroClassMetadata: HeroClassMetadata) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(id = R.string.battle_prep_weapon_prof, heroClassMetadata.weaponProficiency),
            color = Color(0xFFCFD8DC),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(id = R.string.battle_prep_armor_prof, heroClassMetadata.armorProficiency),
            color = Color(0xFFCFD8DC),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StatsRow(baseStats: BaseStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatBadge(label = "STR", value = baseStats.strength)
        StatBadge(label = "AGI", value = baseStats.agility)
        StatBadge(label = "INT", value = baseStats.intellect)
        StatBadge(label = "FAI", value = baseStats.faith)
    }
}

@Composable
private fun StatBadge(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF1C2550),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF536DFE))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = label, color = Color(0xFFB0BEC5), fontSize = 12.sp)
                Text(text = value.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InventorySummary(uiState: BattlePreparationUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0x221C2550),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(label = stringResource(id = R.string.battle_prep_gold), value = uiState.gold.toString())
            SummaryItem(label = stringResource(id = R.string.battle_prep_capacity), value = "${uiState.inventorySlots.count { it.item != null }} / ${uiState.capacity}")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF90A4AE), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun InventoryToggle(isOpen: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val painter = rememberAssetPainter(assetPath = "inventory/inventory.png")
    Box(modifier = modifier.clickable(onClick = onToggle)) {
        Image(
            painter = painter,
            contentDescription = stringResource(id = R.string.battle_prep_inventory_toggle),
            modifier = Modifier.size(64.dp)
        )
        if (isOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .background(Color(0xFF00C853), CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InventoryOverlay(
    uiState: BattlePreparationUiState,
    selectedCategory: InventoryCategoryInfo?,
    onCategorySelected: (InventoryCategoryInfo?) -> Unit,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        color = Color(0xEE111A3A)
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryColumn(
                categories = uiState.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
            InventoryGridPanel(
                slots = selectedCategory?.let { category ->
                    uiState.inventorySlots.map { slot ->
                        if (slot.item?.category?.name == category.id) {
                            slot
                        } else {
                            slot.copy(item = null, isSelected = false)
                        }
                    }
                } ?: uiState.inventorySlots,
                onSlotClick = onSlotClick,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun CategoryColumn(
    categories: List<InventoryCategoryInfo>,
    selectedCategory: InventoryCategoryInfo?,
    onCategorySelected: (InventoryCategoryInfo?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { category ->
            val isSelected = selectedCategory?.id == category.id
            Surface(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onCategorySelected(if (isSelected) null else category) },
                color = if (isSelected) Color(0x332E7DFF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAssetPainter(assetPath = category.iconAsset),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = category.title,
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = category.description,
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                        val capacityLabel = if (category.capacity > 0) {
                            "${category.owned} / ${category.capacity}"
                        } else {
                            category.owned.toString()
                        }
                        Text(
                            text = capacityLabel,
                            color = Color(0xFF6F7BCC),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InventoryGrid(
    slots: List<InventorySlotUiModel>,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        items(slots, key = { it.index }) { slot ->
            InventorySlotCell(slot = slot, onClick = { onSlotClick(slot.index) })
        }
    }
}

@Composable
private fun InventorySlotCell(slot: InventorySlotUiModel, onClick: () -> Unit) {
    val borderColor = when {
        slot.isSelected -> Color(0xFF4FC3F7)
        slot.item != null -> Color(0xFF374785)
        else -> Color(0x552E3A6A)
    }
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color(0x5520315B),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        val item = slot.item
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.battle_prep_empty_slot),
                    color = Color(0xFF546E7A),
                    fontSize = 9.sp
                )
            }
        } else {
            Image(
                painter = rememberAssetPainter(assetPath = item.icon),
                contentDescription = item.name,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
private fun InventoryGridPanel(
    slots: List<InventorySlotUiModel>,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        color = Color(0x6620315B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3A6A))
    ) {
        Box(
            modifier = Modifier
                .background(Color(0x3320315B))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            InventoryGrid(
                slots = slots,
                onSlotClick = onSlotClick,
                modifier = Modifier
                    .widthIn(min = 320.dp)
            )
        }
    }
}

@Composable
private fun ItemDetailsDialog(item: Item, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { Text(text = stringResource(id = R.string.close)) }
        },
        title = { Text(text = item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = item.description)
                Text(text = stringResource(id = R.string.battle_prep_rarity_label, item.rarity.name))
                Text(text = stringResource(id = R.string.battle_prep_item_category, item.category.name))
                item.subcategory?.let { subcategory ->
                    Text(text = stringResource(id = R.string.battle_prep_item_subcategory, subcategory.name))
                }
                item.weaponStats?.let { stats ->
                    Text(text = stringResource(id = R.string.battle_prep_weapon_damage, stats.damage))
                    Text(text = stringResource(id = R.string.battle_prep_weapon_element, stats.element))
                    Text(text = stringResource(id = R.string.battle_prep_weapon_speed, stats.attackSpeed))
                }
                if (item.stackable) {
                    Text(text = stringResource(id = R.string.battle_prep_item_stack, item.quantity))
                }
            }
        }
    )
}

@Composable
private fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(24.dp),
        color = Color(0xAAFF5252),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = message,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun StartBattleSection(
    heroOption: HeroOption,
    heroName: String,
    onStartBattle: (HeroOption, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = { onStartBattle(heroOption, heroName) }, modifier = modifier) {
        Text(text = stringResource(id = R.string.battle_prep_start_battle))
    }
}

@Composable
private fun battlePreparationViewModel(
    heroOption: HeroOption,
    heroName: String,
    heroDescription: String
): BattlePreparationViewModel {
    val context = LocalContext.current
    val factory = remember(heroOption, heroName, heroDescription, context) {
        BattlePreparationViewModelFactory(
            heroOption = heroOption,
            heroName = heroName,
            heroDescription = heroDescription,
            context = context.applicationContext
        )
    }
    return viewModel(factory = factory)
}
