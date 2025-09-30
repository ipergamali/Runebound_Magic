package com.example.runeboundmagic.inventory

/**
 * Απλό inventory που αντιστοιχίζεται σε συγκεκριμένο ήρωα.
 */
class Inventory(
    val id: String,
    val heroId: String,
    var gold: Int,
    val capacity: Int,
    private val items: MutableList<Item> = mutableListOf()
) {

    fun addItem(item: Item): Boolean {
        if (items.size >= capacity) return false
        items += item
        return true
    }

    fun removeItem(itemId: String): Boolean = items.removeIf { it.id == itemId }

    fun getItemsByCategory(category: ItemCategory): List<Item> =
        items.filter { it.category == category }

    fun getAllItems(): List<Item> = items.toList()
}
