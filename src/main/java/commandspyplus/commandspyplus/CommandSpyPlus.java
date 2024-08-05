package commandspyplus.commandspyplus;

import commandspyplus.commandspyplus.commands.AdminCmd;
import commandspyplus.commandspyplus.commands.MainCommands;
import commandspyplus.commandspyplus.data.LogData;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.listeners.CommandSpyListener;
import commandspyplus.commandspyplus.listeners.JoinListener;
import commandspyplus.commandspyplus.manager.HideManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CommandSpyPlus extends JavaPlugin {


    private HideManager hideManager;
    @Override
    public void onEnable() {
        // Plugin startup logic

        File playerDataFile = new File(getDataFolder(), "playerData.yml");

        if (playerDataFile.exists()) {
            playerDataFile.delete();
        }

        PlayerData.initDataFolder(this);
        LogData.initLogsFolder(this);

        // Configs
        saveDefaultConfig();

        // Commands
        MainCommands mainCommands = new MainCommands(this);
        PluginCommand mainCmd = getCommand("commandspyplus");
        if (mainCmd != null) mainCmd.setExecutor(mainCommands);

        AdminCmd adminCommand = new AdminCmd(this);
        PluginCommand adminCmd = getCommand("commandspyadmin");
        if (adminCmd != null) adminCmd.setExecutor(adminCommand);

        // Listeners
        CommandSpyListener commandSpyListener = new CommandSpyListener(this);
        getServer().getPluginManager().registerEvents(commandSpyListener, this);

        new JoinListener(this);

        // Managers
        hideManager = new HideManager(this);
        // Data
        new PlayerData();
    }

    public HideManager getHideManager() {
        return hideManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
