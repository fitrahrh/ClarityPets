package com.harukiyu.claritypets.model;

import org.jetbrains.annotations.NotNull;

public enum PetCategory {
    COMBAT,
    UTILITY,
    SUPPORT;

    public boolean hasAdvantageOver(@NotNull PetCategory opponent) {
        return (this == UTILITY && opponent == COMBAT)
                || (this == SUPPORT && opponent == UTILITY)
                || (this == COMBAT && opponent == SUPPORT);
    }
}
