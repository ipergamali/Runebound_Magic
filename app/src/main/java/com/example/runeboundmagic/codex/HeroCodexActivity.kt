package com.example.runeboundmagic.codex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.R
import com.example.runeboundmagic.inventory.Item
import com.example.runeboundmagic.inventory.Rarity
import com.example.runeboundmagic.inventory.icon

class HeroCodexActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val heroOptionName = intent.getStringExtra(EXTRA_HERO_OPTION)
        val heroOption = HeroOption.fromName(heroOptionName)
        val profile = HeroCodexData.profileFor(heroOption)
        val heroDisplayName = getString(heroOption.displayNameRes)
        val heroDescription = getString(heroOption.descriptionRes)
        val providedName = intent.getStringExtra(EXTRA_HERO_NAME).orEmpty()
        val heroName = if (providedName.isBlank()) heroDisplayName else providedName

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF38B6FF),
                    onPrimary = Color.Black,
                    secondary = Color(0xFF00E5A0),
                    onSecondary = Color.Black,
                    background = Color(0xFF050915),
                    onBackground = Color(0xFFE4E7FF)
                )
            ) {
                HeroCodexScreen(
                    heroName = heroName,
                    heroClass = heroDisplayName,
                    heroDescription = heroDescription,
                    heroProfile = profile,
                    heroAssetPath = heroOption.assetPath,
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_HERO_NAME = "hero_name"
        private const val EXTRA_HERO_OPTION = "hero_option"

        fun createIntent(
            context: Context,
            heroOption: HeroOption,
            heroName: String
        ): Intent {
            return Intent(context, HeroCodexActivity::class.java).apply {
                putExtra(EXTRA_HERO_OPTION, heroOption.name)
                putExtra(EXTRA_HERO_NAME, heroName)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroCodexScreen(
    heroName: String,
    heroClass: String,
    heroDescription: String,
    heroProfile: HeroCodexProfile,
    heroAssetPath: String,
    onBack: () -> Unit
) {
    var showInventory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<InventoryCategoryEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.hero_codex_title),
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                        )
                        Text(
                            text = stringResource(id = R.string.hero_codex_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF38B6FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF060B23)
                )
            )
        },
        containerColor = Color(0xFF060B23)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF060B23), Color(0xFF0B1339))
                    )
                )
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 24.dp)
            ) {
                item {
                    HeroCard(
                        heroName = heroName,
                        heroClass = heroClass,
                        heroDescription = heroDescription,
                        heroProfile = heroProfile,
                        heroAssetPath = heroAssetPath
                    )
                }
                item {
                    InventoryShortcut(
                        onClick = { showInventory = true },
                        weapon = heroProfile.startingWeapon
                    )
                }
            }

            AnimatedVisibility(
                visible = showInventory,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                InventoryOverlay(
                    categories = heroProfile.inventoryCategories,
                    onCategorySelected = { category ->
                        selectedCategory = category
                    },
                    onDismiss = { showInventory = false }
                )
            }

            selectedCategory?.let { category ->
                InventoryCategoryDialog(
                    entry = category,
                    onDismiss = { selectedCategory = null }
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    heroName: String,
    heroClass: String,
    heroDescription: String,
    heroProfile: HeroCodexProfile,
    heroAssetPath: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF10163A)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(assetUri(heroAssetPath))
                    .crossfade(true)
                    .build(),
                contentDescription = heroName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp)),
                alignment = Alignment.Center
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = heroName,
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = heroClass,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9FA8DA))
                )
            }

            Text(
                text = heroDescription,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFC5CAFF))
            )

            Text(
                text = heroProfile.heroCardLore,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
            )

            Divider(color = Color(0xFF1F2750))

            Text(
                text = stringResource(id = R.string.hero_codex_stats_title),
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(label = stringResource(id = R.string.hero_codex_stat_hp), value = heroProfile.stats.hp)
                StatPill(label = stringResource(id = R.string.hero_codex_stat_mana), value = heroProfile.stats.mana)
                StatPill(label = stringResource(id = R.string.hero_codex_stat_attack), value = heroProfile.stats.attack)
                StatPill(label = stringResource(id = R.string.hero_codex_stat_defense), value = heroProfile.stats.defense)
            }

            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF141C3F)),
                border = BorderStroke(width = 1.dp, color = Color(0xFF2A3470))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.hero_codex_weapon_label),
                        style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF38B6FF), fontWeight = FontWeight.Bold)
                    )
                    InventoryItemRow(item = heroProfile.startingWeapon)
                }
            }

            Text(
                text = stringResource(id = R.string.hero_codex_currency_summary, heroProfile.startingGold),
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
            )
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1B254D))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8EA2FF))
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun InventoryShortcut(onClick: () -> Unit, weapon: Item) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF10163A)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.hero_codex_inventory_title),
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(assetUri("inventory/inventory.png"))
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(id = R.string.hero_codex_inventory_icon_cd),
                modifier = Modifier
                    .height(96.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
            Text(
                text = stringResource(id = R.string.hero_codex_inventory_button),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF38B6FF), fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = weapon.description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
            )
        }
    }
}

@Composable
private fun InventoryOverlay(
    categories: List<InventoryCategoryEntry>,
    onCategorySelected: (InventoryCategoryEntry) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA04081F))
    ) {
        ElevatedCard(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF10163A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.hero_codex_inventory_title),
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFF38B6FF)
                        )
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(assetUri("inventory/backbag.png"))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Text(
                    text = stringResource(id = R.string.hero_codex_inventory_hint),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9FA8DA))
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categories) { category ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onCategorySelected(category) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF141C3F))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(assetUri(category.iconAsset))
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = category.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
                                    )
                                    Text(
                                        text = "${category.items.size} αντικείμενα",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7BCC))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryCategoryDialog(entry: InventoryCategoryEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.hero_codex_close))
            }
        },
        title = {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (entry.items.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.hero_codex_inventory_empty),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9FA8DA))
                    )
                } else {
                    entry.items.forEach { item ->
                        InventoryItemRow(item = item)
                    }
                }
            }
        }
    )
}

@Composable
private fun InventoryItemRow(item: Item) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(assetUri(item.icon))
                .build(),
            contentDescription = item.name,
            modifier = Modifier
                .height(48.dp)
                .clip(CircleShape)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = rarityLabel(item.rarity),
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6F7BCC))
            )
        }
    }
}

private fun rarityLabel(rarity: Rarity): String {
    return when (rarity) {
        Rarity.COMMON -> "Κοινό"
        Rarity.RARE -> "Σπάνιο"
        Rarity.EPIC -> "Επικό"
        Rarity.LEGENDARY -> "Θρυλικό"
    }
}

private fun assetUri(path: String): String = "file:///android_asset/$path"

