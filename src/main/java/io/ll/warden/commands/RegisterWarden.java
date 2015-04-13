package io.ll.warden.commands;

import io.ll.warden.utils.PasswordUtils;
import io.ll.warden.utils.UUIDFetcher;
import io.ll.warden.utils.proxy.Warden;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterWarden implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            //TODO: Check for Warden Account
            return true;
        }

        if (args.length != 3) {
            Warden.logWarning("Invalid number of arguments!");
            return false;
        }

        String username = args[0];
        String password = PasswordUtils.hash(args[1]);
        AuthAction.AuthLevel level = AuthAction.AuthLevel.valueOf(args[2]);

        if (level == null) {
            level = AuthAction.AuthLevel.USER;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(username);

        if (!player.hasPlayedBefore()) {
            this.finishAsynchronously(sender, username);
            return true;
        }

        return true;
    }

    private void finishAsynchronously(final CommandSender sender, final String player) {
        Bukkit.getScheduler().runTaskAsynchronously(Warden.getPluginContainer().get(), new Runnable() {
            @Override
            public void run() {
                UUIDFetcher fetcher = new UUIDFetcher(Collections.singletonList(player));
                Map<String, UUID> response = new HashMap<String, UUID>();
                try {
                    response = fetcher.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                UUID uuid = response.get(player);

                if (uuid == null) {
                    sender.sendMessage("Player doesn't exist.");
                }

                // TODO: Check if player has warden account.

            }
        });
    }
}
