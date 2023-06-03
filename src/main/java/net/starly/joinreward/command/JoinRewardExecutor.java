package net.starly.joinreward.command;

import net.starly.joinreward.JoinReward;
import net.starly.joinreward.context.MessageContent;
import net.starly.joinreward.context.MessageType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JoinRewardExecutor implements TabExecutor {

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
            // TODO 접속보상 GUI 오픈
            return true;
        }

        if ("설정".equals(args[0]) || "set".equalsIgnoreCase(args[0])) {

            if (!player.hasPermission("starly.joinreward.set")) {
                content.getMessageAfterPrefix(MessageType.ERROR, "noAnOperator").ifPresent(sender::sendMessage);
                return false;
            }

            if (args.length != 1) {
                content.getMessageAfterPrefix(MessageType.ERROR, "wrongCommand").ifPresent(sender::sendMessage);
                return false;
            }

            // TODO 설정 GUI 오픈
            return false;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabList = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("starly.joinreward.reload")) tabList.add("리로드");
            if (sender.hasPermission("starly.joinreward.set")) tabList.add("설정");
            return StringUtil.copyPartialMatches(args[0], tabList, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
