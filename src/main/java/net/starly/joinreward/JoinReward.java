package net.starly.joinreward;

import lombok.Getter;
import net.starly.joinreward.command.JoinRewardExecutor;
import net.starly.joinreward.database.DatabaseManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinReward extends JavaPlugin {

    @Getter private static JoinReward instance;
    @Getter private DatabaseManager databaseManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // CONFIG
        saveDefaultConfig();
        this.databaseManager = new DatabaseManager(this);

        // COMMAND
        JoinRewardExecutor joinRewardCommand = new JoinRewardExecutor();
        PluginCommand joinReward = getServer().getPluginCommand("접속보상");

        if (joinReward != null) {
            joinReward.setExecutor(joinRewardCommand);
            joinReward.setTabCompleter(joinRewardCommand);
        }

        // LISTENER
        registerListeners(

        );
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private boolean isPluginEnable(String pluginName) {
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
}
