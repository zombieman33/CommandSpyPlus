package commandspyplus.commandspyplus;

import commandspyplus.commandspyplus.commands.AdminCmd;
import commandspyplus.commandspyplus.commands.MainCommands;
import commandspyplus.commandspyplus.data.LogData;
import commandspyplus.commandspyplus.data.PlayerData;
import commandspyplus.commandspyplus.data.redis.RedisSubscriber;
import commandspyplus.commandspyplus.listeners.CommandSpyListener;
import commandspyplus.commandspyplus.listeners.JoinListener;
import commandspyplus.commandspyplus.manager.HideManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;

public final class CommandSpyPlus extends JavaPlugin {


    private HideManager hideManager;
    private JedisPool jedisPool;
    private Thread redisSubscriberThread;
    private volatile boolean running = true;


    @Override
    public void onEnable() {
        // Plugin startup logic

        File playerDataFile = new File(getDataFolder(), "playerData.yml");

        if (playerDataFile.exists()) {
            playerDataFile.delete();
        }

        PlayerData.initDataFolder(this);
        LogData.initLogsFolder(this);

        // Configs
        saveDefaultConfig();

        boolean redisUse = getConfig().getBoolean("database.redis.use");
        if (redisUse) {
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
        }

        getLogger().info("Connected to Redis server");

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
    }

    public HideManager getHideManager() {
        return hideManager;
    }

    public boolean shouldUseRedis() {
        return getConfig().getBoolean("database.redis.use");
    }
    public String getChannel() {
        return getConfig().getString("database.redis.channel", "CSP");
    }
    public Jedis getJedisResource() {
        return jedisPool.getResource();
    }


    @Override
    public void onDisable() {
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

    public String getServerName() {
        return getConfig().getString("serverName", "n/a");
    }
}
