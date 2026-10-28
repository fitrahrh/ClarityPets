package com.harukiyu.claritypets.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.PetMovementType;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PetFloatGoal implements Goal<Mob> {

    private final GoalKey<Mob> key;
    private final Mob mob;
    private final PlayerPetData petData;

    public PetFloatGoal(@NotNull ClarityPets plugin, @NotNull Mob mob, @NotNull PlayerPetData petData) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "pet_float"));
        this.mob = mob;
        this.petData = petData;
        mob.getPathfinder().setCanFloat(true);
    }

    @Override
    public boolean shouldActivate() {
        return mob.isInWater() || mob.isInLava();
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        // Water-breathers & swimming creatures swim freely; ground mobs float upward to breathe
        PetMovementType movement = petData.getPetType().getMovementType();
        if (movement != PetMovementType.SWIMMING) {
            Vector velocity = mob.getVelocity();
            if (velocity.getY() < 0.08) {
                mob.setVelocity(new Vector(velocity.getX(), 0.08, velocity.getZ()));
            }
        }
    }

    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.noneOf(GoalType.class);
    }
}
