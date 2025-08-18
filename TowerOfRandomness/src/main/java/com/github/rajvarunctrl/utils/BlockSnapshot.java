package com.github.rajvarunctrl.utils;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;


public class BlockSnapshot {

    private final int x;
    private final int y;
    private final int z;
    private final String levelName;
    private final int blockId;
    private final int blockData;

    public BlockSnapshot(Position position,Block block) {
        this.x = position.getFloorX();
        this.y = position.getFloorY();
        this.z = position.getFloorZ();
        this.levelName = position.getLevel().getName();
        this.blockId = block.getId();
        this.blockData = block.getDamage();
    }

    public void restore(){
        Level level = cn.nukkit.Server.getInstance().getLevelByName(levelName);
        if(level == null){
            System.out.println("Level '"+levelName+"' not found, while restoring snapshot.");
            return;
        }

        Position pos = new Position(x,y,z,level);
        Block block = Block.get(blockId,blockData);
        level.setBlock(pos,block);
    }

    public Position getPosition(){
        Level level = cn.nukkit.Server.getInstance().getLevelByName(levelName);
        return new Position(x,y,z,level);
    }
}
