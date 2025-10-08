package com.example.runeboundmagic.codex

import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.inventory.InventoryItem
import com.example.runeboundmagic.inventory.ItemCategory
import com.example.runeboundmagic.inventory.Rarity

/**
 * Περιγραφή στατιστικών για την κάρτα ήρωα που εμφανίζεται στον Codex.
 */
data class HeroStats(
    val hp: Int,
    val mana: Int,
    val attack: Int,
    val defense: Int
)

/**
 * Μία κατηγορία inventory με τα αντικείμενα που ανήκουν σε αυτή.
 */
data class InventoryCategoryEntry(
    val category: ItemCategory,
    val title: String,
    val iconAsset: String,
    val description: String,
    val items: List<InventoryItem>
)

/**
 * Πλήρες προφίλ Codex για έναν ήρωα.
 */
data class HeroCodexProfile(
    val heroOption: HeroOption,
    val stats: HeroStats,
    val heroCardLore: String,
    val startingWeapon: InventoryItem,
    val inventoryCategories: List<InventoryCategoryEntry>,
    val startingGold: Int
)

/**
 * Στατικά δεδομένα για τον Codex & Inventory.
 */
object HeroCodexData {

    fun profileFor(heroOption: HeroOption): HeroCodexProfile {
        return when (heroOption) {
            HeroOption.MYSTICAL_PRIESTESS -> priestessProfile()
            HeroOption.MAGE -> mageProfile()
            HeroOption.WARRIOR -> warriorProfile()
            HeroOption.RANGER -> rangerProfile()
        }
    }

    private fun priestessProfile(): HeroCodexProfile {
        val rod = InventoryItem(
            id = "rod_priestess",
            name = "Moonlit Rod",
            description = "Κοντάρι με ενισχυμένη θεραπευτική αύρα. Damage: 32, Element: Light, Skill: Radiant Nova.",
            icon = "weapon/rod.png",
            rarity = Rarity.RARE,
            category = ItemCategory.WEAPONS
        )

        val armor = listOf(
            InventoryItem(
                id = "priestess_veil",
                name = "Veil of Dawn",
                description = "Κράνος με προστατευτικά glyphs φωτός.",
                icon = "armor/helmet.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "priestess_robe",
                name = "Moonweave Robe",
                description = "Θώρακας με +20 Mana Regen.",
                icon = "armor/chest-women.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "priestess_gloves",
                name = "Gloves of Mercy",
                description = "Γάντια που ενισχύουν τις θεραπεύσεις.",
                icon = "armor/gloves.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "priestess_boots",
                name = "Steps of Serenity",
                description = "Μπότες με +10% ταχύτητα κίνησης.",
                icon = "armor/boots.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = listOf(
            InventoryItem(
                id = "priestess_barrier",
                name = "Aegis of Lumina",
                description = "Μαγικό φράγμα με 35 Block.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SHIELDS
            )
        )

        val accessories = listOf(
            InventoryItem(
                id = "moon_talisman",
                name = "Moonlit Talisman",
                description = "Δαχτυλίδι που χαρίζει +15% Healing Power.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ACCESSORIES
            ),
            InventoryItem(
                id = "starlit_belt",
                name = "Starlit Belt",
                description = "Ζώνη που αποθηκεύει 2 επιπλέον potions.",
                icon = "armor/buttos_c.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORIES
            )
        )

        val consumables = listOf(
            InventoryItem(
                id = "major_heal",
                name = "Μεγάλο Φίλτρο Ζωής",
                description = "Αποκαθιστά πλήρως την υγεία του συμμάχου.",
                icon = "inventory/backbag.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLES
            ),
            InventoryItem(
                id = "clarity_draught",
                name = "Draught of Clarity",
                description = "Προσωρινό +25 Mana και +10% Crit Chance.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLES
            )
        )

        val spells = listOf(
            InventoryItem(
                id = "blessing_scroll",
                name = "Scroll of Dawnburst",
                description = "Κάλεσμα φωτεινής έκρηξης που καθαρίζει debuffs.",
                icon = "characters/mystical_priestess.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SPELLS_SCROLLS
            )
        )

        val runes = listOf(
            InventoryItem(
                id = "lunar_rune",
                name = "Lunar Rune",
                description = "Ρούνος που ενισχύει θεραπείες κατά +12%.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNES_GEMS
            )
        )

        val materials = listOf(
            InventoryItem(
                id = "stardust",
                name = "Stardust Essence",
                description = "Υλικό craft για μυστικιστικές αναβαθμίσεις.",
                icon = "armor/chest-women_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CRAFTING_MATERIALS
            )
        )

        val questItems = listOf(
            InventoryItem(
                id = "oracle_fragment",
                name = "Oracle Fragment",
                description = "Κομμάτι κρυστάλλου που ανοίγει αρχαίες πύλες.",
                icon = "characters/black_mage.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.QUEST_ITEMS
            )
        )

        val currency = listOf(
            InventoryItem(
                id = "lunar_coins",
                name = "Λάμποντα Νομίσματα",
                description = "Περιέχει 150 χρυσά νομίσματα.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.GOLD_CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPONS to listOf(rod),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELDS to shield,
            ItemCategory.ACCESSORIES to accessories,
            ItemCategory.CONSUMABLES to consumables,
            ItemCategory.SPELLS_SCROLLS to spells,
            ItemCategory.RUNES_GEMS to runes,
            ItemCategory.CRAFTING_MATERIALS to materials,
            ItemCategory.QUEST_ITEMS to questItems,
            ItemCategory.GOLD_CURRENCY to currency
        )

        return HeroCodexProfile(
            heroOption = HeroOption.MYSTICAL_PRIESTESS,
            stats = HeroStats(hp = 92, mana = 160, attack = 58, defense = 62),
            heroCardLore = "Η Mystical Priestess φέρνει τη δύναμη του φεγγαριού σε κάθε αποστολή και κρατά την ομάδα ζωντανή.",
            startingWeapon = rod,
            inventoryCategories = buildCategories(heroItems),
            startingGold = 150
        )
    }

    private fun mageProfile(): HeroCodexProfile {
        val staff = InventoryItem(
            id = "mage_staff",
            name = "Arcane Staff",
            description = "Damage: 40, Element: Arcane, Skill: Rune Surge.",
            icon = "weapon/mage_staff.png",
            rarity = Rarity.EPIC,
            category = ItemCategory.WEAPONS
        )

        val armor = listOf(
            InventoryItem(
                id = "mage_hat",
                name = "Hat of Channels",
                description = "+25 Mana και +5 Spell Power.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "mage_robe",
                name = "Robe of Runic Flow",
                description = "+18% Rune Damage.",
                icon = "armor/chest_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "mage_gloves",
                name = "Casting Gloves",
                description = "Μειώνουν το κόστος mana κατά 8%.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "mage_boots",
                name = "Boots of Phase Step",
                description = "Επιτρέπουν τηλεμεταφορά μικρής εμβέλειας.",
                icon = "armor/boots_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = listOf(
            InventoryItem(
                id = "mage_barrier",
                name = "Chrono Ward",
                description = "Απορροφά 120 damage και επιβραδύνει εχθρούς.",
                icon = "armor/helmet.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SHIELDS
            )
        )

        val accessories = listOf(
            InventoryItem(
                id = "arcane_ring",
                name = "Arcane Ring",
                description = "+10% Spell Crit και +5 Mana Regen.",
                icon = "armor/gloves.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ACCESSORIES
            ),
            InventoryItem(
                id = "mana_belt",
                name = "Mana Reservoir Belt",
                description = "Αποθηκεύει 3 επιπλέον scrolls.",
                icon = "armor/buttos_c.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORIES
            )
        )

        val consumables = listOf(
            InventoryItem(
                id = "mana_potion",
                name = "Υπέρτατο Mana Potion",
                description = "Επαναφέρει αμέσως 120 Mana.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLES
            ),
            InventoryItem(
                id = "spell_flux",
                name = "Spell Flux Phial",
                description = "Προσωρινό +20% Rune Damage.",
                icon = "inventory/backbag.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.CONSUMABLES
            )
        )

        val spells = listOf(
            InventoryItem(
                id = "meteor_scroll",
                name = "Meteoric Scroll",
                description = "Καλεί μετεωρίτες που κάνουν AoE damage.",
                icon = "characters/mage.png",
                rarity = Rarity.LEGENDARY,
                category = ItemCategory.SPELLS_SCROLLS
            ),
            InventoryItem(
                id = "ward_scroll",
                name = "Temporal Seal",
                description = "Δημιουργεί χρονικό πεδίο άμυνας.",
                icon = "characters/mage.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SPELLS_SCROLLS
            )
        )

        val runes = listOf(
            InventoryItem(
                id = "rune_of_flux",
                name = "Rune of Flux",
                description = "+2 σε κάθε chain spell.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNES_GEMS
            ),
            InventoryItem(
                id = "prismatic_gem",
                name = "Prismatic Gem",
                description = "Επιτρέπει αλλαγή στοιχείου στις μαγείες.",
                icon = "armor/chest-women_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.RUNES_GEMS
            )
        )

        val materials = listOf(
            InventoryItem(
                id = "arcane_powder",
                name = "Arcane Powder",
                description = "Χρησιμοποιείται για crafting μπατών και ράβδων.",
                icon = "armor/chest.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CRAFTING_MATERIALS
            )
        )

        val questItems = listOf(
            InventoryItem(
                id = "rune_tablet",
                name = "Ancient Rune Tablet",
                description = "Απαραίτητη για να ξεκλειδώσει το Rune Vault.",
                icon = "characters/black_mage.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.QUEST_ITEMS
            )
        )

        val currency = listOf(
            InventoryItem(
                id = "arcane_tokens",
                name = "Arcane Tokens",
                description = "Περιέχει 110 χρυσά νομίσματα και 5 arcane σφραγίδες.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.GOLD_CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPONS to listOf(staff),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELDS to shield,
            ItemCategory.ACCESSORIES to accessories,
            ItemCategory.CONSUMABLES to consumables,
            ItemCategory.SPELLS_SCROLLS to spells,
            ItemCategory.RUNES_GEMS to runes,
            ItemCategory.CRAFTING_MATERIALS to materials,
            ItemCategory.QUEST_ITEMS to questItems,
            ItemCategory.GOLD_CURRENCY to currency
        )

        return HeroCodexProfile(
            heroOption = HeroOption.MAGE,
            stats = HeroStats(hp = 78, mana = 185, attack = 66, defense = 48),
            heroCardLore = "Ο Mage είναι κύριος των ρούνων και πειραματίζεται με επικίνδυνη μαγεία.",
            startingWeapon = staff,
            inventoryCategories = buildCategories(heroItems),
            startingGold = 110
        )
    }

    private fun warriorProfile(): HeroCodexProfile {
        val sword = InventoryItem(
            id = "warrior_sword",
            name = "Crimson Blade",
            description = "Damage: 48, Element: Fire, Skill: Blazing Rift.",
            icon = "weapon/shord.png",
            rarity = Rarity.RARE,
            category = ItemCategory.WEAPONS
        )

        val armor = listOf(
            InventoryItem(
                id = "warrior_helm",
                name = "Helm of the Vanguard",
                description = "+40 Defense και +10 Block.",
                icon = "armor/helmet.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "warrior_chest",
                name = "Plate of Embersteel",
                description = "Μειώνει το εισερχόμενο damage κατά 12%.",
                icon = "armor/chest.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "warrior_gloves",
                name = "Grasp of Fury",
                description = "+15 Attack και +5 Rage στο combo.",
                icon = "armor/gloves.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "warrior_boots",
                name = "March of Titans",
                description = "+8% ταχύτητα και +5 Block.",
                icon = "armor/boots.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = listOf(
            InventoryItem(
                id = "warrior_shield",
                name = "Bulwark of Ash",
                description = "Απορροφά 160 damage και αντεπιτίθεται με φωτιά.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SHIELDS
            )
        )

        val accessories = listOf(
            InventoryItem(
                id = "warrior_ring",
                name = "Signet of Valor",
                description = "+10% Crit Damage.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORIES
            )
        )

        val consumables = listOf(
            InventoryItem(
                id = "iron_potion",
                name = "Potion of Ironhide",
                description = "+25 Defense για 3 γύρους.",
                icon = "inventory/backbag.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLES
            ),
            InventoryItem(
                id = "rage_draught",
                name = "Draught of Rage",
                description = "+30 Attack και lifesteal.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLES
            )
        )

        val spells = emptyList<InventoryItem>()

        val runes = listOf(
            InventoryItem(
                id = "ember_gem",
                name = "Ember Gem",
                description = "Ενισχύει πυρίνες επιθέσεις κατά +10%.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNES_GEMS
            )
        )

        val materials = listOf(
            InventoryItem(
                id = "iron_ingot",
                name = "Forged Iron Ingot",
                description = "Βασικό υλικό για αναβαθμίσεις όπλων.",
                icon = "armor/chest_b.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.CRAFTING_MATERIALS
            )
        )

        val questItems = listOf(
            InventoryItem(
                id = "war_banner",
                name = "Warbound Banner",
                description = "Αναγκαίο για το storyline του Warrior.",
                icon = "characters/warrior.png",
                rarity = Rarity.RARE,
                category = ItemCategory.QUEST_ITEMS
            )
        )

        val currency = listOf(
            InventoryItem(
                id = "battle_tokens",
                name = "Battle Tokens",
                description = "Περιέχει 95 χρυσά νομίσματα.",
                icon = "inventory/inventory.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.GOLD_CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPONS to listOf(sword),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELDS to shield,
            ItemCategory.ACCESSORIES to accessories,
            ItemCategory.CONSUMABLES to consumables,
            ItemCategory.SPELLS_SCROLLS to spells,
            ItemCategory.RUNES_GEMS to runes,
            ItemCategory.CRAFTING_MATERIALS to materials,
            ItemCategory.QUEST_ITEMS to questItems,
            ItemCategory.GOLD_CURRENCY to currency
        )

        return HeroCodexProfile(
            heroOption = HeroOption.WARRIOR,
            stats = HeroStats(hp = 128, mana = 70, attack = 82, defense = 78),
            heroCardLore = "Ο Warrior αποτελεί το προπύργιο της πρώτης γραμμής, έτοιμος να αναχαιτίσει κάθε απειλή.",
            startingWeapon = sword,
            inventoryCategories = buildCategories(heroItems),
            startingGold = 95
        )
    }

    private fun rangerProfile(): HeroCodexProfile {
        val bow = InventoryItem(
            id = "ranger_bow",
            name = "Sylvan Bow",
            description = "Damage: 36, Element: Wind, Skill: Arrow Storm.",
            icon = "weapon/crossbow.png",
            rarity = Rarity.RARE,
            category = ItemCategory.WEAPONS
        )

        val armor = listOf(
            InventoryItem(
                id = "ranger_hood",
                name = "Windrunner Hood",
                description = "+12 Evasion.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "ranger_jacket",
                name = "Leather of the Glade",
                description = "+8 Attack και +15 Evasion.",
                icon = "armor/chest-women.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "ranger_gloves",
                name = "Gloves of True Aim",
                description = "Αυξάνει το crit chance κατά 12%.",
                icon = "armor/gloves.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            InventoryItem(
                id = "ranger_boots",
                name = "Boots of Silent Step",
                description = "+15% ταχύτητα και αθόρυβη κίνηση.",
                icon = "armor/boots_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = emptyList<InventoryItem>()

        val accessories = listOf(
            InventoryItem(
                id = "ranger_quiver",
                name = "Quiver of Endless Flight",
                description = "+5 βέλη ανά μάχη.",
                icon = "armor/buttos_c.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ACCESSORIES
            ),
            InventoryItem(
                id = "ranger_ring",
                name = "Ring of Whispering Leaves",
                description = "Ανίχνευση παγίδων σε μεγαλύτερη ακτίνα.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORIES
            )
        )

        val consumables = listOf(
            InventoryItem(
                id = "agility_draught",
                name = "Draught of Agility",
                description = "+20 Evasion για 3 γύρους.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLES
            )
        )

        val spells = listOf(
            InventoryItem(
                id = "nature_scroll",
                name = "Scroll of Nature's Grasp",
                description = "Δένει τους εχθρούς με ρίζες.",
                icon = "characters/ranger.png",
                rarity = Rarity.RARE,
                category = ItemCategory.SPELLS_SCROLLS
            )
        )

        val runes = listOf(
            InventoryItem(
                id = "wind_rune",
                name = "Rune of Tailwind",
                description = "+10% ταχύτητα βολής.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNES_GEMS
            )
        )

        val materials = listOf(
            InventoryItem(
                id = "feather_bundle",
                name = "Bundle of Swift Feathers",
                description = "Crafting υλικό για βελτιωμένα βέλη.",
                icon = "armor/chest-women_b.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.CRAFTING_MATERIALS
            )
        )

        val questItems = listOf(
            InventoryItem(
                id = "scout_map",
                name = "Explorer's Map",
                description = "Οδηγεί σε κρυφές περιοχές της αποστολής.",
                icon = "characters/ranger.png",
                rarity = Rarity.RARE,
                category = ItemCategory.QUEST_ITEMS
            )
        )

        val currency = listOf(
            InventoryItem(
                id = "hunter_coins",
                name = "Hunter's Coins",
                description = "Περιέχει 105 χρυσά νομίσματα.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.GOLD_CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPONS to listOf(bow),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELDS to shield,
            ItemCategory.ACCESSORIES to accessories,
            ItemCategory.CONSUMABLES to consumables,
            ItemCategory.SPELLS_SCROLLS to spells,
            ItemCategory.RUNES_GEMS to runes,
            ItemCategory.CRAFTING_MATERIALS to materials,
            ItemCategory.QUEST_ITEMS to questItems,
            ItemCategory.GOLD_CURRENCY to currency
        )

        return HeroCodexProfile(
            heroOption = HeroOption.RANGER,
            stats = HeroStats(hp = 102, mana = 95, attack = 72, defense = 58),
            heroCardLore = "Η Ranger είναι οφθαλμός της φύσης, ειδική στα βέλη και στη γρήγορη εξερεύνηση.",
            startingWeapon = bow,
            inventoryCategories = buildCategories(heroItems),
            startingGold = 105
        )
    }

    private fun buildCategories(items: Map<ItemCategory, List<InventoryItem>>): List<InventoryCategoryEntry> {
        return CATEGORY_ORDER.map { category ->
            InventoryCategoryEntry(
                category = category,
                title = CATEGORY_TITLES.getValue(category),
                iconAsset = CATEGORY_ICONS.getValue(category),
                description = CATEGORY_DESCRIPTIONS.getValue(category),
                items = items[category].orEmpty()
            )
        }
    }

    private val CATEGORY_ORDER = listOf(
        ItemCategory.WEAPONS,
        ItemCategory.ARMOR,
        ItemCategory.SHIELDS,
        ItemCategory.ACCESSORIES,
        ItemCategory.CONSUMABLES,
        ItemCategory.SPELLS_SCROLLS,
        ItemCategory.RUNES_GEMS,
        ItemCategory.CRAFTING_MATERIALS,
        ItemCategory.QUEST_ITEMS,
        ItemCategory.GOLD_CURRENCY
    )

    private val CATEGORY_TITLES = mapOf(
        ItemCategory.WEAPONS to "Όπλα",
        ItemCategory.ARMOR to "Πανοπλία",
        ItemCategory.SHIELDS to "Ασπίδες",
        ItemCategory.ACCESSORIES to "Αξεσουάρ",
        ItemCategory.CONSUMABLES to "Καταναλώσιμα",
        ItemCategory.SPELLS_SCROLLS to "Μαγείες & Πάπυροι",
        ItemCategory.RUNES_GEMS to "Ρούνες & Πολύτιμοι Λίθοι",
        ItemCategory.CRAFTING_MATERIALS to "Υλικά Crafting",
        ItemCategory.QUEST_ITEMS to "Αντικείμενα Αποστολών",
        ItemCategory.GOLD_CURRENCY to "Χρυσός & Νομίσματα"
    )

    private val CATEGORY_DESCRIPTIONS = mapOf(
        ItemCategory.WEAPONS to "Όλα τα επιθετικά εργαλεία και τα ειδικά όπλα του ήρωα.",
        ItemCategory.ARMOR to "Πανοπλίες και ενισχύσεις για κάθε slot.",
        ItemCategory.SHIELDS to "Ασπίδες και μαγικά φράγματα για άμυνα.",
        ItemCategory.ACCESSORIES to "Δαχτυλίδια, φυλαχτά και ζώνες με buffs.",
        ItemCategory.CONSUMABLES to "Φίλτρα ζωής, mana και προσωρινές ενισχύσεις.",
        ItemCategory.SPELLS_SCROLLS to "Ξεχωριστές μαγείες ή πάπυροι προς χρήση στη μάχη.",
        ItemCategory.RUNES_GEMS to "Ρούνες και πολύτιμοι λίθοι για enchantments.",
        ItemCategory.CRAFTING_MATERIALS to "Υλικά crafting όπως ores, herbs και essences.",
        ItemCategory.QUEST_ITEMS to "Ειδικά αντικείμενα για αποστολές.",
        ItemCategory.GOLD_CURRENCY to "Χρυσά νομίσματα και λοιπά νομίσματα." 
    )

    private val CATEGORY_ICONS = mapOf(
        ItemCategory.WEAPONS to "weapon/rod.png",
        ItemCategory.ARMOR to "armor/chest.png",
        ItemCategory.SHIELDS to "armor/helmet.png",
        ItemCategory.ACCESSORIES to "armor/gloves.png",
        ItemCategory.CONSUMABLES to "inventory/inventory.png",
        ItemCategory.SPELLS_SCROLLS to "characters/mage.png",
        ItemCategory.RUNES_GEMS to "puzzle/circle.png",
        ItemCategory.CRAFTING_MATERIALS to "armor/chest-women_b.png",
        ItemCategory.QUEST_ITEMS to "inventory/backbag.png",
        ItemCategory.GOLD_CURRENCY to "inventory/inventory.png"
    )
}

