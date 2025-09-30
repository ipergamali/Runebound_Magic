package com.example.runeboundmagic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runeboundmagic.HeroType

@Entity(tableName = "lobby_interactions")
data class LobbyInteractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val heroType: HeroType,
    val heroDisplayName: String,
    val heroNameInput: String,
    val timestamp: Long
)
