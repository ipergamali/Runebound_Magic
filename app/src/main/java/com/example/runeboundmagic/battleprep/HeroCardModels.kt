package com.example.runeboundmagic.battleprep

import androidx.compose.ui.graphics.Color
import com.example.runeboundmagic.data.codex.local.HeroCardEntity
import com.example.runeboundmagic.data.codex.local.HeroCardWithMetadata
import com.example.runeboundmagic.data.codex.local.HeroClassMetadataEntity
import com.example.runeboundmagic.data.codex.local.ItemCategoryEntity
import com.example.runeboundmagic.data.codex.local.RarityEntity
import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.heroes.HeroClass

/**
 * Βασικά στατιστικά που εμφανίζονται στην κάρτα ήρωα.
 */
data class BaseStats(
    val strength: Int,
    val agility: Int,
    val intellect: Int,
    val faith: Int
)

/**
 * Περιγραφή κλάσης ήρωα για χρήση στο UI και στην τοπική βάση.
 */
data class HeroClassMetadata(
    val id: HeroClass,
    val name: String,
    val weaponProficiency: String,
    val armorProficiency: String
)

/**
 * Στοιχεία σπανιότητας.
 */
data class RarityMetadata(
    val id: String,
    val displayName: String,
    val colorHex: String
) {
    val color: Color
        get() = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
            .getOrDefault(Color(0xFFBDBDBD))
}

/**
 * Αναλυτικά στοιχεία για την κάρτα ήρωα του Battle Preparation.
 */
data class HeroCardDetails(
    val hero: Hero,
    val heroDescription: String,
    val heroClassMetadata: HeroClassMetadata,
    val rarity: RarityMetadata,
    val baseStats: BaseStats
)

/**
 * Κατηγορία inventory με σύντομη περιγραφή.
 */
data class InventoryCategoryInfo(
    val id: String,
    val title: String,
    val description: String
)

fun HeroClassMetadata.toEntity(): HeroClassMetadataEntity = HeroClassMetadataEntity(
    heroClassId = id.name,
    name = name,
    weaponProficiency = weaponProficiency,
    armorProficiency = armorProficiency
)

fun RarityMetadata.toEntity(): RarityEntity = RarityEntity(
    rarityId = id,
    displayName = displayName,
    colorHex = colorHex
)

fun InventoryCategoryInfo.toEntity(): ItemCategoryEntity = ItemCategoryEntity(
    itemCategoryId = id,
    displayName = title,
    description = description,
    slotType = id
)

fun HeroCardWithMetadata.toDomain(hero: Hero, description: String): HeroCardDetails {
    val heroClassMetadata = HeroClassMetadata(
        id = runCatching { HeroClass.valueOf(heroClass.heroClassId) }.getOrDefault(hero.classType),
        name = heroClass.name,
        weaponProficiency = heroClass.weaponProficiency,
        armorProficiency = heroClass.armorProficiency
    )
    val rarity = rarity?.let {
        RarityMetadata(
            id = it.rarityId,
            displayName = it.displayName,
            colorHex = it.colorHex
        )
    } ?: RarityMetadata(id = "COMMON", displayName = "Common", colorHex = "#BDBDBD")
    return HeroCardDetails(
        hero = hero.copy(cardImage = card.cardImage),
        heroDescription = description,
        heroClassMetadata = heroClassMetadata,
        rarity = rarity,
        baseStats = BaseStats(
            strength = card.strength,
            agility = card.agility,
            intellect = card.intellect,
            faith = card.faith
        )
    )
}

fun HeroCardDetails.toEntity(cardId: String): HeroCardEntity = HeroCardEntity(
    heroCardId = cardId,
    heroId = hero.id,
    heroClassId = heroClassMetadata.id.name,
    heroName = hero.name,
    cardImage = hero.cardImage,
    strength = baseStats.strength,
    agility = baseStats.agility,
    intellect = baseStats.intellect,
    faith = baseStats.faith,
    rarityId = rarity.id
)
