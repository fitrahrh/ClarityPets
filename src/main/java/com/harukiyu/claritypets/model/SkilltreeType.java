package com.harukiyu.claritypets.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum SkilltreeType {
    JUGGERNAUT(
            "juggernaut",
            "Juggernaut",
            PetCategory.COMBAT,
            "Offense and frontline brawler. Focused on raw damage, critical strikes, health, and crowd combat.",
            List.of(
                    PetSkill.DAMAGE,
                    PetSkill.LIFE,
                    PetSkill.ARMOR,
                    PetSkill.THORNS,
                    PetSkill.BLEED,
                    PetSkill.FIRE,
                    PetSkill.KNOCKBACK,
                    PetSkill.STOMP
            )
    ),
    GUARDIAN(
            "guardian",
            "Guardian",
            PetCategory.SUPPORT,
            "Protector and battlefield support. Focused on healing, auras, damage mitigation, and debilitating hexes.",
            List.of(
                    PetSkill.HEAL,
                    PetSkill.BEACON,
                    PetSkill.SHIELD,
                    PetSkill.TAUNT,
                    PetSkill.SLOW,
                    PetSkill.POISON,
                    PetSkill.WITHER,
                    PetSkill.POTION
            )
    ),
    SCAVENGER(
            "scavenger",
            "Scavenger",
            PetCategory.UTILITY,
            "Exploration and resource companion. Focused on item carrying, automatic pickup, speed, and gathering fortunes.",
            List.of(
                    PetSkill.BACKPACK,
                    PetSkill.PICKUP,
                    PetSkill.SPRINT,
                    PetSkill.MINING,
                    PetSkill.LUMBERJACK,
                    PetSkill.FISHING,
                    PetSkill.CAMOUFLAGE
            )
    );

    private final String id;
    private final String displayName;
    private final PetCategory primaryCategory;
    private final String description;
    private final List<PetSkill> skills;

    SkilltreeType(String id, String displayName, PetCategory primaryCategory, String description, List<PetSkill> skills) {
        this.id = id;
        this.displayName = displayName;
        this.primaryCategory = primaryCategory;
        this.description = description;
        this.skills = skills;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public PetCategory getPrimaryCategory() { return primaryCategory; }
    public String getDescription() { return description; }
    public List<PetSkill> getSkills() { return skills; }

    public static SkilltreeType fromId(@NotNull String id) {
        for (SkilltreeType type : values()) {
            if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return JUGGERNAUT;
    }
}
