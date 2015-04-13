package io.ll.warden.utils.proxy;

import com.comphenix.protocol.ProtocolManager;
import com.google.common.base.Optional;
import io.ll.warden.WardenMod;
import io.ll.warden.WardenPlugin;
import io.ll.warden.check.CheckManager;
import io.ll.warden.configuration.ConfigManager;
import io.ll.warden.heuristics.BanManager;
import io.ll.warden.storage.Storage;
import io.ll.warden.utils.BlockUtilities;
import io.ll.warden.utils.MovementHelper;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

public final class Warden {

    private static WardenMod instance;

    public static void setInstance(WardenMod warden) {
        if (Warden.instance != null) {
            throw new UnsupportedOperationException("Can only define WardenMod singleton once.");
        }
        Warden.instance = warden;
    }

    public static Optional<WardenMod> getInstance() {
        if (Warden.instance == null) {
            throw new UnsupportedOperationException("Attempted to access WardenMod singleton before it was initialized");
        }
        return Optional.of(Warden.instance);
    }

    public static Optional<WardenPlugin> getPluginContainer() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getPluginContainer());
        }
        return Optional.absent();
    }

    public static Optional<Logger> getLogger() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getLogger());
        }
        return Optional.absent();
    }

    public static Optional<File> getDataFolder() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getDataFolder());
        }
        return Optional.absent();
    }

    public static Optional<FileConfiguration> getConfig() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getConfig());
        }
        return Optional.absent();
    }

    public static Optional<Storage> getStorage() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getStorage());
        }
        return Optional.absent();
    }

    public static Optional<String> getPrefix() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getPrefix());
        }
        return Optional.absent();
    }

    public static Optional<ProtocolManager> getProtocolManager() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getProtocolManager());
        }
        return Optional.absent();
    }

    public static Optional<ConfigManager> getConfigManager() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getConfigManager());
        }
        return Optional.absent();
    }

    public static Optional<BanManager> getBanManager() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getBanManager());
        }
        return Optional.absent();
    }

    public static Optional<CheckManager> getCheckManager() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getCheckManager());
        }
        return Optional.absent();
    }

    public static Optional<MovementHelper> getMovementHelper() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getMovementHelper());
        }
        return Optional.absent();
    }

    public static Optional<BlockUtilities> getBlockUtilities() {
        if (Warden.getInstance().isPresent()) {
            return Optional.of(Warden.instance.getBlockUtilities());
        }
        return Optional.absent();
    }

    public static void logInfo(String... messages) {
        if (Warden.getInstance().isPresent()) {
            Warden.instance.logInfo(messages);
        }
    }

    public static void logWarning(String... messages) {
        if (Warden.getInstance().isPresent()) {
            Warden.instance.logWarning(messages);
        }
    }

    public static void logSevere(String... messages) {
        if (Warden.getInstance().isPresent()) {
            Warden.instance.logSevere(messages);
        }
    }
}
