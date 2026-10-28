package com.harukiyu.claritypets.hook;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.model.ActivePet;
import com.harukiyu.claritypets.model.DuelStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final ClarityPets plugin;

    public PlaceholderHook(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "claritypets";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Harukiyu";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        DuelStats stats = plugin.getPetManager().getDuelStats(offlinePlayer.getUniqueId());

        String direct = switch (params.toLowerCase(java.util.Locale.ROOT)) {
            case "duel_wins" -> String.valueOf(stats.getWins());
            case "duel_losses" -> String.valueOf(stats.getLosses());
            case "duel_draws" -> String.valueOf(stats.getDraws());
            case "duel_streak" -> String.valueOf(stats.getWinStreak());
            case "duel_best_streak" -> String.valueOf(stats.getBestStreak());
            case "duel_rating" -> String.valueOf(stats.getRating());
            case "duel_winrate" -> String.format("%.1f%%", stats.getWinRate());
            case "total_pets" -> String.valueOf(plugin.getPetManager().getPlayerPets(offlinePlayer.getUniqueId()).size());
            default -> null;
        };
        if (direct != null) return direct;

        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            ActivePet activePet = plugin.getPetManager().getActivePet(offlinePlayer.getPlayer());
            if (activePet != null) {
                return switch (params.toLowerCase(java.util.Locale.ROOT)) {
                    case "active_pet" -> activePet.getPetData().getEffectiveName();
                    case "active_pet_type" -> activePet.getPetData().getPetType().getDisplayName();
                    case "active_pet_level" -> String.valueOf(activePet.getPetData().getLevel());
                    case "active_pet_category" -> activePet.getPetData().getPetType().getCategory().name();
                    case "active_pet_exp" -> String.format("%.0f", activePet.getPetData().getCurrentXp());
                    case "active_pet_mode" -> activePet.getPetData().getBehaviorMode().name();
                    case "active_pet_passive" -> activePet.getPetData().getPetType().getPassiveName();
                    case "active_pet_active_skill" -> activePet.getPetData().getPetType().getActiveSkillName();
                    default -> null;
                };
            } else {
                return switch (params.toLowerCase(java.util.Locale.ROOT)) {
                    case "active_pet", "active_pet_type" -> "None";
                    case "active_pet_level", "active_pet_exp" -> "0";
                    case "active_pet_category", "active_pet_mode", "active_pet_passive", "active_pet_active_skill" -> "-";
                    default -> null;
                };
            }
        }

        return null;
    }
}
