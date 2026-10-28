package com.harukiyu.claritypets.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class PlayerPetData {

    private int id;
    private final UUID playerUuid;
    private final PetType petType;
    private String customName;
    private int level;
    private double currentXp;
    private BehaviorMode behaviorMode;
    private boolean active;
    private ColosseumStance stance;
    private int dailyTreatsUsed;
    private String lastTreatDate;
    private Timestamp unlockedAt;

    // Advanced Skills & Skilltree System
    private SkilltreeType skilltreeType;
    private int skillPoints;
    private final Map<PetSkill, Integer> skillLevels = new ConcurrentHashMap<>();
    private Inventory backpackInventory;

    public PlayerPetData(int id, @NotNull UUID playerUuid, @NotNull PetType petType,
                         @Nullable String customName, int level, double currentXp,
                         @NotNull BehaviorMode behaviorMode, boolean active,
                         @NotNull ColosseumStance stance, int dailyTreatsUsed,
                         @Nullable String lastTreatDate, @NotNull Timestamp unlockedAt,
                         @NotNull SkilltreeType skilltreeType, int skillPoints,
                         @Nullable Map<PetSkill, Integer> skills, @Nullable String backpackBase64) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.petType = petType;
        this.customName = customName;
        this.level = Math.max(1, level);
        this.currentXp = Math.max(0.0, currentXp);
        this.behaviorMode = behaviorMode;
        this.active = active;
        this.stance = stance;
        this.dailyTreatsUsed = Math.max(0, dailyTreatsUsed);
        this.lastTreatDate = lastTreatDate != null ? lastTreatDate : "";
        this.unlockedAt = unlockedAt;
        this.skilltreeType = skilltreeType != null ? skilltreeType : petType.getDefaultSkilltree();
        this.skillPoints = Math.max(0, skillPoints);
        if (skills != null) {
            this.skillLevels.putAll(skills);
        }
        initBackpack(backpackBase64);
    }

    public static PlayerPetData createDefault(@NotNull UUID playerUuid, @NotNull PetType petType) {
        return new PlayerPetData(
                0,
                playerUuid,
                petType,
                null,
                1,
                0.0,
                BehaviorMode.PASSIVE,
                false,
                ColosseumStance.BALANCED,
                0,
                "",
                new Timestamp(System.currentTimeMillis()),
                petType.getDefaultSkilltree(),
                0,
                null,
                null
        );
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NotNull
    public UUID getPlayerUuid() { return playerUuid; }

    @NotNull
    public PetType getPetType() { return petType; }

    @Nullable
    public String getCustomName() { return customName; }
    public void setCustomName(@Nullable String customName) { this.customName = customName; }

    @NotNull
    public String getEffectiveName() {
        return (customName != null && !customName.isBlank()) ? customName : petType.getDisplayName();
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }

    public double getCurrentXp() { return currentXp; }
    public void setCurrentXp(double currentXp) { this.currentXp = Math.max(0.0, currentXp); }

    public double getMaxXpForNextLevel(int baseExp, double multiplier) {
        return baseExp * Math.pow(multiplier, level - 1);
    }

    @NotNull
    public BehaviorMode getBehaviorMode() { return behaviorMode; }
    public void setBehaviorMode(@NotNull BehaviorMode behaviorMode) { this.behaviorMode = behaviorMode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @NotNull
    public ColosseumStance getStance() { return stance; }
    public void setStance(@NotNull ColosseumStance stance) { this.stance = stance; }

    public int getDailyTreatsUsed() { return dailyTreatsUsed; }
    public void setDailyTreatsUsed(int dailyTreatsUsed) { this.dailyTreatsUsed = dailyTreatsUsed; }

    @NotNull
    public String getLastTreatDate() { return lastTreatDate; }
    public void setLastTreatDate(@NotNull String lastTreatDate) { this.lastTreatDate = lastTreatDate; }

    @NotNull
    public Timestamp getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(@NotNull Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }

    // ==========================================
    // Skills & Skilltree Getters / Setters
    // ==========================================

    @NotNull
    public SkilltreeType getSkilltreeType() { return skilltreeType; }
    public void setSkilltreeType(@NotNull SkilltreeType skilltreeType) { this.skilltreeType = skilltreeType; }

    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = Math.max(0, skillPoints); }
    public void addSkillPoints(int points) { this.skillPoints += Math.max(0, points); }

    public int getSkillLevel(@NotNull PetSkill skill) {
        return skillLevels.getOrDefault(skill, 0);
    }

    public boolean hasSkill(@NotNull PetSkill skill) {
        return getSkillLevel(skill) > 0;
    }

    public boolean upgradeSkill(@NotNull PetSkill skill) {
        if (skillPoints <= 0) return false;
        int current = getSkillLevel(skill);
        if (current >= skill.getMaxTier()) return false;
        skillLevels.put(skill, current + 1);
        skillPoints--;
        return true;
    }

    public void resetSkillPoints() {
        int spent = 0;
        for (int lvl : skillLevels.values()) {
            spent += lvl;
        }
        skillPoints += spent;
        skillLevels.clear();
    }

    @NotNull
    public Map<PetSkill, Integer> getSkillLevels() {
        return Collections.unmodifiableMap(skillLevels);
    }

    public void setSkillLevelRaw(@NotNull PetSkill skill, int tier) {
        if (tier > 0) {
            skillLevels.put(skill, Math.min(skill.getMaxTier(), tier));
        } else {
            skillLevels.remove(skill);
        }
    }

    // ==========================================
    // Dynamic Stat Calculations with Skills
    // ==========================================

    public double getMaxHp() {
        double base = petType.calculateMaxHp(level);
        int lifeTier = getSkillLevel(PetSkill.LIFE);
        return base + (lifeTier * 12.0);
    }

    public double getAtk() {
        double base = petType.calculateAtk(level);
        int dmgTier = getSkillLevel(PetSkill.DAMAGE);
        return base + (dmgTier * 2.0);
    }

    public double getDef() {
        double base = petType.calculateDef(level);
        int armTier = getSkillLevel(PetSkill.ARMOR);
        return base + (armTier * 3.0);
    }

    public double getSpd() {
        double base = petType.calculateSpd(level);
        int sprintTier = getSkillLevel(PetSkill.SPRINT);
        return Math.min(0.50, base + (sprintTier * 0.04));
    }

    // ==========================================
    // Virtual Backpack Storage
    // ==========================================

    private void initBackpack(@Nullable String base64) {
        int backpackTier = getSkillLevel(PetSkill.BACKPACK);
        int size = Math.max(9, Math.min(45, (backpackTier > 0 ? backpackTier : 1) * 9));
        this.backpackInventory = Bukkit.createInventory(null, size, Component.text(getEffectiveName() + "'s Backpack"));

        if (base64 != null && !base64.isBlank()) {
            ItemStack[] items = deserializeItemStacks(base64);
            for (int i = 0; i < Math.min(items.length, backpackInventory.getSize()); i++) {
                if (items[i] != null) {
                    backpackInventory.setItem(i, items[i]);
                }
            }
        }
    }

    public void resizeBackpackIfNeeded() {
        int backpackTier = getSkillLevel(PetSkill.BACKPACK);
        int targetSize = Math.max(9, Math.min(45, (backpackTier > 0 ? backpackTier : 1) * 9));
        if (backpackInventory != null && backpackInventory.getSize() == targetSize) {
            return;
        }

        Inventory newInv = Bukkit.createInventory(null, targetSize, Component.text(getEffectiveName() + "'s Backpack"));
        if (backpackInventory != null) {
            ItemStack[] contents = backpackInventory.getContents();
            for (int i = 0; i < Math.min(contents.length, targetSize); i++) {
                if (contents[i] != null) {
                    newInv.setItem(i, contents[i]);
                }
            }
        }
        this.backpackInventory = newInv;
    }

    @NotNull
    public Inventory getBackpackInventory() {
        resizeBackpackIfNeeded();
        return backpackInventory;
    }

    public String serializeBackpackBase64() {
        if (backpackInventory == null) return "";
        return serializeItemStacks(backpackInventory.getContents());
    }

    public static String serializeItemStacks(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return "";
        }
    }

    public static ItemStack[] deserializeItemStacks(String data) {
        if (data == null || data.isBlank()) return new ItemStack[0];
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
            return new ItemStack[0];
        }
    }
}
