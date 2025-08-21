package com.github.rajvarunctrl.game;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.inventory.transaction.action.InventoryAction;

import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import com.github.rajvarunctrl.GameState;
import com.github.rajvarunctrl.TowerOfRandomness;

import com.github.rajvarunctrl.game.RunningGame;

import java.io.File;
import java.util.*;


public class GameManager implements Listener {

    private final TowerOfRandomness plugin;
    private final Map<String,Arena> arenas = new HashMap<>();
    private final Map<Arena,Set<UUID>> frozenPlayers = new HashMap<>();
    private final RunningGame runningGame;

    public GameManager(TowerOfRandomness plugin, RunningGame runningGame) {
        this.plugin = plugin;
        this.runningGame = runningGame;
        loadArena();
    }
    public boolean joinArena(Player player, String arenaName) {
        Arena arena = arenas.get(arenaName);
        if(arena == null){
            player.sendMessage("§cArena '" + arenaName + "' not found.");
            return false;
        }

        if(arena.getWaitingLobby() == null){
            player.sendMessage("§cThis arena doesn't have a waiting lobby set yet.");
            return false;
        }
        if (arena.getPlayers().contains(player)) {
            player.sendMessage("§cYou're already in this arena!");
            return false;
        }
        arena.getPlayers().add(player);
        player.teleport(arena.getWaitingLobby());
        player.sendMessage("§aYou have joined the queue for " + arena.getName());

        for(Player p: arena.getPlayers()){
            p.sendMessage("§e" + player.getName() + " has joined! §7[" + arena.getPlayers().size() + "/12]");
        }

        player.getInventory().clearAll();
        Item nether_star = Item.get(Item.NETHER_STAR);
        nether_star.setCustomName("§bChoose Map §7[Coming Soon]");
        Item blaze_powder = Item.get(Item.BLAZE_POWDER);
        blaze_powder.setCustomName("§bCosmetics §7[Coming Soon]");
        Item dragon_breath = Item.get(Item.DRAGON_BREATH);
        dragon_breath.setCustomName("§cLeave Game §7[Use]");
        player.getInventory().setItem(0, nether_star);
        player.getInventory().setItem(4, blaze_powder);
        player.getInventory().setItem(8, dragon_breath);
        player.getInventory().sendContents(player);

        if (arena.getPlayers().size() >= 2 && arena.getState() == GameState.WAITING) {
            startCountdown(arena);
        }

        return True;
    }
    public void leaveGame(Player player) {
        Arena arena = getArenaByPlayer(player);
        if (arena != null) {
            arena.getPlayers().remove(player);
            player.sendMessage("§cYou have left the queue.");
            for (Player p : arena.getPlayers()) {
                p.sendMessage("§e" + player.getName() + " has left! §7[" + arena.getPlayers().size() + "/12]");
            }
            player.teleport(plugin.getServer().getDefaultLevel().getSafeSpawn());
            runningGame.clearScoreboard(player);
        }
    }

    public void startCountdown(Arena arena) {
        arena.setState(GameState.COUNTDOWN);
        arena.setCountdown(15);

        for(Player p: arena.getPlayers()){
            p.sendMessage("§aGame starting in 15 seconds...");

        }

        Task task = new Task() {
            @Override
            public void onRun(int i) {
                int time = arena.getCountdown();
                if(time==10){
                    List<Location> spawns = arena.getSoloSpawns();
                    List<Player> players = arena.getPlayers();

                    for(int i=0;i<players.size();i++){
                        players.get(i).teleport(spawns.get(i%spawns.size()));
                    }
                    frozenPlayers.putIfAbsent(arena,new HashSet<>());
                    for(Player p: players){
                        p.setGamemode(Player.SURVIVAL);
                        frozenPlayers.get(arena).add(p.getUniqueId());
                    }
                }
                if(time<=5 && time>0){
                    for(Player p: arena.getPlayers()){
                        p.sendTitle("§l§e" + time, "");
                        p.getLevel().addSound(p, Sound.NOTE_BELL);
                        p.getInventory().clearAll();
                    }
                }

                if (time==0){
                    for(Player p: arena.getPlayers()){
                        p.sendTitle("§aGO!", "");
                        if(frozenPlayers.containsKey(arena)){
                            frozenPlayers.get(arena).add(p.getUniqueId());
                        }
                        p.sendMessage("\n§7§l» §r§bTower of Randomness: §r§6Build your tower and fight!\n");
                        p.getLevel().addSound(p, Sound.MOB_ENDERDRAGON_GROWL);
                    }
                    arena.setState(GameState.IN_GAME);
                    startGameLoop(arena);
                    this.getHandler().cancel();
                } arena.setCountdown(time-1);
            }
        };

        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin,task,20);
        arena.setCountdownTask(task);
    }


    private void loadArena() {}

}
