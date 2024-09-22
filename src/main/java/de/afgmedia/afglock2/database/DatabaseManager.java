package de.afgmedia.afglock2.database;

import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.locks.ProtectionTier;
import de.afgmedia.afglock2.locks.ProtectionType;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DATABASE_URL = "jdbc:sqlite:" + AfGLock.getInstance().getDataFolder() + "/afglock.db";

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Locks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "owner UUID NOT NULL," +
            "tier VARCHAR(50) NOT NULL," +
            "cord_x DOUBLE NOT NULL," +
            "cord_y DOUBLE NOT NULL," +
            "cord_z DOUBLE NOT NULL," +
            "cord_world UUID NOT NULL," +
            "access_players TEXT," +
            "access_groups TEXT," +
            "type STRING NOT NULL" +
            ");";

    private static final String INSERT_LOCK_SQL = "INSERT INTO Locks (owner, tier, cord_x, cord_y, cord_z, cord_world, access_players, access_groups, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_LOCK_SQL = "INSERT INTO Locks (owner, tier, cord_x, cord_y, cord_z, cord_world, access_players, access_groups, type, id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET " +
            "access_players = EXCLUDED.access_players, access_groups = EXCLUDED.access_groups";
    private static final String SELECT_LOCK_BY_ID_SQL = "SELECT * FROM Locks WHERE id = ?";
    private static final String SELECT_LOCK_BY_LOCATION_SQL = "SELECT * FROM Locks WHERE cord_x = ? AND cord_y = ? AND cord_z = ? AND cord_world = ?";
    private static final String DELETE_LOCK_SQL = "DELETE FROM Locks WHERE id = ?";

    private Connection conn;

    public DatabaseManager() {
        initConnection();
        initTables();
    }

    private void initConnection() {
        try {
            this.conn = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    private void initTables() {
        try (Statement statement = conn.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database tables", e);
        }
    }

    public void shutdownConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }

    public Protection createLock(UUID owner, Location location, ProtectionType type, ProtectionTier tier) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INSERT_LOCK_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, owner);
            preparedStatement.setString(2, tier.toString());
            preparedStatement.setDouble(3, location.getX());
            preparedStatement.setDouble(4, location.getY());
            preparedStatement.setDouble(5, location.getZ());
            preparedStatement.setObject(6, location.getWorld().getUID());
            preparedStatement.setString(7, "");
            preparedStatement.setString(8, "");
            preparedStatement.setString(9, type.toString());

            preparedStatement.executeUpdate();

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    return new Protection(id, owner, location, type, tier);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create lock", e);
        }
        return null;
    }

    public boolean saveLock(Protection protection) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_LOCK_SQL)) {
            preparedStatement.setObject(1, protection.getOwner());
            preparedStatement.setString(2, protection.getProtectionTier().toString());
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
            if (playersBuilder.length() > 0) {
                playersBuilder.deleteCharAt(playersBuilder.length() - 1);
            }
            if (groupsBuilder.length() > 0) {
                groupsBuilder.deleteCharAt(groupsBuilder.length() - 1);
            }

            preparedStatement.setString(7, playersBuilder.toString());
            preparedStatement.setString(8, groupsBuilder.toString());
            preparedStatement.setString(9, protection.getProtectionType().toString());
            preparedStatement.setInt(10, protection.getId());

            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save lock", e);
            return false;
        }
    }

    public Protection loadLock(int id) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SELECT_LOCK_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToProtection(resultSet);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load lock by ID", e);
        }
        return null;
    }

    public Protection loadLock(Location loc) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(SELECT_LOCK_BY_LOCATION_SQL)) {
            preparedStatement.setDouble(1, loc.getX());
            preparedStatement.setDouble(2, loc.getY());
            preparedStatement.setDouble(3, loc.getZ());
            preparedStatement.setString(4, loc.getWorld().getUID().toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToProtection(resultSet);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load lock by location", e);
        }
        return null;
    }

    public void deleteLock(Protection protection) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(DELETE_LOCK_SQL)) {
            preparedStatement.setInt(1, protection.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete lock", e);
        }
    }

    private Protection mapResultSetToProtection(ResultSet resultSet) throws SQLException {
        double x = resultSet.getDouble("cord_x");
        double y = resultSet.getDouble("cord_y");
        double z = resultSet.getDouble("cord_z");
        UUID world = UUID.fromString(resultSet.getString("cord_world"));
        Location loc = new Location(Bukkit.getWorld(world), x, y, z);

        ProtectionType type = ProtectionType.valueOf(resultSet.getString("type"));
        UUID owner = UUID.fromString(resultSet.getString("owner"));
        ProtectionTier tier = ProtectionTier.valueOf(resultSet.getString("tier"));

        List<AllowSetting> allowSettings = new ArrayList<>();
        String accessPlayers = resultSet.getString("access_players");
        String accessGroups = resultSet.getString("access_groups");

        if (accessPlayers != null && !accessPlayers.isEmpty()) {
            String[] players = accessPlayers.split(",");
            for (String player : players) {
                AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.PLAYER);
                allowSetting.setUuid(player);
                allowSettings.add(allowSetting);
            }
        }

        if (accessGroups != null && !accessGroups.isEmpty()) {
            String[] groups = accessGroups.split(",");
            for (String group : groups) {
                AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.GROUP);
                allowSetting.setGroup(group);
                allowSettings.add(allowSetting);
            }
        }

        return new Protection(resultSet.getInt("id"), owner, loc, type, tier, allowSettings);
    }
}
