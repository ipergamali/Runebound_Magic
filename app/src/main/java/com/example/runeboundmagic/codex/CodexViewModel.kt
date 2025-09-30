package com.example.runeboundmagic.codex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class CodexViewModel(
    private val repository: CodexRepository,
    private val playerId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodexUiState())
    val uiState: StateFlow<CodexUiState> = _uiState.asStateFlow()

    init {
        observeCodex()
        observeFavorites()
        observeProgress()
        observeAchievements()
    }

    private fun observeCodex() {
        viewModelScope.launch {
            repository.observeCodex().collect { entries ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        entries = entries
                    )
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites(playerId).collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            repository.observeProgress(playerId).collect { progress ->
                _uiState.update { it.copy(progress = progress) }
            }
        }
    }

    private fun observeAchievements() {
        viewModelScope.launch {
            repository.observeAchievements(playerId).collect { achievements ->
                _uiState.update { it.copy(achievements = achievements) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(filter = state.filter.copy(searchQuery = query))
        }
    }

    fun onCategorySelected(category: CodexCategory?) {
        _uiState.update { state ->
            state.copy(filter = state.filter.copy(selectedCategory = category))
        }
    }

    fun onEntrySelected(entry: CodexEntry?) {
        _uiState.update { it.copy(selectedEntry = entry) }
    }

    fun toggleFavorite(entry: CodexEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(playerId, entry.id)
        }
    }

    fun saveProgress(heroId: String, level: Int, inventory: List<String>) {
        viewModelScope.launch {
            val progress = PlayerProgress(
                id = UUID.randomUUID().toString(),
                playerId = playerId,
                heroId = heroId,
                level = level,
                inventory = inventory,
                lastUpdated = Date()
            )
            repository.saveProgress(progress)
        }
    }

    fun unlockAchievement(title: String, description: String) {
        viewModelScope.launch {
            val achievement = Achievement(
                id = UUID.randomUUID().toString(),
                playerId = playerId,
                title = title,
                description = description,
                unlockedAt = Date()
            )
            repository.unlockAchievement(achievement)
        }
    }
}

class CodexViewModelFactory(
    private val repository: CodexRepository,
    private val playerId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CodexViewModel::class.java)) {
            return CodexViewModel(repository, playerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
