package com.example.runeboundmagic.inventory

/**
 * Αφηρημένος τύπος που περιγράφει ένα αντικείμενο του Codex.
 * Παρέχεται κλάση υλοποίησης [InventoryItem] για τις πιο συχνές περιπτώσεις.
 */
abstract class Item {
    abstract val id: String
    abstract val name: String
    abstract val description: String
    abstract val icon: String
    abstract val rarity: Rarity
    abstract val category: ItemCategory

    open fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "icon" to icon,
        "rarity" to rarity.name,
        "category" to category.name
    )

    companion object {
        fun fromFirestore(data: Map<String, Any?>): Item? {
            val id = data["id"]?.toString()?.takeIf { it.isNotBlank() } ?: return null
            val name = data["name"]?.toString().orEmpty()
            val description = data["description"]?.toString().orEmpty()
            val icon = data["icon"]?.toString().orEmpty()
            val rarityName = data["rarity"]?.toString()?.uppercase() ?: Rarity.COMMON.name
            val categoryName = data["category"]?.toString()?.uppercase() ?: ItemCategory.WEAPON.name
            val rarity = runCatching { Rarity.valueOf(rarityName) }.getOrDefault(Rarity.COMMON)
            val category = runCatching { ItemCategory.valueOf(categoryName) }.getOrDefault(ItemCategory.WEAPON)
            val damage = (data["damage"] as? Number)?.toInt()
            val attackSpeed = (data["attackSpeed"] as? Number)?.toDouble()?.toFloat()
            val element = data["element"]?.toString()
            return if (category == ItemCategory.WEAPON && damage != null && attackSpeed != null && element != null) {
                WeaponItem(
                    id = id,
                    name = name,
                    description = description,
                    icon = icon,
                    rarity = rarity,
                    damage = damage,
                    element = element,
                    attackSpeed = attackSpeed
                )
            } else {
                InventoryItem(
                    id = id,
                    name = name,
                    description = description,
                    icon = icon,
                    rarity = rarity,
                    category = category
                )
            }
        }
    }
}

/**
 * Απλή υλοποίηση αντικειμένου inventory.
 */
data class InventoryItem(
    override val id: String,
    override val name: String,
    override val description: String,
    override val icon: String,
    override val rarity: Rarity,
    override val category: ItemCategory
) : Item() {
    override fun toFirestoreMap(): Map<String, Any> = super.toFirestoreMap() + ("type" to "ITEM")
}

/**
 * Εξειδικευμένο όπλο με πρόσθετες ιδιότητες για τη μάχη.
 */
data class WeaponItem(
    override val id: String,
    override val name: String,
    override val description: String,
    override val icon: String,
    override val rarity: Rarity,
    val damage: Int,
    val element: String,
    val attackSpeed: Float
) : Item() {
    override val category: ItemCategory = ItemCategory.WEAPON

    override fun toFirestoreMap(): Map<String, Any> = super.toFirestoreMap() + mapOf(
        "type" to "WEAPON",
        "damage" to damage,
        "element" to element,
        "attackSpeed" to attackSpeed
    )
}

/**
 * Βαθμίδες σπανιότητας για εύκολη ομαδοποίηση αντικειμένων.
 */
enum class Rarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}
