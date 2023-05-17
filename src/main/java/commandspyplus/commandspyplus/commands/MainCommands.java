package commandspyplus.commandspyplus.commands;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainCommands implements CommandExecutor, TabCompleter {
    private final CommandSpyPlus plugin;

    MiniMessage miniMessage = MiniMessage.miniMessage();
    LegacyComponentSerializer legacyColors = LegacyComponentSerializer.legacyAmpersand();
    Component helpMessage = miniMessage.deserialize("""
                    <#33FB13><bold>Command Spy Plus </#33FB13>
                    <green>
                    /csp enable <player> ( if you don't put in a player you will see the commands )
                    
                    /csp disable <player> ( if you don't put in a player you will not see the commands anymore )
                    
                    /csp reload
                    /csp reset <config.yml, playerData.yml, all> ( resets the configs to the default version )
                    
                    /csp add-command <command> ( adds the command to the ignored command list )
            """);

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
        File playerDataFile = new File(plugin.getDataFolder(), "playerData.yml");
        FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        if (args.length >= 1) {
            String pName = player.getName();
            UUID pUUID = player.getUniqueId();
            if (args[0].equalsIgnoreCase("help")) {
                if (player.hasPermission("commandspyplus.command.use")) {
                    player.sendMessage(helpMessage);
                } else {
                    String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
                    player.sendMessage(ColorUtils.color(permissionMessage));
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("commandspyplus.command.use")) {
                    long startTime = System.currentTimeMillis();
                    try {
                        plugin.reloadConfig();
                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;

                        // Replace %time% with the elapsed time in the reload message
                        String configMessage = plugin.getConfig().getString("reload-message").replace("*", "'").replace("%player%", player.getName());
                        if (configMessage != null) {
                            String timeMessage = configMessage.replace("%time%", Long.toString(elapsedTime));
                            player.sendMessage(ColorUtils.color(timeMessage));
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Config reloaded successfully!");
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "An error occurred while reloading the plugin: " + e.getMessage());
                    }
                } else {
                    String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
                    player.sendMessage(ColorUtils.color(permissionMessage));
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (player.hasPermission("commandspyplus.command.use")) {
                    if (args[1].equalsIgnoreCase("config.yml")) {
                        long startTime = System.currentTimeMillis();
                        plugin.saveResource("config.yml", true);
                        long endTime = System.currentTimeMillis();
                        long time = endTime - startTime + 1;
                        player.sendMessage(ChatColor.GREEN + "You successfully reset " + args[1] + ChatColor.AQUA + " (" + time + "ms)");
                    } else if (args[1].equalsIgnoreCase("playerData.yml")) {
                        long startTime = System.currentTimeMillis();
                        plugin.saveResource("playerData.yml", true);
                        long endTime = System.currentTimeMillis();
                        long time = endTime - startTime + 1;
                        player.sendMessage(ChatColor.GREEN + "You successfully reset " + args[1] + ChatColor.AQUA + " (" + time + "ms)");

                    } else if (args[1].equalsIgnoreCase("all")) {
                        long startTime = System.currentTimeMillis();
                        plugin.saveResource("config.yml", true);
                        plugin.saveResource("playerData.yml", true);
                        long endTime = System.currentTimeMillis();
                        long time = endTime - startTime + 1;
                        player.sendMessage(ChatColor.GREEN + "You successfully reset all configs " + ChatColor.AQUA + "(" + time + "ms)");
                    }
                } else {
                    String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
                    player.sendMessage(ColorUtils.color(permissionMessage));
                }
            } else if (args[0].equalsIgnoreCase("enable")) {
                boolean wantsEnable = playerDataConfig.getBoolean("commandSpyPlus.player." + pUUID + ".csp", false);
                if (player.hasPermission("commandspyplus.command.use")) {
                    if (!wantsEnable) {
                        String enableMessage = plugin.getConfig().getString("enabled").replace("*", "'").replace("%player%", player.getName());
                        playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", true);
                        playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", player.getName());
                        player.sendMessage(ColorUtils.color(enableMessage));
                        savePlayerDataConfig(playerDataConfig, playerDataFile);
                    } else {
                        player.sendMessage(ChatColor.RED + "You already have this enabled, to disable run /csp disable");
                    }
                } else {
                    String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
                    player.sendMessage(ColorUtils.color(permissionMessage));
                }
            } else if (args[0].equalsIgnoreCase("disable")) {
                boolean hasEnabled = playerDataConfig.getBoolean("commandSpyPlus.player." + pUUID + ".csp", false);
                if (player.hasPermission("commandspyplus.command.use")) {
                    if (hasEnabled) {
                        String disableMessage = plugin.getConfig().getString("disabled").replace("*", "'").replace("%player%", player.getName());
                        playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", false);
                        playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                        player.sendMessage(ColorUtils.color(disableMessage));
                        savePlayerDataConfig(playerDataConfig, playerDataFile);
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to enable command spy first! /csp enable");
                    }
                } else {
                    String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
                    player.sendMessage(ColorUtils.color(permissionMessage));
                }
            } else if (args[0].equalsIgnoreCase("add-command")) {
                if (player.hasPermission("commandspyplus.command.use")) {
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "You need to specify a command.");
                        return true;
                    }
                    String commandToAdd = args[1];
                    String removeSlash = commandToAdd.replace("/", "");
                    String formatCommand = removeSlash.replace(removeSlash, "/" + removeSlash);
                    List<String> ignoredCommands = plugin.getConfig().getStringList("ignored-commands");
                    ignoredCommands.add(formatCommand);
                    plugin.getConfig().set("ignored-commands", ignoredCommands);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "You added: " + formatCommand + " to the list of ignored commands");
                } else {
                    String permissionMessage = plugin.getConfig().getString("no-permission");
                    player.sendMessage(ColorUtils.color(permissionMessage));
                }
            }
        }
        return false;
    }

    private void savePlayerDataConfig(FileConfiguration config, File configFile) {
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while saving the player data config.", e);
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        Player player = (Player) sender;

        if (args.length == 1) {
            if (player.hasPermission("commandspyplus.command.use")) {
                completions.add("help");
            }
            if (player.hasPermission("commandspyplus.command.use")) {
                completions.add("add-command");
            }
            if (player.hasPermission("commandspyplus.command.use")) {
                completions.add("reload");
            }
            if (player.hasPermission("commandspyplus.command.use")) {
                completions.add("reset");
            }
            if (player.hasPermission("commandspyplus.command.use")) {
                completions.add("enable");
            }
            if (player.hasPermission("commandspyplus.command.use")) {
                completions.add("disable");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                completions.add("playerData.yml");
                completions.add("config.yml");
                completions.add("all");
            } else if (args[0].equalsIgnoreCase("add-command")) {
                completions.add("<command>");
            }
        }
        return completions;
    }
}
