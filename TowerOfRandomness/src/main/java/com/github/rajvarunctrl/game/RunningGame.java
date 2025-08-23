package com.github.rajvarunctrl.game;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;
import com.github.rajvarunctrl.GameState;
import com.github.rajvarunctrl.TowerOfRandomness;


import java.util.*;

public class RunningGame {
    private final TowerOfRandomness plugin;
    private GameManager gameManager;
    private Random random = new Random();

    // maps to store the running tasks of each indv arena.
    private final Map<Arena, TaskHandler> blockTasks = new HashMap<>();
    private final Map<Arena, TaskHandler> weaponTasks = new HashMap<>();

    private final List<Item> randomBlocks = new ArrayList<>();
    private final List<Item> randomWeapons = new ArrayList<>();

    public RunningGame(TowerOfRandomness plugin) {
        this.plugin = plugin;
        loadItemPools();
    }

    public void setGameManager(GameManager gameManager){
        this.gameManager = gameManager;
    }

    public void startArenaTasks(Arena arena){
        stopArenaTasks(arena);

        TaskHandler blockTask = plugin.getServer().getScheduler().scheduleRepeatingTask(
                plugin, () -> giveRandomBlocks(arena), 5*20

        );
        TaskHandler weaponTask = plugin.getServer().getScheduler().scheduleRepeatingTask(
                plugin, () -> giveRandomWeapons(arena), 30*20
        );

        blockTasks.put(arena,blockTask);
        weaponTasks.put(arena,weaponTask);
    }

    public void stopArenaTasks(Arena arena) {
        if(blockTasks.containsKey(arena)) {
            blockTasks.get(arena).cancel();
            blockTasks.remove(arena);
        }
        if(weaponTasks.containsKey(arena)) {
            weaponTasks.get(arena).cancel();
            weaponTasks.remove(arena);
        }
    }

    public void giveRandomWeapons(Arena arena) {
        if(arena.getState() != GameState.IN_GAME) return;

        for (Player player: arena.getPlayers()) {
            if(player.isOnline() && player.getGamemode() == Player.SURVIVAL){
                Item weaponToGive = randomWeapons.get(random.nextInt(randomWeapons.size())).clone();
                player.getInventory().addItem(weaponToGive);
                player.sendMessage(TextFormat.AQUA+"You received a supply drop: "+ TextFormat.BOLD + weaponToGive.getName()); // Not needed remove (just for testing_

            }
        }
    }

    public void giveRandomBlocks(Arena arena) {
        if(arena.getState() != GameState.IN_GAME) return;

        for(Player player: arena.getPlayers()) {
            if(player.isOnline() && player.getGamemode() == Player.SURVIVAL){
                Item itemToGive = randomBlocks.get(random.nextInt(randomBlocks.size())).clone();
                itemToGive.setCount(8);
                player.getInventory().addItem(itemToGive);
                player.sendTip(TextFormat.GOLD + "+ " + itemToGive.getCount() + " " + itemToGive.getName()); // Not needed remove (just for testing_

            }
        }
    }

    public void clearScoreboard(Player player){
        // Add logic PANDA DO IT..
    }

    public void loadItemPools() {
        randomBlocks.add(Item.get(Item.STONE,0,1));
        randomBlocks.add(Item.get(Item.WOODEN_PLANKS,0,1));
        randomBlocks.add(Item.get(Item.COBBLESTONE,0,1));
        randomBlocks.add(Item.get(Item.DIRT,0,1));
        randomBlocks.add(Item.get(Item.WOOL,0,1));
        randomBlocks.add(Item.get(Item.LADDER,0,1));
        randomBlocks.add(Item.get(Item.TNT,0,1));


        randomWeapons.add(Item.get(Item.IRON_SWORD,0,1));
        randomWeapons.add(Item.get(Item.DIAMOND_AXE,0,1));
        randomWeapons.add(Item.get(Item.BOW,0,1));
        randomWeapons.add(Item.get(Item.ARROW,0,5));
        randomWeapons.add(Item.get(Item.SHIELD,0,1));
        randomWeapons.add(Item.get(Item.CROSSBOW,0,1));
        randomWeapons.add(Item.get(Item.SNOWBALL,0,16));
        randomWeapons.add(Item.get(Item.GOLDEN_APPLE,0,1));

    }
}


