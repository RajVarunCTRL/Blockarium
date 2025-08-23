package com.github.rajvarunctrl;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.github.rajvarunctrl.game.Arena;
import com.github.rajvarunctrl.game.GameManager;


public class GameListener implements Listener {
    private final GameManager gameManager;
    private final TowerOfRandomness plugin;

    public GameListener(TowerOfRandomness plugin){
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
    }

    @EventHandler
    public void onPlayerDeath(cn.nukkit.event.player.PlayerDeathEvent event){
        Player player = event.getEntity();
        Arena arena = gameManager.getArenaByPlayer(player);

        if(arena!=null && arena.getState() == GameState.IN_GAME){
            arena.getPlayers().remove(player);
            player.setGamemode(Player.SPECTATOR);

            // announce a death message
            for(Player p: arena.getPlayers()){
                p.sendMessage(TextFormat.RED + player.getName() + " was eliminated! "
                        + TextFormat.AQUA + "(" + gameManager.getAlivePlayers(arena).size() + " players left)");
            }

            event.setDrops(player.getInventory().getContents().values().toArray(new Item[0]));
            player.getInventory().clearAll();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Check if the player was in a game when they died
        if (gameManager.wasPlayerInGame(player)) {
            // It's much more reliable to set spectator mode AFTER they respawn
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                player.setGamemode(Player.SPECTATOR);
                player.sendMessage(TextFormat.GRAY + "You are now spectating.");
            }, 5); // 5 ticks delay to ensure the respawn is complete
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = gameManager.getArenaByPlayer(player);

        if (arena != null) {
            // Automatically handle leaving the game if they disconnect
            gameManager.leaveGame(player);
        }
    }

    @EventHandler
    public void onInteractwithHotbarItems(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Item item = event.getItem();
        Arena arena = gameManager.getArenaByPlayer(player);
        if(arena==null || arena.getState() != GameState.WAITING) return;

        if(item!=null){
            if(item.getId() == Item.DRAGON_BREATH){
                gameManager.leaveGame(player);
                event.setCancelled(true);
            } else if (item.getId() == Item.NETHER_STAR||item.getId()==Item.BLAZE_POWDER) {
                player.sendMessage("§eComing Soon!");
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onInventoryTransaction(InventoryTransactionEvent event) {
        Player player = event.getTransaction().getSource();
        Arena arena = gameManager.getArenaByPlayer(player);
        // Prevent moving items in the waiting lobby
        if (arena != null && arena.getState() == GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = gameManager.getArenaByPlayer(player);
        // Prevent dropping items in the waiting lobby
        if (arena != null && arena.getState() == GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    // In-game events
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        if(gameManager.isPlayerFrozen(player)){
            if(event.getFrom().getFloorX() != event.getTo().getFloorX() || event.getFrom().getFloorZ() != event.getTo().getFloorZ()){
                player.teleport(event.getFrom());
            }
        }
    }


}
