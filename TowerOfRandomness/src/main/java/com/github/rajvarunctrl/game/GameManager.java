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
import cn.nukkit.utils.TextFormat;

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
    private final Set<UUID> recentlyEliminated = new HashSet<>();

    public GameManager(TowerOfRandomness plugin, RunningGame runningGame) {
        this.plugin = plugin;
        this.runningGame = runningGame;
        loadArenas();
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
        player.setGamemode(Player.ADVENTURE);
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

        return true;
    }

    public void leaveGame(Player player) {
        Arena arena = getArenaByPlayer(player);
        if (arena != null) {
            arena.getPlayers().remove(player);
            player.getInventory().clearAll();
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

                    for (int j=0;j<players.size();j++){
                        players.get(j).teleport(spawns.get(j % spawns.size()));
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
                            frozenPlayers.get(arena).remove((p.getUniqueId())); // Fixed players not getting unfrozen.
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

    public void startGameLoop(Arena arena) {
        arena.setState(GameState.IN_GAME);
        arena.setGameTime(600);
        runningGame.startArenaTasks(arena);

        Task task = new Task() {
            @Override
            public void onRun(int currentTick) {
                // Win Condition.
                List<Player> alive = getAlivePlayers(arena);

                if(alive.size()<=1){
                    endGame(arena);
                    this.getHandler().cancel();
                    return;
                }
                int time = arena.getGameTime();
                if(time<=0){
                    plugin.getServer().broadcastMessage("§cGame Over! Time limit reached in arena " + arena.getName());
                    endGame(arena);
                    this.getHandler().cancel();
                    return;
                }
                arena.setGameTime(time-1);
            }
        };
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, task,20);
        arena.setGameTask(task);
    }

    public void endGame(Arena arena) {
        runningGame.stopArenaTasks(arena);
        arena.setState(GameState.FINISHED);

        List<Player> players = new ArrayList<>(arena.getPlayers());
        if(getAlivePlayers(arena).size() == 1){
            Player winner = getAlivePlayers(arena).get(0);
            plugin.getServer().broadcastMessage("\n§6[TOR] §e" + winner.getName() + " has won in arena '" + arena.getName() + "'!\n");
        }

        for (Player player: players) {
            runningGame.clearScoreboard(player);
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin,() ->{
               player.teleport(plugin.getServer().getDefaultLevel().getSafeSpawn());
               player.getInventory().clearAll();
               player.sendMessage("§aGame over! Returning to lobby...");
            }, 5*20);
        }
        arena.resetBlocks();
        arena.clearPlayers();
        arena.setState(GameState.WAITING);
    }

    private void loadArenas() {
        Config config = new Config(new File(plugin.getDataFolder(), "arenas.yml"), Config.YAML);
        Map<String, Object> arenasSection = (Map<String, Object>) config.get("arenas");

        if (arenasSection != null) {
            arenas.clear();
            for(Map.Entry<String,Object> entry: arenasSection.entrySet()) {
                String arenaName = entry.getKey();
                Map<String,Object> arenaData = (Map<String, Object>) entry.getValue();
                String world = (String) arenaData.get("world");
                cn.nukkit.level.Level level = plugin.getServer().getLevelByName(world);
                if(level==null){
                    plugin.getLogger().warning("World '" + world + "' for arena '" + arenaName + "' is not loaded.");
                    continue;
                }

                List<Map<String,Object>> spawnList = (List<Map<String,Object>>) arenaData.get("spawns");
                List<Location> spawns = new ArrayList<>();
                if(spawnList!=null){
                    for(Map<String,Object> spawnData : spawnList){
                        spawns.add(new Location(
                                ((Number) spawnData.get("x")).doubleValue(),
                                ((Number) spawnData.get("y")).doubleValue(),
                                ((Number) spawnData.get("z")).doubleValue(),
                                spawnData.containsKey("yaw") ? ((Number) spawnData.get("yaw")).floatValue() : 0,
                                spawnData.containsKey("pitch") ? ((Number) spawnData.get("pitch")).floatValue() : 0,
                                level));
                    }
                }
                Location waitingLobby = null;
                Map<String, Object> waitingLobbyMap = (Map<String,Object>) arenaData.get("waitingLobby");
                if (waitingLobbyMap != null) {
                    waitingLobby = new Location(((Number) waitingLobbyMap.get("x")).doubleValue(),
                            ((Number) waitingLobbyMap.get("y")).doubleValue(),
                            ((Number) waitingLobbyMap.get("z")).doubleValue(), 0, 0, level);
                }
                Arena arena = new Arena(arenaName, world, spawns);
                arena.setWaitingLobby(waitingLobby);
                arenas.put(arenaName, arena);
                plugin.getLogger().info("Loaded arena " + arenaName + " with " + spawns.size() + " spawns.");
            }
        } else {
            plugin.getLogger().warning("No arenas defined in arenas.yml");
        }
    }

    public void setArenaSpawn(Player player, String arenaName){
        File file = new File(plugin.getDataFolder(), "arenas.yml");
        Config config = new Config(file, Config.YAML);
        Location loc = player.getLocation();
        Map<String, Object> arenasSection = config.get("arenas", new LinkedHashMap<>());
        Map<String, Object> arenaData = (Map<String, Object>) arenasSection.getOrDefault(arenaName, new LinkedHashMap<>());
        arenaData.put("world", loc.getLevel().getName());
        List<Map<String, Object>> spawns = (List<Map<String, Object>>) arenaData.getOrDefault("spawns", new ArrayList<>());
        Map<String, Object> spawnPoint = new LinkedHashMap<>();
        spawnPoint.put("x", loc.getX());
        spawnPoint.put("y", loc.getY());
        spawnPoint.put("z", loc.getZ());
        spawnPoint.put("yaw", loc.getYaw());
        spawnPoint.put("pitch", loc.getPitch());
        spawns.add(spawnPoint);
        arenaData.put("spawns", spawns);
        arenasSection.put(arenaName, arenaData);
        config.set("arenas", arenasSection);
        config.save();
        loadArenas();
        player.sendMessage("§aSpawn point saved for arena '" + arenaName + "'.");
    }

    public void setWaitingLobby(Player player, String arenaName){
        File file = new File(plugin.getDataFolder(), "arenas.yml");
        Config config = new Config(file, Config.YAML);
        Position pos = player.getPosition();
        Map<String, Object> arenasSection = config.get("arenas", new LinkedHashMap<>());
        Map<String, Object> arenaData = (Map<String, Object>) arenasSection.getOrDefault(arenaName, new LinkedHashMap<>());
        arenaData.put("world", player.getLevel().getName());
        Map<String, Object> waitingPos = new LinkedHashMap<>();
        waitingPos.put("x", pos.getX());
        waitingPos.put("y", pos.getY());
        waitingPos.put("z", pos.getZ());
        arenaData.put("waitingLobby", waitingPos);
        arenasSection.put(arenaName, arenaData);
        config.set("arenas", arenasSection);
        config.save();
        loadArenas();
        player.sendMessage("§aWaiting lobby set for arena '" + arenaName + "'.");
    }

    public boolean isPlayerFrozen(Player player){
        Arena arena = getArenaByPlayer(player);
        if(arena==null){
            return false;
        }
        return frozenPlayers.containsKey(arena) && frozenPlayers.get(arena).contains(player.getUniqueId());
    }

    public boolean wasPlayerInGame(Player player){
        return recentlyEliminated.contains(player.getUniqueId());
    }

    public void addEliminatedPlayer(UUID uuid){
        recentlyEliminated.add(uuid);
    }

    public void removeEliminatedPlayer(UUID uuid){
        recentlyEliminated.remove(uuid);
    }
    // Utility Methods get methods ig

    public Arena getArenaByPlayer(Player player){
        for(Arena arena: arenas.values()){
            if(arena.getPlayers().contains(player)){
                return arena;
            }
        }
        return null;
    }

    public Arena getArena(String name){
        return arenas.get(name);

    }

    public List<Player> getAlivePlayers(Arena arena){
        List<Player> alive = new ArrayList<>();
        for(Player player: arena.getPlayers()){
            if(player.isOnline() && player.getGamemode() != Player.SPECTATOR){
                alive.add(player);
            }

        }
        return alive;
    }
}
