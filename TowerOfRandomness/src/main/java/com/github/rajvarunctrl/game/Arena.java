package com.github.rajvarunctrl.game;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.Task;
import com.github.rajvarunctrl.GameState;
import com.github.rajvarunctrl.utils.BlockSnapshot;

import java.util.*;

public class Arena {

    private final String name;
    private final String world;
    private final List<Location> soloSpawns = new ArrayList<>();
    private Location waitingLobby;
    private final Map<String,List<Location>> teamSpawns = new HashMap<>();

    private GameState state = GameState.WAITING;
    private final List<Player> players = new ArrayList<>();
    private int countdown = -1;
    private Task countdownTask;
    private final List<BlockSnapshot> changedBlocks = new ArrayList<>();

    public Arena(String name, String world, List<Location> soloSpawns) {
        this.name = name;
        this.world = world;
        this.soloSpawns.addAll(soloSpawns);
    }

    public String getName(){return name;}

    public String getWorld(){return world;}

    public List<Location> getSoloSpawns(){return soloSpawns;}

    public void addSoloSpawn(Location loc){soloSpawns.add(loc);}

    public Location getWaitingLobby(){return waitingLobby;}

    public void setWaitingLobby(Location loc){this.waitingLobby = loc;}

    public void addTeamSpawn(String team, Location loc) {
        teamSpawns.computeIfAbsent(team, k -> new ArrayList<>()).add(loc);
    }

    public List<Location> getTeamSpawns(String team) {
        return teamSpawns.getOrDefault(team, new ArrayList<>());
    }

    public GameState getState(){return state;}

    public void setState(GameState state){this.state = state;}

    public List<Player> getPlayers(){return players;}

    public int getCountdown(){return countdown;}

    public void setCountdown(int countdown){this.countdown = countdown;}

    public Task getCountdownTask() {
        return countdownTask;
    }

    public void setCountdownTask(Task task) {this.countdownTask=task;}

    public List<BlockSnapshot> getChangedBlocks(){return changedBlocks;}

    public void addChangedBlock(BlockSnapshot snapshot){changedBlocks.add(snapshot);}

    public void resetBlocks(){
        for(BlockSnapshot snapshot : changedBlocks){
            snapshot.restore();

        }
        changedBlocks.clear();

    }

    public void clearPlayers(){players.clear();}

    public void broadcast(String s){}

    private int gameTime = 0; // Time left in Seconds.
    private Task gameTask;

    public int getGameTime(){return gameTime;}
    public void setGameTime(int time){this.gameTime = time;}
    public Task getGameTask(){return gameTask;}
    public void setGameTask(Task gameTask){this.gameTask = gameTask;}


}
