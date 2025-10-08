package com.example.runeboundmagic.data.codex.local

import androidx.room.TypeConverter
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.EquipmentSlot
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.ItemSubcategory
import com.example.runeboundmagic.inventory.Rarity

class CodexTypeConverters {
    @TypeConverter
    fun fromHeroClass(value: HeroClass): String = value.name

    @TypeConverter
    fun toHeroClass(value: String): HeroClass = runCatching {
        HeroClass.valueOf(value)
    }.getOrDefault(HeroClass.WARRIOR)

    @TypeConverter
    fun fromItemCategory(category: ItemCategory): String = category.name

    @TypeConverter
    fun toItemCategory(value: String): ItemCategory = runCatching {
        ItemCategory.valueOf(value)
    }.getOrDefault(ItemCategory.WEAPONS)

    @TypeConverter
    fun fromItemSubcategory(subcategory: ItemSubcategory?): String? = subcategory?.name

    @TypeConverter
    fun toItemSubcategory(value: String?): ItemSubcategory? = value
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { ItemSubcategory.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromEquipmentSlots(slots: List<EquipmentSlot>): String = slots.joinToString(separator = ",") { it.name }

    @TypeConverter
    fun toEquipmentSlots(value: String): List<EquipmentSlot> = value
        .takeIf { it.isNotBlank() }
        ?.split(",")
        ?.mapNotNull { token ->
            token.trim().takeIf { it.isNotEmpty() }
                ?.let { runCatching { EquipmentSlot.valueOf(it) }.getOrNull() }
        }
        ?: emptyList()

    @TypeConverter
    fun fromRarity(rarity: Rarity): String = rarity.name

    @TypeConverter
    fun toRarity(value: String): Rarity = runCatching {
        Rarity.valueOf(value)
    }.getOrDefault(Rarity.COMMON)
}
