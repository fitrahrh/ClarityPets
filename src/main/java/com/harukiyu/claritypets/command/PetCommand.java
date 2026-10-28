package com.harukiyu.claritypets.command;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.hook.LanguageHook;
import com.harukiyu.claritypets.model.PetType;
import com.harukiyu.claritypets.model.PlayerPetData;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class PetCommand implements BasicCommand {

    private final ClarityPets plugin;

    public PetCommand(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "message.player_only");
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            plugin.getDialogManager().openMainMenu(player);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "storage" -> plugin.getDialogManager().openPetStorageMenu(player, true);
            case "colosseum" -> plugin.getDialogManager().openColosseumMenu(player, true);
            case "guide", "tips" -> plugin.getDialogManager().openGuideMenu(player, true);
            case "despawn" -> plugin.getPetManager().despawnPet(player, true);
            case "skilltree", "skills" -> {
                com.harukiyu.claritypets.model.ActivePet active = plugin.getPetManager().getActivePet(player);
                if (active == null) {
                    LanguageHook.sendMessage(player, "message.no_active_pet");
                    return;
                }
                plugin.getDialogManager().openSkilltreeMenu(player, active.getPetData(), true);
            }
            case "backpack", "bag" -> {
                com.harukiyu.claritypets.model.ActivePet active = plugin.getPetManager().getActivePet(player);
                if (active == null) {
                    LanguageHook.sendMessage(player, "message.no_active_pet");
                    return;
                }
                plugin.getDialogManager().openBackpack(player, active.getPetData());
            }
            case "mode" -> {
                com.harukiyu.claritypets.model.ActivePet active = plugin.getPetManager().getActivePet(player);
                if (active == null) {
                    LanguageHook.sendMessage(player, "message.no_active_pet");
                    return;
                }
                if (args.length < 2) {
                    plugin.getDialogManager().openBehaviorModeMenu(player, active.getPetData(), true);
                    return;
                }
                com.harukiyu.claritypets.model.BehaviorMode newMode = com.harukiyu.claritypets.model.BehaviorMode.fromString(args[1]);
                active.getPetData().setBehaviorMode(newMode);
                if (newMode == com.harukiyu.claritypets.model.BehaviorMode.SENTRY) {
                    active.setSentryLocation(active.getEntity().getLocation().clone());
                }
                plugin.getPetRepository().savePet(active.getPetData());
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                LanguageHook.sendMessage(player, "message.mode_changed", Placeholder.unparsed("mode", newMode.name()));
            }
            case "summon" -> {
                if (args.length < 2) {
                    LanguageHook.sendMessage(player, "message.summon_usage");
                    return;
                }
                PetType type = PetType.fromString(args[1]);
                if (type == null) {
                    LanguageHook.sendMessage(player, "message.invalid_pet_type");
                    return;
                }
                PlayerPetData data = plugin.getPetManager().getPlayerPet(player.getUniqueId(), type);
                if (data == null) {
                    LanguageHook.sendMessage(player, "message.not_unlocked", Placeholder.unparsed("pet", type.getDisplayName()));
                    return;
                }
                plugin.getPetManager().spawnPet(player, data, true);
            }
            case "duel" -> {
                if (args.length < 2) {
                    plugin.getDialogManager().openColosseumMenu(player, true);
                    return;
                }
                if (args[1].equalsIgnoreCase("accept")) {
                    if (args.length < 3) {
                        LanguageHook.sendMessage(player, "message.duel_accept_usage");
                        return;
                    }
                    Player challenger = Bukkit.getPlayer(args[2]);
                    if (challenger == null) {
                        LanguageHook.sendMessage(player, "message.player_not_found");
                        return;
                    }
                    plugin.getColosseumEngine().acceptChallenge(player, challenger);
                    return;
                }
                if (args[1].equalsIgnoreCase("decline")) {
                    if (args.length < 3) return;
                    Player challenger = Bukkit.getPlayer(args[2]);
                    if (challenger != null) {
                        plugin.getColosseumEngine().declineChallenge(player, challenger);
                    }
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(player, "message.player_not_found");
                    return;
                }
                plugin.getColosseumEngine().sendChallenge(player, target);
            }
            default -> plugin.getDialogManager().openMainMenu(player);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) return list;

        switch (args.length) {
            case 1 -> {
                String prefix = args[0].toLowerCase(Locale.ROOT);
                for (String s : List.of("menu", "storage", "summon", "despawn", "skilltree", "backpack", "colosseum", "duel", "guide", "mode")) {
                    if (s.startsWith(prefix)) list.add(s);
                }
            }
            case 2 -> {
                String prefix = args[1].toLowerCase(Locale.ROOT);
                if (args[0].equalsIgnoreCase("summon")) {
                    for (PlayerPetData pet : plugin.getPetManager().getPlayerPets(player.getUniqueId())) {
                        if (pet.getPetType().getId().startsWith(prefix)) {
                            list.add(pet.getPetType().getId());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("mode")) {
                    for (String m : List.of("passive", "defensive", "aggressive", "sentry")) {
                        if (m.startsWith(prefix)) list.add(m);
                    }
                } else if (args[0].equalsIgnoreCase("duel")) {
                    if ("accept".startsWith(prefix)) list.add("accept");
                    if ("decline".startsWith(prefix)) list.add("decline");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p != null && p.getName() != null && !p.equals(player) && p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(p.getName());
                        }
                    }
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("duel")) {
                    String prefix = args[2].toLowerCase(Locale.ROOT);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p != null && p.getName() != null && !p.equals(player) && p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(p.getName());
                        }
                    }
                }
            }
            default -> {}
        }
        return list;
    }
}
