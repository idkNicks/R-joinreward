package net.starly.joinreward.listener;

import net.starly.joinreward.database.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID player = event.getPlayer().getUniqueId();
        DatabaseManager.savePlayerDataToDatabase(player);
    }
}
