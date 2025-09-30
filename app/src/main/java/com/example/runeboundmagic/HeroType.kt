package com.example.runeboundmagic

/**
 * Αντιπροσωπεύει τον τύπο του ήρωα που επιλέγει ο παίκτης.
 * Παρέχει βοηθητικούς μετασχηματισμούς για αντιστοίχιση με τα υπάρχοντα [HeroOption]
 * ώστε να επαναχρησιμοποιούμε τις ίδιες εικόνες και περιγραφές στο lobby.
 */
enum class HeroType {
    WARRIOR,
    RANGER,
    PRIESTESS,
    MAGE,
    BLACK_MAGE
}

fun HeroType.toHeroOption(): HeroOption = when (this) {
    HeroType.WARRIOR -> HeroOption.WARRIOR
    HeroType.RANGER -> HeroOption.RANGER
    HeroType.PRIESTESS -> HeroOption.MYSTICAL_PRIESTESS
    HeroType.MAGE, HeroType.BLACK_MAGE -> HeroOption.MAGE
}

fun HeroOption.toHeroType(): HeroType = when (this) {
    HeroOption.WARRIOR -> HeroType.WARRIOR
    HeroOption.RANGER -> HeroType.RANGER
    HeroOption.MYSTICAL_PRIESTESS -> HeroType.PRIESTESS
    HeroOption.MAGE -> HeroType.MAGE
}
