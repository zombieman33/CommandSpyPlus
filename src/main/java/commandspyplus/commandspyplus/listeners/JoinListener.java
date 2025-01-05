package commandspyplus.commandspyplus.listeners;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;

public class JoinListener implements Listener {

    private final CommandSpyPlus plugin;

    public JoinListener(CommandSpyPlus plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (!plugin.shouldUseDatabase()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {
                Player player = event.getPlayer();
                UUID uuid = player.getUniqueId();

                if (!plugin.getPlayerCache().fetchEnabled(uuid)) return;

                plugin.getPlayerCache().fetchBoth(uuid);

                if (plugin.getPlayerDatabase().getPlayerData(uuid).getUsername().equalsIgnoreCase(player.getName())) return;

                String placeholder = plugin.getConfig().getString("placeholder");

                if (placeholder == null) {
                    updateUsername(uuid, player);
                    return;
                }

                if (PlaceholderAPI.setPlaceholders(player, placeholder).equalsIgnoreCase("1")) return;

                updateUsername(uuid, player);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    private void updateUsername(UUID uuid, Player player) throws SQLException {
        plugin.getPlayerDatabase().createIfNotExists(uuid, player.getName());
        plugin.getPlayerDatabase().updateUsername(uuid, player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPlayerCache().forget(event.getPlayer().getUniqueId());
        });
    }

}
