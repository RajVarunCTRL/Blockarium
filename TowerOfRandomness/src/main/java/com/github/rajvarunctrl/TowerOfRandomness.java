package com.github.rajvarunctrl;


import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.github.rajvarunctrl.commands.torCommands;
import com.github.rajvarunctrl.game.GameManager;
import com.github.rajvarunctrl.game.RunningGame;

import java.io.File;
import java.util.*;
import java.io.IOException;

public class TowerOfRandomness extends PluginBase {

    private GameManager gameManager;
    private RunningGame runningGame;

    @Override
    public void onEnable() {
        getLogger().info("✅ TOR >> Tower of Randomness plugin enabled.");

        // 1. Default Config
        createArenaConfig();

        // 2. Init game classess are correct, order etc.
        this.runningGame = new RunningGame(this);

        //3. create a Gamemanager.
        this.gameManager = new GameManager(this,this.runningGame);

        //3 Register dedi GameListener
        this.getServer().getPluginManager().registerEvents(new GameListener(this),this);

        //4 registering command handler
        this.getServer().getCommandMap().register("tor", new torCommands(this.gameManager));

        Level level = getServer().getDefaultLevel();
        if(level!=null){
            level.setTime(6000);
            level.stopTime();
            level.setRaining(false);
            level.setThundering(false);
        }
    }

    @Override
    public void onDisable() {getLogger().info("❌ TOR >> Tower of Randomness plugin disabled.");}

    public GameManager getGameManager() {return gameManager;}

    public RunningGame getRunningGame() {return runningGame;}

    private void createArenaConfig() {
        File file = new File(getDataFolder(), "arenas.yml");

        if(!file.exists()) {
            if(!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            try{
                file.createNewFile();
                LinkedHashMap<String, Object> data = new LinkedHashMap<>();

                // Sample arena ig, code from Panda.
                Map<String, Object> arena = new LinkedHashMap<>();
                arena.put("world", "arena");

                List<Map<String,Object>> spawns = new ArrayList<>();
                Map<String,Object> spawn1 = new LinkedHashMap<>();
                spawn1.put("x",100);
                spawn1.put("y",70);
                spawn1.put("z",100);

                Map<String,Object> spawn2 = new LinkedHashMap<>();
                spawn2.put("x",100);
                spawn2.put("y",70);
                spawn2.put("z",100);

                spawns.add(spawn1);
                spawns.add(spawn2);

                arena.put("spawns", spawns);

                Map<String, Object> arenas = new LinkedHashMap<>();
                arenas.put("arena1",arena);
                data.put("arenas", arenas);

                Config config = new Config(file, Config.YAML);
                config.setAll(data);
                config.save();
                getLogger().info("✅ TOR >> Created arenas.yml with default arena.");


            } catch (IOException e) {
                getLogger().error("❌ TOR>> Error occurred, failed to create arenas.yml", e);
            }
        }
    }
}
