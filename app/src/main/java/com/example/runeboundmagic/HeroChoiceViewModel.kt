package com.example.runeboundmagic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.runeboundmagic.data.local.HeroChoiceDao
import com.example.runeboundmagic.data.local.HeroChoiceEntity
import com.example.runeboundmagic.data.local.LobbyInteractionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HeroChoiceViewModel(
    private val dao: HeroChoiceDao
) : ViewModel() {

    private val db = Firebase.firestore

    fun saveHeroChoice(playerName: String, heroType: HeroType, heroName: String) {
        val choice = HeroChoiceEntity(
            playerName = playerName,
            heroType = heroType,
            heroName = heroName,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            dao.insertChoice(choice)

            val data = hashMapOf(
                "playerName" to playerName,
                "heroType" to heroType.name,
                "heroName" to heroName,
                "timestamp" to choice.timestamp
            )
            db.collection("hero_choices").add(data)
        }
    }

    fun getLastChoice(): Flow<HeroChoiceEntity?> = dao.observeLastChoice()

    fun logInteraction(
        event: LobbyInteractionEvent,
        heroType: HeroType,
        heroDisplayName: String,
        heroNameInput: String
    ) {
        val interaction = LobbyInteractionEntity(
            eventType = event.name,
            heroType = heroType,
            heroDisplayName = heroDisplayName,
            heroNameInput = heroNameInput,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            dao.insertInteraction(interaction)

            val data = hashMapOf(
                "eventType" to event.name,
                "heroType" to heroType.name,
                "heroDisplayName" to heroDisplayName,
                "heroNameInput" to heroNameInput,
                "timestamp" to interaction.timestamp
            )
            db.collection("lobby_interactions").add(data)
        }
    }
}

class HeroChoiceViewModelFactory(
    private val dao: HeroChoiceDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeroChoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeroChoiceViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
