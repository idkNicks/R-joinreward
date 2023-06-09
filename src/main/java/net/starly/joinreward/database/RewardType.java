package net.starly.joinreward.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.starly.joinreward.context.MessageContent;
import net.starly.joinreward.context.MessageType;

@AllArgsConstructor
@Getter
public enum RewardType {

    REWARD_30M(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_30M").orElse("30분"), 10),
    REWARD_1H(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_1H").orElse("1시간"), 11),
    REWARD_2H(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_2H").orElse("2시간"), 12),
    REWARD_3H(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_3H").orElse("3시간"), 13),
    REWARD_6H(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_6H").orElse("6시간"), 14),
    REWARD_12H(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_12H").orElse("12시간"), 15),
    REWARD_15H(MessageContent.getInstance().getMessage(MessageType.REWARD, "time_name.REWARD_15H").orElse("15시간"), 16);

    private final String name;
    private final int slot;

    public static RewardType fromSlot(int slot) {
        for (RewardType rewardType : values()) {
            if (rewardType.getSlot() == slot) return rewardType;
        }
        return null;
    }
}
