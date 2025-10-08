package com.example.runeboundmagic.data.codex.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.EquipmentSlot
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.ItemSubcategory
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

@Entity(tableName = "hero_classes")
data class HeroClassMetadataEntity(
    @PrimaryKey val heroClassId: String,
    val name: String,
    val weaponProficiency: String,
    val armorProficiency: String
)

@Entity(tableName = "rarities")
data class RarityEntity(
    @PrimaryKey val rarityId: String,
    val displayName: String,
    val colorHex: String
)

@Entity(tableName = "item_categories")
data class ItemCategoryEntity(
    @PrimaryKey val itemCategoryId: String,
    val displayName: String,
    val description: String,
    val slotType: String
)

@Entity(
    tableName = "hero_cards",
    foreignKeys = [
        ForeignKey(
            entity = CodexHeroEntity::class,
            parentColumns = ["heroId"],
            childColumns = ["heroId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HeroClassMetadataEntity::class,
            parentColumns = ["heroClassId"],
            childColumns = ["heroClassId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = RarityEntity::class,
            parentColumns = ["rarityId"],
            childColumns = ["rarityId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("heroId", unique = true),
        Index("heroClassId"),
        Index("rarityId")
    ]
)
data class HeroCardEntity(
    @PrimaryKey val heroCardId: String,
    val heroId: String,
    val heroClassId: String,
    val heroName: String,
    val cardImage: String,
    val strength: Int,
    val agility: Int,
    val intellect: Int,
    val faith: Int,
    val rarityId: String
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
    val category: ItemCategory,
    val subcategory: ItemSubcategory? = null,
    val stackable: Boolean = false,
    val quantity: Int = 1,
    val allowedSlots: List<EquipmentSlot> = emptyList(),
    val rarityId: String,
    val damage: Int? = null,
    val element: String? = null,
    val attackSpeed: Float? = null
)

data class HeroCardWithMetadata(
    @Embedded val card: HeroCardEntity,
    @Relation(
        parentColumn = "heroClassId",
        entityColumn = "heroClassId"
    )
    val heroClass: HeroClassMetadataEntity,
    @Relation(
        parentColumn = "rarityId",
        entityColumn = "rarityId"
    )
    val rarity: RarityEntity?
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
