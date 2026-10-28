package com.harukiyu.claritypets.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PetLookAtOwnerGoal implements Goal<Mob> {

    private final GoalKey<Mob> key;
    private final Mob mob;
    private final PlayerPetData petData;

    public PetLookAtOwnerGoal(@NotNull ClarityPets plugin, @NotNull Mob mob, @NotNull PlayerPetData petData) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "pet_look_at_owner"));
        this.mob = mob;
        this.petData = petData;
    }

    private Player getOwner() {
        return Bukkit.getPlayer(petData.getPlayerUuid());
    }

    @Override
    public boolean shouldActivate() {
        Player owner = getOwner();
        if (owner == null || !owner.isOnline()) return false;
        if (!mob.getWorld().equals(owner.getWorld())) return false;
        if (mob.getTarget() != null) return false;

        return mob.getLocation().distanceSquared(owner.getLocation()) <= 6.0 * 6.0;
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void tick() {
        Player owner = getOwner();
        if (owner != null && owner.isOnline()) {
            mob.lookAt(owner.getEyeLocation());
        }
    }

    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.LOOK);
    }
}
