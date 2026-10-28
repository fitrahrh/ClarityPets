package com.harukiyu.claritypets.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.PetSkill;
import com.harukiyu.claritypets.model.PlayerPetData;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;

public class PetPickupGoal implements Goal<Mob> {

    private final GoalKey<Mob> key;
    private final Mob mob;
    private final PlayerPetData petData;
    private Item targetItem;

    public PetPickupGoal(@NotNull ClarityPets plugin, @NotNull Mob mob, @NotNull PlayerPetData petData) {
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "pet_pickup"));
        this.mob = mob;
        this.petData = petData;
    }

    @Override
    public boolean shouldActivate() {
        if (!petData.hasSkill(PetSkill.PICKUP)) return false;
        if (mob.getTarget() != null) return false; // In combat, don't pick up

        int tier = petData.getSkillLevel(PetSkill.PICKUP);
        double radius = 3.0 + tier;

        for (Entity e : mob.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Item item && item.isValid() && !item.isDead() && item.getPickupDelay() <= 0) {
                targetItem = item;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldStayActive() {
        return targetItem != null && targetItem.isValid() && !targetItem.isDead() && mob.getTarget() == null;
    }

    @Override
    public void start() {
        if (targetItem != null) {
            mob.getPathfinder().moveTo(targetItem.getLocation(), 1.2);
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        mob.getPathfinder().stopPathfinding();
    }

    @Override
    public void tick() {
        if (targetItem == null || !targetItem.isValid() || targetItem.isDead()) {
            targetItem = null;
            return;
        }

        double distSq = mob.getLocation().distanceSquared(targetItem.getLocation());
        if (distSq <= 1.6 * 1.6) {
            // Pick up item into pet backpack
            ItemStack stack = targetItem.getItemStack();
            Inventory backpack = petData.getBackpackInventory();
            HashMap<Integer, ItemStack> remaining = backpack.addItem(stack);

            if (remaining.isEmpty()) {
                mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.3f);
                mob.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, mob.getLocation().add(0, 0.4, 0), 4, 0.1, 0.1, 0.1, 0.02);
                targetItem.remove();
            } else {
                targetItem.setItemStack(remaining.get(0));
            }
            targetItem = null;
        } else {
            if (mob.getTicksLived() % 10 == 0) {
                mob.getPathfinder().moveTo(targetItem.getLocation(), 1.2);
            }
        }
    }

    @Override
    public @NotNull GoalKey<Mob> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
