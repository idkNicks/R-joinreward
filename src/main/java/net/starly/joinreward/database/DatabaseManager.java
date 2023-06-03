package net.starly.joinreward.database;

import com.zaxxer.hikari.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class DatabaseManager {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public DatabaseManager(JavaPlugin plugin) {
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
            String sql = "CREATE TABLE IF NOT EXISTS playtime (" +
                    "player VARCHAR(50) PRIMARY KEY," +
                    "time INT," +
                    "reward_30m TINYINT(1) DEFAULT 0," +
                    "reward_1h TINYINT(1) DEFAULT 0," +
                    "reward_2h TINYINT(1) DEFAULT 0," +
                    "reward_3h TINYINT(1) DEFAULT 0," +
                    "reward_6h TINYINT(1) DEFAULT 0," +
                    "reward_12h TINYINT(1) DEFAULT 0," +
                    "reward_15h TINYINT(1) DEFAULT 0)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void setPlaytime(String player, int time) {
        try (Connection conn = getConnection()) {
            String query = "INSERT INTO playtime (player, time) VALUES (?, ?)" +
                    "ON DUPLICATE KEY UPDATE time = VALUES(time)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, player);
                stmt.setInt(2, time);
                stmt.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void setRewardReceived(String player, String rewardField) {
        try (Connection conn = getConnection()) {
            String query = "UPDATE playtime SET " + rewardField + " = TRUE WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, player);
                stmt.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static int getPlaytime(String player) {
        try (Connection conn = getConnection()) {
            String query = "SELECT time FROM playtime WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, player);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("time");
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static boolean getRewardReceived(String player, String rewardField) {
        try (Connection conn = getConnection()) {
            String query = "SELECT " + rewardField + " FROM playtime WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, player);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean(rewardField);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
