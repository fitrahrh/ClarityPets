package com.harukiyu.claritypets.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.BehaviorMode;
import com.harukiyu.claritypets.model.PetMovementType;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PetFollowOwnerGoal implements Goal<Mob> {

    private final GoalKey<Mob> key;
    private final Mob mob;
    private final PlayerPetData petData;
    private int tickCounter = 0;

    public PetFollowOwnerGoal(@NotNull ClarityPets plugin, @NotNull Mob mob, @NotNull PlayerPetData petData) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "pet_follow_owner"));
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
        if (petData.getBehaviorMode() == BehaviorMode.SENTRY) return false;

        // Follow if in different world or far enough away
        if (!mob.getWorld().equals(owner.getWorld())) return true;
        double distSq = mob.getLocation().distanceSquared(owner.getLocation());

        LivingEntity target = mob.getTarget();
        boolean inCombat = target != null && target.isValid() && !target.isDead() && petData.getBehaviorMode() != BehaviorMode.PASSIVE;
        if (inCombat) {
            // While actively engaged in combat, do not break combat unless owner is far away (> 20 blocks)
            return distSq > 20.0 * 20.0;
        }

        return distSq > 3.0 * 3.0;
    }

    @Override
    public boolean shouldStayActive() {
        Player owner = getOwner();
        if (owner == null || !owner.isOnline()) return false;
        if (petData.getBehaviorMode() == BehaviorMode.SENTRY) return false;
        if (!mob.getWorld().equals(owner.getWorld())) return true;
        double distSq = mob.getLocation().distanceSquared(owner.getLocation());

        LivingEntity target = mob.getTarget();
        boolean inCombat = target != null && target.isValid() && !target.isDead() && petData.getBehaviorMode() != BehaviorMode.PASSIVE;
        if (inCombat) {
            return distSq > 16.0 * 16.0;
        }

        return distSq > 2.5 * 2.5;
    }

    @Override
    public void start() {
        tickCounter = 0;
    }

    @Override
    public void stop() {
        mob.getPathfinder().stopPathfinding();
    }

    @Override
    public void tick() {
        Player owner = getOwner();
        if (owner == null || !owner.isOnline()) return;

        Location ownerLoc = owner.getLocation();
        Location mobLoc = mob.getLocation();

        // 1. Cross-world or extreme distance teleport
        if (!mob.getWorld().equals(ownerLoc.getWorld())) {
            mob.teleportAsync(ownerLoc);
            return;
        }

        double distSq = mobLoc.distanceSquared(ownerLoc);

        if (distSq > 25.0 * 25.0) {
            // Find a safe spot near the owner to teleport
            mob.teleportAsync(ownerLoc);
            return;
        }

        PetMovementType movement = petData.getPetType().getMovementType();

        // 2. Flying Pets: Smooth 3D velocity gliding & hover
        if (movement == PetMovementType.FLYING) {
            double targetY = ownerLoc.getY() + 1.8;
            Vector targetVec = ownerLoc.toVector().setY(targetY);
            Vector currentVec = mobLoc.toVector();
            Vector dir = targetVec.subtract(currentVec);
            double dist = dir.length();

            if (dist > 2.0) {
                dir.normalize();
                double speed = Math.min(0.45, 0.15 + (dist * 0.02));
                Vector currentVel = mob.getVelocity();
                Vector desiredVel = dir.multiply(speed);
                // Damped interpolation for ultra smooth gliding
                mob.setVelocity(currentVel.multiply(0.7).add(desiredVel.multiply(0.3)));
            } else {
                // Hover gently
                Vector currentVel = mob.getVelocity();
                mob.setVelocity(new Vector(currentVel.getX() * 0.5, 0.02, currentVel.getZ() * 0.5));
            }
            return;
        }

        // 3. Swimming Pets in Water
        if (movement == PetMovementType.SWIMMING && mob.isInWater()) {
            Vector dir = ownerLoc.toVector().subtract(mobLoc.toVector());
            if (dir.lengthSquared() > 3.0 * 3.0) {
                dir.normalize().multiply(0.35);
                mob.setVelocity(dir);
            }
            return;
        }

        // 4. Ground Pets: Smooth native pathfinding with catch-up sprint
        if (tickCounter++ % 5 == 0) {
            double baseSpeed = 1.0;
            // Sprints if owner is far away or sprinting
            if (distSq > 10.0 * 10.0 || owner.isSprinting()) {
                baseSpeed = 1.35;
            }
            mob.getPathfinder().moveTo(ownerLoc, baseSpeed);
        }
    }

    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
}
