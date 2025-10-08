package com.example.runeboundmagic.data.local

import androidx.room.TypeConverter
import com.example.runeboundmagic.HeroType

class HeroTypeConverter {
    @TypeConverter
    fun fromHeroType(heroType: HeroType): String = heroType.name

    @TypeConverter
    fun toHeroType(value: String): HeroType = runCatching {
        HeroType.valueOf(value)
    }.getOrDefault(HeroType.PRIEST)
}
