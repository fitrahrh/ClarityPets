package com.harukiyu.claritypets.listener;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.DuelMatch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class ColosseumArenaListener implements Listener {

    private final ClarityPets plugin;

    public ColosseumArenaListener(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onColosseumDamage(@NotNull EntityDamageByEntityEvent event) {
        DuelMatch match = plugin.getColosseumEngine().getCurrentMatch();
        if (match == null || match.getState() != DuelMatch.State.FIGHTING) return;

        LivingEntity attacker = null;
        if (event.getDamager() instanceof LivingEntity le) {
            attacker = le;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof LivingEntity shooter) {
            attacker = shooter;
        }

        if (attacker == null || !(event.getEntity() instanceof LivingEntity victim)) return;

        if (match.involvesEntity(attacker.getUniqueId()) && match.involvesEntity(victim.getUniqueId())) {
            plugin.getColosseumEngine().handleDuelHit(attacker, victim);
        }
    }
}
