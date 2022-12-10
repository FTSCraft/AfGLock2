package de.afgmedia.afglock2.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    Connection conn = null;

    String username;
    String password;
    String url;

    public DatabaseManager() {

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

            conn = DriverManager.getConnection(url, username, password);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void initTables() {

        String sql = "CREATE TABLES IF NOT EXISTS Locks (" +
                "id INT NOT NULL PRIMARY KEY," +
                "owner VARCHAR(36) NOT NULL," +
                "tier INT NOT NULL," +
                "cord_x INT NOT NULL," +
                "cory_y INT NOT NULL," +
                "cord_z INT NOT NULL," +
                "cord_world VARCHAR(255) NOT NULL," +
                "type STRING NOT NULL" +
                "allow_player STRING NOT NULL," +
                "allow_group STRING NOT NULL" +
                ");";

    }

}
