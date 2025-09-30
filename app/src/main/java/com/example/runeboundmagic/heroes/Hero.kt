package com.example.runeboundmagic.heroes

import com.example.runeboundmagic.inventory.Inventory

/**
 * Μοντέλο για έναν διαθέσιμο ήρωα που εμφανίζεται στο lobby.
 */
data class Hero(
    val id: String,
    val name: String,
    val level: Int,
    val classType: HeroClass,
    val cardImage: String
) {
    /**
     * Γρήγορη δημιουργία προσωπικού inventory με προεπιλεγμένη χωρητικότητα.
     */
    fun createInventory(
        inventoryId: String = "${'$'}id-inventory",
        gold: Int = 0,
        capacity: Int = DEFAULT_CAPACITY
    ): Inventory = Inventory(
        id = inventoryId,
        heroId = id,
        gold = gold,
        capacity = capacity
    )

    companion object {
        const val DEFAULT_CAPACITY = 30
    }
}
