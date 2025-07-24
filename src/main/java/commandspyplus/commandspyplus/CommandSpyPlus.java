package commandspyplus.commandspyplus;

import commandspyplus.commandspyplus.commands.AdminCmd;
import commandspyplus.commandspyplus.commands.MainCommands;
import commandspyplus.commandspyplus.data.LogData;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.data.cache.PlayerSettingsCache;
import commandspyplus.commandspyplus.data.cache.ServerListCache;
import commandspyplus.commandspyplus.data.mysql.PlayerDatabase;
import commandspyplus.commandspyplus.data.mysql.ServerDatabase;
import commandspyplus.commandspyplus.data.redis.RedisSubscriber;
import commandspyplus.commandspyplus.listeners.CommandSpyListener;
import commandspyplus.commandspyplus.listeners.JoinListener;
import commandspyplus.commandspyplus.manager.HideManager;
import commandspyplus.commandspyplus.utils.ServerNameUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CommandSpyPlus extends JavaPlugin {


    private HideManager hideManager;
    private JedisPool jedisPool;
    private Thread redisSubscriberThread;
    private PlayerDatabase playerDatabase;
    private ServerDatabase serverDatabase;
    private PlayerSettingsCache playerSettingsCache;
    private ServerListCache serverListCache;

    public final UUID sessionID = UUID.randomUUID();
    private volatile boolean running = true;
    public boolean consoleCommands = false;


    @Override
    public void onEnable() {
        // Plugin startup logic

        File playerDataFile = new File(getDataFolder(), "playerData.yml");

        if (playerDataFile.exists()) playerDataFile.delete();

        PlayerData.initDataFolder(this);
        LogData.initLogsFolder(this);

        // Configs
        saveDefaultConfig();

        // Check for PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("-----------------------------------------");
            getLogger().warning("WARNING");
            getLogger().warning("PlaceholderAPI plugin is not installed!");
            getLogger().warning(this.getPluginMeta().getName() + " is now being disabled!");
            getLogger().warning("-----------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        boolean use = getConfig().getBoolean("database.use");
        if (use) {

            try {
                String url = getConfig().getString("database.mysql.url");
                String username = getConfig().getString("database.mysql.username");
                String password = getConfig().getString("database.mysql.password");
                playerDatabase = new PlayerDatabase(url, username, password);
                serverDatabase = new ServerDatabase(this, url, username, password);

                getLogger().info("Connected to MySQL database");
            } catch (SQLException e) {
                e.printStackTrace();
                getLogger().severe("Failed to connect to MySQL database! Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            String redisHost = getConfig().getString("database.redis.host");
            int redisPort = getConfig().getInt("database.redis.port");
            String redisPassword = getConfig().getString("database.redis.password", null);
            String redisUsername = getConfig().getString("database.redis.username", null);
            String channel = getConfig().getString("database.redis.channel", "CSP");

            JedisPoolConfig poolConfig = new JedisPoolConfig();

            if (redisUsername != null && redisPassword != null) {
                getLogger().info("Using Redis username and password authentication.");
                jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 2000, redisUsername, redisPassword);
            } else if (redisPassword != null) {
                getLogger().info("Using Redis password authentication.");
                jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 2000, redisPassword);
            } else {
                getLogger().info("Connecting to Redis without password authentication.");
                jedisPool = new JedisPool(poolConfig, redisHost, redisPort);
            }

            redisSubscriberThread = new Thread(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    RedisSubscriber subscriber = new RedisSubscriber(this);
                    jedis.subscribe(subscriber, channel);
                } finally {
                    running = false;
                }
            });
            redisSubscriberThread.start();
            getLogger().info("Connected to Redis server");

            playerSettingsCache = new PlayerSettingsCache(playerDatabase);
            serverListCache = new ServerListCache(serverDatabase);

            serverListCache.fetchServers();

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerSettingsCache.fetchBoth(player.getUniqueId());
                }
            });

        }


        // Commands
        MainCommands mainCommands = new MainCommands(this);
        PluginCommand mainCmd = getCommand("commandspyplus");
        if (mainCmd != null) mainCmd.setExecutor(mainCommands);

        AdminCmd adminCommand = new AdminCmd(this);
        PluginCommand adminCmd = getCommand("commandspyadmin");
        if (adminCmd != null) adminCmd.setExecutor(adminCommand);

        // Listeners
        CommandSpyListener commandSpyListener = new CommandSpyListener(this);
        getServer().getPluginManager().registerEvents(commandSpyListener, this);

        new JoinListener(this);

        // Managers
        hideManager = new HideManager(this);
        // Data
        new PlayerData();


        consoleCommands = this.getConfig().getBoolean("allowConsoleCommands");
    }

    public HideManager getHideManager() {
        return hideManager;
    }

    public boolean shouldUseDatabase() {
        return getConfig().getBoolean("database.use");
    }
    public String getChannel() {
        return getConfig().getString("database.redis.channel", "CSP");
    }
    public Jedis getJedisResource() {
        return jedisPool.getResource();
    }


    @Override
    public void onDisable() {

        if (!shouldUseDatabase()) return;

        if (jedisPool != null) {
            jedisPool.close();
            getLogger().info("Redis connection pool closed.");
        }

        running = false;
        if (redisSubscriberThread != null && redisSubscriberThread.isAlive()) {
            redisSubscriberThread.interrupt();
            try {
                redisSubscriberThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public PlayerDatabase getPlayerDatabase() {
        return playerDatabase;
    }

    public ServerDatabase getServerDatabase() {
        return serverDatabase;
    }

    public PlayerSettingsCache getPlayerCache() {
        return playerSettingsCache;
    }

    public ServerListCache getServerListCache() {
        return serverListCache;
    }

    public String getServerName() {
        return getConfig().getString("serverName", "n/a");
    }
}
