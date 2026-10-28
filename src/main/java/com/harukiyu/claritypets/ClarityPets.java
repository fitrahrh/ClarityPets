package com.harukiyu.claritypets;

import com.harukiyu.claritypets.command.PetAdminCommand;
import com.harukiyu.claritypets.command.PetCommand;
import com.harukiyu.claritypets.config.ConfigManager;
import com.harukiyu.claritypets.database.DatabaseManager;
import com.harukiyu.claritypets.dialog.PetDialogManager;
import com.harukiyu.claritypets.hook.LanguageHook;
import com.harukiyu.claritypets.hook.PlaceholderHook;
import com.harukiyu.claritypets.listener.ColosseumArenaListener;
import com.harukiyu.claritypets.listener.PetCombatListener;
import com.harukiyu.claritypets.listener.PetGatheringListener;
import com.harukiyu.claritypets.listener.PetProtectionListener;
import com.harukiyu.claritypets.listener.PetSurvivalActivityListener;
import com.harukiyu.claritypets.listener.PlayerConnectionListener;
import com.harukiyu.claritypets.repository.DuelRepository;
import com.harukiyu.claritypets.repository.PetRepository;
import com.harukiyu.claritypets.service.ColosseumEngine;
import com.harukiyu.claritypets.service.LevelingManager;
import com.harukiyu.claritypets.service.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClarityPets extends JavaPlugin {

    private static ClarityPets instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PetRepository petRepository;
    private DuelRepository duelRepository;
    private PetManager petManager;
    private LevelingManager levelingManager;
    private ColosseumEngine colosseumEngine;
    private PetDialogManager dialogManager;

    @Override
    public void onEnable() {
        instance = this;

        long start = System.currentTimeMillis();
        getLogger().info("=================================================");
        getLogger().info(() -> " Starting ClarityPets v" + getPluginMeta().getVersion());
        getLogger().info(" Target Server: Paper 26.3 | Java 25");
        getLogger().info("=================================================");

        // 1. Config Manager
        this.configManager = new ConfigManager(this);

        // 2. Multi-Language Hook (ClarityLangCore 9 Languages integration)
        LanguageHook.init(this, "pets");

        // 3. Database Layer (HikariCP Connection Pool)
        this.databaseManager = new DatabaseManager(this, configManager);
        this.databaseManager.initialize();

        this.petRepository = new PetRepository(this, databaseManager);
        this.duelRepository = new DuelRepository(this, databaseManager);

        // 4. Service / Engine Layer
        this.petManager = new PetManager(this, petRepository, duelRepository);
        this.levelingManager = new LevelingManager(this);
        this.colosseumEngine = new ColosseumEngine(this);

        // 5. Native Dialog Menu Manager
        this.dialogManager = new PetDialogManager(this);

        // 6. Register Event Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new PetProtectionListener(this), this);
        pm.registerEvents(new PetSurvivalActivityListener(this), this);
        pm.registerEvents(new PetCombatListener(this), this);
        pm.registerEvents(new PetGatheringListener(this), this);
        pm.registerEvents(new ColosseumArenaListener(this), this);

        // 7. Register Commands (Paper 26.3 Native BasicCommand)
        registerCommand("pet", "Open pet menu and control companions", List.of("pets"), new PetCommand(this));
        registerCommand("petadmin", "Administrative commands for ClarityPets", List.of("petsadmin", "cpadmin"), new PetAdminCommand(this));

        // 8. PlaceholderAPI Hook
        if (pm.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }

        // 9. Reload scenario: load online players
        Bukkit.getOnlinePlayers().forEach(p -> petManager.loadPlayerDataAsync(p));

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info(() -> " ClarityPets enabled successfully in " + elapsed + "ms!");
        getLogger().info(" Native Dialog API: Active | Chest GUI strictly prohibited");
        getLogger().info(" Multi-Language: 9 Languages supported with dynamic switching");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ClarityPets...");

        if (colosseumEngine != null) {
            colosseumEngine.shutdown();
        }

        if (petManager != null) {
            petManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ClarityPets cleanly disabled.");
    }

    @NotNull
    public static ClarityPets getInstance() {
        return instance;
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @NotNull
    public PetRepository getPetRepository() {
        return petRepository;
    }

    @NotNull
    public DuelRepository getDuelRepository() {
        return duelRepository;
    }

    @NotNull
    public PetManager getPetManager() {
        return petManager;
    }

    @NotNull
    public LevelingManager getLevelingManager() {
        return levelingManager;
    }

    @NotNull
    public ColosseumEngine getColosseumEngine() {
        return colosseumEngine;
    }

    @NotNull
    public PetDialogManager getDialogManager() {
        return dialogManager;
    }
}
