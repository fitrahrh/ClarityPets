package com.harukiyu.claritypets.model;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum PetType {
    // ==========================================
    // Canine & Feline
    // ==========================================
    WOLF("wolf", "Wolf", "WOLF", PetCategory.COMBAT, PetMovementType.GROUND, false, 24.0, 5.5, 3.0, 0.28, 0.6, 0.25, 0.15, 0.002, "Pack Leader", "Pounce Strike", SkilltreeType.JUGGERNAUT),
    CAT("cat", "Cat", "CAT", PetCategory.UTILITY, PetMovementType.GROUND, false, 18.0, 4.0, 2.0, 0.30, 0.45, 0.18, 0.10, 0.003, "Creep Repel", "Feline Agility", SkilltreeType.SCAVENGER),
    OCELOT("ocelot", "Ocelot", "OCELOT", PetCategory.UTILITY, PetMovementType.GROUND, false, 18.0, 4.2, 2.0, 0.32, 0.45, 0.20, 0.10, 0.003, "Jungle Prowler", "Nimble Dash", SkilltreeType.SCAVENGER),
    FOX("fox", "Fox", "FOX", PetCategory.UTILITY, PetMovementType.GROUND, false, 18.0, 4.5, 2.0, 0.30, 0.5, 0.20, 0.10, 0.002, "Berry Forager", "Swift Leap", SkilltreeType.SCAVENGER),

    // ==========================================
    // Flying Companions
    // ==========================================
    ALLAY("allay", "Allay", "ALLAY", PetCategory.UTILITY, PetMovementType.FLYING, false, 20.0, 2.5, 2.0, 0.28, 0.5, 0.12, 0.10, 0.002, "Item Beacon", "Melodic Tune", SkilltreeType.SCAVENGER),
    BAT("bat", "Bat", "BAT", PetCategory.UTILITY, PetMovementType.FLYING, false, 12.0, 2.0, 1.0, 0.32, 0.3, 0.10, 0.08, 0.003, "Echolocation", "Sonar Ping", SkilltreeType.SCAVENGER),
    BEE("bee", "Bee", "BEE", PetCategory.SUPPORT, PetMovementType.FLYING, false, 18.0, 3.5, 2.5, 0.28, 0.5, 0.15, 0.12, 0.002, "Pollen Feast", "Honey Splash", SkilltreeType.GUARDIAN),
    BLAZE("blaze", "Blaze", "BLAZE", PetCategory.COMBAT, PetMovementType.FLYING, false, 26.0, 6.0, 3.0, 0.26, 0.7, 0.26, 0.15, 0.001, "Fire Shield", "Fireball Volley", SkilltreeType.JUGGERNAUT),
    BREEZE("breeze", "Breeze", "BREEZE", PetCategory.UTILITY, PetMovementType.FLYING, false, 26.0, 4.5, 3.0, 0.28, 0.65, 0.20, 0.15, 0.002, "Air Cushion", "Wind Burst", SkilltreeType.SCAVENGER),
    GHAST("ghast", "Ghast", "GHAST", PetCategory.COMBAT, PetMovementType.FLYING, false, 32.0, 7.5, 2.0, 0.24, 0.8, 0.30, 0.10, 0.001, "Floating Dread", "Explosive Tear", SkilltreeType.JUGGERNAUT),
    PARROT("parrot", "Parrot", "PARROT", PetCategory.UTILITY, PetMovementType.FLYING, false, 16.0, 3.0, 1.5, 0.30, 0.4, 0.15, 0.10, 0.003, "Mimicry Warning", "Aerial Dance", SkilltreeType.SCAVENGER),
    PHANTOM("phantom", "Phantom", "PHANTOM", PetCategory.COMBAT, PetMovementType.FLYING, false, 24.0, 5.5, 2.0, 0.30, 0.6, 0.25, 0.12, 0.002, "Night Terror", "Swoop Ambush", SkilltreeType.JUGGERNAUT),
    VEX("vex", "Vex", "VEX", PetCategory.COMBAT, PetMovementType.FLYING, false, 18.0, 5.0, 2.5, 0.32, 0.5, 0.22, 0.12, 0.002, "Phase Dodge", "Piercing Charge", SkilltreeType.JUGGERNAUT),

    // ==========================================
    // Aquatic & Amphibious
    // ==========================================
    AXOLOTL("axolotl", "Axolotl", "AXOLOTL", PetCategory.SUPPORT, PetMovementType.AMPHIBIOUS, false, 22.0, 3.5, 3.0, 0.26, 0.6, 0.16, 0.15, 0.002, "Play Dead", "Ocean Grace", SkilltreeType.GUARDIAN),
    COD("cod", "Cod", "COD", PetCategory.UTILITY, PetMovementType.SWIMMING, false, 14.0, 2.0, 1.0, 0.30, 0.35, 0.10, 0.08, 0.003, "Current Glide", "Schooling", SkilltreeType.SCAVENGER),
    DOLPHIN("dolphin", "Dolphin", "DOLPHIN", PetCategory.UTILITY, PetMovementType.SWIMMING, false, 24.0, 4.0, 2.5, 0.32, 0.6, 0.18, 0.12, 0.003, "Dolphin Grace", "Sonar Wave", SkilltreeType.SCAVENGER),
    DROWNED("drowned", "Drowned", "DROWNED", PetCategory.COMBAT, PetMovementType.AMPHIBIOUS, false, 26.0, 5.5, 3.5, 0.25, 0.7, 0.24, 0.16, 0.001, "Sunken Power", "Trident Throw", SkilltreeType.JUGGERNAUT),
    ELDER_GUARDIAN("elder_guardian", "Elder Guardian", "ELDER_GUARDIAN", PetCategory.COMBAT, PetMovementType.SWIMMING, false, 45.0, 8.0, 6.0, 0.22, 1.2, 0.32, 0.25, 0.001, "Mining Fatigue", "Ancient Prism", SkilltreeType.JUGGERNAUT),
    FROG("frog", "Frog", "FROG", PetCategory.SUPPORT, PetMovementType.AMPHIBIOUS, false, 18.0, 3.0, 2.5, 0.26, 0.5, 0.14, 0.12, 0.002, "Swamp Resilience", "Tongue Snatch", SkilltreeType.GUARDIAN),
    GLOW_SQUID("glow_squid", "Glow Squid", "GLOW_SQUID", PetCategory.UTILITY, PetMovementType.SWIMMING, false, 18.0, 2.5, 2.0, 0.28, 0.45, 0.12, 0.10, 0.002, "Luminescence", "Flash Ink", SkilltreeType.SCAVENGER),
    GUARDIAN("guardian", "Guardian", "GUARDIAN", PetCategory.COMBAT, PetMovementType.SWIMMING, false, 30.0, 6.0, 4.5, 0.25, 0.8, 0.25, 0.20, 0.001, "Spike Defense", "Prism Laser", SkilltreeType.JUGGERNAUT),
    PUFFERFISH("pufferfish", "Pufferfish", "PUFFERFISH", PetCategory.SUPPORT, PetMovementType.SWIMMING, false, 16.0, 3.0, 3.5, 0.25, 0.4, 0.15, 0.18, 0.001, "Inflate Armor", "Toxic Burst", SkilltreeType.GUARDIAN),
    SALMON("salmon", "Salmon", "SALMON", PetCategory.UTILITY, PetMovementType.SWIMMING, false, 14.0, 2.0, 1.0, 0.30, 0.35, 0.10, 0.08, 0.003, "River Leap", "Stream Surge", SkilltreeType.SCAVENGER),
    SQUID("squid", "Squid", "SQUID", PetCategory.UTILITY, PetMovementType.SWIMMING, false, 16.0, 2.5, 2.0, 0.28, 0.4, 0.12, 0.10, 0.002, "Ink Cloud", "Tentacle Whip", SkilltreeType.SCAVENGER),
    TADPOLE("tadpole", "Tadpole", "TADPOLE", PetCategory.SUPPORT, PetMovementType.SWIMMING, false, 10.0, 1.5, 1.0, 0.28, 0.25, 0.08, 0.06, 0.003, "Tadpole Wiggle", "Algae Masticate", SkilltreeType.GUARDIAN),
    TROPICAL_FISH("tropical_fish", "Tropical Fish", "TROPICAL_FISH", PetCategory.SUPPORT, PetMovementType.SWIMMING, false, 14.0, 2.0, 1.5, 0.30, 0.35, 0.10, 0.08, 0.003, "Coral Shimmer", "Reef Healing", SkilltreeType.GUARDIAN),
    TURTLE("turtle", "Turtle", "TURTLE", PetCategory.SUPPORT, PetMovementType.AMPHIBIOUS, false, 30.0, 3.5, 6.0, 0.22, 0.9, 0.15, 0.30, 0.001, "Shell Armor", "Reef Shield", SkilltreeType.GUARDIAN),

    // ==========================================
    // Powerful Bosses & Heavy Golems
    // ==========================================
    WARDEN("warden", "Warden", "WARDEN", PetCategory.COMBAT, PetMovementType.GROUND, false, 55.0, 9.5, 7.0, 0.24, 1.4, 0.38, 0.28, 0.001, "Sculk Vibration", "Sonic Boom", SkilltreeType.JUGGERNAUT),
    ENDER_DRAGON("ender_dragon", "Ender Dragon", "ENDER_DRAGON", PetCategory.COMBAT, PetMovementType.FLYING, false, 60.0, 10.0, 6.0, 0.26, 1.5, 0.40, 0.25, 0.001, "Dragon Breath", "Wing Buffet", SkilltreeType.JUGGERNAUT),
    WITHER("wither", "Wither", "WITHER", PetCategory.COMBAT, PetMovementType.FLYING, false, 50.0, 9.0, 5.0, 0.26, 1.3, 0.36, 0.22, 0.001, "Wither Armor", "Skull Volley", SkilltreeType.JUGGERNAUT),
    IRON_GOLEM("iron_golem", "Iron Golem", "IRON_GOLEM", PetCategory.COMBAT, PetMovementType.GROUND, false, 48.0, 8.0, 6.5, 0.22, 1.2, 0.32, 0.26, 0.001, "Ironclad", "Uppercut Fling", SkilltreeType.JUGGERNAUT),
    SNOW_GOLEM("snow_golem", "Snow Golem", "SNOW_GOLEM", PetCategory.SUPPORT, PetMovementType.GROUND, false, 18.0, 3.0, 2.0, 0.26, 0.45, 0.15, 0.10, 0.002, "Snow Trail", "Frost Barrage", SkilltreeType.GUARDIAN),
    RAVAGER("ravager", "Ravager", "RAVAGER", PetCategory.COMBAT, PetMovementType.GROUND, false, 46.0, 8.5, 5.5, 0.24, 1.15, 0.34, 0.24, 0.001, "Ramming Charge", "Roar Stun", SkilltreeType.JUGGERNAUT),

    // ==========================================
    // Undead & Monsters
    // ==========================================
    ZOMBIE("zombie", "Zombie", "ZOMBIE", PetCategory.COMBAT, PetMovementType.GROUND, false, 24.0, 5.0, 3.0, 0.25, 0.6, 0.24, 0.15, 0.001, "Undead Fortitude", "Frenzy Strike", SkilltreeType.JUGGERNAUT),
    SKELETON("skeleton", "Skeleton", "SKELETON", PetCategory.COMBAT, PetMovementType.GROUND, false, 20.0, 6.0, 2.0, 0.25, 0.5, 0.28, 0.10, 0.001, "Crackshot", "Headshot", SkilltreeType.JUGGERNAUT),
    WITHER_SKELETON("wither_skeleton", "Wither Skeleton", "WITHER_SKELETON", PetCategory.COMBAT, PetMovementType.GROUND, false, 28.0, 6.5, 3.5, 0.26, 0.75, 0.28, 0.18, 0.001, "Necrotic Touch", "Wither Curse", SkilltreeType.JUGGERNAUT),
    STRAY("stray", "Stray", "STRAY", PetCategory.COMBAT, PetMovementType.GROUND, false, 22.0, 5.5, 2.5, 0.25, 0.55, 0.25, 0.12, 0.001, "Chilling Aura", "Frostbite Arrow", SkilltreeType.JUGGERNAUT),
    BOGGED("bogged", "Bogged", "BOGGED", PetCategory.COMBAT, PetMovementType.GROUND, false, 22.0, 5.5, 2.5, 0.25, 0.55, 0.25, 0.12, 0.001, "Moss Camo", "Poison Dart", SkilltreeType.JUGGERNAUT),
    HUSK("husk", "Husk", "HUSK", PetCategory.COMBAT, PetMovementType.GROUND, false, 26.0, 5.2, 3.5, 0.25, 0.65, 0.25, 0.16, 0.001, "Desert Endurance", "Hunger Claw", SkilltreeType.JUGGERNAUT),
    CREEPER("creeper", "Creeper", "CREEPER", PetCategory.COMBAT, PetMovementType.GROUND, false, 22.0, 7.0, 2.5, 0.25, 0.55, 0.32, 0.12, 0.001, "Charge Spark", "Sonic Blast", SkilltreeType.JUGGERNAUT),
    ENDERMAN("enderman", "Enderman", "ENDERMAN", PetCategory.COMBAT, PetMovementType.GROUND, false, 32.0, 6.5, 3.0, 0.29, 0.8, 0.28, 0.14, 0.002, "Void Dodge", "Shadow Ambush", SkilltreeType.JUGGERNAUT),
    SPIDER("spider", "Spider", "SPIDER", PetCategory.COMBAT, PetMovementType.GROUND, false, 20.0, 5.0, 2.5, 0.30, 0.5, 0.22, 0.12, 0.002, "Wall Climbing", "Leap Pounce", SkilltreeType.JUGGERNAUT),
    CAVE_SPIDER("cave_spider", "Cave Spider", "CAVE_SPIDER", PetCategory.UTILITY, PetMovementType.GROUND, false, 18.0, 4.5, 2.0, 0.32, 0.45, 0.20, 0.10, 0.003, "Wall-Crawl Agility", "Web Trap", SkilltreeType.SCAVENGER),
    SILVERFISH("silverfish", "Silverfish", "SILVERFISH", PetCategory.COMBAT, PetMovementType.GROUND, false, 12.0, 3.5, 1.5, 0.32, 0.3, 0.16, 0.08, 0.003, "Infestation Swarm", "Stone Burrow", SkilltreeType.JUGGERNAUT),
    ENDERMITE("endermite", "Endermite", "ENDERMITE", PetCategory.COMBAT, PetMovementType.GROUND, false, 12.0, 3.5, 1.5, 0.30, 0.3, 0.16, 0.08, 0.003, "Void Mite", "Dimensional Nip", SkilltreeType.JUGGERNAUT),
    SLIME("slime", "Slime", "SLIME", PetCategory.COMBAT, PetMovementType.GROUND, false, 22.0, 4.5, 2.5, 0.26, 0.55, 0.20, 0.12, 0.002, "Elastic Bounce", "Sticky Slap", SkilltreeType.JUGGERNAUT),
    MAGMA_CUBE("magma_cube", "Magma Cube", "MAGMA_CUBE", PetCategory.COMBAT, PetMovementType.GROUND, false, 24.0, 5.0, 3.5, 0.26, 0.6, 0.24, 0.15, 0.002, "Molten Core", "Fire Leap", SkilltreeType.JUGGERNAUT),
    SHULKER("shulker", "Shulker", "SHULKER", PetCategory.COMBAT, PetMovementType.GROUND, false, 30.0, 4.5, 7.0, 0.18, 0.8, 0.20, 0.32, 0.001, "Shell Fortress", "Levitation Spark", SkilltreeType.JUGGERNAUT),
    WITCH("witch", "Witch", "WITCH", PetCategory.SUPPORT, PetMovementType.GROUND, false, 26.0, 4.5, 3.0, 0.25, 0.7, 0.20, 0.15, 0.001, "Brew Mastery", "Splash Salve", SkilltreeType.GUARDIAN),

    // ==========================================
    // Illagers & Nether Tribes
    // ==========================================
    PILLAGER("pillager", "Pillager", "PILLAGER", PetCategory.COMBAT, PetMovementType.GROUND, false, 24.0, 5.5, 3.0, 0.26, 0.6, 0.26, 0.14, 0.001, "Crossbow Drill", "Piercing Bolt", SkilltreeType.JUGGERNAUT),
    VINDICATOR("vindicator", "Vindicator", "VINDICATOR", PetCategory.COMBAT, PetMovementType.GROUND, false, 28.0, 7.0, 3.5, 0.28, 0.75, 0.32, 0.16, 0.001, "Johnny Rage", "Axe Cleave", SkilltreeType.JUGGERNAUT),
    EVOKER("evoker", "Evoker", "EVOKER", PetCategory.COMBAT, PetMovementType.GROUND, false, 28.0, 6.0, 3.0, 0.25, 0.7, 0.28, 0.14, 0.001, "Fang Ritual", "Vex Summon", SkilltreeType.JUGGERNAUT),
    ILLUSIONER("illusioner", "Illusioner", "ILLUSIONER", PetCategory.COMBAT, PetMovementType.GROUND, false, 28.0, 6.0, 3.0, 0.27, 0.7, 0.28, 0.14, 0.002, "Mirror Mirage", "Blindness Shot", SkilltreeType.JUGGERNAUT),
    PIGLIN("piglin", "Piglin", "PIGLIN", PetCategory.COMBAT, PetMovementType.GROUND, false, 26.0, 6.5, 4.0, 0.26, 0.7, 0.28, 0.20, 0.001, "Brute Force", "Overpower Swing", SkilltreeType.JUGGERNAUT),
    PIGLIN_BRUTE("piglin_brute", "Piglin Brute", "PIGLIN_BRUTE", PetCategory.COMBAT, PetMovementType.GROUND, false, 36.0, 8.0, 5.0, 0.27, 0.95, 0.35, 0.24, 0.001, "Golden Fury", "Devastating Slash", SkilltreeType.JUGGERNAUT),
    ZOMBIFIED_PIGLIN("zombified_piglin", "Zombified Piglin", "ZOMBIFIED_PIGLIN", PetCategory.COMBAT, PetMovementType.GROUND, false, 30.0, 6.0, 4.5, 0.25, 0.8, 0.26, 0.20, 0.001, "Vengeance", "Golden Bastion", SkilltreeType.JUGGERNAUT),
    HOGLIN("hoglin", "Hoglin", "HOGLIN", PetCategory.COMBAT, PetMovementType.GROUND, false, 32.0, 6.5, 4.0, 0.26, 0.85, 0.28, 0.18, 0.001, "Tusk Toss", "Heavy Charge", SkilltreeType.JUGGERNAUT),
    ZOGLIN("zoglin", "Zoglin", "ZOGLIN", PetCategory.COMBAT, PetMovementType.GROUND, false, 34.0, 7.0, 4.0, 0.26, 0.9, 0.30, 0.18, 0.001, "Rotten Tusk", "Frenzied Charge", SkilltreeType.JUGGERNAUT),
    STRIDER("strider", "Strider", "STRIDER", PetCategory.UTILITY, PetMovementType.GROUND, false, 24.0, 3.5, 3.0, 0.24, 0.6, 0.15, 0.14, 0.002, "Lava Walker", "Warm Stride", SkilltreeType.SCAVENGER),

    // ==========================================
    // Passive Animals & Steeds
    // ==========================================
    HORSE("horse", "Horse", "HORSE", PetCategory.UTILITY, PetMovementType.GROUND, false, 28.0, 4.5, 3.0, 0.30, 0.7, 0.20, 0.15, 0.003, "Gallop Endurance", "High Leap", SkilltreeType.SCAVENGER),
    DONKEY("donkey", "Donkey", "DONKEY", PetCategory.UTILITY, PetMovementType.GROUND, false, 26.0, 3.5, 3.0, 0.25, 0.65, 0.16, 0.15, 0.002, "Pannier Pack", "Steadfast", SkilltreeType.SCAVENGER),
    MULE("mule", "Mule", "MULE", PetCategory.UTILITY, PetMovementType.GROUND, false, 26.0, 4.0, 3.0, 0.26, 0.65, 0.18, 0.15, 0.002, "Hybrid Vigor", "Heavy Load", SkilltreeType.SCAVENGER),
    SKELETON_HORSE("skeleton_horse", "Skeleton Horse", "SKELETON_HORSE", PetCategory.UTILITY, PetMovementType.GROUND, false, 26.0, 4.5, 3.0, 0.30, 0.65, 0.20, 0.15, 0.003, "Undead Steed", "Thunder Dash", SkilltreeType.SCAVENGER),
    ZOMBIE_HORSE("zombie_horse", "Zombie Horse", "ZOMBIE_HORSE", PetCategory.UTILITY, PetMovementType.GROUND, false, 26.0, 4.5, 3.0, 0.28, 0.65, 0.20, 0.15, 0.003, "Rotten Gait", "Enduring Stride", SkilltreeType.SCAVENGER),
    CAMEL("camel", "Camel", "CAMEL", PetCategory.UTILITY, PetMovementType.GROUND, false, 32.0, 4.0, 3.5, 0.26, 0.8, 0.18, 0.16, 0.002, "Desert Dash", "Tall Stance", SkilltreeType.SCAVENGER),
    LLAMA("llama", "Llama", "LLAMA", PetCategory.UTILITY, PetMovementType.GROUND, false, 24.0, 4.0, 2.5, 0.25, 0.6, 0.18, 0.12, 0.002, "Caravan Guard", "Spit Volley", SkilltreeType.SCAVENGER),
    TRADER_LLAMA("trader_llama", "Trader Llama", "TRADER_LLAMA", PetCategory.UTILITY, PetMovementType.GROUND, false, 24.0, 4.0, 2.5, 0.25, 0.6, 0.18, 0.12, 0.002, "Merchant Pack", "Merchant Spit", SkilltreeType.SCAVENGER),
    COW("cow", "Cow", "COW", PetCategory.SUPPORT, PetMovementType.GROUND, false, 20.0, 3.0, 2.5, 0.25, 0.5, 0.12, 0.12, 0.001, "Nourishing Milk", "Bovine Stampede", SkilltreeType.GUARDIAN),
    MOOSHROOM("mooshroom", "Mooshroom", "MOOSHROOM", PetCategory.SUPPORT, PetMovementType.GROUND, false, 20.0, 3.0, 2.5, 0.25, 0.5, 0.12, 0.12, 0.001, "Stew Blessing", "Spore Cloud", SkilltreeType.GUARDIAN),
    SHEEP("sheep", "Sheep", "SHEEP", PetCategory.SUPPORT, PetMovementType.GROUND, false, 18.0, 2.5, 3.0, 0.25, 0.45, 0.10, 0.14, 0.001, "Soft Wool", "Woolen Cushion", SkilltreeType.GUARDIAN),
    PIG("pig", "Pig", "PIG", PetCategory.SUPPORT, PetMovementType.GROUND, false, 18.0, 3.0, 2.0, 0.25, 0.45, 0.12, 0.10, 0.002, "Rooter Snout", "Mud Slide", SkilltreeType.GUARDIAN),
    CHICKEN("chicken", "Chicken", "CHICKEN", PetCategory.UTILITY, PetMovementType.GROUND, false, 12.0, 2.0, 1.0, 0.28, 0.3, 0.08, 0.06, 0.003, "Feather Fall", "Egg Drop", SkilltreeType.SCAVENGER),
    RABBIT("rabbit", "Rabbit", "RABBIT", PetCategory.UTILITY, PetMovementType.GROUND, false, 14.0, 2.5, 1.5, 0.32, 0.35, 0.10, 0.08, 0.003, "Lucky Foot", "Burrow Hop", SkilltreeType.SCAVENGER),
    PANDA("panda", "Panda", "PANDA", PetCategory.SUPPORT, PetMovementType.GROUND, false, 26.0, 4.5, 3.5, 0.24, 0.7, 0.20, 0.16, 0.001, "Bamboo Chew", "Panda Roll", SkilltreeType.GUARDIAN),
    POLAR_BEAR("polar_bear", "Polar Bear", "POLAR_BEAR", PetCategory.COMBAT, PetMovementType.GROUND, false, 32.0, 6.5, 4.0, 0.26, 0.85, 0.28, 0.18, 0.001, "Arctic Maul", "Rear Swipe", SkilltreeType.JUGGERNAUT),
    SNIFFER("sniffer", "Sniffer", "SNIFFER", PetCategory.SUPPORT, PetMovementType.GROUND, false, 36.0, 4.5, 4.5, 0.22, 0.95, 0.18, 0.22, 0.001, "Ancient Seed Sniff", "Primal Dig", SkilltreeType.GUARDIAN),
    ARMADILLO("armadillo", "Armadillo", "ARMADILLO", PetCategory.SUPPORT, PetMovementType.GROUND, false, 20.0, 3.0, 5.0, 0.25, 0.5, 0.12, 0.24, 0.001, "Scute Shell", "Ball Roll", SkilltreeType.GUARDIAN),
    GOAT("goat", "Goat", "GOAT", PetCategory.COMBAT, PetMovementType.GROUND, false, 22.0, 5.0, 2.5, 0.28, 0.55, 0.24, 0.12, 0.002, "High Jump", "Horn Ram", SkilltreeType.JUGGERNAUT),

    // ==========================================
    // Humanoids & Villagers
    // ==========================================
    VILLAGER("villager", "Villager", "VILLAGER", PetCategory.SUPPORT, PetMovementType.GROUND, false, 22.0, 3.0, 2.5, 0.25, 0.55, 0.12, 0.12, 0.001, "Emerald Fortune", "Hero Trade", SkilltreeType.GUARDIAN),
    WANDERING_TRADER("wandering_trader", "Wandering Trader", "WANDERING_TRADER", PetCategory.UTILITY, PetMovementType.GROUND, false, 22.0, 3.0, 2.5, 0.25, 0.55, 0.12, 0.12, 0.002, "Exotic Discovery", "Invisibility Trick", SkilltreeType.SCAVENGER),
    ZOMBIE_VILLAGER("zombie_villager", "Zombie Villager", "ZOMBIE_VILLAGER", PetCategory.COMBAT, PetMovementType.GROUND, false, 24.0, 5.0, 3.0, 0.25, 0.6, 0.24, 0.15, 0.001, "Infected Trade", "Undead Strike", SkilltreeType.JUGGERNAUT),

    // ==========================================
    // Baby Mob Variants
    // ==========================================
    BABY_ZOMBIE("baby_zombie", "Baby Zombie", "ZOMBIE", PetCategory.COMBAT, PetMovementType.GROUND, true, 24.0, 5.0, 3.0, 0.28, 0.6, 0.25, 0.15, 0.002, "Relentless", "Frenzy Strike", SkilltreeType.JUGGERNAUT),
    BABY_DROWNED("baby_drowned", "Baby Drowned", "DROWNED", PetCategory.SUPPORT, PetMovementType.AMPHIBIOUS, true, 26.0, 4.5, 3.5, 0.27, 0.75, 0.20, 0.18, 0.002, "Tide Blessing", "Aqua Leach", SkilltreeType.GUARDIAN),
    BABY_ZOMBIFIED_PIGLIN("baby_zombified_piglin", "Baby Zombified Piglin", "ZOMBIFIED_PIGLIN", PetCategory.SUPPORT, PetMovementType.GROUND, true, 32.0, 4.5, 5.0, 0.26, 0.9, 0.15, 0.25, 0.001, "Vengeance", "Golden Bastion", SkilltreeType.GUARDIAN);

    private final String id;
    private final String displayName;
    private final String entityTypeName;
    private final PetCategory category;
    private final PetMovementType movementType;
    private final boolean baby;
    private final double baseHp;
    private final double baseAtk;
    private final double baseDef;
    private final double baseSpd;
    private final double hpGainPerLevel;
    private final double atkGainPerLevel;
    private final double defGainPerLevel;
    private final double spdGainPerLevel;
    private final String passiveName;
    private final String activeSkillName;
    private final SkilltreeType defaultSkilltree;

    PetType(String id, String displayName, String entityTypeName, PetCategory category,
            PetMovementType movementType, boolean baby,
            double baseHp, double baseAtk, double baseDef, double baseSpd,
            double hpGainPerLevel, double atkGainPerLevel, double defGainPerLevel, double spdGainPerLevel,
            String passiveName, String activeSkillName, SkilltreeType defaultSkilltree) {
        this.id = id;
        this.displayName = displayName;
        this.entityTypeName = entityTypeName;
        this.category = category;
        this.movementType = movementType;
        this.baby = baby;
        this.baseHp = baseHp;
        this.baseAtk = baseAtk;
        this.baseDef = baseDef;
        this.baseSpd = baseSpd;
        this.hpGainPerLevel = hpGainPerLevel;
        this.atkGainPerLevel = atkGainPerLevel;
        this.defGainPerLevel = defGainPerLevel;
        this.spdGainPerLevel = spdGainPerLevel;
        this.passiveName = passiveName;
        this.activeSkillName = activeSkillName;
        this.defaultSkilltree = defaultSkilltree;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public PetCategory getCategory() { return category; }
    public PetMovementType getMovementType() { return movementType; }
    public boolean isBaby() { return baby; }
    public String getPassiveName() { return passiveName; }
    public String getActiveSkillName() { return activeSkillName; }
    public SkilltreeType getDefaultSkilltree() { return defaultSkilltree; }

    @Nullable
    public EntityType getEntityType() {
        try {
            return EntityType.valueOf(entityTypeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public double calculateMaxHp(int level) {
        return baseHp + (level - 1) * hpGainPerLevel;
    }

    public double calculateAtk(int level) {
        return baseAtk + (level - 1) * atkGainPerLevel;
    }

    public double calculateDef(int level) {
        return baseDef + (level - 1) * defGainPerLevel;
    }

    public double calculateSpd(int level) {
        return Math.min(0.50, baseSpd + (level - 1) * spdGainPerLevel);
    }

    public double getPassiveValue(int level) {
        return 0.10 + (level * 0.003);
    }

    public double getActiveSkillValue(int level) {
        return 2.0 + (level * 0.2);
    }

    public String getPassiveDescription(int level) {
        return String.format(Locale.ROOT, "%s: Grants +%.1f%% combat efficiency scaling with level.", passiveName, getPassiveValue(level) * 100);
    }

    public String getActiveSkillDescription(int level) {
        return String.format(Locale.ROOT, "%s: Triggers specialized active combat ability dealing %.1f power.", activeSkillName, getActiveSkillValue(level));
    }

    @Nullable
    public static PetType fromString(@NotNull String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            for (PetType type : values()) {
                if (type.id.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
