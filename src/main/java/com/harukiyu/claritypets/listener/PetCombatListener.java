package com.harukiyu.claritypets.listener;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.ActivePet;
import com.harukiyu.claritypets.model.PetSkill;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class PetCombatListener implements Listener {

    private final ClarityPets plugin;

    public PetCombatListener(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(@NotNull EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        ActivePet victimPet = plugin.getPetManager().getActivePetByEntity(victim.getUniqueId());
        ActivePet damagerPet = plugin.getPetManager().getActivePetByEntity(damager.getUniqueId());

        if (victimPet != null && victimPet.isDuelClone()) return;
        if (damagerPet != null && damagerPet.isDuelClone()) return;

        // 1. PET ATTACKS ENEMY
        if (damagerPet != null && victim instanceof LivingEntity target) {
            PlayerPetData petData = damagerPet.getPetData();

            // DAMAGE skill: Flat bonus attack damage
            int dmgTier = petData.getSkillLevel(PetSkill.DAMAGE);
            if (dmgTier > 0) {
                event.setDamage(event.getDamage() + (dmgTier * 2.0));
            }

            // FIRE skill: Set ablaze
            int fireTier = petData.getSkillLevel(PetSkill.FIRE);
            if (fireTier > 0) {
                target.setFireTicks(Math.max(target.getFireTicks(), (2 + fireTier) * 20));
            }

            // KNOCKBACK skill: Increase knockback
            int kbTier = petData.getSkillLevel(PetSkill.KNOCKBACK);
            if (kbTier > 0) {
                Vector kb = target.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
                kb.multiply(0.25 * kbTier).setY(0.15);
                target.setVelocity(target.getVelocity().add(kb));
            }

            // SLOW skill: Froze target
            int slowTier = petData.getSkillLevel(PetSkill.SLOW);
            if (slowTier > 0) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, Math.min(2, slowTier / 2)));
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().clone().add(0, 1.0, 0), 8, 0.2, 0.2, 0.2, 0.05);
            }

            // POISON skill: Toxic poison
            int poisonTier = petData.getSkillLevel(PetSkill.POISON);
            if (poisonTier > 0) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (2 + poisonTier) * 20, 0));
            }

            // WITHER skill: Decay touch
            int witherTier = petData.getSkillLevel(PetSkill.WITHER);
            if (witherTier > 0) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (1 + witherTier) * 20, 0));
            }

            // BLEED skill: Chance for true bleeding damage
            int bleedTier = petData.getSkillLevel(PetSkill.BLEED);
            if (bleedTier > 0) {
                int bleedChance = 15 + (bleedTier * 7);
                if (ThreadLocalRandom.current().nextInt(100) < bleedChance) {
                    applyBleedEffect(target);
                }
            }

            damager.getWorld().spawnParticle(Particle.CRIT, target.getLocation().clone().add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.1);
        }

        // 2. PET DEFENDS AGAINST ATTACKS
        if (victimPet != null && victim instanceof Mob petMob) {
            PlayerPetData petData = victimPet.getPetData();

            // SHIELD skill: Chance to completely deflect incoming attack
            int shieldTier = petData.getSkillLevel(PetSkill.SHIELD);
            if (shieldTier > 0) {
                int shieldChance = 10 + (shieldTier * 5);
                if (ThreadLocalRandom.current().nextInt(100) < shieldChance) {
                    event.setCancelled(true);
                    petMob.getWorld().playSound(petMob.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.1f);
                    petMob.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, petMob.getLocation().clone().add(0, 0.8, 0), 12, 0.3, 0.3, 0.3, 0.05);
                    return;
                }
            }

            // ARMOR skill: Flat damage reduction
            int armorTier = petData.getSkillLevel(PetSkill.ARMOR);
            if (armorTier > 0) {
                double reduction = Math.min(event.getDamage() * 0.5, armorTier * 1.5);
                event.setDamage(Math.max(1.0, event.getDamage() - reduction));
            }

            // THORNS skill: Reflect damage back to melee attacker
            int thornsTier = petData.getSkillLevel(PetSkill.THORNS);
            if (thornsTier > 0 && damager instanceof LivingEntity attacker) {
                int thornsPercent = 10 + (thornsTier * 6);
                double reflected = event.getDamage() * (thornsPercent / 100.0);
                if (reflected > 0.5) {
                    attacker.damage(reflected, petMob);
                    attacker.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, attacker.getLocation().clone().add(0, 1.0, 0), 4, 0.2, 0.2, 0.2, 0.05);
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.ENCHANT_THORNS_HIT, 0.8f, 1.2f);
                }
            }
        }

        // 3. OWNER TAKES DAMAGE -> EMERGENCY POTION
        if (victim instanceof Player owner) {
            ActivePet activePet = plugin.getPetManager().getActivePet(owner);
            if (activePet != null && !activePet.isDuelClone() && activePet.isValid()) {
                PlayerPetData petData = activePet.getPetData();

                // POTION skill: Emergency Absorption & Resistance when HP < 35%
                int potTier = petData.getSkillLevel(PetSkill.POTION);
                if (potTier > 0) {
                    AttributeInstance maxHpAttr = owner.getAttribute(Attribute.MAX_HEALTH);
                    double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
                    double currentHp = owner.getHealth() - event.getFinalDamage();
                    if (currentHp <= maxHp * 0.35) {
                        long now = System.currentTimeMillis();
                        long cdMillis = Math.max(30, 90 - (potTier * 10)) * 1000L;
                        if (now - activePet.getLastPotionProcTime() >= cdMillis) {
                            activePet.setLastPotionProcTime(now);
                            owner.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1, false, true, true));
                            owner.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 0, false, true, true));
                            owner.getWorld().playSound(owner.getLocation(), Sound.ITEM_TOTEM_USE, 0.6f, 1.5f);
                            owner.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, owner.getLocation().clone().add(0, 1.0, 0), 20, 0.4, 0.4, 0.4, 0.1);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMonsterTargetPlayer(@NotNull EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Monster monster)) return;

        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        if (activePet == null || !activePet.isValid() || activePet.isDuelClone()) return;

        // CAMOUFLAGE skill: Reduces monster aggro detection distance
        int camoTier = activePet.getPetData().getSkillLevel(PetSkill.CAMOUFLAGE);
        if (camoTier > 0) {
            double distSq = monster.getLocation().distanceSquared(player.getLocation());
            double reductionPercent = (15 + (camoTier * 7)) / 100.0;
            double effectiveMaxDist = 16.0 * (1.0 - reductionPercent);
            if (distSq > effectiveMaxDist * effectiveMaxDist) {
                event.setCancelled(true);
            }
        }
    }

    private void applyBleedEffect(@NotNull LivingEntity target) {
        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().clone().add(0, 1.0, 0), 12, 0.2, 0.2, 0.2, org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 0.8f);

        for (int i = 1; i <= 4; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (target.isValid() && !target.isDead()) {
                    target.damage(2.0);
                    target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().clone().add(0, 0.8, 0), 8, 0.2, 0.2, 0.2, org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
                }
            }, i * 20L);
        }
    }
}
