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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHero(hero: CodexHeroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(inventory: CodexInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<CodexInventoryItemEntity>)

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
