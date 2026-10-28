package com.harukiyu.claritypets.listener;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.PetSkill;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private final ClarityPets plugin;

    public PlayerConnectionListener(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPetManager().loadPlayerDataAsync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPetManager().unloadPlayerDataAsync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        for (PlayerPetData pet : plugin.getPetManager().getPlayerPets(player.getUniqueId())) {
            if (pet.hasSkill(PetSkill.BACKPACK) && event.getInventory().equals(pet.getBackpackInventory())) {
                plugin.getPetRepository().savePet(pet);
                break;
            }
        }
    }
}
