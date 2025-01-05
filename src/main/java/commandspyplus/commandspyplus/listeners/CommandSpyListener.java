package commandspyplus.commandspyplus.listeners;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.LogData;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.manager.HideManager;
import commandspyplus.commandspyplus.modes.HiddenModes;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import redis.clients.jedis.Jedis;

import java.util.Base64;
import java.util.List;

public class CommandSpyListener implements Listener {
    private final CommandSpyPlus plugin;

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand().toBuilder().hexColors().build();

    public CommandSpyListener(CommandSpyPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0];

        HideManager manager = plugin.getHideManager();
        HiddenModes mode = manager.getHiddenMode();

        LogData.addLog(plugin, event.getMessage() + " run by: " + player.getName() + ". Mode: " + mode.name());

        if (mode == HiddenModes.PERMISSION && player.hasPermission("commandspyplus.event.hide")) return;

        if (mode == HiddenModes.PERSON && manager.getPlayerHidden(player.getName())) return;


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
                .replace("%command%", command)
                .replace("%world%", player.getWorld().getName())
                .replace("%server-name%", plugin.getConfig().getString("serverName", "n/a"));

        TextComponent component = SERIALIZER.deserialize(newFormat)
                .clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize("<green>Click To Suggest: " + command)));

        if (!plugin.shouldUseDatabase()) {

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("commandspyplus.event.see")) continue;

                boolean wantsToSeeCommands = PlayerData.getPlayerDataConfig(plugin, onlinePlayer.getUniqueId()).getBoolean("commandSpyPlus.player." + onlinePlayer.getUniqueId() + ".csp");
                if (!wantsToSeeCommands) continue;

                onlinePlayer.sendMessage(component);
            }

            return;
        }

        String s = new String(Base64.getEncoder().encode(command.getBytes()));

        try (Jedis jedis = plugin.getJedisResource()) {
            jedis.publish(plugin.getChannel(), "MESSAGE:" + s + ":" + player.getName() + ":" + player.getWorld().getName() + ":" + plugin.getServerName());
        }

//        TextComponent formatted = new TextComponent(ColorUtils.color(newFormat));
//        formatted.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
//        formatted.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
//                new ComponentBuilder("Click To Suggest: " + command).color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
//
//
//        boolean shouldWorkWithHexCode = plugin.getConfig().getBoolean("shouldWorkWithHexCode");
//        if (shouldWorkWithHexCode) {
//            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
//                if (onlinePlayer.hasPermission("commandspyplus.event.see")) {
//                    boolean wantsToSeeCommands = PlayerData.getPlayerDataConfig(plugin, onlinePlayer.getUniqueId()).getBoolean("commandSpyPlus.player." + onlinePlayer.getUniqueId() + ".csp");
//                    if (wantsToSeeCommands) {
//                        onlinePlayer.sendMessage(ColorUtils.color(newFormat));
//                    }
//                }
//            }
//        } else {
//            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
//                if (onlinePlayer.hasPermission("commandspyplus.event.see")) {
//                    boolean wantsToSeeCommands = PlayerData.getPlayerDataConfig(plugin, onlinePlayer.getUniqueId()).getBoolean("commandSpyPlus.player." + onlinePlayer.getUniqueId() + ".csp");
//                    if (wantsToSeeCommands) {
//                        onlinePlayer.spigot().sendMessage(formatted);
//                    }
//                }
//            }
//        }

    }
}