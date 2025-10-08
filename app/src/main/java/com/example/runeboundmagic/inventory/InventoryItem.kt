package com.example.runeboundmagic.inventory

typealias InventoryItem = Item

fun InventoryItem(
    id: String,
    name: String,
    description: String,
    icon: String,
    rarity: Rarity,
    category: ItemCategory,
    subcategory: ItemSubcategory? = null,
    stackable: Boolean = false,
    quantity: Int = 1,
    allowedSlots: List<EquipmentSlot>? = null,
    weaponStats: WeaponStats? = null
): InventoryItem {
    val resolvedSubcategory = subcategory ?: inferSubcategory(category, icon, name)
    val resolvedSlots = allowedSlots ?: defaultSlots(category, resolvedSubcategory)
    val sanitizedStackable = if (stackable) {
        require(category in STACKABLE_CATEGORIES) {
            "Η κατηγορία $category δεν επιτρέπει stackable αντικείμενα"
        }
        true
    } else {
        false
    }
    val sanitizedQuantity = if (sanitizedStackable) quantity.coerceAtLeast(1) else 1
    return Item(
        id = id,
        name = name,
        description = description,
        iconPath = icon,
        category = category,
        subcategory = resolvedSubcategory,
        rarity = rarity,
        stackable = sanitizedStackable,
        quantity = sanitizedQuantity,
        allowedSlots = resolvedSlots,
        weaponStats = weaponStats
    )
}

private val STACKABLE_CATEGORIES = setOf(
    ItemCategory.CONSUMABLES,
    ItemCategory.RUNES_GEMS,
    ItemCategory.CRAFTING_MATERIALS,
    ItemCategory.GOLD_CURRENCY
)

private fun inferSubcategory(category: ItemCategory, icon: String, name: String): ItemSubcategory? {
    val normalizedIcon = icon.lowercase()
    val normalizedName = name.lowercase()
    return when (category) {
        ItemCategory.WEAPONS -> when {
            "crossbow" in normalizedIcon || "crossbow" in normalizedName -> ItemSubcategory.CROSSBOW
            "rod" in normalizedIcon || "rod" in normalizedName -> ItemSubcategory.ROD
            "staff" in normalizedIcon || "staff" in normalizedName -> ItemSubcategory.STAFF
            "dagger" in normalizedIcon || "dagger" in normalizedName -> ItemSubcategory.DAGGER
            "axe" in normalizedIcon || "axe" in normalizedName -> ItemSubcategory.AXE
            "bow" in normalizedIcon || "bow" in normalizedName -> ItemSubcategory.BOW
            else -> ItemSubcategory.SWORD
        }
        ItemCategory.ARMOR -> when {
            "helm" in normalizedIcon || "helmet" in normalizedName -> ItemSubcategory.HELMET
            "glove" in normalizedIcon || "glove" in normalizedName -> ItemSubcategory.GLOVES
            "boot" in normalizedIcon || "boot" in normalizedName -> ItemSubcategory.BOOTS
            "cloak" in normalizedIcon || "cloak" in normalizedName -> ItemSubcategory.CLOAK
            "robe" in normalizedIcon || "robe" in normalizedName -> ItemSubcategory.ROBES
            "shirt" in normalizedIcon || "shirt" in normalizedName -> ItemSubcategory.SHIRT
            else -> ItemSubcategory.CHEST
        }
        ItemCategory.SHIELDS -> when {
            "buckler" in normalizedName -> ItemSubcategory.BUCKLER
            "tower" in normalizedName -> ItemSubcategory.TOWER
            else -> ItemSubcategory.MAGIC_BARRIER
        }
        ItemCategory.ACCESSORIES -> when {
            "ring" in normalizedName -> ItemSubcategory.RING
            "amulet" in normalizedName || "talisman" in normalizedName -> ItemSubcategory.AMULET
            "belt" in normalizedName -> ItemSubcategory.BELT
            else -> ItemSubcategory.CHARM
        }
        ItemCategory.CONSUMABLES -> when {
            "mana" in normalizedName -> ItemSubcategory.POTION_MANA
            "potion" in normalizedName -> ItemSubcategory.POTION_HEALTH
            "elixir" in normalizedName -> ItemSubcategory.ELIXIR
            else -> ItemSubcategory.FOOD
        }
        ItemCategory.SPELLS_SCROLLS -> if ("scroll" in normalizedName) ItemSubcategory.SCROLL else ItemSubcategory.SPELL
        ItemCategory.RUNES_GEMS -> if ("gem" in normalizedName) ItemSubcategory.GEM else ItemSubcategory.RUNE
        ItemCategory.CRAFTING_MATERIALS -> when {
            "ore" in normalizedName -> ItemSubcategory.ORE
            "herb" in normalizedName -> ItemSubcategory.HERB
            "leather" in normalizedName -> ItemSubcategory.LEATHER
            "wood" in normalizedName -> ItemSubcategory.WOOD
            else -> ItemSubcategory.ESSENCE
        }
        ItemCategory.QUEST_ITEMS -> when {
            "key" in normalizedName -> ItemSubcategory.KEY
            "map" in normalizedName -> ItemSubcategory.MAP
            else -> ItemSubcategory.ARTIFACT
        }
        ItemCategory.GOLD_CURRENCY -> when {
            "token" in normalizedName -> ItemSubcategory.TOKEN
            "shard" in normalizedName -> ItemSubcategory.SHARD
            else -> ItemSubcategory.GOLD
        }
    }
}

private fun defaultSlots(
    category: ItemCategory,
    subcategory: ItemSubcategory?
): List<EquipmentSlot> = when (category) {
    ItemCategory.WEAPONS -> when (subcategory) {
        ItemSubcategory.DAGGER -> listOf(EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND)
        else -> listOf(EquipmentSlot.MAIN_HAND)
    }
    ItemCategory.ARMOR -> when (subcategory) {
        ItemSubcategory.HELMET -> listOf(EquipmentSlot.HEAD)
        ItemSubcategory.GLOVES -> listOf(EquipmentSlot.HANDS)
        ItemSubcategory.BOOTS -> listOf(EquipmentSlot.FEET)
        ItemSubcategory.CLOAK -> listOf(EquipmentSlot.CLOAK)
        else -> listOf(EquipmentSlot.CHEST)
    }
    ItemCategory.SHIELDS -> listOf(EquipmentSlot.OFF_HAND)
    ItemCategory.ACCESSORIES -> when (subcategory) {
        ItemSubcategory.RING -> listOf(EquipmentSlot.RING1, EquipmentSlot.RING2)
        ItemSubcategory.AMULET, ItemSubcategory.CHARM -> listOf(EquipmentSlot.AMULET)
        ItemSubcategory.BELT -> listOf(EquipmentSlot.BELT)
        else -> emptyList()
    }
    else -> emptyList()
}
