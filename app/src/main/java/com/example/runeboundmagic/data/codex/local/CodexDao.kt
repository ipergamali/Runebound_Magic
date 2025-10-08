package com.example.runeboundmagic.data.codex.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CodexDao {

    @Transaction
    @Query("SELECT * FROM codex_heroes WHERE heroId = :heroId")
    suspend fun getHeroWithInventory(heroId: String): HeroWithInventory?

    @Transaction
    @Query("SELECT * FROM hero_cards WHERE heroId = :heroId LIMIT 1")
    suspend fun getHeroCard(heroId: String): HeroCardWithMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHero(hero: CodexHeroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(inventory: CodexInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<CodexInventoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHeroClass(metadata: HeroClassMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHeroCard(card: HeroCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRarities(entries: List<RarityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemCategories(entries: List<ItemCategoryEntity>)

    @Query("DELETE FROM codex_items WHERE inventoryId = :inventoryId")
    suspend fun clearItems(inventoryId: String)

    @Transaction
    suspend fun upsertHeroProfile(
        hero: CodexHeroEntity,
        inventory: CodexInventoryEntity,
        items: List<CodexInventoryItemEntity>
    ) {
        upsertHero(hero)
        upsertInventory(inventory)
        clearItems(inventory.inventoryId)
        if (items.isNotEmpty()) {
            upsertItems(items)
        }
    }
}
