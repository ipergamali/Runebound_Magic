package com.example.runeboundmagic.inventory

import android.util.Log

/**
 * Απλό inventory που αντιστοιχίζεται σε συγκεκριμένο ήρωα.
 */
class Inventory(
    val id: String,
    val heroId: String,
    var gold: Int,
    val capacity: Int,
    items: List<Item> = emptyList()
) {

    private val items: MutableList<Item> = items.take(capacity).toMutableList()

    fun addItem(item: Item): Boolean {
        if (item.stackable) {
            val index = items.indexOfFirst { it.id == item.id }
            if (index >= 0) {
                val existing = items[index]
                items[index] = existing.copy(quantity = existing.quantity + item.quantity)
                return true
            }
        }
        if (items.size >= capacity) return false
        items += item
        return true
    }

    fun removeItem(itemId: String): Boolean = items.removeIf { it.id == itemId }

    fun replaceAll(newItems: Collection<Item>) {
        items.clear()
        items.addAll(newItems.take(capacity))
    }

    fun getItemsByCategory(category: ItemCategory): List<Item> =
        items.filter { it.category == category }

    fun getAllItems(): List<Item> = items.toList()

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "heroId" to heroId,
        "inventoryId" to id,
        "gold" to gold,
        "capacity" to capacity,
        "items" to items.map(Item::toFirestoreMap)
    )

    fun snapshot(): InventorySnapshot = InventorySnapshot(
        id = id,
        heroId = heroId,
        gold = gold,
        capacity = capacity,
        items = getAllItems()
    )

    companion object {
        fun fromFirestore(
            inventoryId: String,
            data: Map<String, Any?>,
            defaultCapacity: Int
        ): Inventory {
            val heroId = data["heroId"]?.toString().orEmpty()
            val gold = (data["gold"] as? Number)?.toInt() ?: 0
            val capacity = (data["capacity"] as? Number)?.toInt() ?: defaultCapacity
            val items = (data["items"] as? List<*>)
                ?.mapNotNull { element ->
                    (element as? Map<String, Any?>)?.let { itemMap ->
                        val itemIdValue = itemMap["id"]?.toString().orEmpty()
                        runCatching { Item.fromFirestore(itemMap) }
                            .onFailure { error ->
                                Log.w(TAG, "Αποτυχία φόρτωσης item ${'$'}itemIdValue: ${'$'}{error.message}")
                            }
                            .getOrNull()
                    }
                }
                ?: emptyList()
            return Inventory(
                id = inventoryId,
                heroId = heroId,
                gold = gold,
                capacity = capacity,
                items = items
            )
        }
    }
}

private const val TAG = "Inventory"

data class InventorySnapshot(
    val id: String,
    val heroId: String,
    val gold: Int,
    val capacity: Int,
    val items: List<Item>
)
