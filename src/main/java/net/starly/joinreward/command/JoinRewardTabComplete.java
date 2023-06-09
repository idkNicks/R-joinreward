package net.starly.joinreward.command;

import net.starly.joinreward.database.RewardType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JoinRewardTabComplete implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> tabList = new ArrayList<>();
            if (sender.hasPermission("starly.joinreward.reload")) tabList.add("리로드");
            if (sender.hasPermission("starly.joinreward.set")) tabList.add("설정");
            return StringUtil.copyPartialMatches(args[0], tabList, new ArrayList<>());
        }

        if (args.length == 2 && ("설정".equals(args[0]) || "set".equalsIgnoreCase(args[0]) && sender.hasPermission("starly.joinreward.set"))) {
            return StringUtil.copyPartialMatches(args[1], Arrays.stream(RewardType.values()).map(Enum::name).collect(Collectors.toList()), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
