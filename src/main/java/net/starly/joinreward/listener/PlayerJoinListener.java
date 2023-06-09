package net.starly.joinreward.listener;

import net.starly.joinreward.JoinReward;
import net.starly.joinreward.database.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID player = event.getPlayer().getUniqueId();

        JoinReward.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(JoinReward.getInstance(), () ->
                DatabaseManager.loadPlayerDataFromDatabase(player), 20L);
    }
}
