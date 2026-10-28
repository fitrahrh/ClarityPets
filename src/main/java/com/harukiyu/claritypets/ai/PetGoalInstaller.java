package com.harukiyu.claritypets.ai;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public final class PetGoalInstaller {

    private PetGoalInstaller() {}

    public static void install(@NotNull ClarityPets plugin, @NotNull Mob mob, @NotNull PlayerPetData petData) {
        MobGoals mobGoals = Bukkit.getMobGoals();

        // 1. Remove all vanilla AI goals and target selectors to prevent hostile/unwanted wandering
        try {
            mobGoals.removeAllGoals(mob);
        } catch (Throwable ignored) {}

        // 2. Install our pure Paper API AI goals
        mobGoals.addGoal(mob, 0, new PetFloatGoal(plugin, mob, petData));
        mobGoals.addGoal(mob, 1, new PetAttackGoal(plugin, mob, petData));
        mobGoals.addGoal(mob, 2, new PetFollowOwnerGoal(plugin, mob, petData));
        mobGoals.addGoal(mob, 3, new PetPickupGoal(plugin, mob, petData));
        mobGoals.addGoal(mob, 4, new PetLookAtOwnerGoal(plugin, mob, petData));
    }
}
