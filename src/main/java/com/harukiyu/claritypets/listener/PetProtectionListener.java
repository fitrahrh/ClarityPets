package com.harukiyu.claritypets.listener;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.ActivePet;
import com.harukiyu.claritypets.model.BehaviorMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

public class PetProtectionListener implements Listener {

    private final ClarityPets plugin;

    public PetProtectionListener(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(@NotNull EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        ActivePet victimPet = plugin.getPetManager().getActivePetByEntity(victim.getUniqueId());
        ActivePet damagerPet = plugin.getPetManager().getActivePetByEntity(damager.getUniqueId());

        // Don't interfere if part of colosseum match (handled by ColosseumArenaListener)
        if (victimPet != null && victimPet.isDuelClone()) return;
        if (damagerPet != null && damagerPet.isDuelClone()) return;

        // 1. If victim is a summoned pet
        if (victimPet != null) {
            // Passive mode: completely invulnerable
            if (victimPet.getPetData().getBehaviorMode() == BehaviorMode.PASSIVE) {
                event.setCancelled(true);
                return;
            }

            // Owner cannot damage their own pet
            if (damager.getUniqueId().equals(victimPet.getOwnerUuid())) {
                event.setCancelled(true);
                return;
            }
        }

        // 2. If damager is a summoned pet
        if (damagerPet != null) {
            // Pet never damages its owner
            if (victim.getUniqueId().equals(damagerPet.getOwnerUuid())) {
                event.setCancelled(true);
                return;
            }
        }

        // Extract real attacker (handles both direct melee and ranged projectile attacks)
        LivingEntity actualAttacker = null;
        if (damager instanceof LivingEntity le) {
            actualAttacker = le;
        } else if (damager instanceof Projectile proj && proj.getShooter() instanceof LivingEntity shooter) {
            actualAttacker = shooter;
        }

        // 3. Assist / Retaliation triggers for DEFENSIVE & AGGRESSIVE pets
        // A. Owner is attacked -> Pet defends owner
        if (victim instanceof Player p && actualAttacker != null) {
            ActivePet pet = plugin.getPetManager().getActivePet(p);
            if (pet != null && !pet.isDuelClone() && pet.isValid()) {
                BehaviorMode mode = pet.getPetData().getBehaviorMode();
                if (mode == BehaviorMode.DEFENSIVE || mode == BehaviorMode.AGGRESSIVE) {
                    if (!actualAttacker.getUniqueId().equals(p.getUniqueId())) {
                        pet.getEntity().setTarget(actualAttacker);
                    }
                }
            }
        }

        // B. Owner attacks a mob -> Pet assists owner
        if (damager instanceof Player p && victim instanceof LivingEntity hostile) {
            ActivePet pet = plugin.getPetManager().getActivePet(p);
            if (pet != null && !pet.isDuelClone() && pet.isValid()) {
                BehaviorMode mode = pet.getPetData().getBehaviorMode();
                if (mode == BehaviorMode.DEFENSIVE || mode == BehaviorMode.AGGRESSIVE) {
                    if (!hostile.getUniqueId().equals(p.getUniqueId()) && !hostile.getUniqueId().equals(pet.getEntity().getUniqueId())) {
                        pet.getEntity().setTarget(hostile);
                    }
                }
            }
        }

        // C. Pet itself is attacked -> Pet retaliates
        if (victimPet != null && actualAttacker != null && !victimPet.isDuelClone() && victimPet.isValid()) {
            BehaviorMode mode = victimPet.getPetData().getBehaviorMode();
            if (mode == BehaviorMode.DEFENSIVE || mode == BehaviorMode.AGGRESSIVE) {
                if (!actualAttacker.getUniqueId().equals(victimPet.getOwnerUuid()) && !actualAttacker.getUniqueId().equals(victimPet.getEntity().getUniqueId())) {
                    victimPet.getEntity().setTarget(actualAttacker);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityCombust(@NotNull EntityCombustEvent event) {
        if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent) {
            return;
        }
        // Cancel daylight combustion for pets
        ActivePet pet = plugin.getPetManager().getActivePetByEntity(event.getEntity().getUniqueId());
        if (pet != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEndermanChangeBlock(@NotNull EntityChangeBlockEvent event) {
        ActivePet pet = plugin.getPetManager().getActivePetByEntity(event.getEntity().getUniqueId());
        if (pet != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPetTarget(@NotNull EntityTargetLivingEntityEvent event) {
        ActivePet pet = plugin.getPetManager().getActivePetByEntity(event.getEntity().getUniqueId());
        if (pet == null) return;

        // Skip duel clones
        if (pet.isDuelClone()) return;

        // Pet must never target its owner
        Entity target = event.getTarget();
        if (target != null && target.getUniqueId().equals(pet.getOwnerUuid())) {
            event.setCancelled(true);
        }

        // Passive pet must never target anything
        if (pet.getPetData().getBehaviorMode() == BehaviorMode.PASSIVE) {
            event.setCancelled(true);
        }
    }
}
