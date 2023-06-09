package net.starly.joinreward.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DatabaseManager {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    private DatabaseManager() {}

    public static void initialize(JavaPlugin plugin) {
        FileConfiguration configYml = plugin.getConfig();

        String host = configYml.getString("database.host");
        int port = configYml.getInt("database.port");
        String database = configYml.getString("database.database");
        String username = configYml.getString("database.username");
        String password = configYml.getString("database.password");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        ds = new HikariDataSource(config);

        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            String playtimeSql = "CREATE TABLE IF NOT EXISTS joinreward_playtime (" +
                    "player VARCHAR(36) PRIMARY KEY," +
                    "time INT," +
                    "reward_30m TINYINT(1) DEFAULT 0," +
                    "reward_1h TINYINT(1) DEFAULT 0," +
                    "reward_2h TINYINT(1) DEFAULT 0," +
                    "reward_3h TINYINT(1) DEFAULT 0," +
                    "reward_6h TINYINT(1) DEFAULT 0," +
                    "reward_12h TINYINT(1) DEFAULT 0," +
                    "reward_15h TINYINT(1) DEFAULT 0)";
            stmt.executeUpdate(playtimeSql);

            String playerRewardsSql = "CREATE TABLE IF NOT EXISTS joinreward_reward_items (" +
                    "reward_type ENUM('REWARD_30M', 'REWARD_1H', 'REWARD_2H', 'REWARD_3H', 'REWARD_6H', 'REWARD_12H', 'REWARD_15H')," +
                    "item_slot INT," +
                    "serialized_item TEXT," +
                    "PRIMARY KEY (reward_type, item_slot));";
            stmt.executeUpdate(playerRewardsSql);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void loadAllPlayerDataFromDatabase() {
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM joinreward_playtime";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    loadRewardDataFromResultSet(rs, Optional.empty());
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void loadPlayerDataFromDatabase(UUID player) {
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM joinreward_playtime WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, player.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    loadRewardDataFromResultSet(rs, Optional.of(player));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void loadRewardDataFromResultSet(ResultSet rs, Optional<UUID> optPlayer) throws SQLException {
        while ((optPlayer.isPresent() && rs.next()) || rs.next()) {
            UUID player = optPlayer.orElse(UUID.fromString(rs.getString("player")));

            int time = rs.getInt("time");
            PlayerCacheManager.setPlaytime(player, time);

            Map<String, Boolean> rewardReceived = new HashMap<>();
            rewardReceived.put("reward_30m", rs.getBoolean("reward_30m"));
            rewardReceived.put("reward_1h", rs.getBoolean("reward_1h"));
            rewardReceived.put("reward_2h", rs.getBoolean("reward_2h"));
            rewardReceived.put("reward_3h", rs.getBoolean("reward_3h"));
            rewardReceived.put("reward_6h", rs.getBoolean("reward_6h"));
            rewardReceived.put("reward_12h", rs.getBoolean("reward_12h"));
            rewardReceived.put("reward_15h", rs.getBoolean("reward_15h"));
            PlayerCacheManager.getRewardReceivedCache().put(player, rewardReceived);
        }
    }

    public static void savePlayerDataToDatabase(UUID player) {
        if (PlayerCacheManager.getPlaytimeCache().containsKey(player)) {
            int time = PlayerCacheManager.getPlaytime(player);
            try (Connection conn = getConnection()) {
                String query = "INSERT INTO joinreward_playtime (player, time) VALUES (?, ?)" +
                        "ON DUPLICATE KEY UPDATE time = VALUES(time)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, player.toString());
                    stmt.setInt(2, time);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        if (PlayerCacheManager.getRewardReceivedCache().containsKey(player)) {
            Map<String, Boolean> rewardReceived = PlayerCacheManager.getRewardReceivedCache().get(player);
            for (Map.Entry<String, Boolean> entry : rewardReceived.entrySet()) {
                String rewardField = entry.getKey();
                boolean value = entry.getValue();
                try (Connection conn = getConnection()) {
                    String query = "UPDATE joinreward_playtime SET " + rewardField + " = ? WHERE player = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setBoolean(1, value);
                        stmt.setString(2, player.toString());
                        stmt.executeUpdate();
                    }
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public static void saveAllDataToDatabase() {
        PlayerCacheManager.getPlaytimeCache().keySet().forEach(player -> savePlayerDataToDatabase(player));
    }
}


