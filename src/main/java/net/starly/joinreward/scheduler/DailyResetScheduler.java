package net.starly.joinreward.scheduler;

import net.starly.joinreward.JoinReward;
import net.starly.joinreward.context.MessageContent;
import net.starly.joinreward.context.MessageType;
import net.starly.joinreward.database.DatabaseManager;
import net.starly.joinreward.database.PlayerCacheManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

public class DailyResetScheduler extends BukkitRunnable {

    private static final long PERIOD = 24 * 60 * 60; // 24시간
    private MessageContent content = MessageContent.getInstance();

    public DailyResetScheduler() {
        LocalTime now = LocalTime.now();
        LocalTime resetTime = LocalTime.of(content.getInt(MessageType.REWARD, "hour"), content.getInt(MessageType.REWARD, "minute"));
        long initialDelay = ChronoUnit.SECONDS.between(now, resetTime);

        if (initialDelay < 0) {
            initialDelay += 24 * 60 * 60;
        }

        this.runTaskTimer(JoinReward.getInstance(), initialDelay * 20, PERIOD * 20);
    }

    @Override
    public void run() {
        resetPlaytimeAndRewards();
        content.getMessageAfterPrefix(MessageType.NORMAL, "dailyResetMessage").ifPresent(JoinReward.getInstance().getServer()::broadcastMessage);
    }

    private void resetPlaytimeAndRewards() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String resetSql = "UPDATE playtime SET time = 0, reward_30m = 0, reward_1h = 0, " +
                    "reward_2h = 0, reward_3h = 0, reward_6h = 0, reward_12h = 0, reward_15h = 0";
            try (PreparedStatement stmt = conn.prepareStatement(resetSql)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 플레이 타임 캐시 초기화
        for (UUID playerId : PlayerCacheManager.getPlaytimeCache().keySet()) {
            PlayerCacheManager.setPlaytime(playerId, 0);
        }

        // 보상 캐시 초기화
        for (Map.Entry<UUID, Map<String, Boolean>> entry : PlayerCacheManager.getRewardReceivedCache().entrySet()) {
            Map<String, Boolean> rewards = entry.getValue();
            rewards.put("reward_30m", false);
            rewards.put("reward_1h", false);
            rewards.put("reward_2h", false);
            rewards.put("reward_3h", false);
            rewards.put("reward_6h", false);
            rewards.put("reward_12h", false);
            rewards.put("reward_15h", false);
        }
    }
}
