package commandspyplus.commandspyplus.utils;

import com.tcoded.legacycolorcodeparser.LegacyColorCodeParser;
import commandspyplus.commandspyplus.CommandSpyPlus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
