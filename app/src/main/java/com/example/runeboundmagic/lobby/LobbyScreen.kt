package com.example.runeboundmagic.lobby

import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.inventory.Inventory

/**
 * Απλή υλοποίηση του lobby όπου ο παίκτης επιλέγει ήρωα πριν τη μάχη.
 */
class LobbyScreen(
    private val heroes: List<Hero>
) {

    private var selectedHero: Hero? = null
    private var customName: String? = null

    fun showHeroCarousel(): List<HeroCard> = heroes.map(HeroCard::fromHero)

    fun enterHeroName(name: String) {
        customName = name.trim().takeIf { it.isNotEmpty() }
    }

    fun selectHero(heroId: String) {
        selectedHero = heroes.firstOrNull { it.id == heroId }
    }

    fun startBattle(): LobbySelection? = selectedHero?.let { hero ->
        val resolvedHero = customName?.let { hero.copy(name = it) } ?: hero
        LobbySelection(
            hero = resolvedHero,
            inventory = resolvedHero.createInventory()
        )
    }

    data class LobbySelection(
        val hero: Hero,
        val inventory: Inventory
    )
}
