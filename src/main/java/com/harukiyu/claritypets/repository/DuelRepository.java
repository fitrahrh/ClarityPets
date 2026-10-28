package com.harukiyu.claritypets.repository;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.database.DatabaseManager;
import com.harukiyu.claritypets.model.DuelStats;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DuelRepository {

    private final ClarityPets plugin;
    private final DatabaseManager databaseManager;

    public DuelRepository(@NotNull ClarityPets plugin, @NotNull DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<DuelStats> loadDuelStats(@NotNull UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM cp_duel_stats WHERE player_uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new DuelStats(
                                playerUuid,
                                rs.getInt("wins"),
                                rs.getInt("losses"),
                                rs.getInt("draws"),
                                rs.getInt("win_streak"),
                                rs.getInt("best_streak"),
                                rs.getInt("rating")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading duel stats for: " + playerUuid, e);
            }
            return DuelStats.createDefault(playerUuid);
        });
    }

    public CompletableFuture<Void> saveDuelStats(@NotNull DuelStats stats) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                    INSERT INTO cp_duel_stats (player_uuid, wins, losses, draws, win_streak, best_streak, rating)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        wins = VALUES(wins),
                        losses = VALUES(losses),
                        draws = VALUES(draws),
                        win_streak = VALUES(win_streak),
                        best_streak = VALUES(best_streak),
                        rating = VALUES(rating);
                    """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, stats.getPlayerUuid().toString());
                ps.setInt(2, stats.getWins());
                ps.setInt(3, stats.getLosses());
                ps.setInt(4, stats.getDraws());
                ps.setInt(5, stats.getWinStreak());
                ps.setInt(6, stats.getBestStreak());
                ps.setInt(7, stats.getRating());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error saving duel stats for: " + stats.getPlayerUuid(), e);
            }
        });
    }

    public CompletableFuture<List<DuelStats>> getTopLeaderboard(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<DuelStats> list = new ArrayList<>();
            String sql = "SELECT * FROM cp_duel_stats ORDER BY rating DESC, wins DESC LIMIT ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new DuelStats(
                                UUID.fromString(rs.getString("player_uuid")),
                                rs.getInt("wins"),
                                rs.getInt("losses"),
                                rs.getInt("draws"),
                                rs.getInt("win_streak"),
                                rs.getInt("best_streak"),
                                rs.getInt("rating")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error fetching top duel leaderboard!", e);
            }
            return list;
        });
    }
}
