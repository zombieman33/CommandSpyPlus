package commandspyplus.commandspyplus;

import commandspyplus.commandspyplus.commands.MainCommands;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.listeners.CommandSpyListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CommandSpyPlus extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        File playerDataFile = new File(getDataFolder(), "playerData.yml");

        if (playerDataFile.exists()) {
            playerDataFile.delete();
        }

        // Configs
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Config file not found, creating...");
            saveResource("config.yml", false);
        }

        // Commands
        MainCommands mainCommands = new MainCommands(this);
        PluginCommand mainCmd = getCommand("commandspyplus");
        if (mainCmd != null) mainCmd.setExecutor(mainCommands);

        // Listeners
        CommandSpyListener commandSpyListener = new CommandSpyListener(this);
        getServer().getPluginManager().registerEvents(commandSpyListener, this);

        // Data
        new PlayerData();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
