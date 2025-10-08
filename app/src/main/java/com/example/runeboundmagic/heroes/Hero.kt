package com.example.runeboundmagic.heroes

import com.example.runeboundmagic.inventory.Inventory

/**
 * Μοντέλο για έναν διαθέσιμο ήρωα που εμφανίζεται στο lobby.
 */
data class Hero(
    val id: String,
    val name: String,
    val description: String,
    val level: Int,
    val classType: HeroClass,
    val cardImage: String,
    val inventoryId: String = "${'$'}id-inventory"
) {
    /**
     * Γρήγορη δημιουργία προσωπικού inventory με προεπιλεγμένη χωρητικότητα.
     */
    fun createInventory(
        inventoryId: String = this.inventoryId,
        gold: Int = 0,
        capacity: Int = DEFAULT_CAPACITY,
        items: List<com.example.runeboundmagic.inventory.Item> = emptyList()
    ): Inventory = Inventory(
        id = inventoryId,
        heroId = id,
        gold = gold,
        capacity = capacity,
        items = items
    )

    companion object {
        const val DEFAULT_CAPACITY = 30
    }
}
