package com.harukiyu.claritypets.service;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.ai.PetGoalInstaller;
import com.harukiyu.claritypets.hook.LanguageHook;
import com.harukiyu.claritypets.model.*;
import com.harukiyu.claritypets.repository.DuelRepository;
import com.harukiyu.claritypets.repository.PetRepository;
import net.kyori.adventure.text.Component;
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

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PetManager {

    private final ClarityPets plugin;
    private final PetRepository petRepository;
    private final DuelRepository duelRepository;

    // In-memory caches for fast, zero-lag access
    private final Map<UUID, Map<PetType, PlayerPetData>> playerPetsCache = new ConcurrentHashMap<>();
    private final Map<UUID, ActivePet> activePetsByOwner = new ConcurrentHashMap<>();
    private final Map<UUID, ActivePet> activePetsByEntity = new ConcurrentHashMap<>();
    private final Map<UUID, DuelStats> duelStatsCache = new ConcurrentHashMap<>();

    private BukkitTask petTickTask;
    private BukkitTask autoSaveTask;

    public PetManager(@NotNull ClarityPets plugin, @NotNull PetRepository petRepository, @NotNull DuelRepository duelRepository) {
        this.plugin = plugin;
        this.petRepository = petRepository;
        this.duelRepository = duelRepository;
        startTasks();
    }

    private void startTasks() {
        // AI Pathfinding & Follow task running every 10 ticks (0.5s)
        this.petTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickPets, 10L, 10L);

        // Auto-save task running periodically
        long interval = plugin.getConfigManager().getSaveIntervalTicks();
        this.autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllOnlineData, interval, interval);
    }

    public void shutdown() {
        if (petTickTask != null) petTickTask.cancel();
        if (autoSaveTask != null) autoSaveTask.cancel();

        // Despawn all active summoned pets safely
        for (ActivePet activePet : activePetsByOwner.values()) {
            if (activePet.getEntity().isValid()) {
                activePet.getEntity().remove();
            }
        }
        activePetsByOwner.clear();
        activePetsByEntity.clear();

        // Save all data synchronously on shutdown
        saveAllOnlineDataSync();
    }

    // ==========================================
    // Player Cache Management (Join / Quit)
    // ==========================================

    public void loadPlayerDataAsync(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        petRepository.loadPlayerPets(uuid).thenAccept(pets -> {
            Map<PetType, PlayerPetData> map = new ConcurrentHashMap<>();
            PlayerPetData activeToSummon = null;

            for (PlayerPetData pet : pets) {
                map.put(pet.getPetType(), pet);
                if (pet.isActive() && activeToSummon == null) {
                    activeToSummon = pet;
                }
            }
            playerPetsCache.put(uuid, map);

            // Re-summon active pet on main thread if previously active
            if (activeToSummon != null && player.isOnline()) {
                PlayerPetData finalPet = activeToSummon;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        spawnPet(player, finalPet, false);
                    }
                });
            }
        });

        duelRepository.loadDuelStats(uuid).thenAccept(stats -> duelStatsCache.put(uuid, stats));
    }

    public void unloadPlayerDataAsync(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        // Despawn summoned pet on main thread
        despawnPet(player, false);

        // Save data to database asynchronously
        Map<PetType, PlayerPetData> pets = playerPetsCache.remove(uuid);
        if (pets != null && !pets.isEmpty()) {
            petRepository.saveAllPets(pets.values());
        }

        DuelStats stats = duelStatsCache.remove(uuid);
        if (stats != null) {
            duelRepository.saveDuelStats(stats);
        }
    }

    public void saveAllOnlineData() {
        List<PlayerPetData> allPets = new ArrayList<>();
        for (Map<PetType, PlayerPetData> petMap : playerPetsCache.values()) {
            allPets.addAll(petMap.values());
        }
        if (!allPets.isEmpty()) {
            petRepository.saveAllPets(allPets);
        }

        for (DuelStats stats : duelStatsCache.values()) {
            duelRepository.saveDuelStats(stats);
        }
    }

    public void saveAllOnlineDataSync() {
        List<PlayerPetData> allPets = new ArrayList<>();
        for (Map<PetType, PlayerPetData> petMap : playerPetsCache.values()) {
            allPets.addAll(petMap.values());
        }
        if (!allPets.isEmpty()) {
            petRepository.saveAllPets(allPets).join();
        }

        for (DuelStats stats : duelStatsCache.values()) {
            duelRepository.saveDuelStats(stats).join();
        }
    }

    // ==========================================
    // Pet Collection Queries
    // ==========================================

    @NotNull
    public Collection<PlayerPetData> getPlayerPets(@NotNull UUID playerUuid) {
        Map<PetType, PlayerPetData> map = playerPetsCache.get(playerUuid);
        return map != null ? map.values() : Collections.emptyList();
    }

    @Nullable
    public PlayerPetData getPlayerPet(@NotNull UUID playerUuid, @NotNull PetType type) {
        Map<PetType, PlayerPetData> map = playerPetsCache.get(playerUuid);
        return map != null ? map.get(type) : null;
    }

    public void addPetToPlayer(@NotNull UUID playerUuid, @NotNull PlayerPetData petData) {
        Map<PetType, PlayerPetData> map = playerPetsCache.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
        map.put(petData.getPetType(), petData);
        petRepository.savePet(petData);
    }

    public void removePetFromPlayer(@NotNull UUID playerUuid, @NotNull PetType type) {
        ActivePet active = activePetsByOwner.get(playerUuid);
        if (active != null && active.getPetData().getPetType() == type) {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) despawnPet(player, false);
        }

        Map<PetType, PlayerPetData> map = playerPetsCache.get(playerUuid);
        if (map != null) {
            map.remove(type);
        }
        petRepository.deletePet(playerUuid, type);
    }

    @Nullable
    public ActivePet getActivePet(@NotNull Player player) {
        return activePetsByOwner.get(player.getUniqueId());
    }

    @Nullable
    public ActivePet getActivePetByEntity(@NotNull UUID entityUuid) {
        return activePetsByEntity.get(entityUuid);
    }

    @NotNull
    public DuelStats getDuelStats(@NotNull UUID playerUuid) {
        return duelStatsCache.computeIfAbsent(playerUuid, DuelStats::createDefault);
    }

    // ==========================================
    // Pet Spawning & Despawning
    // ==========================================

    public boolean spawnPet(@NotNull Player player, @NotNull PlayerPetData petData, boolean playSound) {
        // Only 1 pet active at a time: despawn existing pet
        despawnPet(player, false);

        Location spawnLoc = player.getLocation();
        PetType petType = petData.getPetType();

        Entity spawned = player.getWorld().spawnEntity(spawnLoc, petType.getEntityType());
        if (!(spawned instanceof Mob mob)) {
            spawned.remove();
            return false;
        }

        // Apply scale attribute = 0.7
        double scale = plugin.getConfigManager().getDefaultScale();
        AttributeInstance scaleAttr = mob.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(scale);
        }

        // Apply combat attributes based on level & skills
        AttributeInstance maxHpAttr = mob.getAttribute(Attribute.MAX_HEALTH);
        if (maxHpAttr != null) {
            maxHpAttr.setBaseValue(petData.getMaxHp());
            mob.setHealth(petData.getMaxHp());
        }

        AttributeInstance atkAttr = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (atkAttr != null) {
            atkAttr.setBaseValue(petData.getAtk());
        }

        AttributeInstance defAttr = mob.getAttribute(Attribute.ARMOR);
        if (defAttr != null) {
            defAttr.setBaseValue(petData.getDef());
        }

        AttributeInstance spdAttr = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        if (spdAttr != null) {
            spdAttr.setBaseValue(petData.getSpd());
        }

        // Specific mob behaviors
        if (petType.isBaby()) {
            if (mob instanceof Ageable ageable) {
                ageable.setBaby();
            }
        }

        if (mob instanceof Piglin piglin) {
            piglin.setImmuneToZombification(true);
            piglin.setIsAbleToHunt(false);
        }

        if (mob instanceof Enderman enderman) {
            enderman.setCarriedBlock(null);
        }

        if (mob instanceof Creeper creeper) {
            creeper.setMaxFuseTicks(Integer.MAX_VALUE);
        }

        mob.setRemoveWhenFarAway(false);
        mob.setPersistent(true);
        mob.setCanPickupItems(false);

        // Set custom nametag
        updateNametag(mob, petData, player);

        // Install genuine Paper MobGoals AI (No NMS hacks)
        PetGoalInstaller.install(plugin, mob, petData);

        petData.setActive(true);
        if (petData.getBehaviorMode() == BehaviorMode.POCKET) {
            petData.setBehaviorMode(BehaviorMode.PASSIVE);
        }

        ActivePet activePet = new ActivePet(player.getUniqueId(), petData, mob, false);
        activePetsByOwner.put(player.getUniqueId(), activePet);
        activePetsByEntity.put(mob.getUniqueId(), activePet);

        // Update database
        petRepository.setActivePet(player.getUniqueId(), petType);

        if (playSound) {
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.7f, 1.4f);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, spawnLoc.clone().add(0, 0.5, 0), 15, 0.3, 0.3, 0.3, 0.05);
            LanguageHook.sendMessage(player, "message.summon_success",
                    Placeholder.unparsed("name", petData.getEffectiveName()),
                    Placeholder.unparsed("level", String.valueOf(petData.getLevel()))
            );
        }

        return true;
    }

    public void despawnPet(@NotNull Player player, boolean playSound) {
        ActivePet active = activePetsByOwner.remove(player.getUniqueId());
        if (active != null) {
            if (active.getEntity().isValid()) {
                activePetsByEntity.remove(active.getEntity().getUniqueId());
                Location loc = active.getEntity().getLocation();
                active.getEntity().remove();
                if (playSound) {
                    player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.3f);
                    loc.getWorld().spawnParticle(Particle.POOF, loc.add(0, 0.4, 0), 10, 0.2, 0.2, 0.2, 0.02);
                }
            }
            active.getPetData().setActive(false);
            petRepository.deactivateAll(player.getUniqueId());
            if (playSound) {
                LanguageHook.sendMessage(player, "message.despawn_success",
                        Placeholder.unparsed("name", active.getPetData().getEffectiveName())
                );
            }
        }
    }

    public void updateNametag(@NotNull LivingEntity entity, @NotNull PlayerPetData data, @NotNull Player player) {
        Component nameTag = LanguageHook.getComponent("pet.nametag_format", player,
                Placeholder.unparsed("name", data.getEffectiveName()),
                Placeholder.unparsed("level", String.valueOf(data.getLevel()))
        );
        entity.customName(nameTag);
        entity.setCustomNameVisible(true);
    }

    // ==========================================
    // Pet Treats & Daily Limit
    // ==========================================

    public boolean feedPetTreat(@NotNull Player player, @NotNull PlayerPetData petData) {
        String today = LocalDate.now().toString();
        if (!today.equals(petData.getLastTreatDate())) {
            petData.setLastTreatDate(today);
            petData.setDailyTreatsUsed(0);
        }

        int maxDaily = plugin.getConfigManager().getDailyMaxTreats();
        if (petData.getDailyTreatsUsed() >= maxDaily) {
            LanguageHook.sendMessage(player, "message.treat_daily_limit",
                    Placeholder.unparsed("max", String.valueOf(maxDaily))
            );
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        petData.setDailyTreatsUsed(petData.getDailyTreatsUsed() + 1);
        double treatExp = plugin.getConfigManager().getTreatExp();

        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.0, 0), 5, 0.2, 0.2, 0.2, 0.05);

        LanguageHook.sendMessage(player, "message.treat_success",
                Placeholder.unparsed("name", petData.getEffectiveName()),
                Placeholder.unparsed("exp", String.format("%.0f", treatExp)),
                Placeholder.unparsed("left", String.valueOf(maxDaily - petData.getDailyTreatsUsed()))
        );

        plugin.getLevelingManager().addExp(player, petData, treatExp);
        petRepository.savePet(petData);
        return true;
    }

    public int getDailyTreatsLeft(@NotNull PlayerPetData petData) {
        String today = LocalDate.now().toString();
        if (!today.equals(petData.getLastTreatDate())) {
            return plugin.getConfigManager().getDailyMaxTreats();
        }
        return Math.max(0, plugin.getConfigManager().getDailyMaxTreats() - petData.getDailyTreatsUsed());
    }

    // ==========================================
    // AI Tick Loop (Following, Sentry, Protection)
    // ==========================================

    private void tickPets() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<UUID, ActivePet>> it = activePetsByOwner.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, ActivePet> entry = it.next();
            UUID ownerUuid = entry.getKey();
            ActivePet activePet = entry.getValue();

            // Skip duel clones (they are handled by ColosseumEngine)
            if (activePet.isDuelClone()) continue;

            Player owner = Bukkit.getPlayer(ownerUuid);
            if (owner == null || !owner.isOnline()) {
                if (activePet.getEntity().isValid()) {
                    activePet.getEntity().remove();
                }
                activePetsByEntity.remove(activePet.getEntity().getUniqueId());
                it.remove();
                continue;
            }

            Mob mob = activePet.getEntity();
            if (!mob.isValid() || mob.isDead()) {
                activePetsByEntity.remove(mob.getUniqueId());
                it.remove();
                continue;
            }

            // Prevent daylight combustion
            if (mob.getFireTicks() > 0 && !mob.isInLava()) {
                mob.setFireTicks(0);
            }

            PlayerPetData petData = activePet.getPetData();
            BehaviorMode mode = petData.getBehaviorMode();

            // Cross-world or far away safety teleport
            if (!mob.getWorld().equals(owner.getWorld())) {
                mob.teleport(owner.getLocation());
                continue;
            }

            double distSq = mob.getLocation().distanceSquared(owner.getLocation());
            if (distSq > 35 * 35 && mode != BehaviorMode.SENTRY) {
                mob.teleport(owner.getLocation());
                continue;
            }

            // Sentry mode anchoring
            if (mode == BehaviorMode.SENTRY) {
                Location sentry = activePet.getSentryLocation();
                if (sentry != null && mob.getWorld().equals(sentry.getWorld())) {
                    if (mob.getLocation().distanceSquared(sentry) > 6 * 6) {
                        mob.getPathfinder().moveTo(sentry);
                    }
                }
            }

            // ==========================================
            // Passive Support & Aura Skills
            // ==========================================

            // 1. BEACON Aura Skill: radiates buffs to owner within 12 blocks
            int beaconTier = petData.getSkillLevel(PetSkill.BEACON);
            if (beaconTier > 0 && distSq <= 12 * 12) {
                applyBeaconAura(owner, beaconTier);
            }

            // 2. HEAL Skill: pet self-regen and nearby owner healing
            int healTier = petData.getSkillLevel(PetSkill.HEAL);
            if (healTier > 0 && now - activePet.getLastHealSkillTime() >= 3000L) {
                activePet.setLastHealSkillTime(now);
                double regenAmount = 0.4 * healTier;
                AttributeInstance maxHpAttr = mob.getAttribute(Attribute.MAX_HEALTH);
                double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : mob.getHealth();
                mob.setHealth(Math.min(maxHp, mob.getHealth() + regenAmount));

                // Restores 1.0 HP to owner if within 8 blocks
                if (distSq <= 8 * 8) {
                    AttributeInstance ownerMaxHpAttr = owner.getAttribute(Attribute.MAX_HEALTH);
                    double ownerMaxHp = ownerMaxHpAttr != null ? ownerMaxHpAttr.getValue() : 20.0;
                    if (owner.getHealth() < ownerMaxHp) {
                        owner.setHealth(Math.min(ownerMaxHp, owner.getHealth() + 1.0));
                        owner.getWorld().spawnParticle(Particle.HEART, owner.getLocation().clone().add(0, 1.2, 0), 2, 0.2, 0.2, 0.2, 0.02);
                    }
                }
            }

            // 3. TAUNT Skill: forces nearby hostile monsters to target pet
            int tauntTier = petData.getSkillLevel(PetSkill.TAUNT);
            if (tauntTier > 0 && now - activePet.getLastTauntTime() >= 10000L) {
                double tauntRadius = 5.0 + (tauntTier * 2.0);
                boolean taunted = false;
                for (Entity e : mob.getNearbyEntities(tauntRadius, tauntRadius, tauntRadius)) {
                    if (e instanceof Monster monster && !monster.isDead()) {
                        if (monster.getTarget() == owner || monster.getTarget() == null) {
                            monster.setTarget(mob);
                            taunted = true;
                        }
                    }
                }
                if (taunted) {
                    activePet.setLastTauntTime(now);
                    mob.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, mob.getLocation().clone().add(0, 1.0, 0), 6, 0.3, 0.3, 0.3, 0.05);
                    mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.5f, 1.6f);
                }
            }

            // 4. STOMP Skill: periodically ground-stomps AoE shockwave when foes are nearby
            int stompTier = petData.getSkillLevel(PetSkill.STOMP);
            if (stompTier > 0 && now - activePet.getLastStompTime() >= 15000L) {
                List<LivingEntity> nearbyFoes = new ArrayList<>();
                for (Entity e : mob.getNearbyEntities(4.5, 2.5, 4.5)) {
                    if (e instanceof Monster monster && !monster.isDead()) {
                        nearbyFoes.add(monster);
                    }
                }
                if (!nearbyFoes.isEmpty()) {
                    activePet.setLastStompTime(now);
                    double stompDmg = 3.0 + (stompTier * 2.0);
                    mob.getWorld().spawnParticle(Particle.EXPLOSION, mob.getLocation(), 1);
                    mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.7f, 0.8f);
                    for (LivingEntity foe : nearbyFoes) {
                        foe.damage(stompDmg, mob);
                        org.bukkit.util.Vector push = foe.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize().multiply(0.8).setY(0.35);
                        foe.setVelocity(push);
                    }
                }
            }
        }
    }

    private void applyBeaconAura(@NotNull Player player, int tier) {
        int duration = 80; // 4 seconds refresh duration
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, tier >= 4 ? 1 : 0, true, false, false));
        if (tier >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, tier >= 5 ? 1 : 0, true, false, false));
        }
        if (tier >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, tier >= 5 ? 1 : 0, true, false, false));
        }
        if (tier >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, tier >= 5 ? 1 : 0, true, false, false));
        }
    }
}
