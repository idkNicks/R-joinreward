package net.starly.joinreward.inventory;

import net.starly.joinreward.JoinReward;
import net.starly.joinreward.builder.ItemBuilder;
import net.starly.joinreward.context.MessageContent;
import net.starly.joinreward.context.MessageType;
import net.starly.joinreward.database.PlayerCacheManager;
import net.starly.joinreward.database.RewardType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RewardInventory extends InventoryListenerBase {

    private static RewardInventory instance;

    private RewardInventory() {}

    public static RewardInventory getInstance() {
        if (instance == null) instance = new RewardInventory();
        return instance;
    }

    private MessageContent content = MessageContent.getInstance();

    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        event.setCancelled(true);

        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

        int clickedSlot = event.getSlot();
        RewardType rewardType = RewardType.fromSlot(clickedSlot);

        if (rewardType != null) {
            try {
                giveReward(player, rewardType);
            } catch (SQLException e) {
                content.getMessageAfterPrefix(MessageType.ERROR, "rewardSQLException").ifPresent(player::sendMessage);
                e.printStackTrace();
            }
        }
    }

    private void giveReward(Player player, RewardType rewardType) throws SQLException {
        UUID playerId = player.getUniqueId();
        Map<String, Boolean> rewardReceived = PlayerCacheManager.getRewardReceivedCache().get(playerId);

        if (rewardReceived.get(rewardType.name().toLowerCase())) {
            content.getMessageAfterPrefix(MessageType.ERROR, "alreadyRewardReceived").ifPresent(player::sendMessage);
        } else if (canReceiveReward(player, rewardType)) {
            ItemStack[] rewardItems = new RewardSettingInventory(rewardType).getRewardItems();

            if (!hasEnoughInventorySpace(player, rewardItems)) {
                content.getMessageAfterPrefix(MessageType.ERROR, "hasEnoughInventorySpace").ifPresent(player::sendMessage);
                return;
            }

            rewardReceived.put(rewardType.name().toLowerCase(), true);
            PlayerCacheManager.getRewardReceivedCache().put(playerId, rewardReceived);

            Arrays.stream(rewardItems).forEach(item -> {
                if (item != null) player.getInventory().addItem(item);
            });

            content.getMessageAfterPrefix(MessageType.NORMAL, "receiveReward").ifPresent(player::sendMessage);
        } else {
            content.getMessageAfterPrefix(MessageType.ERROR, "InsufficientPlaytimeReward").ifPresent(message -> {
                String replacedMessage = message.replace("{time}", calculateRemainingTime(rewardType, player));
                player.sendMessage(replacedMessage);
            });
        }
    }

    private boolean hasEnoughInventorySpace(Player player, ItemStack[] items) {
        Inventory tempInventory = JoinReward.getInstance().getServer().createInventory(null, 9 * 4);
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) != null) {
                tempInventory.setItem(i, player.getInventory().getItem(i).clone());
            }
        }

        for (ItemStack item : items) {
            if (item != null) {
                HashMap<Integer, ItemStack> remainingItems = tempInventory.addItem(item.clone());
                if (!remainingItems.isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean canReceiveReward(Player player, RewardType rewardType) {
        String remainingTime = calculateRemainingTime(rewardType, player);
        return remainingTime.equals("0");
    }

    public void openInventory(Player player) {
        Inventory inventory = JoinReward.getInstance().getServer().createInventory(null, 9 * 3, "접속보상");
        UUID playerId = player.getUniqueId();

        AtomicInteger slot = new AtomicInteger(10);
        Arrays.stream(RewardType.values())
                .forEach(rewardType -> {
                    String remainingTime = calculateRemainingTime(rewardType, player);
                    boolean received = PlayerCacheManager.getRewardReceived(playerId, rewardType.name());
                    ItemStack rewardItem = createItem(rewardType, remainingTime, received);
                    inventory.setItem(slot.getAndIncrement(), rewardItem);
                });

        openInventoryAndRegisterEvent(player, inventory);

        JoinReward.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(JoinReward.getInstance(),
                () -> updateInventory(player), 0L, 20L * 1);
    }

    private void updateInventory(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        UUID playerId = player.getUniqueId();

        if (inventory != null) {
            IntStream.range(0, inventory.getSize())
                    .filter(slot -> inventory.getItem(slot) != null)
                    .forEach(slot -> {
                        RewardType rewardType = RewardType.fromSlot(slot);
                        if (rewardType != null) {
                            String remainingTime = calculateRemainingTime(rewardType, player);
                            boolean received = PlayerCacheManager.getRewardReceived(playerId, rewardType.name());
                            ItemStack updatedItem = createItem(rewardType, remainingTime, received);
                            inventory.setItem(slot, updatedItem);
                        }
                    });
        }
    }

    private ItemStack createItem(RewardType rewardType, String remainingTime, boolean received) {
        String prefix;
        if (received || remainingTime.equals("-1")) {
            prefix = "gui.receivedReward";
        } else if (remainingTime.equals("0")) {
            prefix = "gui.canReward";
        } else {
            prefix = "gui.cantReward";
        }

        String materialKey = prefix + ".material";
        String displayNameKey = prefix + ".displayname";
        String loreKey = prefix + ".lore";

        List<String> lore = content.getMessages(MessageType.REWARD, loreKey);
        if (prefix.equals("gui.cantReward")) {
            lore = lore.stream().map(message -> message.replace("{time}", remainingTime)).collect(Collectors.toList());
        }

        return new ItemBuilder(Material.valueOf(content.getMessage(MessageType.REWARD, materialKey).orElse("BARRIER")))
                .setName(content.getMessage(MessageType.REWARD, displayNameKey)
                        .map(message -> message.replace("{time}", rewardType.getName()))
                        .orElse(""))
                .setLore(lore)
                .build();
    }

    private String calculateRemainingTime(RewardType rewardType, Player player) {
        UUID playerId = player.getUniqueId();
        int playerPlaytime = PlayerCacheManager.getPlaytime(playerId);
        String rewardFieldName = rewardType.name().toLowerCase();

        if (PlayerCacheManager.getRewardReceived(playerId, rewardFieldName)) return "-1";

        int requiredPlaytime = switch (rewardType) {
            case REWARD_30M -> 30 * 60;
            case REWARD_1H -> 60 * 60;
            case REWARD_2H -> 120 * 60;
            case REWARD_3H -> 180 * 60;
            case REWARD_6H -> 360 * 60;
            case REWARD_12H -> 720 * 60;
            case REWARD_15H -> 900 * 60;
        };

        int remainingTime = requiredPlaytime - playerPlaytime;

        if (remainingTime <= 0) return "0";

        int hours = remainingTime / 3600;
        int minutes = (remainingTime % 3600) / 60;
        int seconds = remainingTime % 60;

        return String.format("%d시간 %d분 %d초", hours, minutes, seconds);
    }
}
