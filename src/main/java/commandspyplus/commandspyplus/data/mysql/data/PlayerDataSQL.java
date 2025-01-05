package commandspyplus.commandspyplus.data.mysql.data;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "player_data")
public class PlayerDataSQL {

    @DatabaseField(id = true)
    private String uuid;

    @DatabaseField(canBeNull = false)
    private String username;

    @DatabaseField
    private String servers;

    @DatabaseField
    private boolean enable;

    public PlayerDataSQL() {}

    // Getters and Setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getServers() { return servers; }
    public void setServers(String servers) { this.servers = servers; }

    public boolean getEnabled() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }



}
