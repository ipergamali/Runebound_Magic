package com.example.runeboundmagic.codex

import java.util.Date

/**
 * Περιγραφικά μοντέλα για το Codex του Runebound. Απλά data classes
 * ώστε να είναι εύκολα testable και να χαρτογραφούν ευθέως τα έγγραφα Firestore.
 */

enum class CodexCategory {
    HERO,
    CARD,
    CAMPAIGN,
    LORE
}

/**
 * Βασική οντότητα για κάθε εγγραφή στο Codex.
 */
data class CodexEntry(
    val id: String = "",
    val category: CodexCategory = CodexCategory.HERO,
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val abilities: List<String> = emptyList(),
    val rarity: String? = null,
    val role: String? = null,
    val tags: List<String> = emptyList()
)

/**
 * Καταγραφή προόδου παίκτη η οποία αποθηκεύεται στο Firestore.
 */
data class PlayerProgress(
    val id: String = "",
    val playerId: String = "",
    val heroId: String = "",
    val level: Int = 1,
    val inventory: List<String> = emptyList(),
    val lastUpdated: Date = Date()
)

/**
 * Επίτευγμα που αποθηκεύεται στο cloud.
 */
data class Achievement(
    val id: String = "",
    val playerId: String = "",
    val title: String = "",
    val description: String = "",
    val unlockedAt: Date = Date()
)

/**
 * Bookmark/Favorite εγγραφής Codex για γρήγορη πρόσβαση.
 */
data class FavoriteEntry(
    val entryId: String = "",
    val playerId: String = "",
    val createdAt: Date = Date()
)

/**
 * Απλή περιγραφή φίλτρων που εφαρμόζονται στο UI.
 */
data class CodexFilter(
    val selectedCategory: CodexCategory? = null,
    val searchQuery: String = ""
)

/**
 * State αντικείμενο για την οθόνη του Codex.
 */
data class CodexUiState(
    val isLoading: Boolean = true,
    val entries: List<CodexEntry> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val filter: CodexFilter = CodexFilter(),
    val selectedEntry: CodexEntry? = null,
    val achievements: List<Achievement> = emptyList(),
    val progress: List<PlayerProgress> = emptyList()
) {
    val filteredEntries: List<CodexEntry>
        get() {
            val lowerQuery = filter.searchQuery.trim().lowercase()
            return entries.filter { entry ->
                val matchesCategory = filter.selectedCategory?.let { entry.category == it } ?: true
                val matchesQuery = lowerQuery.isBlank() ||
                    entry.name.lowercase().contains(lowerQuery) ||
                    entry.description.lowercase().contains(lowerQuery) ||
                    entry.tags.any { it.lowercase().contains(lowerQuery) }
                matchesCategory && matchesQuery
            }
        }
}
