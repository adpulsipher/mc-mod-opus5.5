package io.github.adpulsipher.echoes.world;

import io.github.adpulsipher.echoes.history.Terrain;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Reads the landscape of a chunk into one of the broad {@link Terrain} categories that shape its history.
 */
public final class TerrainClassifier {
	private TerrainClassifier() {
	}

	public static Terrain classify(Holder<Biome> biome) {
		if (biome.is(ConventionalBiomeTags.IS_CAVE) || biome.is(ConventionalBiomeTags.IS_UNDERGROUND)) {
			return Terrain.UNDERGROUND;
		}
		if (biome.is(ConventionalBiomeTags.IS_OCEAN) || biome.is(ConventionalBiomeTags.IS_BEACH)) {
			return Terrain.COAST;
		}
		if (biome.is(ConventionalBiomeTags.IS_BADLANDS)) {
			return Terrain.BADLANDS;
		}
		if (biome.is(ConventionalBiomeTags.IS_DESERT)) {
			return Terrain.DESERT;
		}
		if (biome.is(ConventionalBiomeTags.IS_SNOWY) || biome.is(ConventionalBiomeTags.IS_ICY)) {
			return Terrain.SNOW;
		}
		if (biome.is(ConventionalBiomeTags.IS_MOUNTAIN)) {
			return Terrain.MOUNTAINS;
		}
		if (biome.is(ConventionalBiomeTags.IS_SWAMP)) {
			return Terrain.SWAMP;
		}
		if (biome.is(ConventionalBiomeTags.IS_JUNGLE)) {
			return Terrain.JUNGLE;
		}
		if (biome.is(ConventionalBiomeTags.IS_FOREST) || biome.is(ConventionalBiomeTags.IS_TAIGA)) {
			return Terrain.FOREST;
		}
		return Terrain.PLAINS;
	}

	/** Classifies a chunk by the biome at its surface center, which is what its past inhabitants would have seen. */
	public static Terrain classifyChunk(Level level, ChunkPos chunk) {
		int x = chunk.getBlockX(8);
		int z = chunk.getBlockZ(8);
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		return classify(level.getBiome(new BlockPos(x, y, z)));
	}
}
