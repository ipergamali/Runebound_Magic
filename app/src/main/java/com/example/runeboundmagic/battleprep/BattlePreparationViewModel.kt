package com.example.runeboundmagic.battleprep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.HeroType
import com.example.runeboundmagic.codex.CodexManager
import com.example.runeboundmagic.data.codex.local.CodexDao
import com.example.runeboundmagic.data.codex.local.CodexDatabase
import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.Inventory
import com.example.runeboundmagic.inventory.Item
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.WeaponItem
import com.example.runeboundmagic.toHeroType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class BattlePreparationViewModel(
    private val heroOption: HeroOption,
    private val heroName: String,
    private val heroDescription: String,
    private val codexDao: CodexDao,
    private val codexManager: CodexManager,
    private val baseStats: BaseStats,
    private val heroClassMetadata: HeroClassMetadata,
    private val rarityMetadata: RarityMetadata,
    private val heroCardAsset: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BattlePreparationUiState())
    val uiState: StateFlow<BattlePreparationUiState> = _uiState.asStateFlow()

    private val hero: Hero = createHero()

    init {
        preloadData()
    }

    fun selectSlot(index: Int) {
        val slot = _uiState.value.inventorySlots.getOrNull(index) ?: return
        _uiState.update { state ->
            state.copy(
                inventorySlots = state.inventorySlots.map { slotState ->
                    if (slotState.index == index) slotState.copy(isSelected = !slotState.isSelected)
                    else slotState.copy(isSelected = false)
                },
                selectedItem = if (slot.isSelected) null else slot.item
            )
        }
    }

    fun dismissItemDetails() {
        _uiState.update { state ->
            state.copy(
                inventorySlots = state.inventorySlots.map { it.copy(isSelected = false) },
                selectedItem = null
            )
        }
    }

    private fun preloadData() {
        viewModelScope.launch {
            runCatching {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val profile = codexManager.prepareHeroProfile(hero, hero.name)
                persistMetadata(profile.inventory)
                val heroCardDetails = codexDao.getHeroCard(hero.id)?.toDomain(hero, heroDescription)
                    ?: HeroCardDetails(
                        hero = hero,
                        heroDescription = heroDescription,
                        heroClassMetadata = heroClassMetadata,
                        rarity = rarityMetadata,
                        baseStats = baseStats
                    )
                val inventorySlots = buildInventorySlots(profile.inventory)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        heroCard = heroCardDetails,
                        inventorySlots = inventorySlots,
                        gold = profile.inventory.gold,
                        capacity = profile.inventory.capacity,
                        categories = DefaultCategories,
                        selectedItem = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, errorMessage = throwable.message) }
            }
        }
    }

    private suspend fun persistMetadata(inventory: Inventory) {
        codexDao.upsertHeroClass(heroClassMetadata.toEntity())
        codexDao.upsertRarities(DefaultRarities.map(RarityMetadata::toEntity))
        codexDao.upsertItemCategories(DefaultCategories.map(InventoryCategoryInfo::toEntity))
        val details = HeroCardDetails(
            hero = hero.copy(cardImage = heroCardAsset),
            heroDescription = heroDescription,
            heroClassMetadata = heroClassMetadata,
            rarity = rarityMetadata,
            baseStats = baseStats
        )
        codexDao.upsertHeroCard(details.toEntity(cardId = "${hero.id}_card"))
        codexManager.updateInventory(
            com.example.runeboundmagic.codex.HeroProfile(
                hero = hero,
                inventory = inventory
            )
        )
    }

    private fun buildInventorySlots(inventory: Inventory): List<InventorySlotUiModel> {
        val orderedItems = inventory.getAllItems()
            .sortedWith(compareByDescending<Item> { it is WeaponItem }.thenBy { it.name })
        val slots = MutableList(SLOT_COUNT) { index ->
            InventorySlotUiModel(index = index, item = null, isSelected = false)
        }
        orderedItems.forEachIndexed { index, item ->
            if (index < slots.size) {
                slots[index] = slots[index].copy(item = item)
            }
        }
        return slots
    }

    private fun createHero(): Hero {
        val heroClass = when (heroOption) {
            HeroOption.WARRIOR -> HeroClass.WARRIOR
            HeroOption.RANGER -> HeroClass.RANGER
            HeroOption.MAGE -> HeroClass.MAGE
            HeroOption.MYSTICAL_PRIESTESS -> HeroClass.PRIESTESS
        }
        val resolvedName = heroName.ifBlank {
            heroOption.name.lowercase(Locale.getDefault()).replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
        return Hero(
            id = heroOption.name.lowercase(Locale.ROOT),
            name = resolvedName,
            description = heroDescription,
            level = 1,
            classType = heroClass,
            cardImage = heroCardAsset
        )
    }

    companion object {
        private const val SLOT_COUNT = 40

        val DefaultCategories: List<InventoryCategoryInfo> = listOf(
            InventoryCategoryInfo(
                id = ItemCategory.WEAPON.name,
                title = "Weapons",
                description = "Όπλα και εργαλεία μάχης."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.ARMOR.name,
                title = "Armor",
                description = "Κράνη, θώρακες και προστατευτικά."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.SHIELD.name,
                title = "Shields",
                description = "Ασπίδες και αμυντικοί μηχανισμοί."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.ACCESSORY.name,
                title = "Accessories",
                description = "Δαχτυλίδια και φυλαχτά."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.CONSUMABLE.name,
                title = "Consumables",
                description = "Φίλτρα και προμήθειες."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.SPELL_SCROLL.name,
                title = "Spells & Scrolls",
                description = "Μαγικά ξόρκια και πάπυροι."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.RUNE_GEM.name,
                title = "Runes & Gems",
                description = "Μαγικοί λίθοι και ρούνοι."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.CRAFTING_MATERIAL.name,
                title = "Crafting Materials",
                description = "Υλικά κατασκευών."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.QUEST_ITEM.name,
                title = "Quest Items",
                description = "Αντικείμενα αποστολών."
            ),
            InventoryCategoryInfo(
                id = ItemCategory.CURRENCY.name,
                title = "Gold & Currency",
                description = "Νόμισμα και οικονομία."
            ),
        )

        val DefaultRarities: List<RarityMetadata> = listOf(
            RarityMetadata("COMMON", "Common", "#BDBDBD"),
            RarityMetadata("RARE", "Rare", "#4FC3F7"),
            RarityMetadata("EPIC", "Epic", "#9575CD"),
            RarityMetadata("LEGENDARY", "Legendary", "#FFB300")
        )
    }
}

class BattlePreparationViewModelFactory(
    private val heroOption: HeroOption,
    private val heroName: String,
    private val heroDescription: String,
    private val context: android.content.Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(BattlePreparationViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        val database = CodexDatabase.getInstance(context)
        val codexDao = database.codexDao()
        val heroType = heroOption.toHeroType()
        val baseStats = baseStatsFor(heroType)
        val metadata = heroClassMetadataFor(heroType)
        val rarity = rarityMetadataFor(heroType)
        val cardAsset = heroCardAssetFor(heroType)
        val manager = CodexManager(codexDao = codexDao)
        @Suppress("UNCHECKED_CAST")
        return BattlePreparationViewModel(
            heroOption = heroOption,
            heroName = heroName,
            heroDescription = heroDescription,
            codexDao = codexDao,
            codexManager = manager,
            baseStats = baseStats,
            heroClassMetadata = metadata,
            rarityMetadata = rarity,
            heroCardAsset = cardAsset
        ) as T
    }
}

private fun baseStatsFor(heroType: HeroType): BaseStats = when (heroType) {
    HeroType.WARRIOR -> BaseStats(strength = 18, agility = 12, intellect = 6, faith = 8)
    HeroType.HUNTER -> BaseStats(strength = 12, agility = 18, intellect = 8, faith = 6)
    HeroType.MAGE -> BaseStats(strength = 6, agility = 10, intellect = 20, faith = 12)
    HeroType.PRIEST -> BaseStats(strength = 8, agility = 10, intellect = 14, faith = 18)
}

private fun heroClassMetadataFor(heroType: HeroType): HeroClassMetadata = when (heroType) {
    HeroType.WARRIOR -> HeroClassMetadata(
        id = HeroClass.WARRIOR,
        name = "Warrior",
        weaponProficiency = "Crossbows",
        armorProficiency = "Plate Armor"
    )

    HeroType.HUNTER -> HeroClassMetadata(
        id = HeroClass.RANGER,
        name = "Hunter",
        weaponProficiency = "Crossbows",
        armorProficiency = "Leather Armor"
    )

    HeroType.MAGE -> HeroClassMetadata(
        id = HeroClass.MAGE,
        name = "Mage",
        weaponProficiency = "Magic Rods",
        armorProficiency = "Mystic Robes"
    )

    HeroType.PRIEST -> HeroClassMetadata(
        id = HeroClass.PRIESTESS,
        name = "Priest",
        weaponProficiency = "Sacred Rods",
        armorProficiency = "Blessed Vestments"
    )
}

private fun rarityMetadataFor(heroType: HeroType): RarityMetadata = when (heroType) {
    HeroType.WARRIOR, HeroType.HUNTER, HeroType.PRIEST -> RarityMetadata(
        id = "RARE",
        displayName = "Rare",
        colorHex = "#4FC3F7"
    )

    HeroType.MAGE -> RarityMetadata(
        id = "EPIC",
        displayName = "Epic",
        colorHex = "#9575CD"
    )
}

private fun heroCardAssetFor(heroType: HeroType): String = when (heroType) {
    HeroType.WARRIOR -> "heroes/warrior_card.png"
    HeroType.HUNTER -> "heroes/hunter_card.png"
    HeroType.MAGE -> "heroes/mage_card.png"
    HeroType.PRIEST -> "heroes/priest_card.png"
}

