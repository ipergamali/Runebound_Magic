package com.example.runeboundmagic.heroes

/**
 * Κλάσεις ηρώων με απλό περιγραφικό κείμενο για το lobby.
 */
enum class HeroClass(val description: String) {
    WARRIOR("Δυνατός μαχητής σώμα με σώμα."),
    MAGE("Κυρίαρχος των ρούνων και της μαγείας."),
    RANGER("Ευκίνητος τοξότης με κρυφές τακτικές."),
    PRIESTESS("Θεραπεύτρια και σύμμαχος του φωτός.");
}
