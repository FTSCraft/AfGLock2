package de.afgmedia.afglock2.database;

import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.locks.ProtectionType;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    Connection conn = null;

    String url = "jdbc:sqlite:" + AfGLock.getInstance().getDataFolder() + "/afglock.db";

    public DatabaseManager() {
        initConnection();
        initTables();
    }

    public void shutdownConnection() {

        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void initConnection() {
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initTables() {

        if (conn == null)
            return;

        String table_lock_sql = "CREATE TABLE IF NOT EXISTS Locks (" +
                "id INT PRIMARY KEY," +
                "owner UUID NOT NULL," +
                "tier INT NOT NULL," +
                "cord_x DOUBLE NOT NULL," +
                "cord_y DOUBLE NOT NULL," +
                "cord_z DOUBLE NOT NULL," +
                "cord_world UUID NOT NULL," +
                "access_players TEXT," +
                "access_groups TEXT," +
                "type STRING NOT NULL" +
                ");";

        try (Statement statement = conn.createStatement()) {
            statement.execute(table_lock_sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean saveLock(Protection protection) {

        String insertSql = "INSERT INTO Locks (owner, tier, cord_x, cord_y, cord_z, cord_world, access_players, access_groups, type, id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "access_players = EXCLUDED.access_players, " +
                "access_groups = EXCLUDED.access_groups";

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSql)) {
            // Platzhalter durch tatsächliche Werte ersetzen
            preparedStatement.setObject(1, protection.getOwner());
            preparedStatement.setInt(2, protection.getProtectionTier());
            preparedStatement.setDouble(3, protection.getLocation().getX());
            preparedStatement.setDouble(4, protection.getLocation().getY());
            preparedStatement.setDouble(5, protection.getLocation().getZ());
            preparedStatement.setObject(6, protection.getLocation().getWorld().getUID());

            StringBuilder playersBuilder = new StringBuilder();
            StringBuilder groupsBuilder = new StringBuilder();

            for (AllowSetting allowSetting : protection.getAllowSettings()) {
                if (allowSetting.getType() == AllowSetting.AllowSettingType.PLAYER) {
                    playersBuilder.append(allowSetting.getUuid()).append(",");
                } else {
                    groupsBuilder.append(allowSetting.getGroup()).append(",");
                }
            }
            if(!playersBuilder.isEmpty()) {
                playersBuilder.deleteCharAt(playersBuilder.length() - 1);
            }
            if(!groupsBuilder.isEmpty()) {
                groupsBuilder.deleteCharAt(groupsBuilder.length() - 1);
            }
            preparedStatement.setString(7, playersBuilder.toString());
            preparedStatement.setString(8, groupsBuilder.toString());

            preparedStatement.setString(9, protection.getProtectionType().toString());
            preparedStatement.setInt(10, protection.getId());

            // Ausführen der vorbereiteten Anweisung
            preparedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public Protection loadLock(int id) {
        String selectSql = "SELECT * FROM Locks WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(selectSql)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                double x = resultSet.getDouble("cord_x");
                double y = resultSet.getDouble("cord_y");
                double z = resultSet.getDouble("cord_z");
                UUID world = UUID.fromString(resultSet.getString("cord_world"));
                Location loc = new Location(Bukkit.getWorld(world), x, y, z);

                ProtectionType type = ProtectionType.valueOf(resultSet.getString("type"));
                UUID owner = UUID.fromString(resultSet.getString("owner"));
                int tier = resultSet.getInt("tier");

                List<AllowSetting> allowSettings = new ArrayList<>();
                String access_players = resultSet.getString("access_players");
                String access_groups = resultSet.getString("access_groups");
                if (access_players != null && !access_players.isEmpty()) {
                    String[] players = access_players.split(",");
                    for (String player : players) {
                        AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.PLAYER);
                        allowSetting.setUuid(player);
                        allowSettings.add(allowSetting);
                    }
                }
                if(access_groups != null && !access_groups.isEmpty()) {
                    String[] groups = access_groups.split(",");
                    for (String group : groups) {
                        AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.GROUP);
                        allowSetting.setGroup(group);
                        allowSettings.add(allowSetting);
                    }
                }

                AfGLock.getInstance().getProtectionManager().addLock(loc, owner, type, tier, allowSettings, id);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public Protection loadLock(Location loc) {
        String selectSql = "SELECT * FROM Locks WHERE cord_x = ? AND cord_y = ? AND cord_z = ? AND cord_world = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(selectSql)) {

            preparedStatement.setDouble(1, loc.getX());
            preparedStatement.setDouble(2, loc.getY());
            preparedStatement.setDouble(3, loc.getZ());
            preparedStatement.setString(4, String.valueOf(loc.getWorld().getUID()));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                int id = resultSet.getInt("id");

                ProtectionType type = ProtectionType.valueOf(resultSet.getString("type"));
                UUID owner = UUID.fromString(resultSet.getString("owner"));
                int tier = resultSet.getInt("tier");

                List<AllowSetting> allowSettings = new ArrayList<>();
                String access_players = resultSet.getString("access_players");
                String access_groups = resultSet.getString("access_groups");
                if (access_players != null && !access_players.isEmpty()) {
                    String[] players = access_players.split(",");
                    for (String player : players) {
                        AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.PLAYER);
                        allowSetting.setUuid(player);
                        allowSettings.add(allowSetting);
                    }
                }
                if(access_groups != null && !access_groups.isEmpty()) {
                    String[] groups = access_groups.split(",");
                    for (String group : groups) {
                        AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.GROUP);
                        allowSetting.setGroup(group);
                        allowSettings.add(allowSetting);
                    }
                }

                return AfGLock.getInstance().getProtectionManager().addLock(loc, owner, type, tier, allowSettings, id);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public void deleteLock(Protection protection) {
        String deleteSql = "DELETE FROM Locks WHERE id = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(deleteSql)) {
            preparedStatement.setInt(1, protection.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
