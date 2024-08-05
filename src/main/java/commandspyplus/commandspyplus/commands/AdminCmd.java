package commandspyplus.commandspyplus.commands;

import commandspyplus.commandspyplus.CommandSpyPlus;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.manager.HideManager;
import commandspyplus.commandspyplus.modes.HiddenModes;
import commandspyplus.commandspyplus.utils.ColorUtils;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCmd implements CommandExecutor, TabCompleter {
    private final CommandSpyPlus plugin;

    public AdminCmd(CommandSpyPlus plugin) {
        this.plugin = plugin;
    }

    private List<String> modesList = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;
        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId());


        if (!player.hasPermission("commandspyplus.command.hide")) {
            String permissionMessage = plugin.getConfig().getString("no-permission").replace("*", "'").replace("%player%", player.getName());
            player.sendMessage(ColorUtils.color(permissionMessage));
            return false;
        }

        HideManager hideManager = plugin.getHideManager();
        MiniMessage miniMessage = MiniMessage.miniMessage();


        if (args.length >= 1 && args[0].equalsIgnoreCase("getMode")) {
            player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                    "));
            player.sendMessage(miniMessage.deserialize("<#00FF00>Mode: " + hideManager.getHiddenMode().name()));
            player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                    "));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            return false;
        }

        if (args.length >= 2) {

            String action = args[0];
            String targetMode = args[1];

            if (action.equalsIgnoreCase("hide")) {

                for (HiddenModes mode : HiddenModes.values()) {
                    modesList.add(mode.name());
                }


                if (!modesList.contains(targetMode) && PlayerData.checkPlayerExist(plugin, targetMode) && hideManager.getHiddenMode() == HiddenModes.PERSON) {

                    if (!PlayerData.checkPlayerExist(plugin, targetMode)) {
                        notValidPlayer(player, targetMode);
                        return false;
                    }

                    boolean hidden = true;
                    if (args.length >= 3) {
                        try {
                            hidden = Boolean.parseBoolean(args[2]);
                        } catch (IllegalArgumentException e) {
                            hidden = true;
                        }
                    }

                    hideManager.setPlayerHidden(targetMode, hidden);

                    String addedOrRemoved = "removed";
                    String toOrFrom = "from";

                    if (hidden) {
                        addedOrRemoved = "added";
                        toOrFrom = "to";
                    }

                    player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                                 "));
                    player.sendMessage(miniMessage.deserialize("<#00FF00>Successfully " + addedOrRemoved + ": " + targetMode));
                    player.sendMessage(miniMessage.deserialize("<#00FF00>" + toOrFrom + " the hidden players!"));
                    player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                                 "));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

                } else if (modesList.contains(targetMode.toUpperCase())) {
                    hideManager.setHiddenModeInConfig(HiddenModes.valueOf(targetMode));

                    if (args.length >= 3) {

                        if (hideManager.getHiddenMode() != HiddenModes.PERSON) {
                            errorNotModePerson(player, miniMessage);
                            return false;
                        }

                        String targetName = args[2];
                        if (!PlayerData.checkPlayerExist(plugin, targetName)) {
                            notValidPlayer(player, targetName);
                            return false;
                        }

                        boolean hidden = true;
                        if (args.length >= 4) {
                            try {
                                hidden = Boolean.parseBoolean(args[3]);
                            } catch (IllegalArgumentException e) {
                                hidden = true;
                            }
                        }

                        hideManager.setPlayerHidden(targetMode, hidden);

                        String addedOrRemoved = "removed";
                        String toOrFrom = "from";

                        if (hidden) {
                            addedOrRemoved = "added";
                            toOrFrom = "to";
                        }
                        player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                                 "));
                        player.sendMessage(miniMessage.deserialize("<#00FF00>Successfully changed the mode to: " + targetMode.toUpperCase()));
                        player.sendMessage(miniMessage.deserialize("<#00FF00>And successfully " + addedOrRemoved + ": " + targetName));
                        player.sendMessage(miniMessage.deserialize("<#00FF00>" + toOrFrom + " the hidden players!"));
                        player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                                 "));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

                    } else {
                        player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                                 "));
                        player.sendMessage(miniMessage.deserialize("<#00FF00>Successfully changed the mode to: " + targetMode.toUpperCase()));
                        player.sendMessage(miniMessage.deserialize("<#00FF00><strikethrough>                                                                 "));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                    }
                } else if (!modesList.contains(targetMode) && PlayerData.checkPlayerExist(plugin, targetMode) && hideManager.getHiddenMode() != HiddenModes.PERSON) {
                    errorNotModePerson(player, miniMessage);
                    return false;
                } else {
                    player.sendMessage(ChatColor.YELLOW + "/cspadmin <hide> <mode or player> <if mode is 'PERSON' player name>");
                }
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "/cspadmin <hide, getMode> <mode or player> <if mode is 'PERSON' player name>");
        }
        return false;
    }

    private static void notValidPlayer(Player player, String targetName) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<#FF0000>" +targetName + " is not a valid player."));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    private static void errorNotModePerson(Player player, MiniMessage miniMessage) {
        player.sendMessage(miniMessage.deserialize("<#FF0000><strikethrough>                                                                 "));
        player.sendMessage(miniMessage.deserialize("<#FF0000>ERROR: You cannot add a player when the mode isn't 'PERSON'!"));
        player.sendMessage(miniMessage.deserialize("<#FFCC00>Want to change mode to 'PERSON'? [Click Me]")
                .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<#FFCC00>/cspadmin hide PERSON")))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/cspadmin hide PERSON")));
        player.sendMessage(miniMessage.deserialize("<#FF0000><strikethrough>                                                                 "));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        HideManager manager = plugin.getHideManager();

        modesList.clear();

        for (HiddenModes mode : HiddenModes.values()) {
            modesList.add(mode.name());
        }

        Player player = (Player) sender;

        if (player.hasPermission("commandspyplus.command.hide")) {
            if (args.length == 1) {

                completions.add("hide");
                completions.add("getMode");

            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("hide")) {
                    if (manager.getHiddenMode() == HiddenModes.PERSON) {
                        completions.add(HiddenModes.NONE.name());
                        completions.add(HiddenModes.PERMISSION.name());
                        for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                            completions.add(onlinePlayers.getName());
                        }
                    } else {
                        for (HiddenModes mode : HiddenModes.values()) {
                            completions.add(mode.name());
                        }
                    }
                }
            } else if (args.length == 3) {

                if (args[1].equalsIgnoreCase(HiddenModes.PERSON.name())) {
                    for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                        completions.add(onlinePlayers.getName());
                    }
                }

                List<String> nameList = new ArrayList<>();

                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    nameList.add(onlinePlayers.getName());
                }

                if (nameList.contains(args[1]) && manager.getHiddenMode() == HiddenModes.PERSON) {
                    completions.add("true");
                    completions.add("false");
                }
            } else if (args.length == 4) {

                List<String> nameList = new ArrayList<>();

                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    nameList.add(onlinePlayers.getName());
                }

                for (HiddenModes modes : HiddenModes.values()) {
                    modesList.add(modes.name());
                }

                if (nameList.contains(args[2]) && modesList.contains(args[1]) && HiddenModes.valueOf(args[1]) == HiddenModes.PERSON) {
                    completions.add("true");
                    completions.add("false");
                }
            }
        }

        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.toLowerCase().startsWith(lastArg.toLowerCase())).collect(Collectors.toList());
    }
}
