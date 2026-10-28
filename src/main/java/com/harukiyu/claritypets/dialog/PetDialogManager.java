package com.harukiyu.claritypets.dialog;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.hook.LanguageHook;
import com.harukiyu.claritypets.model.*;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages all user interaction menus using the modern Paper 26.3 Native Dialog API.
 * Chest Inventory GUI is strictly prohibited.
 * All formatting strictly adheres to the visual guidelines:
 * - Titles: White only, not bold, no symbols.
 * - Body: Clean white text, not colorful, no symbols/emojis, not bold.
 * - General Buttons: White, not bold, no symbols.
 * - Confirm / Summon Buttons: Green, not bold, no symbols.
 * - Cancel / Release / Danger Buttons: Red, not bold, no symbols.
 */
public class PetDialogManager {

    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(-1)
            .lifetime(Duration.ofDays(365))
            .build();

    private final ClarityPets plugin;

    public PetDialogManager(@NotNull ClarityPets plugin) {
        this.plugin = plugin;
    }

    // ==========================================
    // 1. Main Pet Menu
    // ==========================================

    public void openMainMenu(@NotNull Player player) {
        openMainMenu(player, true);
    }

    public void openMainMenu(@NotNull Player player, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.main.title", player);

        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        Collection<PlayerPetData> pets = plugin.getPetManager().getPlayerPets(player.getUniqueId());

        String activeName = activePet != null ? activePet.getPetData().getEffectiveName() :
                LanguageHook.getString("dialog.main.none", player);
        String activeLevel = activePet != null ? String.valueOf(activePet.getPetData().getLevel()) : "-";
        String activeStatus = activePet != null ? activePet.getPetData().getBehaviorMode().name() :
                LanguageHook.getString("dialog.main.status_stored", player);

        Component body = LanguageHook.getComponent("dialog.main.body", player,
                Placeholder.unparsed("active_name", activeName),
                Placeholder.unparsed("active_level", activeLevel),
                Placeholder.unparsed("active_status", activeStatus),
                Placeholder.unparsed("total_pets", String.valueOf(pets.size()))
        );

        List<ActionButton> buttons = new ArrayList<>();

        // 1. Pet Storage Menu
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_storage", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPetStorageMenu(p, 0, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 2. Colosseum Menu
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_colosseum", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openColosseumMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 3. Pet Guide Menu
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_guide", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 4. Active Pet shortcuts (Skilltree & Backpack)
        if (activePet != null) {
            PlayerPetData activeData = activePet.getPetData();
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_skilltree", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openSkilltreeMenu(p, activeData, false);
                    }, CALLBACK_OPTIONS))
                    .build());

            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_backpack", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openBackpack(p, activeData);
                    }, CALLBACK_OPTIONS))
                    .build());

            // Despawn Active Pet
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_despawn", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            plugin.getPetManager().despawnPet(p, true);
                            openMainMenu(p, false);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Close button (White)
        ActionButton closeBtn = ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) p.closeDialog();
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, closeBtn, 2))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 2. Pet Storage Menu (Paginated)
    // ==========================================

    public void openPetStorageMenu(@NotNull Player player, boolean playSound) {
        openPetStorageMenu(player, 0, playSound);
    }

    public void openPetStorageMenu(@NotNull Player player, int page, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.storage.title", player);

        List<PlayerPetData> pets = new ArrayList<>(plugin.getPetManager().getPlayerPets(player.getUniqueId()));
        // Sort active pet first, then by level descending
        pets.sort((a, b) -> {
            if (a.isActive() != b.isActive()) return a.isActive() ? -1 : 1;
            return Integer.compare(b.getLevel(), a.getLevel());
        });

        int pageSize = 8;
        int totalPages = Math.max(1, (int) Math.ceil((double) pets.size() / pageSize));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, pets.size());

        Component body = LanguageHook.getComponent("dialog.storage.body", player,
                Placeholder.unparsed("page", String.valueOf(currentPage + 1)),
                Placeholder.unparsed("total_pages", String.valueOf(totalPages)),
                Placeholder.unparsed("total_pets", String.valueOf(pets.size()))
        );

        List<ActionButton> buttons = new ArrayList<>();

        for (int i = start; i < end; i++) {
            PlayerPetData pet = pets.get(i);
            Component btnLabel = LanguageHook.getComponent("dialog.storage.btn_pet", player,
                    Placeholder.unparsed("name", pet.getEffectiveName()),
                    Placeholder.unparsed("level", String.valueOf(pet.getLevel()))
            );

            buttons.add(ActionButton.builder(btnLabel)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openPetDetailMenu(p, pet, false);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Previous page button (White)
        if (currentPage > 0) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.storage.btn_prev", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openPetStorageMenu(p, currentPage - 1, false);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Next page button (White)
        if (currentPage < totalPages - 1) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.storage.btn_next", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openPetStorageMenu(p, currentPage + 1, false);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.storage.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        DialogType dialogType = buttons.isEmpty() ?
                DialogType.notice(backBtn) :
                DialogType.multiAction(buttons, backBtn, 2);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(dialogType)
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 3. Pet Detail & Management Menu
    // ==========================================

    public void openPetDetailMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.detail.title", player);

        int baseExp = plugin.getConfigManager().getBaseExp();
        double multiplier = plugin.getConfigManager().getExpMultiplier();
        double maxXp = pet.getMaxXpForNextLevel(baseExp, multiplier);

        String categoryKey = "category." + pet.getPetType().getCategory().name().toLowerCase(java.util.Locale.ROOT);
        String categoryName = LanguageHook.getString(categoryKey, player);

        String modeKey = "mode." + pet.getBehaviorMode().name().toLowerCase(java.util.Locale.ROOT);
        String modeName = LanguageHook.getString(modeKey, player);

        String modeDescKey = "mode_desc." + pet.getBehaviorMode().name().toLowerCase(java.util.Locale.ROOT);
        String modeDesc = LanguageHook.getString(modeDescKey, player);
        if (modeDesc.equals(modeDescKey)) {
            modeDesc = pet.getBehaviorMode().getDefaultDescription();
        }

        String passiveDescKey = "passive." + pet.getPetType().getId() + ".desc";
        String passiveDesc = LanguageHook.getString(passiveDescKey, player);
        if (passiveDesc.equals(passiveDescKey)) {
            passiveDesc = pet.getPetType().getPassiveDescription(pet.getLevel());
        }

        String activeDescKey = "active_skill." + pet.getPetType().getId() + ".desc";
        String activeDesc = LanguageHook.getString(activeDescKey, player);
        if (activeDesc.equals(activeDescKey)) {
            activeDesc = pet.getPetType().getActiveSkillDescription(pet.getLevel());
        }

        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        boolean isCurrentActive = activePet != null && activePet.getPetData().getPetType() == pet.getPetType();
        double currentHp = (isCurrentActive && activePet.getEntity().isValid()) ? activePet.getEntity().getHealth() : pet.getMaxHp();

        Component body = LanguageHook.getComponent("dialog.detail.body", player,
                Placeholder.unparsed("name", pet.getEffectiveName()),
                Placeholder.unparsed("type", pet.getPetType().getDisplayName()),
                Placeholder.unparsed("category", categoryName),
                Placeholder.unparsed("level", String.valueOf(pet.getLevel())),
                Placeholder.unparsed("exp", String.format("%.0f", pet.getCurrentXp())),
                Placeholder.unparsed("max_exp", String.format("%.0f", maxXp)),
                Placeholder.unparsed("hp", String.format("%.1f", currentHp)),
                Placeholder.unparsed("max_hp", String.format("%.1f", pet.getMaxHp())),
                Placeholder.unparsed("atk", String.format("%.1f", pet.getAtk())),
                Placeholder.unparsed("def", String.format("%.1f", pet.getDef())),
                Placeholder.unparsed("spd", String.format("%.2f", pet.getSpd())),
                Placeholder.unparsed("mode", modeName),
                Placeholder.unparsed("mode_desc", modeDesc),
                Placeholder.unparsed("passive_name", pet.getPetType().getPassiveName()),
                Placeholder.unparsed("passive_desc", passiveDesc),
                Placeholder.unparsed("active_skill_name", pet.getPetType().getActiveSkillName()),
                Placeholder.unparsed("active_desc", activeDesc),
                Placeholder.unparsed("skilltree", pet.getSkilltreeType().getDisplayName()),
                Placeholder.unparsed("skill_points", String.valueOf(pet.getSkillPoints()))
        );

        List<ActionButton> buttons = new ArrayList<>();

        // 1. Summon (Green) or Despawn (Red)
        if (isCurrentActive) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_despawn", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            plugin.getPetManager().despawnPet(p, true);
                            openPetDetailMenu(p, pet, false);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        } else {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_summon", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            plugin.getPetManager().spawnPet(p, pet, true);
                            openPetDetailMenu(p, pet, false);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // 2. Skilltree & Trainable Skills (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_skilltree", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openSkilltreeMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 3. Pet Backpack Virtual Storage (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_backpack", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openBackpack(p, pet);
                }, CALLBACK_OPTIONS))
                .build());

        // 4. Change Behavior Mode (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_mode", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openBehaviorModeMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 5. Feed Pet Treat (White)
        int treatsLeft = plugin.getPetManager().getDailyTreatsLeft(pet);
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_treat", player,
                        Placeholder.unparsed("daily_left", String.valueOf(treatsLeft))))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        plugin.getPetManager().feedPetTreat(p, pet);
                        openPetDetailMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // 6. Rename Pet (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_rename", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openRenameMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 7. Pet Guide (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_guide", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build());

        // 8. Release Pet (Red Danger Button)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_release", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openReleaseConfirmMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build());

        // Back button (White)
        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.detail.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPetStorageMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 4. Behavior Mode Selector Menu
    // ==========================================

    public void openBehaviorModeMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.mode.title", player);
        String modeKey = "mode." + pet.getBehaviorMode().name().toLowerCase(java.util.Locale.ROOT);
        String modeName = LanguageHook.getString(modeKey, player);
        String modeDescKey = "mode_desc." + pet.getBehaviorMode().name().toLowerCase(java.util.Locale.ROOT);
        String modeDesc = LanguageHook.getString(modeDescKey, player);
        if (modeDesc.equals(modeDescKey)) {
            modeDesc = pet.getBehaviorMode().getDefaultDescription();
        }

        Component body = LanguageHook.getComponent("dialog.mode.body", player,
                Placeholder.unparsed("current_mode", modeName),
                Placeholder.unparsed("current_mode_desc", modeDesc)
        );

        List<ActionButton> buttons = new ArrayList<>();

        // Passive
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.mode.btn_passive", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setBehaviorMode(BehaviorMode.PASSIVE);
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        LanguageHook.sendMessage(p, "message.mode_changed", Placeholder.unparsed("mode", "Passive"));
                        openPetDetailMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Defensive
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.mode.btn_defensive", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setBehaviorMode(BehaviorMode.DEFENSIVE);
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        LanguageHook.sendMessage(p, "message.mode_changed", Placeholder.unparsed("mode", "Defensive"));
                        openPetDetailMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Aggressive
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.mode.btn_aggressive", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setBehaviorMode(BehaviorMode.AGGRESSIVE);
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        LanguageHook.sendMessage(p, "message.mode_changed", Placeholder.unparsed("mode", "Aggressive"));
                        openPetDetailMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Sentry
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.mode.btn_sentry", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setBehaviorMode(BehaviorMode.SENTRY);
                        ActivePet active = plugin.getPetManager().getActivePet(p);
                        if (active != null && active.getPetData().getPetType() == pet.getPetType()) {
                            active.setSentryLocation(active.getEntity().getLocation().clone());
                        }
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        LanguageHook.sendMessage(p, "message.mode_changed", Placeholder.unparsed("mode", "Sentry"));
                        openPetDetailMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.mode.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPetDetailMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 1))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 5. Rename Pet Menu (TextDialogInput)
    // ==========================================

    public void openRenameMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.rename.title", player);
        Component body = LanguageHook.getComponent("dialog.rename.body", player);
        Component inputLabel = LanguageHook.getComponent("dialog.rename.input_label", player);

        TextDialogInput nameInput = DialogInput.text("pet_name", inputLabel)
                .initial(pet.getEffectiveName())
                .maxLength(24)
                .build();

        // Confirm Button (Green)
        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("dialog.rename.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && view != null) {
                        String newName = view.getText("pet_name");
                        if (newName == null || newName.trim().isEmpty() || newName.length() > 24) {
                            LanguageHook.sendMessage(p, "message.invalid_name");
                            return;
                        }
                        pet.setCustomName(newName.trim());
                        plugin.getPetRepository().savePet(pet);

                        ActivePet active = plugin.getPetManager().getActivePet(p);
                        if (active != null && active.getPetData().getPetType() == pet.getPetType()) {
                            plugin.getPetManager().updateNametag(active.getEntity(), pet, p);
                        }

                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1.0f, 1.2f);
                        LanguageHook.sendMessage(p, "message.rename_success", Placeholder.unparsed("name", newName.trim()));
                        openPetDetailMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        // Cancel Button (Red)
        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("dialog.rename.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPetDetailMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(nameInput))
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 6. Release Pet Confirmation Menu
    // ==========================================

    public void openReleaseConfirmMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
        }

        Component title = LanguageHook.getComponent("dialog.release.title", player);
        Component body = LanguageHook.getComponent("dialog.release.body", player,
                Placeholder.unparsed("name", pet.getEffectiveName())
        );

        // Confirm Release Button (Red)
        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("dialog.release.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        plugin.getPetManager().removePetFromPlayer(p.getUniqueId(), pet.getPetType());
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.9f);
                        LanguageHook.sendMessage(p, "message.release_success", Placeholder.unparsed("name", pet.getEffectiveName()));
                        openPetStorageMenu(p, false);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        // Cancel Button (White)
        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("dialog.release.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPetDetailMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 7. Colosseum Main Menu
    // ==========================================

    public void openColosseumMenu(@NotNull Player player, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.colosseum.title", player);

        DuelStats stats = plugin.getPetManager().getDuelStats(player.getUniqueId());
        ActivePet activePet = plugin.getPetManager().getActivePet(player);
        String stanceName = activePet != null ? activePet.getPetData().getStance().name() : "BALANCED";

        Component body = LanguageHook.getComponent("dialog.colosseum.body", player,
                Placeholder.unparsed("wins", String.valueOf(stats.getWins())),
                Placeholder.unparsed("losses", String.valueOf(stats.getLosses())),
                Placeholder.unparsed("draws", String.valueOf(stats.getDraws())),
                Placeholder.unparsed("streak", String.valueOf(stats.getWinStreak())),
                Placeholder.unparsed("rating", String.valueOf(stats.getRating())),
                Placeholder.unparsed("stance", stanceName)
        );

        List<ActionButton> buttons = new ArrayList<>();

        // Change Tactical Stance (White)
        if (activePet != null) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.colosseum.btn_stance", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openStanceSelectMenu(p, activePet.getPetData(), false);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Challenge Online Player (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.colosseum.btn_challenge", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openChallengeOpponentMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build());

        // Back Button (White)
        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.colosseum.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 8. Stance Selection Menu
    // ==========================================

    public void openStanceSelectMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.stance.title", player);
        Component body = LanguageHook.getComponent("dialog.stance.body", player);

        List<ActionButton> buttons = new ArrayList<>();

        // Aggressive (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.stance.btn_aggressive", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setStance(ColosseumStance.AGGRESSIVE);
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        openColosseumMenu(p, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Defensive (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.stance.btn_defensive", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setStance(ColosseumStance.DEFENSIVE);
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        openColosseumMenu(p, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Balanced (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.stance.btn_balanced", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.setStance(ColosseumStance.BALANCED);
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
                        openColosseumMenu(p, false);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.stance.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openColosseumMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 1))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 9. Challenge Opponent Menu
    // ==========================================

    public void openChallengeOpponentMenu(@NotNull Player player, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.challenge.title", player);
        Component body = LanguageHook.getComponent("dialog.challenge.body", player);

        List<ActionButton> buttons = new ArrayList<>();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            ActivePet pet = plugin.getPetManager().getActivePet(online);
            if (pet == null) continue;

            Component btnLabel = LanguageHook.getComponent("dialog.challenge.btn_player", player,
                    Placeholder.unparsed("player", online.getName()),
                    Placeholder.unparsed("pet_name", pet.getPetData().getEffectiveName()),
                    Placeholder.unparsed("pet_level", String.valueOf(pet.getPetData().getLevel()))
            );

            buttons.add(ActionButton.builder(btnLabel)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            p.closeDialog();
                            plugin.getColosseumEngine().sendChallenge(p, online);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.challenge.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openColosseumMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        DialogType dialogType = buttons.isEmpty() ?
                DialogType.notice(backBtn) :
                DialogType.multiAction(buttons, backBtn, 2);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(dialogType)
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 8. Pet Guide & Tips Dialogs
    // ==========================================

    public void openGuideMenu(@NotNull Player player) {
        openGuideMenu(player, true);
    }

    public void openGuideMenu(@NotNull Player player, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.guide.title", player);
        Component body = LanguageHook.getComponent("dialog.guide.body", player);

        List<ActionButton> buttons = new ArrayList<>();

        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_basics", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideBasics(p);
                }, CALLBACK_OPTIONS))
                .build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_leveling", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideLeveling(p);
                }, CALLBACK_OPTIONS))
                .build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_skills", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideSkills(p);
                }, CALLBACK_OPTIONS))
                .build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_modes", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideModes(p);
                }, CALLBACK_OPTIONS))
                .build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_colosseum", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideColosseum(p);
                }, CALLBACK_OPTIONS))
                .build());

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openGuideBasics(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        Component title = LanguageHook.getComponent("dialog.guide.basics_title", player);
        Component body = LanguageHook.getComponent("dialog.guide.basics_body", player);

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.notice(backBtn))
        );

        player.showDialog(dialog);
    }

    public void openGuideLeveling(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        Component title = LanguageHook.getComponent("dialog.guide.leveling_title", player);
        Component body = LanguageHook.getComponent("dialog.guide.leveling_body", player);

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.notice(backBtn))
        );

        player.showDialog(dialog);
    }

    public void openGuideModes(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        Component title = LanguageHook.getComponent("dialog.guide.modes_title", player);
        Component body = LanguageHook.getComponent("dialog.guide.modes_body", player);

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.notice(backBtn))
        );

        player.showDialog(dialog);
    }

    public void openGuideColosseum(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        Component title = LanguageHook.getComponent("dialog.guide.colosseum_title", player);
        Component body = LanguageHook.getComponent("dialog.guide.colosseum_body", player);

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.notice(backBtn))
        );

        player.showDialog(dialog);
    }

    public void openGuideSkills(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        Component title = LanguageHook.getComponent("dialog.guide.skills_title", player);
        Component body = LanguageHook.getComponent("dialog.guide.skills_body", player);

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.guide.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openGuideMenu(p, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.notice(backBtn))
        );

        player.showDialog(dialog);
    }

    // ==========================================
    // 9. Backpack Virtual Storage
    // ==========================================

    public void openBackpack(@NotNull Player player, @NotNull PlayerPetData pet) {
        int backpackTier = pet.getSkillLevel(PetSkill.BACKPACK);
        if (backpackTier <= 0) {
            LanguageHook.sendMessage(player, "message.backpack_locked");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        player.closeDialog();
        player.openInventory(pet.getBackpackInventory());
        player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 0.8f, 1.2f);
    }

    // ==========================================
    // 10. Skilltree & Trainable Skills
    // ==========================================

    public void openSkilltreeMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.skilltree.title", player,
                Placeholder.unparsed("name", pet.getEffectiveName())
        );

        SkilltreeType st = pet.getSkilltreeType();
        Component body = LanguageHook.getComponent("dialog.skilltree.body", player,
                Placeholder.unparsed("tree", st.getDisplayName()),
                Placeholder.unparsed("tree_desc", st.getDescription()),
                Placeholder.unparsed("points", String.valueOf(pet.getSkillPoints()))
        );

        List<ActionButton> buttons = new ArrayList<>();

        for (PetSkill skill : st.getSkills()) {
            int tier = pet.getSkillLevel(skill);
            Component label = LanguageHook.getComponent("dialog.skilltree.btn_skill", player,
                    Placeholder.unparsed("skill", skill.getDisplayName()),
                    Placeholder.unparsed("tier", String.valueOf(tier)),
                    Placeholder.unparsed("max", String.valueOf(skill.getMaxTier()))
            );

            buttons.add(ActionButton.builder(label)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openSkillDetailMenu(p, pet, skill, false);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Switch Archetype Button (White)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.skilltree.btn_switch", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openSkilltreeSwitchMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build());

        // Reset Points Button (Red)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.skilltree.btn_reset", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openResetPointsConfirm(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build());

        // Back Button (White)
        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.skilltree.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPetDetailMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openSkillDetailMenu(@NotNull Player player, @NotNull PlayerPetData pet, @NotNull PetSkill skill, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.skill_detail.title", player,
                Placeholder.unparsed("skill", skill.getDisplayName())
        );

        int tier = pet.getSkillLevel(skill);
        String currentEffect = skill.getTierDescription(tier);
        String nextEffect = tier < skill.getMaxTier() ? skill.getTierDescription(tier + 1) : LanguageHook.getString("dialog.skill_detail.maxed", player);

        Component body = LanguageHook.getComponent("dialog.skill_detail.body", player,
                Placeholder.unparsed("skill", skill.getDisplayName()),
                Placeholder.unparsed("category", skill.getCategory().name()),
                Placeholder.unparsed("desc", skill.getDefaultDescription()),
                Placeholder.unparsed("tier", String.valueOf(tier)),
                Placeholder.unparsed("max", String.valueOf(skill.getMaxTier())),
                Placeholder.unparsed("current_effect", currentEffect),
                Placeholder.unparsed("next_effect", nextEffect),
                Placeholder.unparsed("points", String.valueOf(pet.getSkillPoints()))
        );

        List<ActionButton> buttons = new ArrayList<>();

        if (tier < skill.getMaxTier()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.skill_detail.btn_upgrade", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            if (pet.getSkillPoints() > 0) {
                                pet.upgradeSkill(skill);
                                plugin.getPetRepository().savePet(pet);
                                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
                                openSkillDetailMenu(p, pet, skill, false);
                            } else {
                                LanguageHook.sendMessage(p, "message.no_skill_points");
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            }
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.skill_detail.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openSkilltreeMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        DialogType dialogType = buttons.isEmpty() ?
                DialogType.notice(backBtn) :
                DialogType.multiAction(buttons, backBtn, 1);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(dialogType)
        );

        player.showDialog(dialog);
    }

    public void openSkilltreeSwitchMenu(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }

        Component title = LanguageHook.getComponent("dialog.switch_tree.title", player);
        Component body = LanguageHook.getComponent("dialog.switch_tree.body", player,
                Placeholder.unparsed("current_tree", pet.getSkilltreeType().getDisplayName())
        );

        List<ActionButton> buttons = new ArrayList<>();

        for (SkilltreeType st : SkilltreeType.values()) {
            if (st == pet.getSkilltreeType()) continue;

            Component label = LanguageHook.getComponent("dialog.switch_tree.btn_tree", player,
                    Placeholder.unparsed("tree", st.getDisplayName())
            );

            buttons.add(ActionButton.builder(label)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            pet.resetSkillPoints();
                            pet.setSkilltreeType(st);
                            plugin.getPetRepository().savePet(pet);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 0.8f, 1.2f);
                            LanguageHook.sendMessage(p, "message.skilltree_switched", Placeholder.unparsed("tree", st.getDisplayName()));
                            openSkilltreeMenu(p, pet, false);
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.switch_tree.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openSkilltreeMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 1))
        );

        player.showDialog(dialog);
    }

    public void openResetPointsConfirm(@NotNull Player player, @NotNull PlayerPetData pet, boolean playSound) {
        if (playSound) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
        }

        Component title = LanguageHook.getComponent("dialog.reset_points.title", player);
        Component body = LanguageHook.getComponent("dialog.reset_points.body", player);

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("dialog.reset_points.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        pet.resetSkillPoints();
                        plugin.getPetRepository().savePet(pet);
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6f, 1.3f);
                        LanguageHook.sendMessage(p, "message.points_reset");
                        openSkilltreeMenu(p, pet, false);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("dialog.reset_points.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openSkilltreeMenu(p, pet, false);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }
}
