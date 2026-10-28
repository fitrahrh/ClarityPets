package com.harukiyu.claritypets.listener;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.ActivePet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

public class PetSurvivalActivityListener implements Listener {

    private final ClarityPets plugin;

    public PetSurvivalActivityListener(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKill(@NotNull EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ActivePet activePet = plugin.getPetManager().getActivePet(killer);
        if (activePet != null && !activePet.isDuelClone()) {
            double exp = plugin.getConfigManager().getExpMobKill();
            plugin.getLevelingManager().addExp(killer, activePet.getPetData(), exp);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        if (activePet == null || activePet.isDuelClone()) return;

        Block block = event.getBlock();
        Material mat = block.getType();

        // 1. Ore Mining
        if (isOre(mat)) {
            double exp = plugin.getConfigManager().getExpOreMining();
            plugin.getLevelingManager().addExp(player, activePet.getPetData(), exp);
            return;
        }

        // 2. Crop Harvesting
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                double exp = plugin.getConfigManager().getExpCropHarvest();
                plugin.getLevelingManager().addExp(player, activePet.getPetData(), exp);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(@NotNull PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = event.getPlayer();
        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        if (activePet != null && !activePet.isDuelClone()) {
            double exp = plugin.getConfigManager().getExpFishing();
            plugin.getLevelingManager().addExp(player, activePet.getPetData(), exp);
        }
    }

    private boolean isOre(@NotNull Material material) {
        String name = material.name();
        return name.endsWith("_ORE") || name.equals("ANCIENT_DEBRIS");
    }
}
