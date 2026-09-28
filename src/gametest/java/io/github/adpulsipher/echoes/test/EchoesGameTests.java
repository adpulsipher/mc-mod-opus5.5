package io.github.adpulsipher.echoes.test;

import java.util.List;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import io.github.adpulsipher.echoes.component.EchoMemory;
import io.github.adpulsipher.echoes.entity.EchoFigureEntity;
import io.github.adpulsipher.echoes.entity.EchoWyrmEntity;
import io.github.adpulsipher.echoes.entity.LingererEntity;
import io.github.adpulsipher.echoes.entity.MemoryMothEntity;
import io.github.adpulsipher.echoes.history.Era;
import io.github.adpulsipher.echoes.history.EventType;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.history.HistoryGenerator;
import io.github.adpulsipher.echoes.history.Terrain;
import io.github.adpulsipher.echoes.projection.EchoLore;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import io.github.adpulsipher.echoes.registry.ModAttachments;
import io.github.adpulsipher.echoes.registry.ModBlocks;
import io.github.adpulsipher.echoes.registry.ModComponents;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.replay.Choreographer;
import io.github.adpulsipher.echoes.replay.ReplayScript;
import io.github.adpulsipher.echoes.world.ModWorldgen;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public class EchoesGameTests {
	private static final String[] ADVANCEMENTS = {
		"root", "careful_hands", "witness_replay", "where_it_happened", "unearthed", "historian", "keeper_of_ages",
		"elder_dawn", "moth_to_a_flame", "lingering_doubts", "laid_to_rest", "wyrmslayer", "echo_chamber"
	};

	private static Component msg(String text) {
		return Component.literal(text);
	}

	@GameTest
	public void historyIsDeterministicAndComplete(GameTestHelper helper) {
		for (Terrain terrain : Terrain.values()) {
			for (int i = 0; i < 20; i++) {
				List<HistoricEvent> first = HistoryGenerator.chunkHistory(1234L, i * 7 - 40, i * 3 + 5, terrain);
				List<HistoricEvent> again = HistoryGenerator.chunkHistory(1234L, i * 7 - 40, i * 3 + 5, terrain);
				helper.assertTrue(first.size() == Era.values().length, msg("one event per era"));
				for (int e = 0; e < first.size(); e++) {
					HistoricEvent a = first.get(e);
					HistoricEvent b = again.get(e);
					helper.assertTrue(a.title().equals(b.title()) && a.type() == b.type(), msg("history must be deterministic"));
					helper.assertTrue(a.narration().size() == Choreographer.BEATS.length, msg("five narration beats for " + a.type()));
					helper.assertTrue(!a.chronicle().isEmpty(), msg("chronicle text for " + a.type()));
					ReplayScript script = Choreographer.stage(a);
					helper.assertTrue(!script.actors().isEmpty(), msg("replay has actors for " + a.type()));
				}
			}
		}
		helper.succeed();
	}

	@GameTest
	public void everyEventTypeCanBeStaged(GameTestHelper helper) {
		boolean[] seen = new boolean[EventType.values().length];
		for (int x = 0; x < 3000; x++) {
			HistoricEvent event = HistoryGenerator.generate(99L, x, -x, Era.values()[x % 5], Terrain.values()[x % Terrain.values().length]);
			seen[event.type().ordinal()] = true;
			ReplayScript script = Choreographer.stage(event);
			helper.assertTrue(script.duration() == Choreographer.DURATION, msg("script duration"));
		}
		for (EventType type : EventType.values()) {
			helper.assertTrue(seen[type.ordinal()], msg("event type never generated: " + type));
		}
		helper.succeed();
	}

	@GameTest
	public void dataIsLoaded(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String name : ADVANCEMENTS) {
			helper.assertTrue(level.getServer().getAdvancements().get(EchoesOfThePast.id(name)) != null, msg("advancement " + name + " should load"));
		}
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(ModWorldgen.ECHO_DEPOSITS).isPresent(), msg("echo deposit placed feature"));
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(ModWorldgen.ECHO_DEPOSITS_DEEP).isPresent(), msg("deep echo deposit placed feature"));
		helper.succeed();
	}

	@GameTest
	public void transcriptsAreWritten(GameTestHelper helper) {
		HistoricEvent event = HistoryGenerator.generate(5L, 1, 2, Era.CROWNS, Terrain.PLAINS);
		ItemStack book = ReplayOutcome.transcript(event, false, null, false);
		helper.assertTrue(!book.isEmpty(), msg("transcript book"));
		helper.succeed();
	}

	@GameTest(maxTicks = 200)
	public void projectorSummonsGhosts(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos rel = new BlockPos(1, 1, 1);
		helper.setBlock(rel, ModBlocks.ECHO_PROJECTOR);
		BlockPos abs = helper.absolutePos(rel);
		EchoMemory memory = EchoLore.memoryAt(level, abs.atY(Era.CROWNS.representativeY()));
		ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
		echo.set(ModComponents.ECHO_MEMORY, memory);
		if (!(level.getBlockEntity(abs) instanceof EchoProjectorBlockEntity projector)) {
			throw helper.assertionException(msg("projector block entity missing"));
		}
		helper.assertTrue(projector.insert(level, echo), msg("projector should accept an echo"));
		helper.succeedWhen(() -> helper.assertTrue(
				!level.getEntitiesOfClass(EchoFigureEntity.class, new AABB(abs).inflate(16)).isEmpty(),
				msg("ghosts should appear around the projector")));
	}

	@GameTest(maxTicks = 1000)
	public void resonantReplayBuriesRelics(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos rel = new BlockPos(2, 1, 2);
		helper.setBlock(rel, ModBlocks.ECHO_PROJECTOR);
		BlockPos abs = helper.absolutePos(rel);
		EchoMemory memory = EchoLore.memoryAt(level, abs.atY(Era.IRON_AND_ASH.representativeY()));
		ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
		echo.set(ModComponents.ECHO_MEMORY, memory);
		EchoProjectorBlockEntity projector = (EchoProjectorBlockEntity) level.getBlockEntity(abs);
		helper.assertTrue(projector != null && projector.insert(level, echo), msg("projector should accept an echo"));
		ChunkPos chunk = ChunkPos.containing(abs);
		helper.succeedWhen(() -> {
			helper.assertTrue(!projector.isProjecting(), msg("replay should finish"));
			List<String> unearthed = level.getChunk(chunk.x(), chunk.z()).getAttachedOrElse(ModAttachments.UNEARTHED_ERAS, List.of());
			helper.assertTrue(unearthed.contains(Era.IRON_AND_ASH.name()), msg("relics should be buried after a resonant replay"));
		});
	}

	@GameTest(maxTicks = 80)
	public void mobsLiveAndTick(GameTestHelper helper) {
		LingererEntity lingerer = helper.spawn(ModEntities.LINGERER, new BlockPos(1, 2, 1));
		MemoryMothEntity moth = helper.spawn(ModEntities.MEMORY_MOTH, new BlockPos(3, 3, 3));
		EchoWyrmEntity wyrm = helper.spawn(ModEntities.ECHO_WYRM, new BlockPos(4, 6, 4));
		helper.runAtTickTime(60, () -> {
			helper.assertTrue(lingerer.isAlive(), msg("lingerer alive"));
			helper.assertTrue(moth.isAlive(), msg("moth alive"));
			helper.assertTrue(wyrm.isAlive(), msg("wyrm alive"));
			wyrm.discard();
			helper.succeed();
		});
	}
}
