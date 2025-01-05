package commandspyplus.commandspyplus.data.cache;

import commandspyplus.commandspyplus.data.mysql.PlayerDatabase;
import commandspyplus.commandspyplus.data.mysql.data.PlayerDataSQL;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSettingsCache {

    private final PlayerDatabase dataSource;
    private final ConcurrentHashMap<UUID, Boolean> enabled = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> servers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> username = new ConcurrentHashMap<>();

    public PlayerSettingsCache(PlayerDatabase dataSource) {
        this.dataSource = dataSource;
    }


    public boolean fetchEnabled(UUID uuid) {
        try {
            PlayerDataSQL playerData = dataSource.getPlayerData(uuid);
            if (playerData == null) return false;

            boolean enabled = playerData.getEnabled();
            this.enabled.put(uuid, enabled);
            return enabled;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String fetchServers(UUID uuid) {
        try {
            PlayerDataSQL playerData = dataSource.getPlayerData(uuid);
            if (playerData == null) return null;

            String servers = playerData.getServers();
            this.servers.put(uuid, servers);
            return servers;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fetchBoth(UUID uuid) {
        fetchEnabled(uuid);
        fetchServers(uuid);
    }

    public boolean getEnabled(UUID uuid) {
        Boolean enabled = this.enabled.get(uuid);
        if (enabled == null) return false;
        return enabled;
    }

    public String getServers(UUID uuid) {
        return this.servers.get(uuid);
    }

    public void setEnabled(UUID uuid, boolean enabled) {
        this.enabled.put(uuid, enabled);
    }

    public void setServers(UUID uuid, String servers) {
        this.servers.put(uuid, servers);
    }

    public void forget(UUID uuid) {
        this.servers.remove(uuid);
        this.enabled.remove(uuid);
    }

}
