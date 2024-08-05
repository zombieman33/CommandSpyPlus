package commandspyplus.commandspyplus.manager;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.modes.HiddenModes;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HideManager {

    private HiddenModes mode;
    private CommandSpyPlus plugin;

    public HideManager(CommandSpyPlus plugin) {
        this.plugin = plugin;
        this.mode = getHiddenModeInConfig();
    }

    public void setHiddenMode(HiddenModes mode) {
        this.mode = mode;
    }
    public HiddenModes getHiddenMode() {
        return mode;
    }

    public boolean getPlayerHidden(String name) {
        FileConfiguration playerDataConfigByName = PlayerData.getPlayerDataConfigByName(plugin, name);
        if (playerDataConfigByName != null) {
            return playerDataConfigByName.getBoolean("hidden");
        }
        return false;
    }
    public void setPlayerHidden(String name, boolean hidden) {
        FileConfiguration playerDataConfigByName = PlayerData.getPlayerDataConfigByName(plugin, name);
        if (playerDataConfigByName != null) {
            playerDataConfigByName.set("hidden", hidden);
            PlayerData.savePlayerDataByName(plugin, name);
        }
    }

    public void setHiddenModeInConfig(HiddenModes mode) {
        setHiddenMode(mode);
        plugin.getConfig().set("mode", mode.name());
        plugin.saveConfig();
    }
    public HiddenModes getHiddenModeInConfig() {
        String mode = plugin.getConfig().getString("mode");

        List<String> modesList = new ArrayList<>();

        for (HiddenModes hiddenModes : HiddenModes.values()) {
            modesList.add(hiddenModes.name());
        }

        if (mode == null || !modesList.contains(mode)) return HiddenModes.NONE;

        return HiddenModes.valueOf(mode);
    }
}
