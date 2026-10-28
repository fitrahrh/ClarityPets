package com.harukiyu.claritypets.repository;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.database.DatabaseManager;
import com.harukiyu.claritypets.model.BehaviorMode;
import com.harukiyu.claritypets.model.ColosseumStance;
import com.harukiyu.claritypets.model.PetSkill;
import com.harukiyu.claritypets.model.PetType;
import com.harukiyu.claritypets.model.PlayerPetData;
import com.harukiyu.claritypets.model.SkilltreeType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PetRepository {

    private final ClarityPets plugin;
    private final DatabaseManager databaseManager;

    public PetRepository(@NotNull ClarityPets plugin, @NotNull DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<List<PlayerPetData>> loadPlayerPets(@NotNull UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerPetData> list = new ArrayList<>();
            String sql = "SELECT * FROM cp_player_pets WHERE player_uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        PlayerPetData data = mapResultSet(rs);
                        if (data != null) {
                            list.add(data);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading pets for player: " + playerUuid, e);
            }
            return list;
        });
    }

    public CompletableFuture<Void> savePet(@NotNull PlayerPetData pet) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                    INSERT INTO cp_player_pets (player_uuid, pet_type, custom_name, level, current_xp, behavior_mode, active, stance, daily_treats_used, last_treat_date, unlocked_at, skilltree_id, skill_points, skills_data, backpack_data)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        custom_name = VALUES(custom_name),
                        level = VALUES(level),
                        current_xp = VALUES(current_xp),
                        behavior_mode = VALUES(behavior_mode),
                        active = VALUES(active),
                        stance = VALUES(stance),
                        daily_treats_used = VALUES(daily_treats_used),
                        last_treat_date = VALUES(last_treat_date),
                        skilltree_id = VALUES(skilltree_id),
                        skill_points = VALUES(skill_points),
                        skills_data = VALUES(skills_data),
                        backpack_data = VALUES(backpack_data);
                    """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pet.getPlayerUuid().toString());
                ps.setString(2, pet.getPetType().getId());
                ps.setString(3, pet.getCustomName());
                ps.setInt(4, pet.getLevel());
                ps.setDouble(5, pet.getCurrentXp());
                ps.setString(6, pet.getBehaviorMode().name());
                ps.setBoolean(7, pet.isActive());
                ps.setString(8, pet.getStance().name());
                ps.setInt(9, pet.getDailyTreatsUsed());
                ps.setString(10, pet.getLastTreatDate());
                ps.setTimestamp(11, pet.getUnlockedAt());
                ps.setString(12, pet.getSkilltreeType().getId());
                ps.setInt(13, pet.getSkillPoints());
                ps.setString(14, serializeSkills(pet.getSkillLevels()));
                ps.setString(15, pet.serializeBackpackBase64());
                ps.executeUpdate();

                if (pet.getId() == 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            pet.setId(keys.getInt(1));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error saving pet [" + pet.getPetType() + "] for: " + pet.getPlayerUuid(), e);
            }
        });
    }

    public CompletableFuture<Void> saveAllPets(@NotNull Collection<PlayerPetData> pets) {
        if (pets.isEmpty()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> {
            String sql = """
                    INSERT INTO cp_player_pets (player_uuid, pet_type, custom_name, level, current_xp, behavior_mode, active, stance, daily_treats_used, last_treat_date, unlocked_at, skilltree_id, skill_points, skills_data, backpack_data)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        custom_name = VALUES(custom_name),
                        level = VALUES(level),
                        current_xp = VALUES(current_xp),
                        behavior_mode = VALUES(behavior_mode),
                        active = VALUES(active),
                        stance = VALUES(stance),
                        daily_treats_used = VALUES(daily_treats_used),
                        last_treat_date = VALUES(last_treat_date),
                        skilltree_id = VALUES(skilltree_id),
                        skill_points = VALUES(skill_points),
                        skills_data = VALUES(skills_data),
                        backpack_data = VALUES(backpack_data);
                    """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (PlayerPetData pet : pets) {
                    ps.setString(1, pet.getPlayerUuid().toString());
                    ps.setString(2, pet.getPetType().getId());
                    ps.setString(3, pet.getCustomName());
                    ps.setInt(4, pet.getLevel());
                    ps.setDouble(5, pet.getCurrentXp());
                    ps.setString(6, pet.getBehaviorMode().name());
                    ps.setBoolean(7, pet.isActive());
                    ps.setString(8, pet.getStance().name());
                    ps.setInt(9, pet.getDailyTreatsUsed());
                    ps.setString(10, pet.getLastTreatDate());
                    ps.setTimestamp(11, pet.getUnlockedAt());
                    ps.setString(12, pet.getSkilltreeType().getId());
                    ps.setInt(13, pet.getSkillPoints());
                    ps.setString(14, serializeSkills(pet.getSkillLevels()));
                    ps.setString(15, pet.serializeBackpackBase64());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error batch saving player pets!", e);
            }
        });
    }

    public CompletableFuture<Void> deletePet(@NotNull UUID playerUuid, @NotNull PetType petType) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM cp_player_pets WHERE player_uuid = ? AND pet_type = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setString(2, petType.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error deleting pet [" + petType + "] for: " + playerUuid, e);
            }
        });
    }

    public CompletableFuture<Void> setActivePet(@NotNull UUID playerUuid, @NotNull PetType petType) {
        return CompletableFuture.runAsync(() -> {
            String deactivateSql = "UPDATE cp_player_pets SET active = FALSE WHERE player_uuid = ?";
            String activateSql = "UPDATE cp_player_pets SET active = TRUE WHERE player_uuid = ? AND pet_type = ?";
            try (Connection conn = databaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps1 = conn.prepareStatement(deactivateSql)) {
                        ps1.setString(1, playerUuid.toString());
                        ps1.executeUpdate();
                    }
                    try (PreparedStatement ps2 = conn.prepareStatement(activateSql)) {
                        ps2.setString(1, playerUuid.toString());
                        ps2.setString(2, petType.getId());
                        ps2.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error setting active pet for: " + playerUuid, e);
            }
        });
    }

    public CompletableFuture<Void> deactivateAll(@NotNull UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE cp_player_pets SET active = FALSE WHERE player_uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error deactivating pets for: " + playerUuid, e);
            }
        });
    }

    private PlayerPetData mapResultSet(ResultSet rs) throws SQLException {
        String typeId = rs.getString("pet_type");
        PetType petType = PetType.fromString(typeId);
        if (petType == null) return null;

        String modeStr = rs.getString("behavior_mode");
        BehaviorMode mode = modeStr != null ? BehaviorMode.fromString(modeStr) : BehaviorMode.PASSIVE;

        String stanceStr = rs.getString("stance");
        ColosseumStance stance = stanceStr != null ? ColosseumStance.fromString(stanceStr) : ColosseumStance.BALANCED;

        String treeStr = null;
        try {
            treeStr = rs.getString("skilltree_id");
        } catch (SQLException ignored) {}
        SkilltreeType tree = treeStr != null ? SkilltreeType.fromId(treeStr) : petType.getDefaultSkilltree();

        int points = 0;
        try {
            points = rs.getInt("skill_points");
        } catch (SQLException ignored) {}

        String skillsData = null;
        try {
            skillsData = rs.getString("skills_data");
        } catch (SQLException ignored) {}
        Map<PetSkill, Integer> skills = deserializeSkills(skillsData);

        String backpackData = null;
        try {
            backpackData = rs.getString("backpack_data");
        } catch (SQLException ignored) {}

        return new PlayerPetData(
                rs.getInt("id"),
                UUID.fromString(rs.getString("player_uuid")),
                petType,
                rs.getString("custom_name"),
                rs.getInt("level"),
                rs.getDouble("current_xp"),
                mode,
                rs.getBoolean("active"),
                stance,
                rs.getInt("daily_treats_used"),
                rs.getString("last_treat_date"),
                rs.getTimestamp("unlocked_at"),
                tree,
                points,
                skills,
                backpackData
        );
    }

    public static String serializeSkills(Map<PetSkill, Integer> skills) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<PetSkill, Integer> entry : skills.entrySet()) {
            if (entry.getValue() > 0) {
                if (!sb.isEmpty()) sb.append(",");
                sb.append(entry.getKey().name()).append(":").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    public static Map<PetSkill, Integer> deserializeSkills(String data) {
        Map<PetSkill, Integer> map = new HashMap<>();
        if (data == null || data.isBlank()) return map;
        String[] parts = data.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                try {
                    PetSkill skill = PetSkill.fromId(kv[0]);
                    if (skill != null) {
                        int level = Integer.parseInt(kv[1]);
                        map.put(skill, level);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }
}
