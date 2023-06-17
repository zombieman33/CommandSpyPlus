package commandspyplus.commandspyplus.utils;

import com.tcoded.legacycolorcodeparser.LegacyColorCodeParser;
import commandspyplus.commandspyplus.CommandSpyPlus;
import org.bukkit.ChatColor;

public class ColorUtils {

    public ColorUtils(CommandSpyPlus plugin) {
    }
    public static String color(String string) {
        string = LegacyColorCodeParser.convertHexToLegacy('&', string);
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }
}
