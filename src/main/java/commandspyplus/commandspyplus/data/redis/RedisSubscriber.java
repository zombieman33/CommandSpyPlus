package commandspyplus.commandspyplus.data.redis;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

import static commandspyplus.commandspyplus.listeners.CommandSpyListener.SERIALIZER;

public class RedisSubscriber extends JedisPubSub {
    private final CommandSpyPlus plugin;

    public RedisSubscriber(CommandSpyPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!plugin.isEnabled()) {
            return;
        }

        String[] parts = message.split(":", 5);
        String action = parts[0];

        switch (action.toUpperCase()) {
            case "MESSAGE":
                if (parts.length >= 3) {
                    String command = parts[1];
                    String playerName = parts[2];
                    String world = parts[3];
                    String serverName = parts[4];
                    handleMessage(command, playerName, world, serverName);
                }
                break;
            default:
                plugin.getLogger().warning("Received unrecognized message: " + message);
                break;
        }
    }

    private void handleMessage(String command, String playerName, String world, String serverName) {
        String format = plugin.getConfig().getString("format");

        String decodedCommand = new String(Base64.getDecoder().decode(command));

        String newFormat = format
                .replace("%player%", playerName)
                .replace("%command%", decodedCommand)
                .replace("%world%", world)
                .replace("%server-name%", serverName);

        TextComponent component = SERIALIZER.deserialize(newFormat)
                .clickEvent(ClickEvent.suggestCommand(decodedCommand))
                .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize("<green>Click To Suggest: " + decodedCommand)));


        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("commandspyplus.event.see")) continue;

            boolean wantsToSeeCommands = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getBoolean("commandSpyPlus.player." + player.getUniqueId() + ".csp");

            if (!wantsToSeeCommands) continue;

            player.sendMessage(component);
        }
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        plugin.getLogger().info("Subscribed to Redis channel: " + channel);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        plugin.getLogger().info("Unsubscribed from Redis channel: " + channel);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        plugin.getLogger().info("Pattern subscribed: " + pattern);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        plugin.getLogger().info("Pattern unsubscribed: " + pattern);
    }
}
