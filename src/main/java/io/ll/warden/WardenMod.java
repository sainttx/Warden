package io.ll.warden;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import io.ll.warden.check.CheckManager;
import io.ll.warden.configuration.ConfigManager;
import io.ll.warden.heuristics.BanManager;
import io.ll.warden.storage.MySQL;
import io.ll.warden.storage.SQLite;
import io.ll.warden.storage.Storage;
import io.ll.warden.storage.StorageType;
import io.ll.warden.utils.BlockUtilities;
import io.ll.warden.utils.MovementHelper;
import io.ll.warden.utils.proxy.Warden;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

public class WardenMod {

    private final WardenPlugin pluginContainer;
    private final Logger logger;
    private final File dataFolder;
    private final FileConfiguration config;
    private final Storage storage;
    private final String prefix;

    private final ProtocolManager protocolManager;
    private final ConfigManager configManager;
    private final BanManager banManager;
    private final CheckManager checkManager;
    private final MovementHelper movementHelper;
    private final BlockUtilities blockUtilities;

    private final boolean hiddenMode;

    public WardenMod(WardenPlugin pluginContainer) {
        this.pluginContainer = pluginContainer;
        this.pluginContainer.saveDefaultConfig();
        this.logger = this.pluginContainer.getLogger();
        this.dataFolder = this.pluginContainer.getDataFolder();
        this.config = this.pluginContainer.getConfig();
        this.storage = this.determineStorage();
        this.prefix = this.determinePrefix();

        this.hiddenMode = this.config.getBoolean("HideWarden");

        this.logInfo("Getting Protocol Manager.");
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.logInfo("Checking the database connection.");
        this.runDatabaseCheck();

        this.logInfo("Initializing Managers...");
        this.configManager = new ConfigManager();

        this.banManager = new BanManager();
        this.banManager.setup(this.storage);

        this.checkManager = new CheckManager();

        this.movementHelper = new MovementHelper();
        this.movementHelper.setProtocolManager(this.protocolManager);

        this.blockUtilities = new BlockUtilities();
        this.blockUtilities.setup();

        this.logInfo("Done.");

        //TODO: Register Listeners
        //TODO: Register commands

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //TODO: Save/clean configurations and databases.
                //TODO: Force runnables to run one-more-time.
            }
        });

        /* Remember to set the Warden proxy instance at the VERY END! */
        Warden.setInstance(this);
    }

    private Storage determineStorage() {
        switch (StorageType.fromString(this.config.getString("DatabaseType"))) {
            case MYSQL:
                return new MySQL(this.config.getConfigurationSection("MySQL"));
            default:
                return new SQLite(this.config.getConfigurationSection("SQLite"));
        }
    }

    private String determinePrefix() {
        if (this.config.getBoolean("UseLongPrefix")) {
            return "[Warden AC] ";
        } else {
            return "[WAC] ";
        }
    }

    private void runDatabaseCheck() {
        if (this.storage.checkForWorkingConnection()) {
            this.logInfo("Connection to database was successful.");
        } else {
            this.logSevere(
                    "Connection to database FAILED! This is FATAL!",
                    "Using Database: " + this.storage.getStorageType().toString()
            );
        }
    }

    public WardenPlugin getPluginContainer() {
        return this.pluginContainer;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public File getDataFolder() {
        return this.dataFolder;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public Storage getStorage() {
        return this.storage;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public BanManager getBanManager() {
        return this.banManager;
    }

    public CheckManager getCheckManager() {
        return this.checkManager;
    }

    public MovementHelper getMovementHelper() {
        return this.movementHelper;
    }

    public BlockUtilities getBlockUtilities() {
        return this.blockUtilities;
    }

    public boolean isHiddenMode() {
        return this.hiddenMode;
    }

    public void logInfo(String... messages) {
        for (String message : messages) {
            this.logger.info(this.prefix + message);
        }
    }

    public void logWarning(String... messages) {
        for (String message : messages) {
            this.logger.warning(this.prefix + message);
        }
    }

    public void logSevere(String... messages) {
        for (String message : messages) {
            this.logger.severe(this.prefix + message);
        }
    }
}
