package com.kitisplode.golemfirststonemod.util.golem_pattern;

import com.kitisplode.golemfirststonemod.entity.entity.golem.EntityPawn;
import com.kitisplode.golemfirststonemod.entity.entity.interfaces.IEntityDandoriFollower;
import com.kitisplode.golemfirststonemod.util.ExtraMath;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Predicate;

abstract public class AbstractGolemPattern
{
    protected ArrayList<BlockPattern> patternList = new ArrayList();
    protected Predicate<BlockState> spawnBlockPredicate;

    protected Vec3i spawnPositionOffset = new Vec3i(0,0,0);

    public AbstractGolemPattern(Predicate<BlockState> pPredicate)
    {
        spawnBlockPredicate = pPredicate;

        // Override to add patterns.
    }

    // Checks to see if the pattern is matched, and returns null if not, a PatternMatch otherwise.
    @Nullable
    public BlockPattern.Result CheckForPatternMatch(World pLevel, BlockPos pPos)
    {
        // Check to see if any of the patterns match.
        for (int i = 0; i < patternList.size(); i++)
        {
            BlockPattern currentPattern = patternList.get(i);
            // If somehow the current pattern is null, skip to the next pattern.
            if (currentPattern == null) continue;
            // Actually check the pattern now.
            BlockPattern.Result match = currentPattern.searchAround(pLevel, pPos);
            // If we got a match, return it.
            if (match != null) return match;
        }
        // If we didn't get any matches, return null.
        return null;
    }

    public ArrayList<Entity> SpawnGolem(World pLevel, BlockPattern.Result pPatternMatch, BlockPos pPos, Entity pPlayer)
    {
        clearPatternBlocks(pLevel, pPatternMatch);

        // Spawn the golems.
        ArrayList<Entity> pGolems = SpawnGolemForReal(pLevel, pPatternMatch, pPos);
        // Position the golems.
        for (Entity pGolem : pGolems)
        {
            if (pGolem != null)
            {
                BlockPos spawnPosition = pPatternMatch.translate(spawnPositionOffset.getX(),
                                spawnPositionOffset.getY(),
                                spawnPositionOffset.getZ())
                        .getBlockPos();
                positionGolem(pLevel,
                        spawnPosition,
                        (float) ExtraMath.getYawBetweenPoints(spawnPosition.toCenterPos(), pPlayer.getPos()) * MathHelper.DEGREES_PER_RADIAN,
                        pGolem);


                if (pGolem instanceof IEntityDandoriFollower && pPlayer instanceof LivingEntity)
                {
                    if (pGolem instanceof EntityPawn)
                    {
                        ((EntityPawn) pGolem).setOwnerType(EntityPawn.OWNER_TYPES.PLAYER.ordinal());
                    }
                    ((IEntityDandoriFollower) pGolem).setOwner((LivingEntity) pPlayer);
                }
            }
        }

        updatePatternBlocks(pLevel, pPatternMatch);
        return pGolems;
    }

    // Intended to be overridden to actually spawn the golem.
    protected abstract ArrayList<Entity> SpawnGolemForReal(World pLevel, BlockPattern.Result pPatternMatch, BlockPos pPos);

    private void positionGolem(World pLevel, BlockPos pPos, float pYaw, Entity pGolem)
    {
        if (pGolem == null) return;
        pGolem.refreshPositionAndAngles((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.05D, (double)pPos.getZ() + 0.5D, pYaw, 0.0F);
        pLevel.spawnEntity(pGolem);

        for(ServerPlayerEntity serverplayer : pLevel.getNonSpectatingEntities(ServerPlayerEntity.class, pGolem.getBoundingBox().expand(5.0D))) {
            Criteria.SUMMONED_ENTITY.trigger(serverplayer, pGolem);
        }
    }

    private void clearPatternBlocks(World pLevel, BlockPattern.Result pPatternMatch)
    {
        for(int i = 0; i < pPatternMatch.getWidth(); ++i) {
            for(int j = 0; j < pPatternMatch.getHeight(); ++j) {
                for(int k = 0; k < pPatternMatch.getDepth(); ++k)
                {
                    CachedBlockPosition blockinworld = pPatternMatch.translate(i, j, k);
                    pLevel.setBlockState(blockinworld.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    pLevel.syncWorldEvent(2001, blockinworld.getBlockPos(), Block.getRawIdFromState(blockinworld.getBlockState()));
                }
            }
        }
    }

    private void updatePatternBlocks(World pLevel, BlockPattern.Result pPatternMatch)
    {
        for(int i = 0; i < pPatternMatch.getWidth(); ++i) {
            for(int j = 0; j < pPatternMatch.getHeight(); ++j) {
                for(int k = 0; k < pPatternMatch.getDepth(); ++k) {
                    CachedBlockPosition blockinworld = pPatternMatch.translate(i, j, k);
                    pLevel.updateNeighbors(blockinworld.getBlockPos(), Blocks.AIR);
                }
            }
        }
    }
}