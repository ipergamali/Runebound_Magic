package com.example.runeboundmagic.codex

import com.example.runeboundmagic.data.codex.local.CodexHeroEntity
import com.example.runeboundmagic.data.codex.local.CodexInventoryEntity
import com.example.runeboundmagic.data.codex.local.CodexInventoryItemEntity
import com.example.runeboundmagic.data.codex.local.CodexInventoryWithItems
import com.example.runeboundmagic.data.codex.local.HeroWithInventory
import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.inventory.Inventory
import com.example.runeboundmagic.inventory.InventoryItem

data class HeroProfile(
    val hero: Hero,
    val inventory: Inventory
)

fun HeroProfile.toHeroEntity(): CodexHeroEntity = CodexHeroEntity(
    heroId = hero.id,
    name = hero.name,
    description = hero.description,
    level = hero.level,
    heroClass = hero.classType,
    cardImage = hero.cardImage,
    inventoryId = hero.inventoryId
)

fun HeroProfile.toInventoryEntity(): CodexInventoryEntity = CodexInventoryEntity(
    inventoryId = inventory.id,
    heroId = hero.id,
    gold = inventory.gold,
    capacity = inventory.capacity
)

fun HeroProfile.toItemEntities(): List<CodexInventoryItemEntity> = inventory.getAllItems()
    .mapNotNull { item ->
        val concreteItem = item as? InventoryItem ?: return@mapNotNull null
        CodexInventoryItemEntity(
            inventoryId = inventory.id,
            itemId = concreteItem.id,
            name = concreteItem.name,
            description = concreteItem.description,
            icon = concreteItem.icon,
            rarity = concreteItem.rarity,
            category = concreteItem.category
        )
    }

fun HeroWithInventory.toDomain(): HeroProfile {
    val heroEntity = hero
    val hero = Hero(
        id = heroEntity.heroId,
        name = heroEntity.name,
        description = heroEntity.description,
        level = heroEntity.level,
        classType = heroEntity.heroClass,
        cardImage = heroEntity.cardImage,
        inventoryId = heroEntity.inventoryId
    )

    val inventoryRelation = inventory.firstOrNull()
    val domainInventory = inventoryRelation?.toDomainInventory(hero) ?: hero.createInventory()
    return HeroProfile(hero = hero, inventory = domainInventory)
}

private fun CodexInventoryWithItems.toDomainInventory(hero: Hero): Inventory {
    val items = items.map { entity ->
        InventoryItem(
            id = entity.itemId,
            name = entity.name,
            description = entity.description,
            icon = entity.icon,
            rarity = entity.rarity,
            category = entity.category
        )
    }
    return Inventory(
        id = inventory.inventoryId,
        heroId = hero.id,
        gold = inventory.gold,
        capacity = inventory.capacity,
        items = items
    )
}
