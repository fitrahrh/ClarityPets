package com.harukiyu.claritypets.service;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.hook.LanguageHook;
import com.harukiyu.claritypets.model.ActivePet;
import com.harukiyu.claritypets.model.PlayerPetData;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LevelingManager {

    private final ClarityPets plugin;

    public LevelingManager(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    public void addExp(@NotNull Player player, @NotNull PlayerPetData petData, double amount) {
        int maxLevel = plugin.getConfigManager().getMaxLevel();
        if (petData.getLevel() >= maxLevel) {
            petData.setLevel(maxLevel);
            petData.setCurrentXp(0.0);
            return;
        }

        int baseExp = plugin.getConfigManager().getBaseExp();
        double multiplier = plugin.getConfigManager().getExpMultiplier();

        double currentXp = petData.getCurrentXp() + amount;
        int currentLevel = petData.getLevel();
        boolean leveledUp = false;

        while (currentLevel < maxLevel) {
            double needed = petData.getMaxXpForNextLevel(baseExp, multiplier);
            if (currentXp >= needed) {
                currentXp -= needed;
                currentLevel++;
                petData.addSkillPoints(1);
                leveledUp = true;
            } else {
                break;
            }
        }

        if (currentLevel >= maxLevel) {
            currentLevel = maxLevel;
            currentXp = 0.0;
        }

        petData.setLevel(currentLevel);
        petData.setCurrentXp(currentXp);

        if (leveledUp) {
            handleLevelUp(player, petData);
        }
    }

    private void handleLevelUp(@NotNull Player player, @NotNull PlayerPetData petData) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1.2, 0), 20, 0.4, 0.4, 0.4, 0.1);

        LanguageHook.sendMessage(player, "message.level_up",
                Placeholder.unparsed("name", petData.getEffectiveName()),
                Placeholder.unparsed("level", String.valueOf(petData.getLevel()))
        );

        // Update active entity attributes if summoned
        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        if (activePet != null && activePet.getPetData().getPetType() == petData.getPetType()) {
            LivingEntity entity = activePet.getEntity();
            if (entity != null && entity.isValid()) {
                AttributeInstance maxHpAttr = entity.getAttribute(Attribute.MAX_HEALTH);
                if (maxHpAttr != null) {
                    maxHpAttr.setBaseValue(petData.getMaxHp());
                    entity.setHealth(petData.getMaxHp());
                }

                AttributeInstance atkAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
                if (atkAttr != null) {
                    atkAttr.setBaseValue(petData.getAtk());
                }

                AttributeInstance defAttr = entity.getAttribute(Attribute.ARMOR);
                if (defAttr != null) {
                    defAttr.setBaseValue(petData.getDef());
                }

                AttributeInstance spdAttr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
                if (spdAttr != null) {
                    spdAttr.setBaseValue(petData.getSpd());
                }

                plugin.getPetManager().updateNametag(entity, petData, player);
            }
        }

        plugin.getPetRepository().savePet(petData);
    }
}
