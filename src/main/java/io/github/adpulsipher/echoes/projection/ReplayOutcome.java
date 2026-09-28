package io.github.adpulsipher.echoes.projection;

import java.util.ArrayList;
import java.util.List;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.entity.EchoWyrmEntity;
import io.github.adpulsipher.echoes.entity.LingererEntity;
import io.github.adpulsipher.echoes.history.Era;
import io.github.adpulsipher.echoes.history.EventType;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.registry.ModAttachments;
import io.github.adpulsipher.echoes.registry.ModBlocks;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What a finished replay leaves behind: transcripts for its audience, relics in the ground, and sometimes
 * something that followed the memory back into the present.
 */
public final class ReplayOutcome {
	private static final int PAGE_CHARS = 220;

	private ReplayOutcome() {
	}

	public static void conclude(ServerLevel level, BlockPos projector, ReplayDirector director) {
		HistoricEvent event = director.event();
		List<ServerPlayer> audience = director.audience();

		BlockPos relic = null;
		boolean alreadyUnearthed = false;
		if (director.resonant()) {
			relic = revealRelic(level, event);
			alreadyUnearthed = relic == null;
		}

		for (ServerPlayer player : audience) {
			giveTranscript(player, event, director.resonant(), relic, alreadyUnearthed);
			recordWitness(player, event);
			grant(player, "witness_replay", "witnessed");
			if (director.resonant()) {
				grant(player, "where_it_happened", "resonant");
			}
			if (event.era() == Era.ELDER_DAWN) {
				grant(player, "elder_dawn", "witnessed");
			}
		}

		level.playSound(null, projector, ModSounds.REPLAY_END, SoundSource.BLOCKS, 1.0f, 1.0f);

		if (relic != null) {
			announceRelic(level, relic, audience, event);
		} else if (director.resonant()) {
			for (ServerPlayer player : audience) {
				player.sendSystemMessage(Component.translatable("replay.echoes_of_the_past.relic_taken").withStyle(Style.EMPTY.withColor(0x9AA6C8).withItalic(true)));
			}
		} else {
			int x = event.chunkX() * 16 + 8;
			int z = event.chunkZ() * 16 + 8;
			for (ServerPlayer player : audience) {
				player.sendSystemMessage(Component.translatable("replay.echoes_of_the_past.distant", x, z).withStyle(Style.EMPTY.withColor(0x9AD7FF).withItalic(true)));
			}
		}

		if (director.riftPending()) {
			openRift(level, projector, event, audience);
		}
	}

	// ------------------------------------------------------------------ transcripts

	public static ItemStack transcript(HistoricEvent event, boolean resonant, BlockPos relic, boolean alreadyUnearthed) {
		List<Filterable<Component>> pages = new ArrayList<>();
		for (String paragraph : event.chronicle()) {
			for (String page : paginate(paragraph)) {
				pages.add(Filterable.passThrough(Component.literal(page)));
			}
		}

		String ending;
		if (relic != null) {
			ending = event.relicHint() + "\n\nThe echo has settled into the earth. Dig at " + relic.getX() + ", " + relic.getY() + ", " + relic.getZ() + " and brush away the soil.";
		} else if (alreadyUnearthed) {
			ending = event.relicHint() + "\n\nBut someone has already dug here. Only the memory remains.";
		} else {
			ending = event.relicHint() + "\n\nThis echo is far from home. Replay it where it formed, near " + (event.chunkX() * 16 + 8) + ", " + (event.chunkZ() * 16 + 8) + ", and the ground may give up its secret.";
		}
		pages.add(Filterable.passThrough(Component.literal(ending)));

		String title = event.title();
		if (title.length() > 32) {
			title = title.substring(0, 31) + "…";
		}
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title), "An echo of " + event.place(), 0, pages, true));
		return book;
	}

	static List<String> paginate(String text) {
		List<String> pages = new ArrayList<>();
		String[] words = text.split(" ");
		StringBuilder page = new StringBuilder();
		for (String word : words) {
			if (page.length() + word.length() + 1 > PAGE_CHARS && !page.isEmpty()) {
				pages.add(page.toString());
				page.setLength(0);
			}
			if (!page.isEmpty()) {
				page.append(' ');
			}
			page.append(word);
		}
		if (!page.isEmpty()) {
			pages.add(page.toString());
		}
		return pages;
	}

	private static void giveTranscript(ServerPlayer player, HistoricEvent event, boolean resonant, BlockPos relic, boolean alreadyUnearthed) {
		ItemStack book = transcript(event, resonant, relic, alreadyUnearthed);
		player.getInventory().placeItemBackInInventory(book);
	}

	// ------------------------------------------------------------------ progress

	private static void recordWitness(ServerPlayer player, HistoricEvent event) {
		List<String> seen = new ArrayList<>(player.getAttachedOrElse(ModAttachments.WITNESSED_EVENTS, List.of()));
		String id = event.type().id();
		if (!seen.contains(id)) {
			seen.add(id);
			player.setAttached(ModAttachments.WITNESSED_EVENTS, List.copyOf(seen));
		}
		grant(player, "keeper_of_ages", id);
		if (seen.size() >= 5) {
			grant(player, "historian", "witnessed_five");
		}
	}

	/** Grants an advancement criterion. Advancements use impossible triggers and are awarded from code. */
	public static void grant(ServerPlayer player, String advancement, String criterion) {
		MinecraftServer server = player.level().getServer();
		if (server == null) {
			return;
		}
		String command = "advancement grant " + player.getStringUUID() + " only " + EchoesOfThePast.MOD_ID + ":" + advancement + " " + criterion;
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
	}

	// ------------------------------------------------------------------ relics

	public static ResourceKey<LootTable> relicLootTable(EventType type) {
		return ResourceKey.create(Registries.LOOT_TABLE, EchoesOfThePast.id("relics/" + type.id()));
	}

	/**
	 * Buries a relic cache for the event in its chunk, unless this era's relics have already been unearthed there.
	 *
	 * @return where the cache was buried, or null if there is nothing left to find
	 */
	public static BlockPos revealRelic(ServerLevel level, HistoricEvent event) {
		LevelChunk chunk = level.getChunk(event.chunkX(), event.chunkZ());
		List<String> unearthed = chunk.getAttachedOrElse(ModAttachments.UNEARTHED_ERAS, List.of());
		String eraKey = event.era().name();
		if (unearthed.contains(eraKey)) {
			return null;
		}

		ChunkPos chunkPos = new ChunkPos(event.chunkX(), event.chunkZ());
		int x = chunkPos.getBlockX(event.relicOffsetX());
		int z = chunkPos.getBlockZ(event.relicOffsetZ());
		int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
		int minY = level.getMinY() + 3;
		int targetY = Mth.clamp(Math.min(surface - 3, event.era().representativeY()), minY, surface - 2);

		BlockPos found = null;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, targetY, z);
		for (int i = 0; i < 24 && pos.getY() > minY; i++, pos.move(0, -1, 0)) {
			BlockState state = level.getBlockState(pos);
			if (canHoldRelic(state)) {
				found = pos.immutable();
				break;
			}
		}
		if (found == null) {
			found = new BlockPos(x, Math.max(minY, surface - 2), z);
		}

		BlockState original = level.getBlockState(found);
		Block cache = isSoil(original) ? ModBlocks.RELIC_CACHE_SOIL : ModBlocks.RELIC_CACHE_STONE;
		level.setBlock(found, cache.defaultBlockState(), Block.UPDATE_ALL);
		if (level.getBlockEntity(found) instanceof BrushableBlockEntity brushable) {
			brushable.setLootTable(relicLootTable(event.type()), event.seed());
		}

		List<String> updated = new ArrayList<>(unearthed);
		updated.add(eraKey);
		chunk.setAttached(ModAttachments.UNEARTHED_ERAS, List.copyOf(updated));
		return found;
	}

	private static boolean isSoil(BlockState state) {
		return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL);
	}

	private static boolean canHoldRelic(BlockState state) {
		return !state.isAir() && state.getFluidState().isEmpty() && (isSoil(state)
				|| state.is(BlockTags.BASE_STONE_OVERWORLD)
				|| state.is(BlockTags.TERRACOTTA));
	}

	private static void announceRelic(ServerLevel level, BlockPos relic, List<ServerPlayer> audience, HistoricEvent event) {
		level.playSound(null, relic, ModSounds.RELIC_REVEAL, SoundSource.BLOCKS, 1.4f, 1.0f);
		int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, relic.getX(), relic.getZ()) + 12;
		for (int y = relic.getY(); y < top; y += 1) {
			level.sendParticles(ParticleTypes.END_ROD, relic.getX() + 0.5, y + 0.5, relic.getZ() + 0.5, 2, 0.1, 0.3, 0.1, 0.0);
		}
		Component message = Component.translatable("replay.echoes_of_the_past.relic_revealed", relic.getX(), relic.getY(), relic.getZ())
				.withStyle(Style.EMPTY.withColor(0xFFD98A));
		for (ServerPlayer player : audience) {
			player.sendSystemMessage(message);
		}
	}

	// ------------------------------------------------------------------ rifts

	private static void openRift(ServerLevel level, BlockPos projector, HistoricEvent event, List<ServerPlayer> audience) {
		Vec3 center = Vec3.atBottomCenterOf(projector);
		level.playSound(null, projector, ModSounds.RIFT_OPEN, SoundSource.HOSTILE, 2.0f, 0.7f);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 3, center.z, 200, 2.0, 2.0, 2.0, 0.2);
		level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 3, center.z, 1, 0, 0, 0, 0);

		boolean dragonEra = event.era() == Era.DRAGONS || event.era() == Era.ELDER_DAWN;
		boolean wyrmAlready = !level.getEntitiesOfClass(EchoWyrmEntity.class, new AABB(projector).inflate(96)).isEmpty();
		if (event.type() == EventType.DRAGON_ATTACK && dragonEra && !wyrmAlready) {
			EchoWyrmEntity wyrm = new EchoWyrmEntity(ModEntities.ECHO_WYRM, level);
			wyrm.setPos(center.x, center.y + 10, center.z);
			wyrm.setHome(projector.above(10));
			wyrm.setWyrmName(event.foe());
			level.addFreshEntity(wyrm);
			Component name = Component.literal(event.foe()).withStyle(Style.EMPTY.withColor(0xE0A8FF).withBold(true));
			for (ServerPlayer player : audience) {
				player.sendSystemMessage(Component.translatable("replay.echoes_of_the_past.wyrm_rift", name));
			}
			return;
		}

		int count = 2 + level.getRandom().nextInt(3);
		for (int i = 0; i < count; i++) {
			double angle = level.getRandom().nextDouble() * Math.PI * 2;
			double r = 3 + level.getRandom().nextDouble() * 3;
			LingererEntity lingerer = new LingererEntity(ModEntities.LINGERER, level);
			double x = center.x + Math.cos(angle) * r;
			double z = center.z + Math.sin(angle) * r;
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
			if (Math.abs(y - projector.getY()) > 6) {
				y = projector.getY();
			}
			lingerer.setPos(x, y, z);
			lingerer.equipFromEcho(event);
			level.addFreshEntity(lingerer);
			level.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1, z, 12, 0.3, 0.6, 0.3, 0.02);
		}
		for (ServerPlayer player : audience) {
			player.sendSystemMessage(Component.translatable("replay.echoes_of_the_past.lingerer_rift").withStyle(Style.EMPTY.withColor(0xFF7A9A)));
		}
	}
}
