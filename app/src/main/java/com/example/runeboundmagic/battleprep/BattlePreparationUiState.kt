package com.example.runeboundmagic.battleprep

import com.example.runeboundmagic.inventory.Item

/**
 * Μοντέλα κατάστασης για την οθόνη Battle Preparation.
 */
data class InventorySlotUiModel(
    val index: Int,
    val item: Item?,
    val isSelected: Boolean
)

data class BattlePreparationUiState(
    val isLoading: Boolean = true,
    val heroCard: HeroCardDetails? = null,
    val inventorySlots: List<InventorySlotUiModel> = emptyList(),
    val gold: Int = 0,
    val capacity: Int = 0,
    val categories: List<InventoryCategoryInfo> = emptyList(),
    val isBackpackOpen: Boolean = false,
    val selectedItem: Item? = null,
    val errorMessage: String? = null
)
