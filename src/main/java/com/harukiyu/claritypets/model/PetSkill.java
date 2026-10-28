package com.harukiyu.claritypets.model;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum PetSkill {
    // Combat Skills
    DAMAGE("damage", "Damage", PetCategory.COMBAT, 5, "Increases pet melee and ranged attack damage."),
    LIFE("life", "Vitality", PetCategory.COMBAT, 5, "Increases pet maximum health points."),
    ARMOR("armor", "Iron Shell", PetCategory.COMBAT, 5, "Grants bonus armor defense, mitigating incoming damage."),
    THORNS("thorns", "Spiked Carapace", PetCategory.COMBAT, 5, "Reflects a percentage of incoming melee damage back to the attacker."),
    BLEED("bleed", "Laceration", PetCategory.COMBAT, 5, "Chance to inflict bleeding wounds, dealing true damage over time."),
    FIRE("fire", "Ignition", PetCategory.COMBAT, 5, "Attacks set enemies ablaze for a duration."),
    KNOCKBACK("knockback", "Impact Force", PetCategory.COMBAT, 5, "Increases the knockback impulse applied to struck targets."),
    STOMP("stomp", "Earthquake Stomp", PetCategory.COMBAT, 5, "Active ability: periodically stomps the ground dealing AoE damage to nearby foes."),

    // Support Skills
    HEAL("heal", "Soothing Presence", PetCategory.SUPPORT, 5, "Passively regenerates pet health and restores owner health when close."),
    BEACON("beacon", "Inspiring Aura", PetCategory.SUPPORT, 5, "Radiates beneficial beacon status auras (Speed, Regen, Resistance, Haste) to owner."),
    SHIELD("shield", "Aegis Guard", PetCategory.SUPPORT, 5, "Chance to completely block an incoming attack and negate damage."),
    TAUNT("taunt", "Provocation", PetCategory.SUPPORT, 5, "Forces nearby hostile monsters to target the pet instead of the owner."),
    SLOW("slow", "Frostbite", PetCategory.SUPPORT, 5, "Attacks apply freezing slowness to the target."),
    POISON("poison", "Toxic Fangs", PetCategory.SUPPORT, 5, "Attacks inflict corrosive poison on the target."),
    WITHER("wither", "Decay Touch", PetCategory.SUPPORT, 5, "Attacks afflict targets with life-draining wither decay."),
    POTION("potion", "Alchemical Infusion", PetCategory.SUPPORT, 5, "Bestows emergency absorption and resistance when owner drops to low health."),

    // Utility Skills
    BACKPACK("backpack", "Pack Mule", PetCategory.UTILITY, 5, "Grants a dedicated virtual storage backpack for your pet."),
    PICKUP("pickup", "Item Fetcher", PetCategory.UTILITY, 5, "Pet actively seeks out nearby dropped items and stores them into its backpack."),
    SPRINT("sprint", "Swift Trot", PetCategory.UTILITY, 5, "Increases pet movement speed and responsiveness when following."),
    MINING("mining", "Prospector Instinct", PetCategory.UTILITY, 5, "Grants a chance for bonus ore yields and double EXP when owner mines."),
    LUMBERJACK("lumberjack", "Timber Harvester", PetCategory.UTILITY, 5, "Grants a chance for bonus wood and saplings when owner chops trees."),
    FISHING("fishing", "Angler Companion", PetCategory.UTILITY, 5, "Grants a chance for double fish catches and rare treasure while fishing."),
    CAMOUFLAGE("camouflage", "Shadow Veil", PetCategory.UTILITY, 5, "Dampens monster awareness, reducing aggro detection range around player.");

    private final String id;
    private final String displayName;
    private final PetCategory category;
    private final int maxTier;
    private final String defaultDescription;

    PetSkill(String id, String displayName, PetCategory category, int maxTier, String defaultDescription) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.maxTier = maxTier;
        this.defaultDescription = defaultDescription;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public PetCategory getCategory() { return category; }
    public int getMaxTier() { return maxTier; }
    public String getDefaultDescription() { return defaultDescription; }

    /**
     * Returns human-readable effect string for a given tier (1..maxTier).
     */
    public String getTierDescription(int tier) {
        if (tier <= 0) return "Locked (Not learned)";
        return switch (this) {
            case DAMAGE -> String.format(Locale.ROOT, "+%.1f Bonus Attack Damage", tier * 2.0);
            case LIFE -> String.format(Locale.ROOT, "+%d Bonus Max Health", tier * 12);
            case ARMOR -> String.format(Locale.ROOT, "+%d Bonus Armor Defense", tier * 3);
            case THORNS -> String.format(Locale.ROOT, "Reflects %d%% physical damage back to attacker", 10 + (tier * 6));
            case BLEED -> String.format(Locale.ROOT, "%d%% chance to inflict bleed (2 true damage/sec for 4s)", 15 + (tier * 7));
            case FIRE -> String.format(Locale.ROOT, "Sets target on fire for %d seconds", 2 + tier);
            case KNOCKBACK -> String.format(Locale.ROOT, "+%d%% attack knockback impulse", tier * 25);
            case STOMP -> String.format(Locale.ROOT, "Every 15s in combat: stomps ground for %.1f AoE damage", 3.0 + (tier * 2.0));
            case HEAL -> String.format(Locale.ROOT, "+%.1f HP/s pet regen; restores 1.0 HP to owner every %ds", 0.4 * tier, Math.max(4, 12 - tier));
            case BEACON -> switch (tier) {
                case 1 -> "Grants Speed I aura to owner within 12 blocks";
                case 2 -> "Grants Speed I & Regeneration I aura to owner";
                case 3 -> "Grants Speed I, Regeneration I & Resistance I aura";
                case 4 -> "Grants Speed II, Regeneration I, Resistance I & Haste I aura";
                default -> "Grants Speed II, Regeneration II, Resistance II & Haste II aura";
            };
            case SHIELD -> String.format(Locale.ROOT, "%d%% chance to completely deflect an attack", 10 + (tier * 5));
            case TAUNT -> String.format(Locale.ROOT, "Every 10s: taunts all monsters within %d blocks to attack pet", 5 + (tier * 2));
            case SLOW -> String.format(Locale.ROOT, "Slows enemies by %d%% for 3 seconds on hit", 15 + (tier * 7));
            case POISON -> String.format(Locale.ROOT, "Poisons enemies for %d seconds on hit", 2 + tier);
            case WITHER -> String.format(Locale.ROOT, "Inflicts Wither decay for %d seconds on hit", 1 + tier);
            case POTION -> String.format(Locale.ROOT, "When owner HP < 35%%: grants Absorption II & Resistance I (%ds cooldown)", Math.max(30, 90 - (tier * 10)));
            case BACKPACK -> String.format(Locale.ROOT, "Provides %d slots of pet inventory storage (%d rows)", tier * 9, tier);
            case PICKUP -> String.format(Locale.ROOT, "Pet collects dropped items within %d blocks into backpack", 3 + tier);
            case SPRINT -> String.format(Locale.ROOT, "+%.2f Pet Movement & Follow Speed", tier * 0.04);
            case MINING -> String.format(Locale.ROOT, "%d%% chance for double ore drops & +20%% mining EXP", 10 + (tier * 6));
            case LUMBERJACK -> String.format(Locale.ROOT, "%d%% chance for bonus wood drops & +20%% foraging EXP", 10 + (tier * 6));
            case FISHING -> String.format(Locale.ROOT, "%d%% chance for double catch & rare fishing treasure", 10 + (tier * 6));
            case CAMOUFLAGE -> String.format(Locale.ROOT, "Reduces hostile monster detection range by %d%%", 15 + (tier * 7));
        };
    }

    public static PetSkill fromId(@NotNull String id) {
        for (PetSkill skill : values()) {
            if (skill.id.equalsIgnoreCase(id) || skill.name().equalsIgnoreCase(id)) {
                return skill;
            }
        }
        return null;
    }
}
