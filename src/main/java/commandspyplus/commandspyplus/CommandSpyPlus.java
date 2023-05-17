package commandspyplus.commandspyplus;

import commandspyplus.commandspyplus.commands.MainCommands;
import commandspyplus.commandspyplus.listeners.CommandSpyListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CommandSpyPlus extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic


        // Configs
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Config file not found, creating...");
            saveResource("config.yml", false);
        }
        File playerDataFile = new File(getDataFolder(), "playerData.yml");
        if (!playerDataFile.exists()) {
            getLogger().info("Player Data file not found, creating...");
            saveResource("playerData.yml", false);
        }

        // Commands
        MainCommands mainCommands = new MainCommands(this);
        PluginCommand mainCmd = getCommand("commandspyplus");
        if (mainCmd != null) mainCmd.setExecutor(mainCommands);

        // Listeners
        CommandSpyListener commandSpyListener = new CommandSpyListener(this);
        getServer().getPluginManager().registerEvents(commandSpyListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
