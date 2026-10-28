package com.harukiyu.claritypets.listener;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.ActivePet;
import com.harukiyu.claritypets.model.PetSkill;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class PetGatheringListener implements Listener {

    private final ClarityPets plugin;

    public PetGatheringListener(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        if (activePet == null || !activePet.isValid() || activePet.isDuelClone()) return;

        Block block = event.getBlock();
        Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
        if (!loc.getWorld().equals(activePet.getEntity().getWorld()) || loc.distanceSquared(activePet.getEntity().getLocation()) > 16 * 16) {
            return;
        }

        PlayerPetData petData = activePet.getPetData();
        Material mat = block.getType();
        String matName = mat.name();

        // 1. MINING Skill: Double ore drops & bonus EXP
        if (matName.endsWith("_ORE") || matName.equals("ANCIENT_DEBRIS")) {
            int miningTier = petData.getSkillLevel(PetSkill.MINING);
            if (miningTier > 0) {
                int chance = 10 + (miningTier * 6);
                if (ThreadLocalRandom.current().nextInt(100) < chance) {
                    Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);
                    for (ItemStack drop : drops) {
                        block.getWorld().dropItemNaturally(loc, drop.clone());
                    }
                    event.setExpToDrop((int) (event.getExpToDrop() * 1.20));
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 8, 0.3, 0.3, 0.3, 0.05);
                    loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.8f);
                }
            }
            return;
        }

        // 2. LUMBERJACK Skill: Bonus wood drops
        if (matName.endsWith("_LOG") || matName.endsWith("_WOOD") || matName.endsWith("_STEM") || matName.endsWith("_HYPHAE")) {
            int lumberTier = petData.getSkillLevel(PetSkill.LUMBERJACK);
            if (lumberTier > 0) {
                int chance = 10 + (lumberTier * 6);
                if (ThreadLocalRandom.current().nextInt(100) < chance) {
                    Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);
                    for (ItemStack drop : drops) {
                        block.getWorld().dropItemNaturally(loc, drop.clone());
                    }
                    loc.getWorld().spawnParticle(Particle.COMPOSTER, loc, 8, 0.3, 0.3, 0.3, 0.05);
                    loc.getWorld().playSound(loc, Sound.BLOCK_WOOD_BREAK, 0.6f, 1.5f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(@NotNull PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = event.getPlayer();
        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        if (activePet == null || !activePet.isValid() || activePet.isDuelClone()) return;

        PlayerPetData petData = activePet.getPetData();
        int fishTier = petData.getSkillLevel(PetSkill.FISHING);
        if (fishTier <= 0) return;

        int chance = 10 + (fishTier * 6);
        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            if (event.getCaught() instanceof Item caughtItem) {
                ItemStack item = caughtItem.getItemStack();
                caughtItem.getWorld().dropItemNaturally(caughtItem.getLocation(), item.clone());
                caughtItem.getWorld().spawnParticle(Particle.SPLASH, caughtItem.getLocation(), 15, 0.3, 0.3, 0.3, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 0.8f, 1.4f);
            }
        }
    }
}
