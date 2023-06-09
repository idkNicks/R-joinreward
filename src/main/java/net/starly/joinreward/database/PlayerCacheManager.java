package net.starly.joinreward.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCacheManager {

    private static Map<UUID, Integer> playtimeCache = new HashMap<>();
    private static Map<UUID, Map<String, Boolean>> rewardReceivedCache = new HashMap<>();

    private PlayerCacheManager() {}

    public static void setPlaytime(UUID player, int time) {
        playtimeCache.put(player, time);
    }

    public static void setRewardReceived(UUID player, String rewardField, boolean value) {
        Map<String, Boolean> rewardReceived = rewardReceivedCache.getOrDefault(player, new HashMap<>());
        rewardReceived.put(rewardField, value);
        rewardReceivedCache.put(player, rewardReceived);
    }

    public static int getPlaytime(UUID player) {
        return playtimeCache.getOrDefault(player, 0);
    }

    public static boolean getRewardReceived(UUID player, String rewardField) {
        Map<String, Boolean> rewardReceived = rewardReceivedCache.getOrDefault(player, new HashMap<>());
        return rewardReceived.getOrDefault(rewardField, false);
    }

    public static Map<UUID, Integer> getPlaytimeCache() {
        return playtimeCache;
    }

    public static Map<UUID, Map<String, Boolean>> getRewardReceivedCache() {
        return rewardReceivedCache;
    }
}
