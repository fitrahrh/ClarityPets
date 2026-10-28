package com.harukiyu.claritypets.config;

import com.harukiyu.claritypets.ClarityPets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigManager {

    private final ClarityPets plugin;

    private String dbHost;
    private int dbPort;
    private String dbName;
    private String dbUser;
    private String dbPass;
    private boolean dbUseSsl;
    private int dbMaxPoolSize;
    private int dbMinIdle;
    private int dbConnTimeout;

    private double defaultScale;
    private int maxLevel;
    private long saveIntervalTicks;

    private int baseExp;
    private double expMultiplier;
    private double expMobKill;
    private double expOreMining;
    private double expCropHarvest;
    private double expFishing;
    private double treatExp;
    private int dailyMaxTreats;

    private int matchDurationSeconds;
    private int healCooldownSeconds;
    private double skillProcChance;

    private String arenaWorldName;
    private Location arenaPos1;
    private Location arenaPos2;
    private Location arenaSpawn1;
    private Location arenaSpawn2;

    public ConfigManager(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Database
        this.dbHost = config.getString("database.host", "localhost");
        this.dbPort = config.getInt("database.port", 3306);
        this.dbName = config.getString("database.database", "clarity");
        this.dbUser = config.getString("database.username", "root");
        this.dbPass = config.getString("database.password", "password");
        this.dbUseSsl = config.getBoolean("database.use-ssl", false);
        this.dbMaxPoolSize = config.getInt("database.pool.maximum-pool-size", 10);
        this.dbMinIdle = config.getInt("database.pool.minimum-idle", 5);
        this.dbConnTimeout = config.getInt("database.pool.connection-timeout", 30000);

        // Pets
        this.defaultScale = config.getDouble("pets.default-scale", 0.7);
        this.maxLevel = config.getInt("pets.max-level", 50);
        this.saveIntervalTicks = config.getLong("pets.save-interval-ticks", 6000L);

        // Leveling
        this.baseExp = config.getInt("leveling.base-exp", 100);
        this.expMultiplier = config.getDouble("leveling.exp-multiplier", 1.25);
        this.expMobKill = config.getDouble("leveling.survival-exp.mob-kill", 25.0);
        this.expOreMining = config.getDouble("leveling.survival-exp.ore-mining", 15.0);
        this.expCropHarvest = config.getDouble("leveling.survival-exp.crop-harvest", 10.0);
        this.expFishing = config.getDouble("leveling.survival-exp.fishing", 20.0);
        this.treatExp = config.getDouble("leveling.treats.exp-per-treat", 150.0);
        this.dailyMaxTreats = config.getInt("leveling.treats.daily-max-treats", 5);

        // Colosseum
        this.matchDurationSeconds = config.getInt("colosseum.match-duration-seconds", 60);
        this.healCooldownSeconds = config.getInt("colosseum.heal-cooldown-seconds", 10);
        this.skillProcChance = config.getDouble("colosseum.skill-proc-chance", 0.20);

        this.arenaWorldName = config.getString("colosseum.arena.world", "world");
        World world = Bukkit.getWorld(arenaWorldName);

        this.arenaPos1 = readLocation(config, "colosseum.arena.pos1", world);
        this.arenaPos2 = readLocation(config, "colosseum.arena.pos2", world);
        this.arenaSpawn1 = readLocation(config, "colosseum.arena.spawn1", world);
        this.arenaSpawn2 = readLocation(config, "colosseum.arena.spawn2", world);
    }

    private Location readLocation(FileConfiguration config, String path, @Nullable World fallbackWorld) {
        if (!config.contains(path + ".x")) return null;
        World w = fallbackWorld;
        if (config.contains(path + ".world")) {
            String wName = config.getString(path + ".world");
            if (wName != null) {
                World custom = Bukkit.getWorld(wName);
                if (custom != null) w = custom;
            }
        }
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public void setArenaPos1(@NotNull Location loc) {
        this.arenaPos1 = loc.clone();
        saveArenaLoc("pos1", loc, false);
    }

    public void setArenaPos2(@NotNull Location loc) {
        this.arenaPos2 = loc.clone();
        saveArenaLoc("pos2", loc, false);
    }

    public void setArenaSpawn1(@NotNull Location loc) {
        this.arenaSpawn1 = loc.clone();
        saveArenaLoc("spawn1", loc, true);
    }

    public void setArenaSpawn2(@NotNull Location loc) {
        this.arenaSpawn2 = loc.clone();
        saveArenaLoc("spawn2", loc, true);
    }

    private void saveArenaLoc(String key, Location loc, boolean includeYawPitch) {
        FileConfiguration config = plugin.getConfig();
        String path = "colosseum.arena." + key;
        if (loc.getWorld() != null) {
            config.set("colosseum.arena.world", loc.getWorld().getName());
            this.arenaWorldName = loc.getWorld().getName();
        }
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        if (includeYawPitch) {
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
        }
        plugin.saveConfig();
    }

    public String getDbHost() { return dbHost; }
    public int getDbPort() { return dbPort; }
    public String getDbName() { return dbName; }
    public String getDbUser() { return dbUser; }
    public String getDbPass() { return dbPass; }
    public boolean isDbUseSsl() { return dbUseSsl; }
    public int getDbMaxPoolSize() { return dbMaxPoolSize; }
    public int getDbMinIdle() { return dbMinIdle; }
    public int getDbConnTimeout() { return dbConnTimeout; }

    public double getDefaultScale() { return defaultScale; }
    public int getMaxLevel() { return maxLevel; }
    public long getSaveIntervalTicks() { return saveIntervalTicks; }

    public int getBaseExp() { return baseExp; }
    public double getExpMultiplier() { return expMultiplier; }
    public double getExpMobKill() { return expMobKill; }
    public double getExpOreMining() { return expOreMining; }
    public double getExpCropHarvest() { return expCropHarvest; }
    public double getExpFishing() { return expFishing; }
    public double getTreatExp() { return treatExp; }
    public int getDailyMaxTreats() { return dailyMaxTreats; }

    public int getMatchDurationSeconds() { return matchDurationSeconds; }
    public int getHealCooldownSeconds() { return healCooldownSeconds; }
    public double getSkillProcChance() { return skillProcChance; }

    public String getArenaWorldName() { return arenaWorldName; }
    public Location getArenaPos1() { return arenaPos1; }
    public Location getArenaPos2() { return arenaPos2; }
    public Location getArenaSpawn1() { return arenaSpawn1; }
    public Location getArenaSpawn2() { return arenaSpawn2; }
}
