package com.example.runeboundmagic

/**
 * Αντιπροσωπεύει τον τύπο του ήρωα που επιλέγει ο παίκτης.
 * Παρέχει βοηθητικούς μετασχηματισμούς για αντιστοίχιση με τα υπάρχοντα [HeroOption]
 * ώστε να επαναχρησιμοποιούμε τις ίδιες εικόνες και περιγραφές στο lobby.
 */
enum class HeroType {
    WARRIOR,
    HUNTER,
    MAGE,
    PRIEST
}

fun HeroType.toHeroOption(): HeroOption = when (this) {
    HeroType.WARRIOR -> HeroOption.WARRIOR
    HeroType.HUNTER -> HeroOption.RANGER
    HeroType.MAGE -> HeroOption.MAGE
    HeroType.PRIEST -> HeroOption.MYSTICAL_PRIESTESS
}

fun HeroOption.toHeroType(): HeroType = when (this) {
    HeroOption.WARRIOR -> HeroType.WARRIOR
    HeroOption.RANGER -> HeroType.HUNTER
    HeroOption.MYSTICAL_PRIESTESS -> HeroType.PRIEST
    HeroOption.MAGE -> HeroType.MAGE
}
