package com.github.rajvarunctrl.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.github.rajvarunctrl.GameState;
import com.github.rajvarunctrl.game.Arena;
import com.github.rajvarunctrl.game.GameManager;


public class torCommands extends Command {

    private final GameManager gameManager;

    public torCommands(GameManager gameManager) {
        super("tor", "Tower of Randomness command", "/tor <subcommand>");
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be used in-game only.");
            return false;
        }

        Player player = (Player) sender;

        if(args.length==0){
            infoSend(player);
            return true;
        }

        switch(args[0].toLowerCase()){
            case "join":
                break;
            case "setspawn":
                break;
            case "setwaiting":
                break;
            case "startgame":
                break;
            default:
                break;
        }

        return true;
    }

    private void  infoSend(Player player){
        String command = "§e/tor";
        player.sendMessage(command+" join <arena> §7- Join an arena.");
        player.sendMessage(command+" setspawn <arena> §7- Set solo spawn point for arena.");
        player.sendMessage(command+" setwaiting <arena> §7- Set waiting lobby location.");
        player.sendMessage(command+" startgame <arena> §7- Force start an arena.");
    }
}

