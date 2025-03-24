package commandspyplus.commandspyplus.utils;

import java.util.Arrays;
import java.util.List;

public class ServerNameUtil {

    public static List<String> fromString(String serverString) {
        if (serverString == null || serverString.isEmpty()) return List.of();

        return Arrays.asList(serverString.split(","));
    }

    public static String toString(List<String> serverList) {
        if (serverList == null || serverList.isEmpty()) return "";

        return String.join(",", serverList);
    }
}

