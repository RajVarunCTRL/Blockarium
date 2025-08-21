package com.github.rajvarunctrl.game;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;
import com.github.rajvarunctrl.TowerOfRandomness;
import com.github.rajvarunctrl.GameState;
import cn.nukkit.scheduler.Task;
import com.github.rajvarunctrl.RunningGame;
import com.github.rajvarunctrl.utils.BlockSnapshot;

import java.io.File;
import java.util.*;

public class GameManager implements Listener {

    private final TowerOfRandomness plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<Arena, UUID> frozenPlayers = new HashMap<>();

    private final RunningGame runningGame;

    public GameManager(TowerOfRandomness plugin, RunningGame runninggame) {}

}
