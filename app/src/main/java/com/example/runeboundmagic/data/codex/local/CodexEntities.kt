package com.example.runeboundmagic.data.codex.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.Rarity

@Entity(tableName = "codex_heroes")
data class CodexHeroEntity(
    @PrimaryKey val heroId: String,
    val name: String,
    val description: String,
    val level: Int,
    val heroClass: HeroClass,
    val cardImage: String,
    val inventoryId: String
)

@Entity(
    tableName = "codex_inventories",
    foreignKeys = [
        ForeignKey(
            entity = CodexHeroEntity::class,
            parentColumns = ["heroId"],
            childColumns = ["heroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["heroId"], unique = true)]
)
data class CodexInventoryEntity(
    @PrimaryKey val inventoryId: String,
    val heroId: String,
    val gold: Int,
    val capacity: Int
)

@Entity(
    tableName = "codex_items",
    foreignKeys = [
        ForeignKey(
            entity = CodexInventoryEntity::class,
            parentColumns = ["inventoryId"],
            childColumns = ["inventoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["inventoryId", "itemId"],
    indices = [Index(value = ["itemId"])]
)
data class CodexInventoryItemEntity(
    val inventoryId: String,
    val itemId: String,
    val name: String,
    val description: String,
    val icon: String,
    val rarity: Rarity,
    val category: ItemCategory
)

data class CodexInventoryWithItems(
    @Embedded val inventory: CodexInventoryEntity,
    @Relation(
        parentColumn = "inventoryId",
        entityColumn = "inventoryId"
    )
    val items: List<CodexInventoryItemEntity>
)

data class HeroWithInventory(
    @Embedded val hero: CodexHeroEntity,
    @Relation(
        entity = CodexInventoryEntity::class,
        parentColumn = "inventoryId",
        entityColumn = "inventoryId"
    )
    val inventory: List<CodexInventoryWithItems>
)
