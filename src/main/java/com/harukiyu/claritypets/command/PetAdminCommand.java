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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class PetAdminCommand implements BasicCommand {

    private final ClarityPets plugin;

    public PetAdminCommand(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable String permission() {
        return "claritypets.admin";
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission("claritypets.admin")) {
            LanguageHook.sendMessage(sender, "message.no_permission");
            return;
        }

        if (args.length == 0) {
            LanguageHook.sendMessage(sender, "message.admin_help");
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                plugin.getConfigManager().loadConfig();
                LanguageHook.reload();
                LanguageHook.sendMessage(sender, "message.admin_reload_success");
            }
            case "setarena" -> {
                if (!(sender instanceof Player player)) {
                    LanguageHook.sendMessage(sender, "message.player_only");
                    return;
                }
                if (args.length < 2) {
                    LanguageHook.sendMessage(player, "message.admin_setarena_usage");
                    return;
                }
                String point = args[1].toLowerCase(Locale.ROOT);
                switch (point) {
                    case "pos1" -> plugin.getConfigManager().setArenaPos1(player.getLocation());
                    case "pos2" -> plugin.getConfigManager().setArenaPos2(player.getLocation());
                    case "spawn1" -> plugin.getConfigManager().setArenaSpawn1(player.getLocation());
                    case "spawn2" -> plugin.getConfigManager().setArenaSpawn2(player.getLocation());
                    default -> {
                        LanguageHook.sendMessage(player, "message.admin_setarena_usage");
                        return;
                    }
                }
                LanguageHook.sendMessage(player, "message.admin_arena_set", Placeholder.unparsed("point", point));
            }
            case "give" -> {
                if (args.length < 3) {
                    LanguageHook.sendMessage(sender, "message.admin_give_usage");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(sender, "message.player_not_found");
                    return;
                }
                PetType type = PetType.fromString(args[2]);
                if (type == null) {
                    LanguageHook.sendMessage(sender, "message.invalid_pet_type");
                    return;
                }
                int level = 1;
                if (args.length >= 4) {
                    try {
                        level = Math.max(1, Integer.parseInt(args[3]));
                    } catch (NumberFormatException ignored) {}
                }

                PlayerPetData data = plugin.getPetManager().getPlayerPet(target.getUniqueId(), type);
                if (data != null) {
                    LanguageHook.sendMessage(sender, "message.already_unlocked", Placeholder.unparsed("pet", type.getDisplayName()));
                    return;
                }

                PlayerPetData newPet = PlayerPetData.createDefault(target.getUniqueId(), type);
                newPet.setLevel(level);
                plugin.getPetManager().addPetToPlayer(target.getUniqueId(), newPet);

                LanguageHook.sendMessage(sender, "message.admin_give_success",
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("pet", type.getDisplayName()),
                        Placeholder.unparsed("level", String.valueOf(level))
                );
                LanguageHook.sendMessage(target, "message.admin_given_notice",
                        Placeholder.unparsed("pet", type.getDisplayName()),
                        Placeholder.unparsed("level", String.valueOf(level))
                );
            }
            case "remove" -> {
                if (args.length < 3) {
                    LanguageHook.sendMessage(sender, "message.admin_remove_usage");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(sender, "message.player_not_found");
                    return;
                }
                PetType type = PetType.fromString(args[2]);
                if (type == null) {
                    LanguageHook.sendMessage(sender, "message.invalid_pet_type");
                    return;
                }

                plugin.getPetManager().removePetFromPlayer(target.getUniqueId(), type);
                LanguageHook.sendMessage(sender, "message.admin_remove_success",
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("pet", type.getDisplayName())
                );
            }
            case "setlevel" -> {
                if (args.length < 4) {
                    LanguageHook.sendMessage(sender, "message.admin_setlevel_usage");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(sender, "message.player_not_found");
                    return;
                }
                PetType type = PetType.fromString(args[2]);
                if (type == null) {
                    LanguageHook.sendMessage(sender, "message.invalid_pet_type");
                    return;
                }
                int level;
                try {
                    level = Math.max(1, Integer.parseInt(args[3]));
                } catch (NumberFormatException e) {
                    LanguageHook.sendMessage(sender, "message.invalid_level");
                    return;
                }

                PlayerPetData data = plugin.getPetManager().getPlayerPet(target.getUniqueId(), type);
                if (data == null) {
                    LanguageHook.sendMessage(sender, "message.not_unlocked", Placeholder.unparsed("pet", type.getDisplayName()));
                    return;
                }

                data.setLevel(level);
                data.setCurrentXp(0.0);
                plugin.getPetRepository().savePet(data);

                LanguageHook.sendMessage(sender, "message.admin_setlevel_success",
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("pet", type.getDisplayName()),
                        Placeholder.unparsed("level", String.valueOf(level))
                );
            }
            case "resetdailytreats" -> {
                if (args.length < 2) {
                    LanguageHook.sendMessage(sender, "message.admin_reset_treats_usage");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(sender, "message.player_not_found");
                    return;
                }

                for (PlayerPetData pet : plugin.getPetManager().getPlayerPets(target.getUniqueId())) {
                    pet.setDailyTreatsUsed(0);
                    pet.setLastTreatDate("");
                    plugin.getPetRepository().savePet(pet);
                }

                LanguageHook.sendMessage(sender, "message.admin_reset_treats_success",
                        Placeholder.unparsed("player", target.getName())
                );
            }
            case "addpoints" -> {
                if (args.length < 4) {
                    LanguageHook.sendMessage(sender, "message.admin_addpoints_usage");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(sender, "message.player_not_found");
                    return;
                }
                PetType type = PetType.fromString(args[2]);
                if (type == null) {
                    LanguageHook.sendMessage(sender, "message.invalid_pet_type");
                    return;
                }
                int points;
                try {
                    points = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    LanguageHook.sendMessage(sender, "message.invalid_points");
                    return;
                }
                PlayerPetData pet = plugin.getPetManager().getPlayerPet(target.getUniqueId(), type);
                if (pet == null) {
                    LanguageHook.sendMessage(sender, "message.not_unlocked", Placeholder.unparsed("pet", type.getDisplayName()));
                    return;
                }
                pet.addSkillPoints(points);
                plugin.getPetRepository().savePet(pet);
                LanguageHook.sendMessage(sender, "message.admin_addpoints_success",
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("points", String.valueOf(points)),
                        Placeholder.unparsed("pet", pet.getEffectiveName())
                );
            }
            case "resetpoints" -> {
                if (args.length < 3) {
                    LanguageHook.sendMessage(sender, "message.admin_resetpoints_usage");
                    return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    LanguageHook.sendMessage(sender, "message.player_not_found");
                    return;
                }
                PetType type = PetType.fromString(args[2]);
                if (type == null) {
                    LanguageHook.sendMessage(sender, "message.invalid_pet_type");
                    return;
                }
                PlayerPetData pet = plugin.getPetManager().getPlayerPet(target.getUniqueId(), type);
                if (pet == null) {
                    LanguageHook.sendMessage(sender, "message.not_unlocked", Placeholder.unparsed("pet", type.getDisplayName()));
                    return;
                }
                pet.resetSkillPoints();
                plugin.getPetRepository().savePet(pet);
                LanguageHook.sendMessage(sender, "message.admin_resetpoints_success",
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("pet", pet.getEffectiveName())
                );
            }
            default -> LanguageHook.sendMessage(sender, "message.admin_help");
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (!stack.getSender().hasPermission("claritypets.admin")) return list;

        switch (args.length) {
            case 1 -> {
                String prefix = args[0].toLowerCase(Locale.ROOT);
                for (String s : List.of("give", "remove", "setlevel", "addpoints", "resetpoints", "resetdailytreats", "setarena", "reload")) {
                    if (s.startsWith(prefix)) list.add(s);
                }
            }
            case 2 -> {
                String prefix = args[1].toLowerCase(Locale.ROOT);
                if (args[0].equalsIgnoreCase("setarena")) {
                    for (String p : List.of("pos1", "pos2", "spawn1", "spawn2")) {
                        if (p.startsWith(prefix)) list.add(p);
                    }
                } else if (List.of("give", "remove", "setlevel", "addpoints", "resetpoints", "resetdailytreats").contains(args[0].toLowerCase(Locale.ROOT))) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p != null && p.getName() != null && p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(p.getName());
                        }
                    }
                }
            }
            case 3 -> {
                String prefix = args[2].toLowerCase(Locale.ROOT);
                if (List.of("give", "remove", "setlevel", "addpoints", "resetpoints").contains(args[0].toLowerCase(Locale.ROOT))) {
                    for (PetType type : PetType.values()) {
                        if (type.getId().startsWith(prefix)) {
                            list.add(type.getId());
                        }
                    }
                }
            }
            case 4 -> {
                if (args[0].equalsIgnoreCase("setlevel") || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("addpoints")) {
                    list.addAll(List.of("1", "5", "10", "20", "30", "50"));
                }
            }
            default -> {}
        }
        return list;
    }
}
