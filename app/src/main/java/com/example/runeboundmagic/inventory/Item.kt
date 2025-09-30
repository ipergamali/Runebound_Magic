package com.example.runeboundmagic.inventory

/**
 * Βασική περιγραφή αντικειμένου που μπορεί να αποθηκευτεί σε inventory.
 */
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val rarity: Rarity,
    val category: ItemCategory
)

/**
 * Βαθμίδες σπανιότητας για εύκολη ομαδοποίηση αντικειμένων.
 */
enum class Rarity {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY
}
