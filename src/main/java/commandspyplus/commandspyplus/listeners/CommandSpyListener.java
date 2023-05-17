package commandspyplus.commandspyplus.listeners;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class CommandSpyListener implements Listener {
    private final CommandSpyPlus plugin;

    public CommandSpyListener(CommandSpyPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        List<String> ignoredCommands = plugin.getConfig().getStringList("ignored-commands");

        if (!ignoredCommands.contains(command)) {
            String format = plugin.getConfig().getString("format");
            String newFormat = format.replace("%player%", player.getName()).replace("%command%", command);

            File playerDataFile = new File(plugin.getDataFolder(), "playerData.yml");
            FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("commandspyplus.see")) {
                    boolean wantsToSeeCommands = playerDataConfig.getBoolean("commandSpyPlus.player." + onlinePlayer.getUniqueId() + ".csp");
                    if (wantsToSeeCommands) {
                        onlinePlayer.sendMessage(ColorUtils.color(newFormat));
                    }
                }
            }
        }
    }
}