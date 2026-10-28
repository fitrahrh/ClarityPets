package com.harukiyu.claritypets.database;

import com.harukiyu.claritypets.ClarityPets;
import com.harukiyu.claritypets.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    private final ClarityPets plugin;
    private final ConfigManager config;
    private HikariDataSource dataSource;

    public DatabaseManager(@NotNull ClarityPets plugin, @NotNull ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                    config.getDbHost(),
                    config.getDbPort(),
                    config.getDbName(),
                    config.isDbUseSsl());

            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(config.getDbUser());
            hikariConfig.setPassword(config.getDbPass());
            hikariConfig.setMaximumPoolSize(config.getDbMaxPoolSize());
            hikariConfig.setMinimumIdle(config.getDbMinIdle());
            hikariConfig.setConnectionTimeout(config.getDbConnTimeout());
            hikariConfig.setPoolName("ClarityPets-Pool");

            // Recommended MySQL performance properties
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
            hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

            this.dataSource = new HikariDataSource(hikariConfig);
            createTables();
            plugin.getLogger().info("Successfully connected to MySQL database via HikariCP.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize MySQL database connection pool!", e);
        }
    }

    private void createTables() {
        String createPetsTable = """
                CREATE TABLE IF NOT EXISTS cp_player_pets (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL,
                    pet_type VARCHAR(64) NOT NULL,
                    custom_name VARCHAR(64),
                    level INT DEFAULT 1,
                    current_xp DOUBLE DEFAULT 0.0,
                    behavior_mode VARCHAR(32) DEFAULT 'PASSIVE',
                    active BOOLEAN DEFAULT FALSE,
                    stance VARCHAR(32) DEFAULT 'BALANCED',
                    daily_treats_used INT DEFAULT 0,
                    last_treat_date VARCHAR(16) DEFAULT '',
                    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    skilltree_id VARCHAR(32) DEFAULT 'juggernaut',
                    skill_points INT DEFAULT 0,
                    skills_data TEXT,
                    backpack_data LONGTEXT,
                    UNIQUE KEY uk_player_pet (player_uuid, pet_type),
                    INDEX idx_pet_player (player_uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        String createDuelStatsTable = """
                CREATE TABLE IF NOT EXISTS cp_duel_stats (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL UNIQUE,
                    wins INT DEFAULT 0,
                    losses INT DEFAULT 0,
                    draws INT DEFAULT 0,
                    win_streak INT DEFAULT 0,
                    best_streak INT DEFAULT 0,
                    rating INT DEFAULT 1000,
                    INDEX idx_duel_rating (rating)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPetsTable);
            stmt.execute(createDuelStatsTable);

            // Migration: Add new columns if missing
            addColumnIfNotExists(conn, "cp_player_pets", "skilltree_id", "VARCHAR(32) DEFAULT 'juggernaut'");
            addColumnIfNotExists(conn, "cp_player_pets", "skill_points", "INT DEFAULT 0");
            addColumnIfNotExists(conn, "cp_player_pets", "skills_data", "TEXT");
            addColumnIfNotExists(conn, "cp_player_pets", "backpack_data", "LONGTEXT");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables!", e);
        }
    }

    private void addColumnIfNotExists(Connection conn, String table, String column, String definition) {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                }
            }
        } catch (SQLException ignored) {}
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("HikariDataSource is not initialized or has been closed!");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed successfully.");
        }
    }
}
