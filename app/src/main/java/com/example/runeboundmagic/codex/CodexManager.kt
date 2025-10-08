package com.example.runeboundmagic.codex

import com.example.runeboundmagic.data.codex.local.CodexDao
import com.example.runeboundmagic.heroes.Hero
import com.example.runeboundmagic.heroes.HeroClass
import com.example.runeboundmagic.inventory.EquipmentSlot
import com.example.runeboundmagic.inventory.Inventory
import com.example.runeboundmagic.inventory.InventoryItem
import com.example.runeboundmagic.inventory.Item
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.ItemSubcategory
import com.example.runeboundmagic.inventory.Rarity
import com.example.runeboundmagic.inventory.WeaponStats
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

            val ensuredProfile = ensureDefaultWeapon(profile, resolvedHero)

            persistLocal(ensuredProfile)
            syncRemote(ensuredProfile)
            ensuredProfile
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
        val startingItems = DefaultEquipmentLoadout.loadoutFor(hero)
        val inventory = hero.createInventory(items = startingItems)
        return HeroProfile(hero = hero, inventory = inventory)
    }

    private fun ensureDefaultWeapon(profile: HeroProfile, hero: Hero): HeroProfile {
        val inventory = profile.inventory
        val hasMainHandWeapon = inventory.getItemsByCategory(ItemCategory.WEAPONS)
            .any { item -> EquipmentSlot.MAIN_HAND in item.allowedSlots }
        if (!hasMainHandWeapon) {
            inventory.addItem(DefaultEquipmentLoadout.weaponFor(hero))
        }
        return profile
    }

    private fun inventoryCollection() = firestore.collection(INVENTORY_COLLECTION)

    companion object {
        private const val INVENTORY_COLLECTION = "hero_inventories"
    }
}

private object DefaultEquipmentLoadout {

    fun loadoutFor(hero: Hero): List<Item> {
        val prefix = hero.id.ifBlank { hero.classType.name.lowercase() }
        val weapon = weaponFor(hero)
        val otherCategories = ItemCategory.values()
            .filter { it != ItemCategory.WEAPONS }
            .mapNotNull { category -> defaultItem(category, prefix) }
        return buildList {
            add(weapon)
            addAll(otherCategories)
        }
    }

    fun weaponFor(hero: Hero): Item {
        val prefix = hero.id.ifBlank { hero.classType.name.lowercase() }
        return when (hero.classType) {
            HeroClass.WARRIOR -> InventoryItem(
                id = "${'$'}prefix_weapon",
                name = "Ξίφος Τιμής",
                description = "Ένα αξιόπιστο σπαθί για τους πολεμιστές της πρώτης γραμμής.",
                icon = "weapon/sword.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.WEAPONS,
                subcategory = ItemSubcategory.SWORD,
                weaponStats = WeaponStats(damage = 24, element = "SLASHING", attackSpeed = 1.25f)
            )

            HeroClass.RANGER -> InventoryItem(
                id = "${'$'}prefix_weapon",
                name = "Ελαφριά Βαλλίστρα",
                description = "Γρήγορη βαλλίστρα για τους έμπειρους κυνηγούς.",
                icon = "weapon/crossbow.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.WEAPONS,
                subcategory = ItemSubcategory.CROSSBOW,
                weaponStats = WeaponStats(damage = 22, element = "PIERCING", attackSpeed = 1.4f)
            )

            HeroClass.MAGE -> InventoryItem(
                id = "${'$'}prefix_weapon",
                name = "Ραβδί Αιθέρα",
                description = "Μαγεμένο ραβδί που διοχετεύει ενέργεια.",
                icon = "weapon/rod.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.WEAPONS,
                subcategory = ItemSubcategory.ROD,
                weaponStats = WeaponStats(damage = 18, element = "ARCANE", attackSpeed = 1.15f)
            )

            HeroClass.PRIESTESS -> InventoryItem(
                id = "${'$'}prefix_weapon",
                name = "Ραβδί Αγνότητας",
                description = "Ιερό ραβδί που ενισχύει ξόρκια ίασης.",
                icon = "weapon/rod.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.WEAPONS,
                subcategory = ItemSubcategory.ROD,
                weaponStats = WeaponStats(damage = 16, element = "HOLY", attackSpeed = 1.2f)
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

        ItemCategory.SHIELDS -> baseItem(
            prefix,
            category,
            name = "Φυλαχτό Άμυνας",
            description = "Αναχαιτίζει τις πρώτες επιθέσεις των αντιπάλων.",
            icon = "armor/helmet.png"
        )

        ItemCategory.ACCESSORIES -> baseItem(
            prefix,
            category,
            name = "Φυλαχτό Ισορροπίας",
            description = "Προσφέρει μικρά μπόνους στα βασικά στατιστικά.",
            icon = "armor/gloves.png"
        )

        ItemCategory.CONSUMABLES -> baseItem(
            prefix,
            category,
            name = "Φίλτρο Ζωτικότητας",
            description = "Επαναφέρει μέρος της ζωής κατά τη μάχη.",
            icon = "inventory/backbag.png"
        )

        ItemCategory.SPELLS_SCROLLS -> baseItem(
            prefix,
            category,
            name = "Πάπυρος Σπινθήρα",
            description = "Απελευθερώνει μια απλή μαγική επίθεση.",
            icon = "inventory/inventory.png",
            rarity = Rarity.RARE
        )

        ItemCategory.RUNES_GEMS -> baseItem(
            prefix,
            category,
            name = "Ρούνος Ενδυνάμωσης",
            description = "Ενισχύει προσωρινά τον εξοπλισμό.",
            icon = "puzzle/circle.png"
        )

        ItemCategory.CRAFTING_MATERIALS -> baseItem(
            prefix,
            category,
            name = "Δέμα Υλικών",
            description = "Περιέχει βότανα και μέταλλα για crafting.",
            icon = "armor/chest-women_b.png"
        )

        ItemCategory.QUEST_ITEMS -> baseItem(
            prefix,
            category,
            name = "Σύμβολο Αποστολής",
            description = "Ξεκλειδώνει την πρώτη αποστολή της εκστρατείας.",
            icon = "inventory/backbag.png",
            rarity = Rarity.RARE
        )

        ItemCategory.GOLD_CURRENCY -> baseItem(
            prefix,
            category,
            name = "Πουγκί Χρυσού",
            description = "Μικρό απόθεμα νομισμάτων για αγορές.",
            icon = "inventory/inventory.png"
        )

        ItemCategory.WEAPONS -> null
    }

    private fun baseItem(
        prefix: String,
        category: ItemCategory,
        name: String,
        description: String,
        icon: String,
        rarity: Rarity = Rarity.COMMON,
        stackable: Boolean = category in STACKABLE_DEFAULTS,
        quantity: Int = if (stackable) 5 else 1
    ): Item = InventoryItem(
        id = "${'$'}prefix_${'$'}{category.name.lowercase()}_01",
        name = name,
        description = description,
        icon = icon,
        rarity = rarity,
        category = category,
        stackable = stackable,
        quantity = quantity
    )
}

private val STACKABLE_DEFAULTS = setOf(
    ItemCategory.CONSUMABLES,
    ItemCategory.RUNES_GEMS,
    ItemCategory.CRAFTING_MATERIALS,
    ItemCategory.GOLD_CURRENCY
)

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
