package com.example.runeboundmagic.codex

import com.example.runeboundmagic.data.codex.local.CodexDao
import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.Inventory
import com.example.runeboundmagic.inventory.InventoryItem
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.Item
import com.example.runeboundmagic.inventory.Rarity
import com.example.runeboundmagic.inventory.WeaponItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CodexManager(
    private val codexDao: CodexDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun prepareHeroProfile(hero: Hero, customName: String): HeroProfile =
        withContext(ioDispatcher) {
            val resolvedHero = hero.copy(
                name = customName.takeIf { it.isNotBlank() } ?: hero.name
            )

            val localProfile = codexDao.getHeroWithInventory(resolvedHero.id)?.toDomain()
            val profile = when {
                localProfile == null -> createDefaultProfile(resolvedHero)
                localProfile.hero.name != resolvedHero.name ->
                    localProfile.copy(hero = resolvedHero)
                else -> localProfile
            }

            persistLocal(profile)
            syncRemote(profile)
            profile
        }

    suspend fun updateInventory(profile: HeroProfile) = withContext(ioDispatcher) {
        persistLocal(profile)
        syncRemote(profile)
    }

    suspend fun refreshFromRemote(heroId: String): HeroProfile? = withContext(ioDispatcher) {
        val localProfile = codexDao.getHeroWithInventory(heroId)?.toDomain() ?: return@withContext null
        val snapshot = inventoryCollection()
            .whereEqualTo("heroId", heroId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val remoteInventory = snapshot?.data?.let { data ->
            val inventoryId = data["inventoryId"]?.toString()?.takeIf { it.isNotBlank() }
                ?: localProfile.inventory.id
            Inventory.fromFirestore(
                inventoryId = inventoryId,
                data = data,
                defaultCapacity = localProfile.inventory.capacity
            )
        }

        val mergedProfile = remoteInventory?.let { inventory ->
            HeroProfile(hero = localProfile.hero, inventory = inventory)
        } ?: localProfile

        persistLocal(mergedProfile)
        mergedProfile
    }

    private suspend fun persistLocal(profile: HeroProfile) {
        codexDao.upsertHeroProfile(
            hero = profile.toHeroEntity(),
            inventory = profile.toInventoryEntity(),
            items = profile.toItemEntities()
        )
    }

    private suspend fun syncRemote(profile: HeroProfile) {
        inventoryCollection()
            .document(profile.inventory.id)
            .set(profile.toFirestoreMap())
            .await()
    }

    private fun createDefaultProfile(hero: Hero): HeroProfile {
        val startingItems = DefaultEquipmentLoadout.loadoutFor(hero.classType)
        val inventory = hero.createInventory(items = startingItems)
        return HeroProfile(hero = hero, inventory = inventory)
    }

    private fun inventoryCollection() = firestore.collection(INVENTORY_COLLECTION)

    companion object {
        private const val INVENTORY_COLLECTION = "hero_inventories"
    }
}

private object DefaultEquipmentLoadout {

    fun loadoutFor(heroClass: HeroClass): List<Item> {
        val prefix = heroClass.name.lowercase()
        val weapon = weaponFor(heroClass, prefix)
        val otherCategories = ItemCategory.values()
            .filter { it != ItemCategory.WEAPON }
            .mapNotNull { category -> defaultItem(category, prefix) }
        return buildList {
            add(weapon)
            addAll(otherCategories)
        }
    }

    private fun weaponFor(heroClass: HeroClass, prefix: String): WeaponItem {
        return when (heroClass) {
            HeroClass.WARRIOR -> WeaponItem(
                id = "${'$'}prefix_weapon",
                name = "Βολίδα Μάχης",
                description = "Ελαφρύ βαλλίστρα που επιτρέπει γρήγορες επιθέσεις πριν τη σύγκρουση.",
                icon = "weapon/crossbow.png",
                rarity = Rarity.RARE,
                damage = 24,
                element = "PIERCING",
                attackSpeed = 1.3f
            )

            HeroClass.RANGER -> WeaponItem(
                id = "${'$'}prefix_weapon",
                name = "Κυνηγετικό Τόξο",
                description = "Αξιόπιστη βαλλίστρα για τους κυνηγούς των Runebound Lands.",
                icon = "weapon/crossbow.png",
                rarity = Rarity.RARE,
                damage = 22,
                element = "PIERCING",
                attackSpeed = 1.45f
            )

            HeroClass.MAGE -> WeaponItem(
                id = "${'$'}prefix_weapon",
                name = "Ραβδί Αιθέρα",
                description = "Μαγεμένο ραβδί με μπλε αύρα που επικαλείται στοιχειακή ενέργεια.",
                icon = "weapon/rod.png",
                rarity = Rarity.EPIC,
                damage = 18,
                element = "ARCANE",
                attackSpeed = 1.1f
            )

            HeroClass.PRIESTESS -> WeaponItem(
                id = "${'$'}prefix_weapon",
                name = "Ραβδί Φωτεινών Ρούνων",
                description = "Ιερό ραβδί με φωτεινά runes που ενισχύει τα ξόρκια θεραπείας.",
                icon = "weapon/rod.png",
                rarity = Rarity.RARE,
                damage = 16,
                element = "HOLY",
                attackSpeed = 1.2f
            )
        }
    }

    private fun defaultItem(category: ItemCategory, prefix: String): Item? = when (category) {
        ItemCategory.ARMOR -> baseItem(
            prefix,
            category,
            name = "Ελαφριά Πανοπλία",
            description = "Βασική προστασία για τις πρώτες αποστολές.",
            icon = "armor/chest.png"
        )

        ItemCategory.SHIELD -> baseItem(
            prefix,
            category,
            name = "Φυλαχτό Άμυνας",
            description = "Αναχαιτίζει τις πρώτες επιθέσεις των αντιπάλων.",
            icon = "armor/helmet.png"
        )

        ItemCategory.ACCESSORY -> baseItem(
            prefix,
            category,
            name = "Φυλαχτό Ισορροπίας",
            description = "Προσφέρει μικρά μπόνους στα βασικά στατιστικά.",
            icon = "armor/gloves.png"
        )

        ItemCategory.CONSUMABLE -> baseItem(
            prefix,
            category,
            name = "Φίλτρο Ζωτικότητας",
            description = "Επαναφέρει μέρος της ζωής κατά τη μάχη.",
            icon = "inventory/backbag.png"
        )

        ItemCategory.SPELL_SCROLL -> baseItem(
            prefix,
            category,
            name = "Πάπυρος Σπινθήρα",
            description = "Απελευθερώνει μια απλή μαγική επίθεση.",
            icon = "inventory/inventory.png",
            rarity = Rarity.RARE
        )

        ItemCategory.RUNE_GEM -> baseItem(
            prefix,
            category,
            name = "Ρούνος Ενδυνάμωσης",
            description = "Ενισχύει προσωρινά τον εξοπλισμό.",
            icon = "puzzle/circle.png"
        )

        ItemCategory.CRAFTING_MATERIAL -> baseItem(
            prefix,
            category,
            name = "Δέμα Υλικών",
            description = "Περιέχει βότανα και μέταλλα για crafting.",
            icon = "armor/chest-women_b.png"
        )

        ItemCategory.QUEST_ITEM -> baseItem(
            prefix,
            category,
            name = "Σύμβολο Αποστολής",
            description = "Ξεκλειδώνει την πρώτη αποστολή της εκστρατείας.",
            icon = "inventory/backbag.png",
            rarity = Rarity.RARE
        )

        ItemCategory.CURRENCY -> baseItem(
            prefix,
            category,
            name = "Πουγκί Χρυσού",
            description = "Μικρό απόθεμα νομισμάτων για αγορές.",
            icon = "inventory/inventory.png"
        )

        ItemCategory.WEAPON -> null
    }

    private fun baseItem(
        prefix: String,
        category: ItemCategory,
        name: String,
        description: String,
        icon: String,
        rarity: Rarity = Rarity.COMMON
    ): Item = InventoryItem(
        id = "${'$'}prefix_${'$'}{category.name.lowercase()}_01",
        name = name,
        description = description,
        icon = icon,
        rarity = rarity,
        category = category
    )
}

private fun HeroProfile.toFirestoreMap(): Map<String, Any> = mapOf(
    "heroId" to hero.id,
    "heroName" to hero.name,
    "heroClass" to hero.classType.name,
    "description" to hero.description,
    "level" to hero.level,
    "cardImage" to hero.cardImage,
    "inventoryId" to inventory.id,
    "gold" to inventory.gold,
    "capacity" to inventory.capacity,
    "items" to inventory.getAllItems().map(Item::toFirestoreMap)
)
