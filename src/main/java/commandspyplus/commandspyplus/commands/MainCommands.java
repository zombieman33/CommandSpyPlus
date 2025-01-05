package commandspyplus.commandspyplus.commands;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.data.mysql.data.ServerData;
import commandspyplus.commandspyplus.utils.ColorUtils;
import commandspyplus.commandspyplus.utils.ServerNameUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MainCommands implements CommandExecutor, TabCompleter {
    private final CommandSpyPlus plugin;

    public MainCommands(CommandSpyPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;
        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId());

        if (!player.hasPermission("commandspyplus.command.main")) {
            String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
            player.sendMessage(ColorUtils.color(permissionMessage));
            return false;
        }

        String pName = player.getName();
        UUID pUUID = player.getUniqueId();
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(ColorUtils.color("&#33FB13&lCommand Spy Plus"));
                player.sendMessage(ColorUtils.color("&#33FB13/csp enable <server>"));
                player.sendMessage(ColorUtils.color("&#33FB13/csp disable <server>"));
                player.sendMessage(ColorUtils.color("&#33FB13/csp reload"));
                player.sendMessage(ColorUtils.color("&#33FB13/csp reset <config.yml> ( resets the configs to the default version )"));
                player.sendMessage(ColorUtils.color("&#33FB13/csp add-command <command> ( adds the command to the ignored command list"));
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                long startTime = System.currentTimeMillis();
                try {
                    plugin.reloadConfig();
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;

                    String configMessage = plugin.getConfig().getString("reload-message").replace("*", "'").replace("%player%", player.getName());
                    if (configMessage != null) {
                        String timeMessage = configMessage.replace("%time%", Long.toString(elapsedTime));
                        player.sendMessage(ColorUtils.color(timeMessage));
                        return true;
                    }

                    player.sendMessage(ChatColor.GREEN + "Config reloaded successfully!");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "An error occurred while reloading the plugin: " + e.getMessage());
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (args[1].equalsIgnoreCase("config.yml")) {
                    long startTime = System.currentTimeMillis();
                    plugin.saveResource("config.yml", true);
                    plugin.reloadConfig();
                    long endTime = System.currentTimeMillis();
                    long time = endTime - startTime + 1;
                    player.sendMessage(ChatColor.GREEN + "You successfully reset " + args[1] + ChatColor.AQUA + " (" + time + "ms)");
                    return true;
                }

                player.sendMessage(ChatColor.YELLOW + "/csp reset <config.yml>");

            } else if (args[0].equalsIgnoreCase("enable")) {

                if (args.length > 1) {
                    setPlayerServerSetting(player, true, args[1]);
                    return true;
                }

                csp(player, true);

            } else if (args[0].equalsIgnoreCase("disable")) {


                if (args.length > 1) {
                    setPlayerServerSetting(player, false, args[1]);
                    return true;
                }

                csp(player, false);

            } else if (args[0].equalsIgnoreCase("add-command")) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You need to specify a command.");
                    return true;
                }
                String commandToAdd = args[1];
                String removeSlash = commandToAdd.replace("/", "");
                String formatCommand = removeSlash.replace(removeSlash, "/" + removeSlash);
                List<String> ignoredCommands = plugin.getConfig().getStringList("ignored-commands");

                if (ignoredCommands.contains(formatCommand)) {
                    player.sendMessage(ChatColor.RED + "The command '" + formatCommand + "' is already a ignored command.");
                    return false;
                }

                ignoredCommands.add(formatCommand);
                plugin.getConfig().set("ignored-commands", ignoredCommands);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "You added: " + formatCommand + " to the list of ignored commands");
            } else if (args[0].equalsIgnoreCase("remove-command")) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "You need to specify a command.");
                    return true;
                }
                String commandToAdd = args[1];
                String removeSlash = commandToAdd.replace("/", "");
                String formatCommand = removeSlash.replace(removeSlash, "/" + removeSlash);
                List<String> ignoredCommands = plugin.getConfig().getStringList("ignored-commands");

                if (!ignoredCommands.contains(formatCommand)) {
                    player.sendMessage(ChatColor.RED + "The command '" + formatCommand + "' is not in the ignored command list");
                    return false;
                }

                ignoredCommands.remove(formatCommand);
                plugin.getConfig().set("ignored-commands", ignoredCommands);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "You removed: " + formatCommand + " from the list of ignored commands");
            } else {
                player.sendMessage(ChatColor.YELLOW + "/csp help");
            }
        } else {
            csp(player, null);
        }
        return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        Player player = (Player) sender;

        if (args.length == 1) {
            if (player.hasPermission("commandspyplus.command.main")) {
                completions.add("help");
                completions.add("add-command");
                completions.add("remove-command");
                completions.add("reload");
                completions.add("reset");
                completions.add("enable");
                completions.add("disable");
            }
        } else if (args.length == 2) {
            if (player.hasPermission("commandspyplus.command.main")) {
                if (args[0].equalsIgnoreCase("reset")) {
                    completions.add("config.yml");
                } else if (args[0].equalsIgnoreCase("add-command")) {
                    completions.add("<command>");
                } else if (args[0].equalsIgnoreCase("remove-command")) {
                    List<String> ignoredCommands = plugin.getConfig().getStringList("ignored-commands");
                    if (ignoredCommands.isEmpty()) {
                        completions.add("there aren't any ignored commands. '/csp add-command' to add one!");
                    } else {
                        completions.addAll(ignoredCommands);
                    }
                } else if (args[0].equalsIgnoreCase("disable")) {
                    Collection<ServerData> servers = plugin.getServerListCache().getServers();
                    completions.addAll(servers.stream().map(ServerData::getServerID).collect(Collectors.toList()));

                } else if (args[0].equalsIgnoreCase("enable")) {
                    String servers = plugin.getPlayerCache().getServers(player.getUniqueId());
                    if (servers == null) return completions;

                    completions.addAll(ServerNameUtil.fromString(servers));
                }
            }
        }
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.toLowerCase().startsWith(lastArg.toLowerCase())).collect(Collectors.toList());
    }


    private void setPlayerServerSetting(Player player, boolean enable, String serverName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {

                Collection<ServerData> servers = plugin.getServerListCache().getServers();
                boolean anyMatch = servers.stream().anyMatch(serverData -> serverData.getServerID().equalsIgnoreCase(serverName));

                if (!anyMatch) {
                    player.sendMessage(ChatColor.RED + "This is not a valid server!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                UUID uuid = player.getUniqueId();
                String playerServer = plugin.getPlayerCache().getServers(uuid);

                List<String> serverList = new ArrayList<>();

                if (playerServer != null) {
                    serverList.addAll(ServerNameUtil.fromString(playerServer.toLowerCase()));
                }

                String playerServers = ServerNameUtil.toString(serverList);
                if (enable) {
                    if (!serverList.contains(serverName.toLowerCase())) {
                        player.sendMessage(ChatColor.RED + "You this server is already enabled.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    serverList.remove(serverName.toLowerCase());

                    player.sendMessage(ChatColor.GREEN + "You can now see commands from the '" + serverName +"' server.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f,1.0f);
                } else {

                    if (serverList.contains(serverName.toLowerCase())) {
                        player.sendMessage(ChatColor.RED + "You this server is already disabled.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    serverList.add(serverName.toLowerCase());

                    player.sendMessage(ChatColor.GREEN + "You can no longer see commands from the '" + serverName + "' server.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                }

                String serverString = ServerNameUtil.toString(serverList);

                plugin.getPlayerDatabase().createIfNotExists(uuid, player.getName());
                plugin.getPlayerDatabase().updateServers(uuid, serverString);
                plugin.getPlayerCache().setServers(uuid, serverString);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    private void csp(Player player, @Nullable Boolean csp) {

        String disabled = ColorUtils.color(plugin.getConfig().getString("disabled").replace("*", "'").replace("%player%", player.getName()));
        String enabledString = ColorUtils.color(plugin.getConfig().getString("enabled").replace("*", "'").replace("%player%", player.getName()));

        UUID uuid = player.getUniqueId();
        if (plugin.shouldUseDatabase()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    boolean enabled = plugin.getPlayerCache().getEnabled(uuid);

                    boolean enableOrDisable = false;

                    if (csp == null) {
                        if (enabled) {
                            player.sendMessage(disabled);
                        } else {
                            enableOrDisable = true;
                            player.sendMessage(enabledString);
                        }

                        plugin.getPlayerDatabase().createIfNotExists(uuid, player.getName());
                        plugin.getPlayerDatabase().updateEnabled(uuid, enableOrDisable);
                        plugin.getPlayerCache().setEnabled(uuid, enableOrDisable);
                        return;
                    }

                    if (csp) {
                        if (enabled) {
                            player.sendMessage(ChatColor.RED + "You already have this enabled, to disable run /csp disable");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            return;
                        }
                        player.sendMessage(enabledString);

                        plugin.getPlayerDatabase().createIfNotExists(uuid, player.getName());
                        plugin.getPlayerDatabase().updateEnabled(uuid, true);
                        plugin.getPlayerCache().setEnabled(uuid, true);
                    }

                    if (!enabled) {
                        player.sendMessage(ChatColor.RED + "You already have this disabled, to enable run /csp enable");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    player.sendMessage(disabled);

                    plugin.getPlayerDatabase().createIfNotExists(uuid, player.getName());
                    plugin.getPlayerDatabase().updateEnabled(uuid, true);
                    plugin.getPlayerCache().setEnabled(uuid, true);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            return;
        }

        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, uuid);
        UUID pUUID = uuid;
        String pName = player.getName();

        boolean hasEnabled = playerDataConfig.getBoolean("commandSpyPlus.player." + pUUID + ".csp", false);

        if (csp == null) {
            if (hasEnabled) {
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", false);
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                player.sendMessage(disabled);
                PlayerData.savePlayerData(plugin, pUUID);
            } else {
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", true);
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                player.sendMessage(enabledString);
                PlayerData.savePlayerData(plugin, pUUID);
            }
        } else {
            if (csp) {
                if (!hasEnabled) {
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", true);
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                    player.sendMessage(enabledString);
                    PlayerData.savePlayerData(plugin, pUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "You already have this enabled, to disable run /csp disable");
                }
            } else {
                if (hasEnabled) {
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", false);
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                    player.sendMessage(disabled);
                    PlayerData.savePlayerData(plugin, pUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "You already have this disabled, to enable run /csp enable");
                }
            }
        }
    }
}
