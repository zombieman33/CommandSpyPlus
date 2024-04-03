package commandspyplus.commandspyplus.listeners;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.utils.ColorUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        String command = event.getMessage().split(" ")[0];

        List<String> ignoredCommands = plugin.getConfig().getStringList("ignored-commands");

        for (String ignoredCommand : ignoredCommands) {
            if (command.startsWith(ignoredCommand)) {
                return;
            }
        }

        command = event.getMessage();

        String format = plugin.getConfig().getString("format");
        String newFormat = format
                .replace("%player%", player.getName())
                .replace("%command%", command);

        TextComponent formatted = new TextComponent(ColorUtils.color(newFormat));
        formatted.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        formatted.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click To Suggest: " + command).color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));

        boolean shouldWorkWithHexCode = plugin.getConfig().getBoolean("shouldWorkWithHexCode");
        if (shouldWorkWithHexCode) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("commandspyplus.see")) {
                    boolean wantsToSeeCommands = PlayerData.getPlayerDataConfig(plugin, onlinePlayer.getUniqueId()).getBoolean("commandSpyPlus.player." + onlinePlayer.getUniqueId() + ".csp");
                    if (wantsToSeeCommands) {
                        onlinePlayer.sendMessage(ColorUtils.color(newFormat));
                    }
                }
            }
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("commandspyplus.see")) {
                    boolean wantsToSeeCommands = PlayerData.getPlayerDataConfig(plugin, onlinePlayer.getUniqueId()).getBoolean("commandSpyPlus.player." + onlinePlayer.getUniqueId() + ".csp");
                    if (wantsToSeeCommands) {
                        onlinePlayer.spigot().sendMessage(formatted);
                    }
                }
            }
        }
    }
}