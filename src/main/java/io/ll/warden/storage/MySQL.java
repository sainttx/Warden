package io.ll.warden.storage;

import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL extends Storage {

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public MySQL(ConfigurationSection mySQL) {
        this.host = mySQL.getString("host");
        this.port = mySQL.getString("port");
        this.database = mySQL.getString("database");
        this.username = mySQL.getString("username");
        this.password = mySQL.getString("password");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://"
                        + this.host + ":"
                        + this.port + "/"
                        + this.database + "?autoReconnect=true&user="
                        + this.username
                        + (this.password == null || this.password.isEmpty()
                        ? "" : "&password=" + this.password)
        );
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MYSQL;
    }
}
