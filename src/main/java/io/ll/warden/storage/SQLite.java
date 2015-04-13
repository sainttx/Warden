package io.ll.warden.storage;

import io.ll.warden.WardenPlugin;
import io.ll.warden.utils.proxy.Warden;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends Storage {

    private final String location;

    public SQLite(ConfigurationSection sqLite) {
        this.location = sqLite.getString("database");
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (!this.checkFileExists(new File(Warden.getDataFolder(), this.location))) {
            Warden.logSevere("Failed to create SQLite database file!");
            return null;
        }

        return DriverManager.getConnection("jdbc:sqlite:"
                + Warden.getDataFolder().toPath().toString() + "/"
                + this.location);
    }

    private boolean checkFileExists(File file) {
        if (file.exists()) {
            return true;
        }

        try {
            return file.createNewFile();
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.SQLITE;
    }
}
