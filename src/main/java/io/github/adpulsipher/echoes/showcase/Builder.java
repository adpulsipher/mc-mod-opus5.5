package io.github.adpulsipher.echoes.showcase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Places blocks relative to an origin on the ground. y = 0 is the floor, y = 1 the first block of open air.
 */
final class Builder {
	private static final int FLAGS = Block.UPDATE_CLIENTS;

	final ServerLevel level;
	final BlockPos origin;
	final RandomSource random;
	private final List<BlockPos> connectables = new ArrayList<>();

	Builder(ServerLevel level, BlockPos origin, long seed) {
		this.level = level;
		this.origin = origin;
		this.random = RandomSource.create(seed);
	}

	BlockPos at(int x, int y, int z) {
		return origin.offset(x, y, z);
	}

	void set(int x, int y, int z, BlockState state) {
		BlockPos pos = at(x, y, z);
		if (!level.isInWorldBounds(pos)) {
			return;
		}
		level.setBlock(pos, state, FLAGS);
		if (state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.is(Blocks.IRON_BARS) || state.is(Blocks.GLASS_PANE)) {
			connectables.add(pos);
		}
	}

	void set(int x, int y, int z, Block block) {
		set(x, y, z, block.defaultBlockState());
	}

	void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
		for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
			for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
				for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
					set(x, y, z, state);
				}
			}
		}
	}

	void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
		fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
	}

	void fill(int x0, int y0, int z0, int x1, int y1, int z1, Supplier<BlockState> palette) {
		for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
			for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
				for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
					set(x, y, z, palette.get());
				}
			}
		}
	}

	/** Chooses randomly among states, weighted by repetition. */
	Supplier<BlockState> mix(Block... blocks) {
		return () -> blocks[random.nextInt(blocks.length)].defaultBlockState();
	}

	/**
	 * Levels a site: clears the air above, lays the floor and shores up the ground below so the build never floats.
	 * {@code round} makes the site a disc instead of a square.
	 */
	void clearSite(int rx, int rz, int height, boolean round, Supplier<BlockState> floor, Supplier<BlockState> foundation) {
		for (int x = -rx; x <= rx; x++) {
			for (int z = -rz; z <= rz; z++) {
				if (round && x * x + z * z > rx * rx + rx) {
					continue;
				}
				for (int y = 1; y <= height; y++) {
					set(x, y, z, Blocks.AIR.defaultBlockState());
				}
				set(x, 0, z, floor.get());
				for (int y = -1; y >= -8; y--) {
					BlockState below = level.getBlockState(at(x, y, z));
					if (!below.isAir() && below.getFluidState().isEmpty() && !below.canBeReplaced()) {
						break;
					}
					set(x, y, z, foundation.get());
				}
			}
		}
	}

	/** Looks a vanilla block up by id; used for the dyed blocks, which are not plain constants. */
	static Block byId(String id) {
		return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(id));
	}

	/** Stairs facing the given direction. */
	static BlockState stairs(Block block, Direction facing) {
		return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
	}

	static BlockState facing(Block block, Direction facing) {
		BlockState state = block.defaultBlockState();
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
		}
		if (state.hasProperty(BlockStateProperties.FACING)) {
			return state.setValue(BlockStateProperties.FACING, facing);
		}
		return state;
	}

	/** Lets fences, walls, bars and panes join up with their neighbours once everything is placed. */
	void finish() {
		for (BlockPos pos : connectables) {
			BlockState state = level.getBlockState(pos);
			BlockState updated = Block.updateFromNeighbourShapes(state, level, pos);
			if (updated != state) {
				level.setBlock(pos, updated, FLAGS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
	}
}
