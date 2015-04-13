package io.ll.warden;

import org.bukkit.plugin.java.JavaPlugin;

public class WardenPlugin extends JavaPlugin {

    public static void main(String[] args) {
        System.out.println("Whoops! You can only run Warden as a Spigot plugin!");
        System.out.println("Please drop this file into your server's 'plugins' folder, and run the server!");
    }

    @Override
    public void onEnable() {
        new WardenMod(this);
    }
}
