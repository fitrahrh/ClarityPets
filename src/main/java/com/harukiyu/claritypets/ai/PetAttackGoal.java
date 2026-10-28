package com.harukiyu.claritypets.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.BehaviorMode;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PetAttackGoal implements Goal<Mob> {

    private final ClarityPets plugin;
    private final GoalKey<Mob> key;
    private final Mob mob;
    private final PlayerPetData petData;
    private int attackCooldown = 0;

    public PetAttackGoal(@NotNull ClarityPets plugin, @NotNull Mob mob, @NotNull PlayerPetData petData) {
        this.plugin = plugin;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "pet_attack"));
        this.mob = mob;
        this.petData = petData;
    }

    private Player getOwner() {
        return Bukkit.getPlayer(petData.getPlayerUuid());
    }

    @Override
    public boolean shouldActivate() {
        if (petData.getBehaviorMode() == BehaviorMode.PASSIVE) {
            mob.setTarget(null);
            return false;
        }

        Player owner = getOwner();
        if (owner == null || !owner.isOnline() || !owner.getWorld().equals(mob.getWorld())) {
            mob.setTarget(null);
            return false;
        }

        // Break off combat if owner is too far away (> 25 blocks)
        if (mob.getLocation().distanceSquared(owner.getLocation()) > 25.0 * 25.0) {
            mob.setTarget(null);
            return false;
        }

        LivingEntity target = mob.getTarget();

        // Clear target if dead, invalid, or targeting owner
        if (target != null && (!target.isValid() || target.isDead() || target.equals(owner))) {
            mob.setTarget(null);
            target = null;
        }

        // Proactive aggro scan in AGGRESSIVE mode if no target exists
        if (target == null && petData.getBehaviorMode() == BehaviorMode.AGGRESSIVE) {
            target = scanForAggressiveTarget(owner);
            if (target != null) {
                mob.setTarget(target);
            }
        }

        return target != null && target.isValid() && !target.isDead();
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void start() {
        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getPathfinder().moveTo(target.getLocation(), 1.25);
        }
    }

    @Override
    public void stop() {
        mob.getPathfinder().stopPathfinding();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        Player owner = getOwner();

        if (target == null || !target.isValid() || target.isDead() || (owner != null && target.equals(owner))) {
            mob.setTarget(null);
            return;
        }

        // If owner is too far, stop attacking and return to owner
        if (owner != null && (!mob.getWorld().equals(owner.getWorld()) || mob.getLocation().distanceSquared(owner.getLocation()) > 25.0 * 25.0)) {
            mob.setTarget(null);
            return;
        }

        double distSq = mob.getLocation().distanceSquared(target.getLocation());

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Dynamic melee reach based on mob size
        double reach = (mob.getWidth() * 1.5) + (target.getWidth() * 1.5) + 0.8;
        double reachSq = Math.max(2.8 * 2.8, reach * reach);

        if (distSq <= reachSq) {
            if (attackCooldown <= 0) {
                // Strike target using safe damage attribution that works for all mobs (including passive animals like Cow)
                double damage = Math.max(1.0, petData.getAtk());
                target.damage(damage, mob);

                // Play swing animation if supported by mob entity
                try {
                    mob.swingMainHand();
                } catch (Throwable ignored) {}

                // Sound & particle feedback
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.7f, 1.2f);
                mob.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().clone().add(0, target.getHeight() * 0.5, 0), 1);

                attackCooldown = 20; // 1 second attack cadence
            }
        } else {
            // Pathfind towards target
            if (mob.getTicksLived() % 5 == 0) {
                mob.getPathfinder().moveTo(target.getLocation(), 1.25);
            }
        }
    }

    private LivingEntity scanForAggressiveTarget(Player owner) {
        double radius = 10.0;
        LivingEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity e : mob.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !le.isDead() && le.isValid()) {
                // Never target players in survival mode
                if (le instanceof Player) continue;

                // Never target friendly pets
                if (plugin.getPetManager().getActivePetByEntity(le.getUniqueId()) != null) continue;

                // Target hostile monsters
                if (le instanceof Monster || le instanceof Phantom || le instanceof Slime || le instanceof Hoglin) {
                    double d = mob.getLocation().distanceSquared(le.getLocation());
                    if (d < closestDistSq) {
                        closestDistSq = d;
                        closest = le;
                    }
                }
            }
        }
        return closest;
    }

    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET, GoalType.MOVE);
    }
}
