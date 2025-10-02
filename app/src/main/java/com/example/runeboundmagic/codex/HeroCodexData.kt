package com.example.runeboundmagic.codex

import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.inventory.Item
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
    val items: List<Item>
)

/**
 * Πλήρες προφίλ Codex για έναν ήρωα.
 */
data class HeroCodexProfile(
    val heroOption: HeroOption,
    val stats: HeroStats,
    val heroCardLore: String,
    val startingWeapon: Item,
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
        val rod = Item(
            id = "rod_priestess",
            name = "Moonlit Rod",
            description = "Κοντάρι με ενισχυμένη θεραπευτική αύρα. Damage: 32, Element: Light, Skill: Radiant Nova.",
            icon = "weapon/rod.png",
            rarity = Rarity.RARE,
            category = ItemCategory.WEAPON
        )

        val armor = listOf(
            Item(
                id = "priestess_veil",
                name = "Veil of Dawn",
                description = "Κράνος με προστατευτικά glyphs φωτός.",
                icon = "armor/helmet.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "priestess_robe",
                name = "Moonweave Robe",
                description = "Θώρακας με +20 Mana Regen.",
                icon = "armor/chest-women.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "priestess_gloves",
                name = "Gloves of Mercy",
                description = "Γάντια που ενισχύουν τις θεραπεύσεις.",
                icon = "armor/gloves.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "priestess_boots",
                name = "Steps of Serenity",
                description = "Μπότες με +10% ταχύτητα κίνησης.",
                icon = "armor/boots.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = listOf(
            Item(
                id = "priestess_barrier",
                name = "Aegis of Lumina",
                description = "Μαγικό φράγμα με 35 Block.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SHIELD
            )
        )

        val accessories = listOf(
            Item(
                id = "moon_talisman",
                name = "Moonlit Talisman",
                description = "Δαχτυλίδι που χαρίζει +15% Healing Power.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ACCESSORY
            ),
            Item(
                id = "starlit_belt",
                name = "Starlit Belt",
                description = "Ζώνη που αποθηκεύει 2 επιπλέον potions.",
                icon = "armor/buttos_c.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORY
            )
        )

        val consumables = listOf(
            Item(
                id = "major_heal",
                name = "Μεγάλο Φίλτρο Ζωής",
                description = "Αποκαθιστά πλήρως την υγεία του συμμάχου.",
                icon = "inventory/backbag.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLE
            ),
            Item(
                id = "clarity_draught",
                name = "Draught of Clarity",
                description = "Προσωρινό +25 Mana και +10% Crit Chance.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLE
            )
        )

        val spells = listOf(
            Item(
                id = "blessing_scroll",
                name = "Scroll of Dawnburst",
                description = "Κάλεσμα φωτεινής έκρηξης που καθαρίζει debuffs.",
                icon = "characters/mystical_priestess.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SPELL_SCROLL
            )
        )

        val runes = listOf(
            Item(
                id = "lunar_rune",
                name = "Lunar Rune",
                description = "Ρούνος που ενισχύει θεραπείες κατά +12%.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNE_GEM
            )
        )

        val materials = listOf(
            Item(
                id = "stardust",
                name = "Stardust Essence",
                description = "Υλικό craft για μυστικιστικές αναβαθμίσεις.",
                icon = "armor/chest-women_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.MATERIAL
            )
        )

        val questItems = listOf(
            Item(
                id = "oracle_fragment",
                name = "Oracle Fragment",
                description = "Κομμάτι κρυστάλλου που ανοίγει αρχαίες πύλες.",
                icon = "characters/black_mage.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.QUEST_ITEM
            )
        )

        val currency = listOf(
            Item(
                id = "lunar_coins",
                name = "Λάμποντα Νομίσματα",
                description = "Περιέχει 150 χρυσά νομίσματα.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPON to listOf(rod),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELD to shield,
            ItemCategory.ACCESSORY to accessories,
            ItemCategory.CONSUMABLE to consumables,
            ItemCategory.SPELL_SCROLL to spells,
            ItemCategory.RUNE_GEM to runes,
            ItemCategory.MATERIAL to materials,
            ItemCategory.QUEST_ITEM to questItems,
            ItemCategory.CURRENCY to currency
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
        val staff = Item(
            id = "mage_staff",
            name = "Arcane Staff",
            description = "Damage: 40, Element: Arcane, Skill: Rune Surge.",
            icon = "weapon/mage_staff.png",
            rarity = Rarity.EPIC,
            category = ItemCategory.WEAPON
        )

        val armor = listOf(
            Item(
                id = "mage_hat",
                name = "Hat of Channels",
                description = "+25 Mana και +5 Spell Power.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "mage_robe",
                name = "Robe of Runic Flow",
                description = "+18% Rune Damage.",
                icon = "armor/chest_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "mage_gloves",
                name = "Casting Gloves",
                description = "Μειώνουν το κόστος mana κατά 8%.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "mage_boots",
                name = "Boots of Phase Step",
                description = "Επιτρέπουν τηλεμεταφορά μικρής εμβέλειας.",
                icon = "armor/boots_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = listOf(
            Item(
                id = "mage_barrier",
                name = "Chrono Ward",
                description = "Απορροφά 120 damage και επιβραδύνει εχθρούς.",
                icon = "armor/helmet.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SHIELD
            )
        )

        val accessories = listOf(
            Item(
                id = "arcane_ring",
                name = "Arcane Ring",
                description = "+10% Spell Crit και +5 Mana Regen.",
                icon = "armor/gloves.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ACCESSORY
            ),
            Item(
                id = "mana_belt",
                name = "Mana Reservoir Belt",
                description = "Αποθηκεύει 3 επιπλέον scrolls.",
                icon = "armor/buttos_c.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORY
            )
        )

        val consumables = listOf(
            Item(
                id = "mana_potion",
                name = "Υπέρτατο Mana Potion",
                description = "Επαναφέρει αμέσως 120 Mana.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLE
            ),
            Item(
                id = "spell_flux",
                name = "Spell Flux Phial",
                description = "Προσωρινό +20% Rune Damage.",
                icon = "inventory/backbag.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.CONSUMABLE
            )
        )

        val spells = listOf(
            Item(
                id = "meteor_scroll",
                name = "Meteoric Scroll",
                description = "Καλεί μετεωρίτες που κάνουν AoE damage.",
                icon = "characters/mage.png",
                rarity = Rarity.LEGENDARY,
                category = ItemCategory.SPELL_SCROLL
            ),
            Item(
                id = "ward_scroll",
                name = "Temporal Seal",
                description = "Δημιουργεί χρονικό πεδίο άμυνας.",
                icon = "characters/mage.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SPELL_SCROLL
            )
        )

        val runes = listOf(
            Item(
                id = "rune_of_flux",
                name = "Rune of Flux",
                description = "+2 σε κάθε chain spell.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNE_GEM
            ),
            Item(
                id = "prismatic_gem",
                name = "Prismatic Gem",
                description = "Επιτρέπει αλλαγή στοιχείου στις μαγείες.",
                icon = "armor/chest-women_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.RUNE_GEM
            )
        )

        val materials = listOf(
            Item(
                id = "arcane_powder",
                name = "Arcane Powder",
                description = "Χρησιμοποιείται για crafting μπατών και ράβδων.",
                icon = "armor/chest.png",
                rarity = Rarity.RARE,
                category = ItemCategory.MATERIAL
            )
        )

        val questItems = listOf(
            Item(
                id = "rune_tablet",
                name = "Ancient Rune Tablet",
                description = "Απαραίτητη για να ξεκλειδώσει το Rune Vault.",
                icon = "characters/black_mage.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.QUEST_ITEM
            )
        )

        val currency = listOf(
            Item(
                id = "arcane_tokens",
                name = "Arcane Tokens",
                description = "Περιέχει 110 χρυσά νομίσματα και 5 arcane σφραγίδες.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPON to listOf(staff),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELD to shield,
            ItemCategory.ACCESSORY to accessories,
            ItemCategory.CONSUMABLE to consumables,
            ItemCategory.SPELL_SCROLL to spells,
            ItemCategory.RUNE_GEM to runes,
            ItemCategory.MATERIAL to materials,
            ItemCategory.QUEST_ITEM to questItems,
            ItemCategory.CURRENCY to currency
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
        val sword = Item(
            id = "warrior_sword",
            name = "Crimson Blade",
            description = "Damage: 48, Element: Fire, Skill: Blazing Rift.",
            icon = "weapon/shord.png",
            rarity = Rarity.RARE,
            category = ItemCategory.WEAPON
        )

        val armor = listOf(
            Item(
                id = "warrior_helm",
                name = "Helm of the Vanguard",
                description = "+40 Defense και +10 Block.",
                icon = "armor/helmet.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "warrior_chest",
                name = "Plate of Embersteel",
                description = "Μειώνει το εισερχόμενο damage κατά 12%.",
                icon = "armor/chest.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "warrior_gloves",
                name = "Grasp of Fury",
                description = "+15 Attack και +5 Rage στο combo.",
                icon = "armor/gloves.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "warrior_boots",
                name = "March of Titans",
                description = "+8% ταχύτητα και +5 Block.",
                icon = "armor/boots.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = listOf(
            Item(
                id = "warrior_shield",
                name = "Bulwark of Ash",
                description = "Απορροφά 160 damage και αντεπιτίθεται με φωτιά.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.SHIELD
            )
        )

        val accessories = listOf(
            Item(
                id = "warrior_ring",
                name = "Signet of Valor",
                description = "+10% Crit Damage.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORY
            )
        )

        val consumables = listOf(
            Item(
                id = "iron_potion",
                name = "Potion of Ironhide",
                description = "+25 Defense για 3 γύρους.",
                icon = "inventory/backbag.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLE
            ),
            Item(
                id = "rage_draught",
                name = "Draught of Rage",
                description = "+30 Attack και lifesteal.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLE
            )
        )

        val spells = emptyList<Item>()

        val runes = listOf(
            Item(
                id = "ember_gem",
                name = "Ember Gem",
                description = "Ενισχύει πυρίνες επιθέσεις κατά +10%.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNE_GEM
            )
        )

        val materials = listOf(
            Item(
                id = "iron_ingot",
                name = "Forged Iron Ingot",
                description = "Βασικό υλικό για αναβαθμίσεις όπλων.",
                icon = "armor/chest_b.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.MATERIAL
            )
        )

        val questItems = listOf(
            Item(
                id = "war_banner",
                name = "Warbound Banner",
                description = "Αναγκαίο για το storyline του Warrior.",
                icon = "characters/warrior.png",
                rarity = Rarity.RARE,
                category = ItemCategory.QUEST_ITEM
            )
        )

        val currency = listOf(
            Item(
                id = "battle_tokens",
                name = "Battle Tokens",
                description = "Περιέχει 95 χρυσά νομίσματα.",
                icon = "inventory/inventory.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPON to listOf(sword),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELD to shield,
            ItemCategory.ACCESSORY to accessories,
            ItemCategory.CONSUMABLE to consumables,
            ItemCategory.SPELL_SCROLL to spells,
            ItemCategory.RUNE_GEM to runes,
            ItemCategory.MATERIAL to materials,
            ItemCategory.QUEST_ITEM to questItems,
            ItemCategory.CURRENCY to currency
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
        val bow = Item(
            id = "ranger_bow",
            name = "Sylvan Bow",
            description = "Damage: 36, Element: Wind, Skill: Arrow Storm.",
            icon = "weapon/crossbow.png",
            rarity = Rarity.RARE,
            category = ItemCategory.WEAPON
        )

        val armor = listOf(
            Item(
                id = "ranger_hood",
                name = "Windrunner Hood",
                description = "+12 Evasion.",
                icon = "armor/helmet_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "ranger_jacket",
                name = "Leather of the Glade",
                description = "+8 Attack και +15 Evasion.",
                icon = "armor/chest-women.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "ranger_gloves",
                name = "Gloves of True Aim",
                description = "Αυξάνει το crit chance κατά 12%.",
                icon = "armor/gloves.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            ),
            Item(
                id = "ranger_boots",
                name = "Boots of Silent Step",
                description = "+15% ταχύτητα και αθόρυβη κίνηση.",
                icon = "armor/boots_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ARMOR
            )
        )

        val shield = emptyList<Item>()

        val accessories = listOf(
            Item(
                id = "ranger_quiver",
                name = "Quiver of Endless Flight",
                description = "+5 βέλη ανά μάχη.",
                icon = "armor/buttos_c.png",
                rarity = Rarity.EPIC,
                category = ItemCategory.ACCESSORY
            ),
            Item(
                id = "ranger_ring",
                name = "Ring of Whispering Leaves",
                description = "Ανίχνευση παγίδων σε μεγαλύτερη ακτίνα.",
                icon = "armor/gloves_b.png",
                rarity = Rarity.RARE,
                category = ItemCategory.ACCESSORY
            )
        )

        val consumables = listOf(
            Item(
                id = "agility_draught",
                name = "Draught of Agility",
                description = "+20 Evasion για 3 γύρους.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CONSUMABLE
            )
        )

        val spells = listOf(
            Item(
                id = "nature_scroll",
                name = "Scroll of Nature's Grasp",
                description = "Δένει τους εχθρούς με ρίζες.",
                icon = "characters/ranger.png",
                rarity = Rarity.RARE,
                category = ItemCategory.SPELL_SCROLL
            )
        )

        val runes = listOf(
            Item(
                id = "wind_rune",
                name = "Rune of Tailwind",
                description = "+10% ταχύτητα βολής.",
                icon = "puzzle/circle.png",
                rarity = Rarity.RARE,
                category = ItemCategory.RUNE_GEM
            )
        )

        val materials = listOf(
            Item(
                id = "feather_bundle",
                name = "Bundle of Swift Feathers",
                description = "Crafting υλικό για βελτιωμένα βέλη.",
                icon = "armor/chest-women_b.png",
                rarity = Rarity.COMMON,
                category = ItemCategory.MATERIAL
            )
        )

        val questItems = listOf(
            Item(
                id = "scout_map",
                name = "Explorer's Map",
                description = "Οδηγεί σε κρυφές περιοχές της αποστολής.",
                icon = "characters/ranger.png",
                rarity = Rarity.RARE,
                category = ItemCategory.QUEST_ITEM
            )
        )

        val currency = listOf(
            Item(
                id = "hunter_coins",
                name = "Hunter's Coins",
                description = "Περιέχει 105 χρυσά νομίσματα.",
                icon = "inventory/inventory.png",
                rarity = Rarity.RARE,
                category = ItemCategory.CURRENCY
            )
        )

        val heroItems = mapOf(
            ItemCategory.WEAPON to listOf(bow),
            ItemCategory.ARMOR to armor,
            ItemCategory.SHIELD to shield,
            ItemCategory.ACCESSORY to accessories,
            ItemCategory.CONSUMABLE to consumables,
            ItemCategory.SPELL_SCROLL to spells,
            ItemCategory.RUNE_GEM to runes,
            ItemCategory.MATERIAL to materials,
            ItemCategory.QUEST_ITEM to questItems,
            ItemCategory.CURRENCY to currency
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

    private fun buildCategories(items: Map<ItemCategory, List<Item>>): List<InventoryCategoryEntry> {
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
        ItemCategory.WEAPON,
        ItemCategory.ARMOR,
        ItemCategory.SHIELD,
        ItemCategory.ACCESSORY,
        ItemCategory.CONSUMABLE,
        ItemCategory.SPELL_SCROLL,
        ItemCategory.RUNE_GEM,
        ItemCategory.MATERIAL,
        ItemCategory.QUEST_ITEM,
        ItemCategory.CURRENCY
    )

    private val CATEGORY_TITLES = mapOf(
        ItemCategory.WEAPON to "Όπλα",
        ItemCategory.ARMOR to "Πανοπλία",
        ItemCategory.SHIELD to "Ασπίδες",
        ItemCategory.ACCESSORY to "Αξεσουάρ",
        ItemCategory.CONSUMABLE to "Καταναλώσιμα",
        ItemCategory.SPELL_SCROLL to "Μαγείες & Πάπυροι",
        ItemCategory.RUNE_GEM to "Ρούνες & Πολύτιμοι Λίθοι",
        ItemCategory.MATERIAL to "Υλικά Crafting",
        ItemCategory.QUEST_ITEM to "Αντικείμενα Αποστολών",
        ItemCategory.CURRENCY to "Χρυσός & Νομίσματα"
    )

    private val CATEGORY_DESCRIPTIONS = mapOf(
        ItemCategory.WEAPON to "Όλα τα επιθετικά εργαλεία και τα ειδικά όπλα του ήρωα.",
        ItemCategory.ARMOR to "Πανοπλίες και ενισχύσεις για κάθε slot.",
        ItemCategory.SHIELD to "Ασπίδες και μαγικά φράγματα για άμυνα.",
        ItemCategory.ACCESSORY to "Δαχτυλίδια, φυλαχτά και ζώνες με buffs.",
        ItemCategory.CONSUMABLE to "Φίλτρα ζωής, mana και προσωρινές ενισχύσεις.",
        ItemCategory.SPELL_SCROLL to "Ξεχωριστές μαγείες ή πάπυροι προς χρήση στη μάχη.",
        ItemCategory.RUNE_GEM to "Ρούνες και πολύτιμοι λίθοι για enchantments.",
        ItemCategory.MATERIAL to "Υλικά crafting όπως ores, herbs και essences.",
        ItemCategory.QUEST_ITEM to "Ειδικά αντικείμενα για αποστολές.",
        ItemCategory.CURRENCY to "Χρυσά νομίσματα και λοιπά νομίσματα." 
    )

    private val CATEGORY_ICONS = mapOf(
        ItemCategory.WEAPON to "weapon/rod.png",
        ItemCategory.ARMOR to "armor/chest.png",
        ItemCategory.SHIELD to "armor/helmet.png",
        ItemCategory.ACCESSORY to "armor/gloves.png",
        ItemCategory.CONSUMABLE to "inventory/inventory.png",
        ItemCategory.SPELL_SCROLL to "characters/mage.png",
        ItemCategory.RUNE_GEM to "puzzle/circle.png",
        ItemCategory.MATERIAL to "armor/chest-women_b.png",
        ItemCategory.QUEST_ITEM to "inventory/backbag.png",
        ItemCategory.CURRENCY to "inventory/inventory.png"
    )
}

