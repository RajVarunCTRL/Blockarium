package com.github.rajvarunctrl;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;

public class Fly extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this); // Register events
        getLogger().info("Fly plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Fly plugin disabled!");
    }
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command is only for players.");
            return true;
        }

        Player player = (Player) sender;

        if (player.getGamemode() == Player.SURVIVAL || player.getGamemode() == Player.ADVENTURE) {
            boolean flight = player.getAllowFlight();

            if (flight) {
                player.setAllowFlight(false);
                player.sendMessage("§cFly disabled.");
            } else {
                player.setAllowFlight(true);

                player.sendMessage("§aFly enabled.");
            }
        } else {
            player.sendMessage("§eYou're already in a mode that allows flying.");
        }

        return true;
    }
}
