package com.harukiyu.claritypets.model;

import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ActivePet {

    private final UUID ownerUuid;
    private final PlayerPetData petData;
    private final Mob entity;
    private Location sentryLocation;
    private boolean duelClone;

    // Combat tracking state
    private int comboHits;
    private long lastHealSkillTime;
    private long lastActiveSkillProcTime;
    private long lastStompTime;
    private long lastTauntTime;
    private long lastPotionProcTime;
    private long rootedUntil;
    private long stunnedUntil;
    private long defenseBuffUntil;

    public ActivePet(@NotNull UUID ownerUuid, @NotNull PlayerPetData petData, @NotNull Mob entity, boolean duelClone) {
        this.ownerUuid = ownerUuid;
        this.petData = petData;
        this.entity = entity;
        this.duelClone = duelClone;
        this.sentryLocation = entity.getLocation().clone();
        this.comboHits = 0;
        this.lastHealSkillTime = 0L;
        this.lastActiveSkillProcTime = 0L;
        this.lastStompTime = 0L;
        this.lastTauntTime = 0L;
        this.lastPotionProcTime = 0L;
        this.rootedUntil = 0L;
        this.stunnedUntil = 0L;
        this.defenseBuffUntil = 0L;
    }

    @NotNull
    public UUID getOwnerUuid() { return ownerUuid; }

    @NotNull
    public PlayerPetData getPetData() { return petData; }

    @NotNull
    public Mob getEntity() { return entity; }

    @Nullable
    public Location getSentryLocation() { return sentryLocation; }
    public void setSentryLocation(@Nullable Location sentryLocation) { this.sentryLocation = sentryLocation; }

    public boolean isDuelClone() { return duelClone; }
    public void setDuelClone(boolean duelClone) { this.duelClone = duelClone; }

    public int getComboHits() { return comboHits; }
    public void incrementComboHits() { this.comboHits++; }
    public void resetComboHits() { this.comboHits = 0; }

    public long getLastHealSkillTime() { return lastHealSkillTime; }
    public void setLastHealSkillTime(long lastHealSkillTime) { this.lastHealSkillTime = lastHealSkillTime; }

    public long getLastActiveSkillProcTime() { return lastActiveSkillProcTime; }
    public void setLastActiveSkillProcTime(long lastActiveSkillProcTime) { this.lastActiveSkillProcTime = lastActiveSkillProcTime; }

    public long getLastStompTime() { return lastStompTime; }
    public void setLastStompTime(long lastStompTime) { this.lastStompTime = lastStompTime; }

    public long getLastTauntTime() { return lastTauntTime; }
    public void setLastTauntTime(long lastTauntTime) { this.lastTauntTime = lastTauntTime; }

    public long getLastPotionProcTime() { return lastPotionProcTime; }
    public void setLastPotionProcTime(long lastPotionProcTime) { this.lastPotionProcTime = lastPotionProcTime; }

    public boolean isRooted() { return System.currentTimeMillis() < rootedUntil; }
    public void setRootedDuration(long durationMillis) { this.rootedUntil = System.currentTimeMillis() + durationMillis; }

    public boolean isStunned() { return System.currentTimeMillis() < stunnedUntil; }
    public void setStunnedDuration(long durationMillis) { this.stunnedUntil = System.currentTimeMillis() + durationMillis; }

    public boolean hasDefenseBuff() { return System.currentTimeMillis() < defenseBuffUntil; }
    public void setDefenseBuffDuration(long durationMillis) { this.defenseBuffUntil = System.currentTimeMillis() + durationMillis; }

    public boolean isValid() {
        return entity.isValid() && !entity.isDead();
    }
}
