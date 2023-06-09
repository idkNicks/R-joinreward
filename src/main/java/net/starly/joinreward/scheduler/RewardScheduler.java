package net.starly.joinreward.scheduler;

import lombok.AllArgsConstructor;
import net.starly.joinreward.database.PlayerCacheManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@AllArgsConstructor
public class RewardScheduler extends BukkitRunnable {

    private final JavaPlugin plugin;

    @Override
    public void run() {
        plugin.getServer().getOnlinePlayers()
                .stream()
                .filter(player -> PlayerCacheManager.getPlaytime(player.getUniqueId()) < 15 * 60 * 60)
                .forEach(player -> {
                    UUID playerId = player.getUniqueId();
                    int playtime = PlayerCacheManager.getPlaytime(playerId);
                    PlayerCacheManager.setPlaytime(playerId, playtime + 1);
                });
    }
}
