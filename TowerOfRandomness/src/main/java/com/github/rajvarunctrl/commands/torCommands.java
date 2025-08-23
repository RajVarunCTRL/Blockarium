package com.github.rajvarunctrl.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
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
                if(args.length < 2){
                    player.sendMessage(TextFormat.RED + "Usage: /tor join <arena>");
                }
                String arenaToJoin = args[1];
                boolean success =  gameManager.joinArena(player, arenaToJoin);
                if(success){
                    player.sendMessage(TextFormat.GREEN + "You have joined ToR Arena `"+ arenaToJoin+ "`.");
                }

                break;

            case "setspawn":
                if(args.length < 2){
                    player.sendMessage(TextFormat.RED + "Usage: /tor setspawn <arena>");
                }
                String spawnArena = args[1];
                gameManager.setArenaSpawn(player, spawnArena);
                break;

            case "setwaiting":
                if(args.length < 2){
                    player.sendMessage(TextFormat.RED + "Usage: /tor setwaiting <arena>");
                }
                String waitingArena = args[1];
                gameManager.setWaitingLobby(player, waitingArena);
                break;

            case "startgame":
                if(args.length < 2){
                    player.sendMessage(TextFormat.RED + "Usage: /tor startgame <arena>");
                    return true;
                }

                String arenaName = args[1];
                Arena selectedArena = gameManager.getArena(arenaName);

                if(selectedArena == null){
                    player.sendMessage(TextFormat.DARK_PURPLE + "Arena "+ arenaName+ " not found!");
                    return true;
                }

                if (selectedArena.getState() == null || selectedArena.getState() != GameState.WAITING) {
                    player.sendMessage(TextFormat.RED +"Arena " + arenaName +" state not set or already in progress.");
                    return true;
                }

                if(selectedArena.getPlayers().isEmpty()){
                    player.sendMessage(TextFormat.RED+"No players in the arena to start game.");
                }
                gameManager.startCountdown(selectedArena);
                player.sendMessage("§aStarting countdown manually for arena: §e" + arenaName);
                break;
            case "help":
                infoSend(player);
                break;
            case "?":
                infoSend(player);
                break;
            default:
                infoSend(player);
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

