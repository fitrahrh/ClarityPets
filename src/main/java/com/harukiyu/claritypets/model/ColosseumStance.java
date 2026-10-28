package com.harukiyu.claritypets.model;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ColosseumStance {
    AGGRESSIVE(1.15, 0.90, 0.0),
    DEFENSIVE(1.0, 1.20, 0.10),
    BALANCED(1.0, 1.0, 0.0);

    private final double attackMultiplier;
    private final double defenseMultiplier;
    private final double procBonus;

    ColosseumStance(double attackMultiplier, double defenseMultiplier, double procBonus) {
        this.attackMultiplier = attackMultiplier;
        this.defenseMultiplier = defenseMultiplier;
        this.procBonus = procBonus;
    }

    public double getAttackMultiplier() {
        return attackMultiplier;
    }

    public double getDefenseMultiplier() {
        return defenseMultiplier;
    }

    public double getProcBonus() {
        return procBonus;
    }

    @NotNull
    public static ColosseumStance fromString(@NotNull String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BALANCED;
        }
    }
}
