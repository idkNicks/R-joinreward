package net.starly.joinreward.inventory;

import net.starly.joinreward.JoinReward;
import net.starly.joinreward.database.DatabaseManager;
import net.starly.joinreward.database.RewardType;
import net.starly.joinreward.util.ItemSerializationUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RewardSettingInventory extends InventoryListenerBase {

    private static final int INVENTORY_SIZE = 36;
    private final String INVENTORY_TITLE;
    private final RewardType rewardType;

    public RewardSettingInventory(RewardType rewardType) {
        this.rewardType = rewardType;
        this.INVENTORY_TITLE = rewardType + " 보상 설정";
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        saveItemsToDatabase(inventory);
    }

    @Override
    public void openInventory(Player player) {
        Inventory inventory = JoinReward.getInstance().getServer().createInventory(player, INVENTORY_SIZE, INVENTORY_TITLE);
        loadItemsFromDatabase(inventory);
        openInventoryAndRegisterEvent(player, inventory);
    }

    private void saveItemsToDatabase(Inventory inventory) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String deleteQuery = "DELETE FROM reward_items WHERE reward_type = ? AND item_slot = ?";
            String insertOrUpdateQuery = "INSERT INTO reward_items (reward_type, item_slot, serialized_item) VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE serialized_item = ?";
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    String serializedItem = ItemSerializationUtil.serializeItemStack(item);
                    try (PreparedStatement insertOrUpdateStmt = conn.prepareStatement(insertOrUpdateQuery)) {
                        insertOrUpdateStmt.setString(1, this.rewardType.toString());
                        insertOrUpdateStmt.setInt(2, i);
                        insertOrUpdateStmt.setString(3, serializedItem);
                        insertOrUpdateStmt.setString(4, serializedItem);
                        insertOrUpdateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setString(1, this.rewardType.toString());
                        deleteStmt.setInt(2, i);
                        deleteStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadItemsFromDatabase(Inventory inventory) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String query = "SELECT item_slot, serialized_item FROM reward_items WHERE reward_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, this.rewardType.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int slot = rs.getInt("item_slot");
                        String serializedItem = rs.getString("serialized_item");
                        ItemStack item = ItemSerializationUtil.deserializeItemStack(serializedItem);
                        inventory.setItem(slot, item);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public ItemStack[] getRewardItems() throws SQLException {
        ItemStack[] rewardItems = new ItemStack[INVENTORY_SIZE];
        try (Connection conn = DatabaseManager.getConnection()) {
            String query = "SELECT item_slot, serialized_item FROM reward_items WHERE reward_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, this.rewardType.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int slot = rs.getInt("item_slot");
                        String serializedItem = rs.getString("serialized_item");
                        ItemStack item = ItemSerializationUtil.deserializeItemStack(serializedItem);
                        rewardItems[slot] = item;
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return rewardItems;
        }
    }
}