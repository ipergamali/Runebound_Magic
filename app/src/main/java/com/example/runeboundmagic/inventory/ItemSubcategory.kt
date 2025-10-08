package com.example.runeboundmagic.inventory

/**
 * Διακριτές υποκατηγορίες ανά βασική κατηγορία.
 */
enum class ItemSubcategory(val parent: ItemCategory) {
    SWORD(ItemCategory.WEAPONS),
    AXE(ItemCategory.WEAPONS),
    CROSSBOW(ItemCategory.WEAPONS),
    ROD(ItemCategory.WEAPONS),
    DAGGER(ItemCategory.WEAPONS),
    STAFF(ItemCategory.WEAPONS),
    BOW(ItemCategory.WEAPONS),

    HELMET(ItemCategory.ARMOR),
    CHEST(ItemCategory.ARMOR),
    GLOVES(ItemCategory.ARMOR),
    BOOTS(ItemCategory.ARMOR),
    CLOAK(ItemCategory.ARMOR),
    ROBES(ItemCategory.ARMOR),
    SHIRT(ItemCategory.ARMOR),

    BUCKLER(ItemCategory.SHIELDS),
    TOWER(ItemCategory.SHIELDS),
    MAGIC_BARRIER(ItemCategory.SHIELDS),

    RING(ItemCategory.ACCESSORIES),
    AMULET(ItemCategory.ACCESSORIES),
    BELT(ItemCategory.ACCESSORIES),
    CHARM(ItemCategory.ACCESSORIES),

    POTION_HEALTH(ItemCategory.CONSUMABLES),
    POTION_MANA(ItemCategory.CONSUMABLES),
    ELIXIR(ItemCategory.CONSUMABLES),
    FOOD(ItemCategory.CONSUMABLES),

    SPELL(ItemCategory.SPELLS_SCROLLS),
    SCROLL(ItemCategory.SPELLS_SCROLLS),

    RUNE(ItemCategory.RUNES_GEMS),
    GEM(ItemCategory.RUNES_GEMS),

    ORE(ItemCategory.CRAFTING_MATERIALS),
    HERB(ItemCategory.CRAFTING_MATERIALS),
    ESSENCE(ItemCategory.CRAFTING_MATERIALS),
    LEATHER(ItemCategory.CRAFTING_MATERIALS),
    WOOD(ItemCategory.CRAFTING_MATERIALS),

    KEY(ItemCategory.QUEST_ITEMS),
    MAP(ItemCategory.QUEST_ITEMS),
    ARTIFACT(ItemCategory.QUEST_ITEMS),

    GOLD(ItemCategory.GOLD_CURRENCY),
    TOKEN(ItemCategory.GOLD_CURRENCY),
    SHARD(ItemCategory.GOLD_CURRENCY);
}
