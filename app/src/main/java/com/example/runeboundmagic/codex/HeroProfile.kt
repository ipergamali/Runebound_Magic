package com.example.runeboundmagic.codex

import com.example.runeboundmagic.data.codex.local.CodexHeroEntity
import com.example.runeboundmagic.data.codex.local.CodexInventoryEntity
import com.example.runeboundmagic.data.codex.local.CodexInventoryItemEntity
import com.example.runeboundmagic.data.codex.local.CodexInventoryWithItems
import com.example.runeboundmagic.data.codex.local.HeroWithInventory
import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.inventory.Inventory
import com.example.runeboundmagic.inventory.InventoryItem
import com.example.runeboundmagic.inventory.Item
import com.example.runeboundmagic.inventory.WeaponItem

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
    .map { item -> item.toEntity(inventoryId = inventory.id) }

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
        if (entity.category == com.example.runeboundmagic.inventory.ItemCategory.WEAPON &&
            entity.damage != null &&
            entity.attackSpeed != null &&
            entity.element != null
        ) {
            WeaponItem(
                id = entity.itemId,
                name = entity.name,
                description = entity.description,
                icon = entity.icon,
                rarity = entity.rarity,
                damage = entity.damage,
                element = entity.element,
                attackSpeed = entity.attackSpeed
            )
        } else {
            InventoryItem(
                id = entity.itemId,
                name = entity.name,
                description = entity.description,
                icon = entity.icon,
                rarity = entity.rarity,
                category = entity.category
            )
        }
    }
    return Inventory(
        id = inventory.inventoryId,
        heroId = hero.id,
        gold = inventory.gold,
        capacity = inventory.capacity,
        items = items
    )
}

private fun Item.toEntity(inventoryId: String): CodexInventoryItemEntity = when (this) {
    is WeaponItem -> CodexInventoryItemEntity(
        inventoryId = inventoryId,
        itemId = id,
        name = name,
        description = description,
        icon = icon,
        rarity = rarity,
        category = category,
        rarityId = rarity.name,
        damage = damage,
        element = element,
        attackSpeed = attackSpeed
    )

    is InventoryItem -> CodexInventoryItemEntity(
        inventoryId = inventoryId,
        itemId = id,
        name = name,
        description = description,
        icon = icon,
        rarity = rarity,
        category = category,
        rarityId = rarity.name
    )

    else -> CodexInventoryItemEntity(
        inventoryId = inventoryId,
        itemId = id,
        name = name,
        description = description,
        icon = icon,
        rarity = rarity,
        category = category,
        rarityId = rarity.name
    )
}
