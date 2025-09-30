package com.example.runeboundmagic.codex

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

interface CodexRepository {
    fun observeCodex(): Flow<List<CodexEntry>>
    fun observeFavorites(playerId: String): Flow<Set<String>>
    fun observeProgress(playerId: String): Flow<List<PlayerProgress>>
    fun observeAchievements(playerId: String): Flow<List<Achievement>>
    suspend fun toggleFavorite(playerId: String, entryId: String)
    suspend fun saveProgress(progress: PlayerProgress)
    suspend fun unlockAchievement(achievement: Achievement)
}

class FirestoreCodexRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CodexRepository {

    private val codexCollection get() = firestore.collection("codex")
    private fun favoritesCollection(playerId: String) =
        firestore.collection("players").document(playerId).collection("favorites")
    private fun progressCollection(playerId: String) =
        firestore.collection("playersProgress").document(playerId).collection("runs")
    private fun achievementsCollection(playerId: String) =
        firestore.collection("achievements").document(playerId).collection("unlocked")

    override fun observeCodex(): Flow<List<CodexEntry>> = callbackFlow {
        val registration = codexCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val entries = snapshot?.documents.orEmpty().mapNotNull { document ->
                val map = document.data.orEmpty()
                CodexEntry(
                    id = document.id,
                    category = CodexCategory.values()
                        .firstOrNull { it.name.equals(map["category"].toString(), ignoreCase = true) }
                        ?: CodexCategory.HERO,
                    name = map["name"]?.toString().orEmpty(),
                    description = map["description"]?.toString().orEmpty(),
                    imageUrl = map["imageUrl"]?.toString().orEmpty(),
                    abilities = (map["abilities"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    rarity = map["rarity"]?.toString(),
                    role = map["role"]?.toString(),
                    tags = (map["tags"] as? List<*>)?.map { it.toString() } ?: emptyList()
                )
            }
            trySend(entries)
        }
        awaitClose { registration.remove() }
    }

    override fun observeFavorites(playerId: String): Flow<Set<String>> = callbackFlow {
        val registration = favoritesCollection(playerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptySet())
                return@addSnapshotListener
            }
            val ids = snapshot?.documents.orEmpty().mapNotNull { it.getString("entryId") }.toSet()
            trySend(ids)
        }
        awaitClose { registration.remove() }
    }

    override fun observeProgress(playerId: String): Flow<List<PlayerProgress>> = callbackFlow {
        val registration = progressCollection(playerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot.toProgressList(playerId))
        }
        awaitClose { registration.remove() }
    }

    override fun observeAchievements(playerId: String): Flow<List<Achievement>> = callbackFlow {
        val registration = achievementsCollection(playerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val achievements = snapshot?.documents.orEmpty().map { document ->
                Achievement(
                    id = document.id,
                    playerId = playerId,
                    title = document.getString("title").orEmpty(),
                    description = document.getString("description").orEmpty(),
                    unlockedAt = document.getDate("unlockedAt") ?: Date()
                )
            }
            trySend(achievements)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun toggleFavorite(playerId: String, entryId: String) {
        val favorites = favoritesCollection(playerId)
        val existing = favorites.whereEqualTo("entryId", entryId).get().await()
        if (existing.isEmpty) {
            val data = mapOf(
                "entryId" to entryId,
                "playerId" to playerId,
                "createdAt" to Date()
            )
            favorites.add(data).await()
        } else {
            existing.documents.firstOrNull()?.reference?.delete()?.await()
        }
    }

    override suspend fun saveProgress(progress: PlayerProgress) {
        val documentId = progress.id.ifBlank { UUID.randomUUID().toString() }
        progressCollection(progress.playerId)
            .document(documentId)
            .set(
                mapOf(
                    "heroId" to progress.heroId,
                    "level" to progress.level,
                    "inventory" to progress.inventory,
                    "lastUpdated" to progress.lastUpdated
                )
            )
            .await()
    }

    override suspend fun unlockAchievement(achievement: Achievement) {
        val documentId = achievement.id.ifBlank { UUID.randomUUID().toString() }
        achievementsCollection(achievement.playerId)
            .document(documentId)
            .set(
                mapOf(
                    "title" to achievement.title,
                    "description" to achievement.description,
                    "unlockedAt" to achievement.unlockedAt
                )
            )
            .await()
    }
}

private fun QuerySnapshot?.toProgressList(playerId: String): List<PlayerProgress> {
    return this?.documents.orEmpty().map { document ->
        PlayerProgress(
            id = document.id,
            playerId = playerId,
            heroId = document.getString("heroId").orEmpty(),
            level = document.getLong("level")?.toInt() ?: 1,
            inventory = document.get("inventory")?.let { list ->
                (list as? List<*>)?.map { it.toString() } ?: emptyList()
            } ?: emptyList(),
            lastUpdated = document.getDate("lastUpdated") ?: Date()
        )
    }
}
