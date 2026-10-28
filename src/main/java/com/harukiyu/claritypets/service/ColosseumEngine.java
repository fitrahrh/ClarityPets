package com.harukiyu.claritypets.service;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.hook.LanguageHook;
import com.harukiyu.claritypets.model.*;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ColosseumEngine {

    private final ClarityPets plugin;
    private final Map<UUID, UUID> pendingChallenges = new ConcurrentHashMap<>(); // challenger -> target
    private final Map<UUID, Long> challengeTimestamps = new ConcurrentHashMap<>();

    private DuelMatch currentMatch = null;
    private BukkitTask duelTickTask = null;

    public ColosseumEngine(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    public void shutdown() {
        if (duelTickTask != null) {
            duelTickTask.cancel();
            duelTickTask = null;
        }
        if (currentMatch != null) {
            cleanupDuelEntities(currentMatch);
            currentMatch = null;
        }
    }

    public boolean isArenaBusy() {
        return currentMatch != null && currentMatch.getState() != DuelMatch.State.COMPLETED;
    }

    public boolean isArenaConfigured() {
        return plugin.getConfigManager().getArenaPos1() != null
                && plugin.getConfigManager().getArenaPos2() != null
                && plugin.getConfigManager().getArenaSpawn1() != null
                && plugin.getConfigManager().getArenaSpawn2() != null;
    }

    // ==========================================
    // Matchmaking & Challenge Handling
    // ==========================================

    public boolean sendChallenge(@NotNull Player challenger, @NotNull Player target) {
        if (challenger.equals(target)) {
            return false;
        }

        if (isArenaBusy()) {
            LanguageHook.sendMessage(challenger, "message.duel_arena_busy");
            return false;
        }

        if (!isArenaConfigured()) {
            LanguageHook.sendMessage(challenger, "message.duel_arena_not_configured");
            return false;
        }

        ActivePet cPet = plugin.getPetManager().getActivePet(challenger);
        if (cPet == null) {
            LanguageHook.sendMessage(challenger, "message.no_active_pet");
            return false;
        }

        ActivePet tPet = plugin.getPetManager().getActivePet(target);
        if (tPet == null) {
            LanguageHook.sendMessage(challenger, "message.duel_target_no_pet",
                    Placeholder.unparsed("target", target.getName())
            );
            return false;
        }

        pendingChallenges.put(challenger.getUniqueId(), target.getUniqueId());
        challengeTimestamps.put(challenger.getUniqueId(), System.currentTimeMillis());

        LanguageHook.sendMessage(challenger, "message.duel_challenge_sent",
                Placeholder.unparsed("target", target.getName())
        );

        LanguageHook.sendMessage(target, "message.duel_challenge_received",
                Placeholder.unparsed("challenger", challenger.getName())
        );
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.2f);
        return true;
    }

    public boolean acceptChallenge(@NotNull Player target, @NotNull Player challenger) {
        UUID pendingTarget = pendingChallenges.get(challenger.getUniqueId());
        if (pendingTarget == null || !pendingTarget.equals(target.getUniqueId())) {
            return false;
        }

        pendingChallenges.remove(challenger.getUniqueId());
        challengeTimestamps.remove(challenger.getUniqueId());

        if (isArenaBusy()) {
            LanguageHook.sendMessage(target, "message.duel_arena_busy");
            return false;
        }

        if (!isArenaConfigured()) {
            LanguageHook.sendMessage(target, "message.duel_arena_not_configured");
            return false;
        }

        ActivePet cPet = plugin.getPetManager().getActivePet(challenger);
        ActivePet tPet = plugin.getPetManager().getActivePet(target);
        if (cPet == null || tPet == null) {
            return false;
        }

        startDuel(challenger, cPet.getPetData(), target, tPet.getPetData());
        return true;
    }

    public void declineChallenge(@NotNull Player target, @NotNull Player challenger) {
        pendingChallenges.remove(challenger.getUniqueId());
        challengeTimestamps.remove(challenger.getUniqueId());
        LanguageHook.sendMessage(challenger, "message.duel_challenge_declined",
                Placeholder.unparsed("target", target.getName())
        );
    }

    // ==========================================
    // Auto-Battle Simulation
    // ==========================================

    public void startDuel(@NotNull Player player1, @NotNull PlayerPetData petData1,
                          @NotNull Player player2, @NotNull PlayerPetData petData2) {
        Location spawn1 = plugin.getConfigManager().getArenaSpawn1();
        Location spawn2 = plugin.getConfigManager().getArenaSpawn2();

        if (spawn1 == null || spawn2 == null || spawn1.getWorld() == null) {
            return;
        }

        // Spawn duel representation mobs
        Mob entity1 = spawnDuelEntity(spawn1, petData1, player1);
        Mob entity2 = spawnDuelEntity(spawn2, petData2, player2);

        ActivePet activePet1 = new ActivePet(player1.getUniqueId(), petData1, entity1, true);
        ActivePet activePet2 = new ActivePet(player2.getUniqueId(), petData2, entity2, true);

        // Lock targets onto each other
        entity1.setTarget(entity2);
        entity2.setTarget(entity1);

        int duration = plugin.getConfigManager().getMatchDurationSeconds();
        this.currentMatch = new DuelMatch(player1, petData1, activePet1, petData1.getStance(),
                player2, petData2, activePet2, petData2.getStance(), duration);
        this.currentMatch.setState(DuelMatch.State.FIGHTING);

        // Register to plugin entity tracking
        plugin.getPetManager().getActivePetByEntity(entity1.getUniqueId());

        // Broadcast duel start
        LanguageHook.sendMessage(player1, "message.duel_start",
                Placeholder.unparsed("opponent", player2.getName()),
                Placeholder.unparsed("pet1", petData1.getEffectiveName()),
                Placeholder.unparsed("pet2", petData2.getEffectiveName())
        );
        LanguageHook.sendMessage(player2, "message.duel_start",
                Placeholder.unparsed("opponent", player1.getName()),
                Placeholder.unparsed("pet1", petData2.getEffectiveName()),
                Placeholder.unparsed("pet2", petData1.getEffectiveName())
        );

        player1.playSound(player1.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
        player2.playSound(player2.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);

        // Start match tick loop (every 5 ticks / 0.25s)
        this.duelTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickDuel, 5L, 5L);
    }

    private Mob spawnDuelEntity(Location loc, PlayerPetData data, Player owner) {
        PetType type = data.getPetType();
        Mob entity = (Mob) loc.getWorld().spawnEntity(loc, type.getEntityType());

        // Attribute Scale = 0.7
        double scale = plugin.getConfigManager().getDefaultScale();
        AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(scale);

        // Calculate stats with Tactical Stance
        double baseMaxHp = data.getMaxHp();
        double baseAtk = data.getAtk() * data.getStance().getAttackMultiplier();
        double baseDef = data.getDef() * data.getStance().getDefenseMultiplier();
        double baseSpd = data.getSpd();

        AttributeInstance maxHpAttr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHpAttr != null) {
            maxHpAttr.setBaseValue(baseMaxHp);
            entity.setHealth(baseMaxHp);
        }

        AttributeInstance atkAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (atkAttr != null) atkAttr.setBaseValue(baseAtk);

        AttributeInstance defAttr = entity.getAttribute(Attribute.ARMOR);
        if (defAttr != null) defAttr.setBaseValue(baseDef);

        AttributeInstance spdAttr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
        if (spdAttr != null) spdAttr.setBaseValue(baseSpd);

        if (type.isBaby()) {
            if (entity instanceof Ageable ageable) ageable.setBaby();
            else if (entity instanceof Zombie zombie) zombie.setBaby();
        }

        if (entity instanceof Piglin piglin) {
            piglin.setImmuneToZombification(true);
            piglin.setIsAbleToHunt(false);
        }

        if (entity instanceof Enderman enderman) {
            enderman.setCarriedBlock(null);
        }

        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);
        entity.setCanPickupItems(false);

        plugin.getPetManager().updateNametag(entity, data, owner);
        return entity;
    }

    private void tickDuel() {
        if (currentMatch == null || currentMatch.getState() != DuelMatch.State.FIGHTING) {
            return;
        }

        ActivePet p1 = currentMatch.getActivePet1();
        ActivePet p2 = currentMatch.getActivePet2();

        LivingEntity e1 = p1.getEntity();
        LivingEntity e2 = p2.getEntity();

        // Check if one of them is KO or invalid
        if (e1 == null || !e1.isValid() || e1.isDead() || e1.getHealth() <= 0.0) {
            finishDuel(currentMatch.getPlayer2(), currentMatch.getPlayer1(), false);
            return;
        }

        if (e2 == null || !e2.isValid() || e2.isDead() || e2.getHealth() <= 0.0) {
            finishDuel(currentMatch.getPlayer1(), currentMatch.getPlayer2(), false);
            return;
        }

        // Check time expiration
        if (currentMatch.isExpired()) {
            double hp1Ratio = e1.getHealth() / p1.getPetData().getMaxHp();
            double hp2Ratio = e2.getHealth() / p2.getPetData().getMaxHp();

            if (Math.abs(hp1Ratio - hp2Ratio) < 0.001) {
                finishDuel(null, null, true);
            } else if (hp1Ratio > hp2Ratio) {
                finishDuel(currentMatch.getPlayer1(), currentMatch.getPlayer2(), false);
            } else {
                finishDuel(currentMatch.getPlayer2(), currentMatch.getPlayer1(), false);
            }
            return;
        }

        // Arena boundary check: keep pets inside pos1-pos2 bounding box
        enforceArenaBounds(e1, plugin.getConfigManager().getArenaSpawn1());
        enforceArenaBounds(e2, plugin.getConfigManager().getArenaSpawn2());

        // Passive tide blessing (Baby Drowned sustain)
        checkPassiveRegen(p1, e1);
        checkPassiveRegen(p2, e2);

        // Keep targets locked
        if (e1 instanceof Mob mob1 && mob1.getTarget() != e2) {
            mob1.setTarget(e2);
        }
        if (e2 instanceof Mob mob2 && mob2.getTarget() != e1) {
            mob2.setTarget(e1);
        }

        // Apply crowd-control constraints (Web root, Stun)
        handleCrowdControl(p1, e1);
        handleCrowdControl(p2, e2);
    }

    private void enforceArenaBounds(LivingEntity entity, Location fallback) {
        Location pos1 = plugin.getConfigManager().getArenaPos1();
        Location pos2 = plugin.getConfigManager().getArenaPos2();
        if (pos1 == null || pos2 == null || pos1.getWorld() == null) return;

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        Location cur = entity.getLocation();
        if (cur.getX() < minX || cur.getX() > maxX
                || cur.getY() < minY || cur.getY() > maxY
                || cur.getZ() < minZ || cur.getZ() > maxZ) {
            if (fallback != null) {
                entity.teleport(fallback);
                entity.getWorld().spawnParticle(Particle.PORTAL, fallback, 10, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    private void checkPassiveRegen(ActivePet pet, LivingEntity entity) {
        if (pet.getPetData().getPetType() == PetType.BABY_DROWNED) {
            long now = System.currentTimeMillis();
            if (now - pet.getLastHealSkillTime() >= 3000L) {
                pet.setLastHealSkillTime(now);
                double regenPct = pet.getPetData().getPetType().getPassiveValue(pet.getPetData().getLevel());
                double amount = pet.getPetData().getMaxHp() * regenPct;
                double newHp = Math.min(pet.getPetData().getMaxHp(), entity.getHealth() + amount);
                entity.setHealth(newHp);
                entity.getWorld().spawnParticle(Particle.SPLASH, entity.getLocation().add(0, 0.5, 0), 8, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    private void handleCrowdControl(ActivePet pet, LivingEntity entity) {
        if (pet.isRooted() || pet.isStunned()) {
            if (entity instanceof Mob mob) {
                mob.getPathfinder().stopPathfinding();
            }
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 255, false, false));
        }
    }

    // ==========================================
    // Colosseum Combat Event Resolution
    // ==========================================

    public void handleDuelHit(@NotNull LivingEntity attacker, @NotNull LivingEntity victim) {
        if (currentMatch == null || currentMatch.getState() != DuelMatch.State.FIGHTING) return;

        ActivePet aPet = currentMatch.getActivePet1().getEntity().equals(attacker) ? currentMatch.getActivePet1() :
                (currentMatch.getActivePet2().getEntity().equals(attacker) ? currentMatch.getActivePet2() : null);
        ActivePet vPet = currentMatch.getActivePet1().getEntity().equals(victim) ? currentMatch.getActivePet1() :
                (currentMatch.getActivePet2().getEntity().equals(victim) ? currentMatch.getActivePet2() : null);

        if (aPet == null || vPet == null) return;

        PetCategory aCat = aPet.getPetData().getPetType().getCategory();
        PetCategory vCat = vPet.getPetData().getPetType().getCategory();

        // Rock-Paper-Scissors bonuses
        if (aCat.hasAdvantageOver(vCat)) {
            // Penetration / Accuracy bonus
            attacker.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 0.5, 0), 5, 0.2, 0.2, 0.2, 0.02);
        }

        // Passive: Baby Zombie Relentless (consecutive hits increase attack speed)
        if (aPet.getPetData().getPetType() == PetType.BABY_ZOMBIE) {
            aPet.incrementComboHits();
            if (aPet.getComboHits() >= 3) {
                aPet.resetComboHits();
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, false, false));
            }
        }

        // Passive: Baby Zombified Piglin Vengeance (Counter Tanker reflect damage)
        if (vPet.getPetData().getPetType() == PetType.BABY_ZOMBIFIED_PIGLIN) {
            double reflectPct = vPet.getPetData().getPetType().getPassiveValue(vPet.getPetData().getLevel());
            double reflected = Math.max(1.0, aPet.getPetData().getAtk() * reflectPct);
            attacker.damage(reflected);
            attacker.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, attacker.getLocation().add(0, 0.5, 0), 3);
        }

        // Active Skill Trigger (20% base proc chance + stance bonus)
        double procChance = plugin.getConfigManager().getSkillProcChance() + aPet.getPetData().getStance().getProcBonus();
        if (Math.random() < procChance) {
            triggerActiveSkill(aPet, attacker, vPet, victim);
        }
    }

    private void triggerActiveSkill(ActivePet aPet, LivingEntity attacker, ActivePet vPet, LivingEntity victim) {
        PetType type = aPet.getPetData().getPetType();
        int level = aPet.getPetData().getLevel();
        long now = System.currentTimeMillis();

        switch (type) {
            case BABY_ZOMBIE -> {
                // Frenzy Strike: Strikes 2 rapid hits with no delay
                victim.damage(aPet.getPetData().getAtk());
                victim.damage(aPet.getPetData().getAtk());
                attacker.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation().add(0, 0.5, 0), 2);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.4f);
            }
            case SKELETON -> {
                // Headshot: Critical strike that stuns enemy for 1 second
                vPet.setStunnedDuration(1000L);
                victim.damage(aPet.getPetData().getAtk() * 1.5);
                attacker.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1.0, 0), 10, 0.1, 0.1, 0.1, 0.1);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.2f);
            }
            case PIGLIN -> {
                // Overpower Swing: Heavy slash with flat damage bonus and knockback
                double bonus = type.getActiveSkillValue(level);
                victim.damage(aPet.getPetData().getAtk() + bonus);
                victim.setVelocity(victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(0.6).setY(0.2));
                attacker.getWorld().spawnParticle(Particle.EXPLOSION, victim.getLocation().add(0, 0.5, 0), 1);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
            }
            case WITHER_SKELETON -> {
                // Wither Curse: Inflicts Wither for 3 seconds
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1, false, false));
                attacker.getWorld().spawnParticle(Particle.SMOKE, victim.getLocation().add(0, 0.5, 0), 8, 0.2, 0.2, 0.2, 0.05);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 1.0f, 1.0f);
            }
            case ENDERMAN -> {
                // Shadow Ambush: Teleport behind target with guaranteed crit
                Location behind = victim.getLocation().add(victim.getLocation().getDirection().multiply(-1.5));
                enforceArenaBounds(attacker, behind);
                attacker.teleport(behind);
                victim.damage(aPet.getPetData().getAtk() * 1.5);
                attacker.getWorld().spawnParticle(Particle.PORTAL, behind, 15, 0.2, 0.2, 0.2, 0.1);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            }
            case CAVE_SPIDER -> {
                // Web Trap: Roots target for 2 seconds
                vPet.setRootedDuration(2000L);
                attacker.getWorld().spawnParticle(Particle.POOF, victim.getLocation().add(0, 0.2, 0), 10, 0.3, 0.1, 0.3, 0.02);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_COBWEB_PLACE, 1.0f, 1.0f);
            }
            case STRAY -> {
                // Frostbite Arrow: Slowness II for 3 seconds
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false));
                attacker.getWorld().spawnParticle(Particle.SNOWFLAKE, victim.getLocation().add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.05);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
            }
            case BREEZE -> {
                // Wind Burst: Knockback and reset target pathfinding
                victim.setVelocity(victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(0.8).setY(0.3));
                if (victim instanceof Mob mob) mob.getPathfinder().stopPathfinding();
                attacker.getWorld().spawnParticle(Particle.GUST, victim.getLocation().add(0, 0.5, 0), 1);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.0f);
            }
            case WITCH -> {
                // Splash Salve: Heals 20% max HP with internal cooldown
                long healCooldownMs = plugin.getConfigManager().getHealCooldownSeconds() * 1000L;
                if (now - aPet.getLastHealSkillTime() >= healCooldownMs) {
                    aPet.setLastHealSkillTime(now);
                    double healPct = type.getActiveSkillValue(level);
                    double healAmount = aPet.getPetData().getMaxHp() * healPct;
                    attacker.setHealth(Math.min(aPet.getPetData().getMaxHp(), attacker.getHealth() + healAmount));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false));
                    attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation().add(0, 1.0, 0), 6, 0.2, 0.2, 0.2, 0.05);
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 1.2f);
                }
            }
            case BABY_ZOMBIFIED_PIGLIN -> {
                // Golden Bastion: DEF +40% for 3 seconds
                aPet.setDefenseBuffDuration(3000L);
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false));
                attacker.getWorld().spawnParticle(Particle.ENCHANT, attacker.getLocation().add(0, 0.5, 0), 10, 0.2, 0.2, 0.2, 0.1);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
            }
            case BABY_DROWNED -> {
                // Aqua Leach: Drains 50% damage dealt
                double leechPct = type.getActiveSkillValue(level);
                double damage = aPet.getPetData().getAtk();
                victim.damage(damage);
                attacker.setHealth(Math.min(aPet.getPetData().getMaxHp(), attacker.getHealth() + (damage * leechPct)));
                attacker.getWorld().spawnParticle(Particle.BUBBLE_POP, victim.getLocation().add(0, 0.5, 0), 8, 0.2, 0.2, 0.2, 0.05);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_DROWNED_SWIM, 1.0f, 1.2f);
            }
            default -> {}
        }
    }

    // ==========================================
    // Duel Completion & Async Record Saving
    // ==========================================

    public void finishDuel(@Nullable Player winner, @Nullable Player loser, boolean draw) {
        if (currentMatch == null) return;
        currentMatch.setState(DuelMatch.State.COMPLETED);

        if (duelTickTask != null) {
            duelTickTask.cancel();
            duelTickTask = null;
        }

        Player p1 = currentMatch.getPlayer1();
        Player p2 = currentMatch.getPlayer2();

        if (draw || winner == null || loser == null) {
            LanguageHook.sendMessage(p1, "message.duel_draw");
            LanguageHook.sendMessage(p2, "message.duel_draw");

            DuelStats s1 = plugin.getPetManager().getDuelStats(p1.getUniqueId());
            DuelStats s2 = plugin.getPetManager().getDuelStats(p2.getUniqueId());
            s1.recordDraw();
            s2.recordDraw();

            plugin.getDuelRepository().saveDuelStats(s1);
            plugin.getDuelRepository().saveDuelStats(s2);
        } else {
            LanguageHook.sendMessage(winner, "message.duel_victory",
                    Placeholder.unparsed("winner", winner.getName()),
                    Placeholder.unparsed("loser", loser.getName())
            );
            LanguageHook.sendMessage(loser, "message.duel_victory",
                    Placeholder.unparsed("winner", winner.getName()),
                    Placeholder.unparsed("loser", loser.getName())
            );

            winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            loser.playSound(loser.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);

            winner.getWorld().spawnParticle(Particle.FIREWORK, winner.getLocation().add(0, 1.5, 0), 30, 0.5, 0.5, 0.5, 0.1);

            DuelStats winStats = plugin.getPetManager().getDuelStats(winner.getUniqueId());
            DuelStats loseStats = plugin.getPetManager().getDuelStats(loser.getUniqueId());

            winStats.recordWin(25);
            loseStats.recordLoss(15);

            plugin.getDuelRepository().saveDuelStats(winStats);
            plugin.getDuelRepository().saveDuelStats(loseStats);
        }

        cleanupDuelEntities(currentMatch);
        currentMatch = null;
    }

    private void cleanupDuelEntities(@NotNull DuelMatch match) {
        LivingEntity e1 = match.getActivePet1().getEntity();
        if (e1 != null && e1.isValid()) {
            e1.remove();
        }
        LivingEntity e2 = match.getActivePet2().getEntity();
        if (e2 != null && e2.isValid()) {
            e2.remove();
        }
    }

    @Nullable
    public DuelMatch getCurrentMatch() {
        return currentMatch;
    }
}
