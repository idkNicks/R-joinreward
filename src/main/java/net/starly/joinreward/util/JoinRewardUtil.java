package net.starly.joinreward.util;

import net.starly.joinreward.database.PlayerCacheManager;
import net.starly.joinreward.database.RewardType;

import java.util.UUID;

public class JoinRewardUtil {

    private JoinRewardUtil() {}

    public static void setPlaytime(UUID playerId, int time) {
        PlayerCacheManager.setPlaytime(playerId, time);
    }

    public static int getPlaytime(UUID playerId) {
        return PlayerCacheManager.getPlaytime(playerId);
    }

    public static void setRewardReceived(UUID playerId, RewardType rewardType, boolean value) {
        String rewardField = getRewardField(rewardType);
        PlayerCacheManager.setRewardReceived(playerId, rewardField, value);
    }

    public static boolean getRewardReceived(UUID playerId, RewardType rewardType) {
        String rewardField = getRewardField(rewardType);
        return PlayerCacheManager.getRewardReceived(playerId, rewardField);
    }

    private static String getRewardField(RewardType rewardType) {
        switch (rewardType) {
            case REWARD_30M:
                return "reward_30m";
            case REWARD_1H:
                return "reward_1h";
            case REWARD_2H:
                return "reward_2h";
            case REWARD_3H:
                return "reward_3h";
            case REWARD_6H:
                return "reward_6h";
            case REWARD_12H:
                return "reward_12h";
            case REWARD_15H:
                return "reward_15h";
            default:
                throw new IllegalArgumentException("올바르지 않은 리워드 타입입니다.");
        }
    }
}
