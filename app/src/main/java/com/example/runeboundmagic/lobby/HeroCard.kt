package com.example.runeboundmagic.lobby

import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.heroes.HeroClass

/**
 * Απλή αναπαράσταση κάρτας ήρωα που εμφανίζεται στο lobby.
 */
data class HeroCard(
    val heroId: String,
    val name: String,
    val classType: HeroClass,
    val cardImage: String
) {
    companion object {
        fun fromHero(hero: Hero): HeroCard = HeroCard(
            heroId = hero.id,
            name = hero.name,
            classType = hero.classType,
            cardImage = hero.cardImage
        )
    }
}
