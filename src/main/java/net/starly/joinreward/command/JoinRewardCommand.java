package net.starly.joinreward.command;

import net.starly.joinreward.JoinReward;
import net.starly.joinreward.context.MessageContent;
import net.starly.joinreward.context.MessageType;
import net.starly.joinreward.database.PlayerCacheManager;
import net.starly.joinreward.database.RewardType;
import net.starly.joinreward.inventory.RewardInventory;
import net.starly.joinreward.inventory.RewardSettingInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JoinRewardCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        MessageContent content = MessageContent.getInstance();

        if (args.length > 0 && ("리로드".equals(args[0]) || "reload".equalsIgnoreCase(args[0]))) {

            if (!sender.hasPermission("starly.joinreward.reload")) {
                content.getMessageAfterPrefix(MessageType.ERROR, "noAnOperator").ifPresent(sender::sendMessage);
                return false;
            }

            JoinReward.getInstance().reloadConfig();
            content.initialize(JoinReward.getInstance().getConfig());
            content.getMessageAfterPrefix(MessageType.NORMAL, "reloadComplete").ifPresent(sender::sendMessage);
            return true;
        }

        if (!(sender instanceof Player player)) {
            content.getMessageAfterPrefix(MessageType.ERROR, "noConsoleCommand").ifPresent(sender::sendMessage);
            return true;
        }

        if (args.length == 0) {
            RewardInventory.getInstance().openInventory(player);
            return true;
        }

        if ("설정".equals(args[0]) || "set".equalsIgnoreCase(args[0])) {

            if (!player.hasPermission("starly.joinreward.set")) {
                content.getMessageAfterPrefix(MessageType.ERROR, "noAnOperator").ifPresent(player::sendMessage);
                return false;
            }

            if (args.length != 2) {
                content.getMessageAfterPrefix(MessageType.ERROR, "wrongCommand").ifPresent(player::sendMessage);
                return false;
            }

            try {
                RewardType rewardType = RewardType.valueOf(args[1].toUpperCase());
                RewardSettingInventory rewardSettingInventory = new RewardSettingInventory(rewardType);
                rewardSettingInventory.openInventory(player);
            } catch (IllegalArgumentException e) {
                content.getMessageAfterPrefix(MessageType.ERROR, "wrongRewardType").ifPresent(player::sendMessage);
            }
            return false;
        }
        return false;
    }
}
