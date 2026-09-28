package io.github.adpulsipher.echoes.projection;

import io.github.adpulsipher.echoes.component.EchoMemory;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.history.HistoryGenerator;
import io.github.adpulsipher.echoes.history.Terrain;
import io.github.adpulsipher.echoes.world.TerrainClassifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Glue between the world and the history generator.
 */
public final class EchoLore {
	private EchoLore() {
	}

	/** The memory an echo formed at {@code pos} holds. */
	public static EchoMemory memoryAt(ServerLevel level, BlockPos pos) {
		ChunkPos chunk = ChunkPos.containing(pos);
		Terrain terrain = TerrainClassifier.classifyChunk(level, chunk);
		HistoricEvent event = HistoryGenerator.generateAtDepth(level.getSeed(), chunk.x(), chunk.z(), pos.getY(), terrain);
		return new EchoMemory(level.dimension().identifier().toString(), chunk.x(), chunk.z(), pos.getY(), terrain.name(), event.type().id(), event.title());
	}

	public static HistoricEvent eventOf(ServerLevel level, EchoMemory memory) {
		return HistoryGenerator.generateAtDepth(level.getSeed(), memory.chunkX(), memory.chunkZ(), memory.depth(), memory.terrainValue());
	}

	/** Whether a projector at {@code projector} stands in the same place the memory formed. */
	public static boolean isResonant(ServerLevel level, BlockPos projector, EchoMemory memory) {
		ChunkPos chunk = ChunkPos.containing(projector);
		return chunk.x() == memory.chunkX() && chunk.z() == memory.chunkZ()
				&& level.dimension().identifier().toString().equals(memory.dimension());
	}
}
