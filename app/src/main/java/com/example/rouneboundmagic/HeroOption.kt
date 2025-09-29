package com.example.rouneboundmagic

import androidx.annotation.StringRes

enum class HeroOption(
    @StringRes val displayNameRes: Int,
    val assetPath: String,
    @StringRes val descriptionRes: Int
) {
    WARRIOR(
        displayNameRes = R.string.hero_warrior,
        assetPath = "characters/warrior.png",
        descriptionRes = R.string.hero_warrior_desc
    ),
    MAGE(
        displayNameRes = R.string.hero_mage,
        assetPath = "characters/mage.png",
        descriptionRes = R.string.hero_mage_desc
    ),
    MYSTICAL_PRIESTESS(
        displayNameRes = R.string.hero_priestess,
        assetPath = "characters/mystical_priestess.png",
        descriptionRes = R.string.hero_priestess_desc
    ),
    RANGER(
        displayNameRes = R.string.hero_ranger,
        assetPath = "characters/ranger.png",
        descriptionRes = R.string.hero_ranger_desc
    );

    companion object {
        fun fromName(name: String?): HeroOption {
            return values().firstOrNull { it.name == name } ?: MYSTICAL_PRIESTESS
        }
    }
}
