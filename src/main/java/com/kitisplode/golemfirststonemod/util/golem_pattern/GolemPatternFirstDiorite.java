package com.kitisplode.golemfirststonemod.util.golem_pattern;

import com.kitisplode.golemfirststonemod.block.ModBlocks;
import com.kitisplode.golemfirststonemod.entity.ModEntities;
import com.kitisplode.golemfirststonemod.entity.entity.golem.first.EntityGolemFirstDiorite;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Predicate;

public class GolemPatternFirstDiorite extends AbstractGolemPattern
{
	private static final Predicate<BlockState> buildingBlockPredicate = state -> state != null
		&& (state.isOf(Blocks.DIORITE)
		|| state.isOf(Blocks.POLISHED_DIORITE));

	private static final Predicate<BlockState> airPredicate = state -> state != null
			&& (state.isAir()
			|| !state.isOpaque()
			|| state.isOf(Blocks.SNOW)
	);

	private static final Predicate<BlockState> corePredicate = state -> state != null
			&& (state.isOf(ModBlocks.BLOCK_CORE_DIORITE)
	);

	public GolemPatternFirstDiorite(Predicate<BlockState> pPredicate)
	{
		super(pPredicate);
		spawnPositionOffset = new Vec3i(1,2,1);
		patternList.add(BlockPatternBuilder.start()
			.aisle(
				"###",
				"#^#",
				"###",
				"~~~"
			)
			.aisle(
				"###",
				"#*#",
				"###",
				"#~#"
			)
			.aisle(
				"###",
				"###",
				"###",
				"~~~"
			)
			.where('^', CachedBlockPosition.matchesBlockState(spawnBlockPredicate))
			.where('#', CachedBlockPosition.matchesBlockState(buildingBlockPredicate))
			.where('~', CachedBlockPosition.matchesBlockState(airPredicate))
			.where('*', CachedBlockPosition.matchesBlockState(corePredicate))
			.build());
		// Mirrored version
		patternList.add(BlockPatternBuilder.start()
			.aisle(
				"###",
				"###",
				"###",
				"~#~"
			)
			.aisle(
				"###",
				"^*#",
				"###",
				"~~~"
			)
			.aisle(
				"###",
				"###",
				"###",
				"~#~"
			)
			.where('^', CachedBlockPosition.matchesBlockState(spawnBlockPredicate))
			.where('#', CachedBlockPosition.matchesBlockState(buildingBlockPredicate))
			.where('~', CachedBlockPosition.matchesBlockState(airPredicate))
			.where('*', CachedBlockPosition.matchesBlockState(corePredicate))
			.build());
	}

	@Override
	protected ArrayList<Entity> SpawnGolemForReal(World pLevel, BlockPattern.Result pPatternMatch, BlockPos pPos)
	{
		ArrayList<Entity> golems = new ArrayList<>();
		EntityGolemFirstDiorite golem = ModEntities.ENTITY_GOLEM_FIRST_DIORITE.create(pLevel);
		if (golem != null)
		{
			golem.setPlayerCreated(true);
			golems.add(golem);
		}
		return golems;
	}
}
