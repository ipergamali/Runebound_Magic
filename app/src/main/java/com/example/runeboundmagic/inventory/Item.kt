package com.example.runeboundmagic.inventory

import kotlin.math.max

/**
 * Περιγράφει ένα αντικείμενο του inventory με αυστηρή κατηγοριοποίηση.
 */
data class Item(
    val id: String,
    val name: String,
    val description: String = "",
    val iconPath: String,
    val category: ItemCategory,
    val subcategory: ItemSubcategory? = null,
    val rarity: Rarity = Rarity.COMMON,
    val stackable: Boolean = false,
    val quantity: Int = 1,
    val allowedSlots: List<EquipmentSlot> = emptyList(),
    val weaponStats: WeaponStats? = null
) {

    init {
        require(id.isNotBlank()) { "Item id δεν μπορεί να είναι κενό" }
        require(name.isNotBlank()) { "Item name δεν μπορεί να είναι κενό" }
        if (category.requiresIcon()) {
            require(iconPath.isNotBlank()) { "Icon path είναι υποχρεωτικό για την κατηγορία $category" }
        }
        if (subcategory != null) {
            require(subcategory.parent == category) {
                "Η υποκατηγορία $subcategory δεν ανήκει στην κατηγορία $category"
            }
        }
        if (stackable) {
            require(category in STACKABLE_CATEGORIES) {
                "Μόνο οι κατηγορίες $STACKABLE_CATEGORIES μπορούν να κάνουν stack"
            }
            require(quantity > 0) { "Το quantity πρέπει να είναι θετικό" }
        }
        if (!stackable) {
            require(quantity == 1) { "Τα μη stackable αντικείμενα πρέπει να έχουν quantity = 1" }
        }
        validateAllowedSlots()
        if (weaponStats != null) {
            require(category == ItemCategory.WEAPONS) { "Τα weaponStats επιτρέπονται μόνο σε weapons" }
        }
    }

    private fun validateAllowedSlots() {
        if (allowedSlots.isEmpty()) {
            require(category !in EQUIPPABLE_CATEGORIES) {
                "Η κατηγορία $category απαιτεί τουλάχιστον ένα equipment slot"
            }
            return
        }
        when (category) {
            ItemCategory.WEAPONS -> require(allowedSlots.all { it == EquipmentSlot.MAIN_HAND || it == EquipmentSlot.OFF_HAND }) {
                "Όπλα μπορούν να τοποθετηθούν μόνο σε main/off hand"
            }
            ItemCategory.ARMOR -> require(allowedSlots.all { it in ARMOR_SLOTS }) {
                "Η πανοπλία πρέπει να αντιστοιχεί σε έγκυρο armor slot"
            }
            ItemCategory.SHIELDS -> require(allowedSlots == listOf(EquipmentSlot.OFF_HAND)) {
                "Οι ασπίδες επιτρέπονται μόνο στο off hand"
            }
            ItemCategory.ACCESSORIES -> require(allowedSlots.all { it in ACCESSORY_SLOTS }) {
                "Τα αξεσουάρ μπαίνουν μόνο σε accessory slots"
            }
            else -> require(allowedSlots.isEmpty()) {
                "Η κατηγορία $category δεν υποστηρίζει equipment slots"
            }
        }
    }

    fun toFirestoreMap(): Map<String, Any> = buildMap {
        put("id", id)
        put("name", name)
        put("description", description)
        put("iconPath", iconPath)
        put("category", category.name)
        subcategory?.let { put("subcategory", it.name) }
        put("rarity", rarity.name)
        put("stackable", stackable)
        put("quantity", quantity)
        if (allowedSlots.isNotEmpty()) {
            put("allowedSlots", allowedSlots.map(EquipmentSlot::name))
        }
        weaponStats?.let { stats ->
            put("weaponStats", mapOf(
                "damage" to stats.damage,
                "element" to stats.element,
                "attackSpeed" to stats.attackSpeed
            ))
        }
    }

    companion object {
        private val STACKABLE_CATEGORIES = setOf(
            ItemCategory.CONSUMABLES,
            ItemCategory.RUNES_GEMS,
            ItemCategory.CRAFTING_MATERIALS,
            ItemCategory.GOLD_CURRENCY
        )
        private val EQUIPPABLE_CATEGORIES = setOf(
            ItemCategory.WEAPONS,
            ItemCategory.ARMOR,
            ItemCategory.SHIELDS,
            ItemCategory.ACCESSORIES
        )
        private val ARMOR_SLOTS = setOf(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.HANDS,
            EquipmentSlot.FEET,
            EquipmentSlot.CLOAK
        )
        private val ACCESSORY_SLOTS = setOf(
            EquipmentSlot.RING1,
            EquipmentSlot.RING2,
            EquipmentSlot.AMULET,
            EquipmentSlot.BELT
        )

        fun fromFirestore(data: Map<String, Any?>): Item {
            val id = data["id"]?.toString()?.takeIf { it.isNotBlank() }
                ?: error("ITEM_ID_MISSING")
            val name = data["name"]?.toString()?.ifBlank { null }
                ?: error("ITEM_NAME_MISSING")
            val categoryName = data["category"]?.toString()?.uppercase()
                ?: error("ITEM_CATEGORY_MISSING")
            val category = runCatching { ItemCategory.valueOf(categoryName) }
                .getOrElse { error("ITEM_CATEGORY_INVALID") }
            val iconPath = data["iconPath"]?.toString().orEmpty()
            val description = data["description"]?.toString().orEmpty()
            val rarityName = data["rarity"]?.toString()?.uppercase() ?: Rarity.COMMON.name
            val rarity = runCatching { Rarity.valueOf(rarityName) }.getOrDefault(Rarity.COMMON)
            val subcategory = data["subcategory"]?.toString()?.uppercase()?.let { value ->
                runCatching { ItemSubcategory.valueOf(value) }
                    .getOrNull()
            }
            val stackable = data["stackable"] as? Boolean ?: false
            val quantity = (data["quantity"] as? Number)?.toInt() ?: 1
            val slotNames = (data["allowedSlots"] as? List<*>)
                ?.mapNotNull { element ->
                    (element as? String)
                        ?.uppercase()
                        ?.let { runCatching { EquipmentSlot.valueOf(it) }.getOrNull() }
                }
                ?: emptyList()
            val weaponStats = (data["weaponStats"] as? Map<*, *>)?.let { map ->
                val damage = (map["damage"] as? Number)?.toInt()
                val element = map["element"]?.toString()
                val attackSpeed = (map["attackSpeed"] as? Number)?.toFloat()
                if (damage != null && element != null && attackSpeed != null) {
                    WeaponStats(damage = damage, element = element, attackSpeed = attackSpeed)
                } else {
                    null
                }
            }

            return Item(
                id = id,
                name = name,
                description = description,
                iconPath = iconPath,
                category = category,
                subcategory = subcategory,
                rarity = rarity,
                stackable = stackable,
                quantity = max(1, quantity),
                allowedSlots = slotNames,
                weaponStats = weaponStats
            )
        }
    }
}

/**
 * Πληροφορίες επιπλέον για όπλα.
 */
data class WeaponStats(
    val damage: Int,
    val element: String,
    val attackSpeed: Float
)

private fun ItemCategory.requiresIcon(): Boolean = when (this) {
    ItemCategory.WEAPONS,
    ItemCategory.ARMOR,
    ItemCategory.SHIELDS,
    ItemCategory.ACCESSORIES,
    ItemCategory.CONSUMABLES,
    ItemCategory.SPELLS_SCROLLS -> true

    ItemCategory.RUNES_GEMS,
    ItemCategory.CRAFTING_MATERIALS,
    ItemCategory.QUEST_ITEMS,
    ItemCategory.GOLD_CURRENCY -> false
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

val Item.icon: String
    get() = iconPath
