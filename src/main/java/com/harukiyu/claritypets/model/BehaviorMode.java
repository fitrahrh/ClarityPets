package com.harukiyu.claritypets.model;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum BehaviorMode {
    PASSIVE("Follows peacefully, never attacks or engages in combat."),
    DEFENSIVE("Follows closely, actively defends against any enemy attacking owner or pet."),
    AGGRESSIVE("Proactively hunts and attacks hostile monsters nearby, aggressively defending owner."),
    SENTRY("Guards current location, attacking any hostile mobs entering the perimeter."),
    POCKET("Inactive in pet storage.");

    private final String defaultDescription;

    BehaviorMode(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    @NotNull
    public static BehaviorMode fromString(@NotNull String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return PASSIVE;
        }
    }
}
