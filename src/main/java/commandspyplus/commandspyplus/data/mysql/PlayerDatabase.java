package commandspyplus.commandspyplus.data.mysql;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import commandspyplus.commandspyplus.data.mysql.data.PlayerDataSQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDatabase {
    private final Dao<PlayerDataSQL, String> dataDao;
    private final ConnectionSource connectionSource;

    public PlayerDatabase(String jdbcUrl, String username, String password) throws SQLException {
        connectionSource = new JdbcConnectionSource(jdbcUrl, username, password);
        TableUtils.createTableIfNotExists(connectionSource, PlayerDataSQL.class);
        dataDao = DaoManager.createDao(connectionSource, PlayerDataSQL.class);
        System.out.println("Database connection established and tables checked.");
    }

    public void close() {
        try {
            if (connectionSource != null && connectionSource.isOpen("default")) {
                connectionSource.close();
                System.out.println("Database connection closed.");
            }
        } catch (Exception e) {
            System.out.println("Failed to close database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PlayerDataSQL getPlayerData(UUID uuid, String username) throws SQLException {
        PlayerDataSQL data = dataDao.queryForId(uuid.toString());

        if (data == null) {
            data = new PlayerDataSQL();
            data.setUuid(uuid.toString());
            data.setUsername(username);
            data.setServers(null);
            data.setEnable(false);
            dataDao.create(data);
        }

        return data;
    }
    public PlayerDataSQL getPlayerData(UUID uuid) throws SQLException {
        PlayerDataSQL data = dataDao.queryForId(uuid.toString());

        return data;
    }
    public void updateUsername(UUID uuid, String username) throws SQLException {
        PlayerDataSQL data = dataDao.queryForId(uuid.toString());
        if (data != null) {
            data.setUsername(username);
            dataDao.update(data);
        }
    }

    public void updateEnabled(UUID uuid, boolean enabled) throws SQLException {
        PlayerDataSQL data = dataDao.queryForId(uuid.toString());
        if (data != null) {
            data.setEnable(enabled);
            dataDao.update(data);
        }
    }
    public void updateServers(UUID uuid, String servers) throws SQLException {
        PlayerDataSQL data = dataDao.queryForId(uuid.toString());
        if (data != null) {
            data.setServers(servers);
            dataDao.update(data);
        }
    }
    public void createIfNotExists(UUID uuid, String username) throws SQLException {
        PlayerDataSQL data = new PlayerDataSQL();
        data.setUuid(uuid.toString());

        if (dataDao.queryForId(data.getUuid()) != null) return;

        data.setServers("");
        data.setEnable(false);
        data.setUsername(username);
        dataDao.createOrUpdate(data);
    }

    public List<String> getAllUsernames() throws SQLException {
        List<PlayerDataSQL> allData = dataDao.queryForAll();

        List<String> usernames = new ArrayList<>();

        for (PlayerDataSQL data : allData) {
            usernames.add(data.getUsername());
        }

        return usernames;
    }

    public UUID getUuidByUsername(String username) throws SQLException {
        List<PlayerDataSQL> result = dataDao.queryForEq("username", username);

        if (!result.isEmpty()) {
            return UUID.fromString(result.get(0).getUuid());
        }
        return null;
    }
    public UUID getUuidByDiscordTag(String discordTag) throws SQLException {
        List<PlayerDataSQL> result = dataDao.queryForEq("discordTag", discordTag);

        if (!result.isEmpty()) {
            System.out.println(UUID.fromString(result.get(0).getUuid()));
            return UUID.fromString(result.get(0).getUuid());
        }

        return null;
    }

    public String getUsernameByDiscordTag(String discordTag) throws SQLException {
        List<PlayerDataSQL> result = dataDao.queryForEq("discordTag", discordTag);

        if (!result.isEmpty()) {
            return result.get(0).getUsername();
        }

        return null;
    }
}
