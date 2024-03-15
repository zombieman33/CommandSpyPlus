package commandspyplus.commandspyplus.commands;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.utils.ColorUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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


        if (player.hasPermission("commandspyplus.command.use")) {
            String pName = player.getName();
            UUID pUUID = player.getUniqueId();
            boolean wantsEnable = playerDataConfig.getBoolean("commandSpyPlus.player." + pUUID + ".csp", false);
            String enableMessage = plugin.getConfig().getString("enabled").replace("*", "'").replace("%player%", player.getName());
            String disableMessage = plugin.getConfig().getString("disabled").replace("*", "'").replace("%player%", player.getName());
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    player.sendMessage(ColorUtils.color("&#33FB13&lCommand Spy Plus"));
                    player.sendMessage(ColorUtils.color("&#33FB13/csp enable <player> ( if you don't put in a player you will see the commands )"));
                    player.sendMessage(ColorUtils.color("&#33FB13/csp disable <player> ( if you don't put in a player you will not see the commands anymore )"));
                    player.sendMessage(ColorUtils.color("&#33FB13/csp reload"));
                    player.sendMessage(ColorUtils.color("&#33FB13/csp reset <config.yml> ( resets the configs to the default version )"));
                    player.sendMessage(ColorUtils.color("&#33FB13/csp add-command <command> ( adds the command to the ignored command list"));
                } else if (args[0].equalsIgnoreCase("reload")) {
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
                } else if (args[0].equalsIgnoreCase("reset")) {
                    if (args[1].equalsIgnoreCase("config.yml")) {
                        long startTime = System.currentTimeMillis();
                        plugin.saveResource("config.yml", true);
                        plugin.reloadConfig();
                        long endTime = System.currentTimeMillis();
                        long time = endTime - startTime + 1;
                        player.sendMessage(ChatColor.GREEN + "You successfully reset " + args[1] + ChatColor.AQUA + " (" + time + "ms)");

                    } else {
                        player.sendMessage(ChatColor.YELLOW + "/csp reset <config.yml>");
                    }
                } else if (args[0].equalsIgnoreCase("enable")) {

                    csp(player, true);

                } else if (args[0].equalsIgnoreCase("disable")) {

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
        } else {
            String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
            player.sendMessage(ColorUtils.color(permissionMessage));
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
                completions.add("add-command");
                completions.add("remove-command");
                completions.add("reload");
                completions.add("reset");
                completions.add("enable");
                completions.add("disable");
            }
        } else if (args.length == 2) {
            if (player.hasPermission("commandspyplus.command.use")) {
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
                }
            }
        }
        String lastArg = args[args.length - 1];
        return completions.stream().filter(s -> s.startsWith(lastArg)).collect(Collectors.toList());
    }

    private void csp(Player player, @Nullable Boolean csp) {

        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId());
        UUID pUUID = player.getUniqueId();
        String pName = player.getName();

        boolean hasEnabled = playerDataConfig.getBoolean("commandSpyPlus.player." + pUUID + ".csp", false);

        if (csp == null) {
            if (hasEnabled) {
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", false);
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                player.sendMessage(ColorUtils.color(plugin.getConfig().getString("disabled").replace("*", "'").replace("%player%", player.getName())));
                PlayerData.savePlayerData(plugin, pUUID);
            } else {
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", true);
                playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                player.sendMessage(ColorUtils.color(plugin.getConfig().getString("enabled").replace("*", "'").replace("%player%", player.getName())));
                PlayerData.savePlayerData(plugin, pUUID);
            }
        } else {
            if (csp) {
                if (!hasEnabled) {
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", true);
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                    player.sendMessage(ColorUtils.color(plugin.getConfig().getString("enabled").replace("*", "'").replace("%player%", player.getName())));
                    PlayerData.savePlayerData(plugin, pUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "You already have this enabled, to disable run /csp disable");
                }
            } else {
                if (hasEnabled) {
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".csp", false);
                    playerDataConfig.set("commandSpyPlus.player." + pUUID + ".ign", pName);
                    player.sendMessage(ColorUtils.color(plugin.getConfig().getString("disabled").replace("*", "'").replace("%player%", player.getName())));
                    PlayerData.savePlayerData(plugin, pUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "You already have this disabled, to enable run /csp enable");
                }
            }
        }
    }
}
