package com.example.runeboundmagic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runeboundmagic.HeroType

@Entity(tableName = "hero_choices")
data class HeroChoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val heroType: HeroType,
    val heroName: String,
    val timestamp: Long
)
