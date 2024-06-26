package com.kitisplode.golemfirststonemod.entity.entity.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface IEntityCanAttackBlocks
{
    void setBlockTarget(BlockPos pBlockPos);
    BlockPos getBlockTarget();
    boolean canTargetBlock(BlockPos pBlockPos);
    default boolean tryAttackBlock() { return false; }

    Random getRandom();

    default void findNewTargetBlock()
    {
        BlockPos tempBp = this.getBlockTarget();
        this.setBlockTarget(null);
        for (int i = 0; i < 4; i++)
        {
            BlockPos bp = null;
            if (i == 0) bp = tempBp.add(1,0,0);
            else if (i == 1) bp = tempBp.add(-1,0,0);
            else if (i == 2) bp = tempBp.add(0,0,1);
            else             bp = tempBp.add(0,0,-1);
//            else if (i == 4) bp = tempBp.add(0,1, 0);
//            else             bp = tempBp.add(0,-1,0);
            if (bp == null) continue;
            if (canTargetBlock(bp))
            {
                if (this.getBlockTarget() == null || this.getRandom().nextInt(100) < 75) this.setBlockTarget(bp);
            }
        }
    }

    default int blockPreference(BlockState bs)
    {
        return 100;
    }
}
