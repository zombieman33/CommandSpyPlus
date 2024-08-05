package commandspyplus.commandspyplus.data;

import commandspyplus.commandspyplus.CommandSpyPlus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

public class LogData {
    public static final String LOGS_FOLDER_NAME = "logs";

    public static void initLogsFolder(CommandSpyPlus plugin) {
        File logsFolder = new File(plugin.getDataFolder(), LOGS_FOLDER_NAME);
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
    }

    @NotNull
    private static File getLogFile(CommandSpyPlus plugin, LocalDate date) {
        return new File(plugin.getDataFolder(), LOGS_FOLDER_NAME + "/" + date + ".yml");
    }

    public static void cleanupOldLogs(CommandSpyPlus plugin) {
        File logsFolder = new File(plugin.getDataFolder(), LOGS_FOLDER_NAME);
        if (logsFolder.exists() && logsFolder.isDirectory()) {
            File[] logFiles = logsFolder.listFiles();
            if (logFiles != null && logFiles.length > plugin.getConfig().getInt("maxLogDays", 30)) {
                Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));
                for (int i = 0; i < logFiles.length - plugin.getConfig().getInt("maxLogDays", 30); i++) {
                    logFiles[i].delete();
                }
            }
        }
    }

    public static void addLog(CommandSpyPlus plugin, String logMessage) {
        LocalDate today = LocalDate.now();
        File logFile = getLogFile(plugin, today);

        cleanupOldLogs(plugin);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedLog = "[" + timestamp + "] " + logMessage;

        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(formattedLog + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
