package net.starly.joinreward;

import lombok.Getter;
import net.starly.joinreward.command.JoinRewardCommand;
import net.starly.joinreward.command.JoinRewardTabComplete;
import net.starly.joinreward.context.MessageContent;
import net.starly.joinreward.database.DatabaseManager;
import net.starly.joinreward.listener.PlayerJoinListener;
import net.starly.joinreward.listener.PlayerQuitListener;
import net.starly.joinreward.scheduler.DailyResetScheduler;
import net.starly.joinreward.scheduler.RewardScheduler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinReward extends JavaPlugin {

    @Getter private static JoinReward instance;
    private RewardScheduler rewardScheduler;
    private DailyResetScheduler resetScheduler;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // CONFIG
        saveDefaultConfig();
        MessageContent.getInstance().initialize(getConfig());

        // DATABASE
        DatabaseManager.initialize(this);
        DatabaseManager.loadAllPlayerDataFromDatabase();

        // SCHEDULER
        this.rewardScheduler = new RewardScheduler(this);
        this.rewardScheduler.runTaskTimer(this, 0, 20);
        this.resetScheduler = new DailyResetScheduler();

        // COMMAND
        getCommand("접속보상").setExecutor(new JoinRewardCommand());
        getCommand("접속보상").setTabCompleter(new JoinRewardTabComplete());

        // LISTENER
        registerListeners(
                new PlayerQuitListener(),
                new PlayerJoinListener()
        );
    }

    @Override
    public void onDisable() {
        if (rewardScheduler != null) rewardScheduler.cancel();
        if (resetScheduler != null) resetScheduler.cancel();

        DatabaseManager.saveAllDataToDatabase();
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
