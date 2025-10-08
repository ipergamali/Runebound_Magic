package com.example.runeboundmagic.data.codex.local

import androidx.room.TypeConverter
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.ItemCategory
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
    }.getOrDefault(ItemCategory.WEAPON)

    @TypeConverter
    fun fromRarity(rarity: Rarity): String = rarity.name

    @TypeConverter
    fun toRarity(value: String): Rarity = runCatching {
        Rarity.valueOf(value)
    }.getOrDefault(Rarity.COMMON)
}
