package io.ll.warden.storage;

public enum StorageType {
    MYSQL("MySQL"),
    SQLITE("SQLite");

    private final String name;

    StorageType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static StorageType fromString(String name) {
        for (StorageType type : StorageType.values()) {
            if (type.toString().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return SQLITE;
    }
}
