package io.github.adpulsipher.echoes.showcase;

import java.util.List;

import io.github.adpulsipher.echoes.Gifts;
import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import io.github.adpulsipher.echoes.component.EchoMemory;
import io.github.adpulsipher.echoes.entity.BossKind;
import io.github.adpulsipher.echoes.entity.Manifestations;
import io.github.adpulsipher.echoes.history.Era;
import io.github.adpulsipher.echoes.history.EventType;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.history.HistoryGenerator;
import io.github.adpulsipher.echoes.history.Terrain;
import io.github.adpulsipher.echoes.registry.ModComponents;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.registry.ModSounds;
import io.github.adpulsipher.echoes.world.TerrainClassifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Carries out the Codex of Echoes' entries for a player: builds, summons, replays and gifts.
 */
public final class ShowcaseActions {
	private ShowcaseActions() {
	}

	/** Returns a message describing what happened, or null if the entry is unknown. */
	public static @Nullable Component run(ServerPlayer player, String category, String id) {
		if (!ShowcaseCatalog.contains(category, id)) {
			return null;
		}
		ServerLevel level = (ServerLevel) player.level();
		Component name = Component.translatable("showcase.echoes_of_the_past." + category + "." + id);
		switch (category) {
			case "structure" -> {
				BlockPos origin = ahead(player, Structures.radius(id) + 3);
				Structures.build(level, id, origin, facingPlayer(player));
				level.playSound(null, origin, ModSounds.SHOWCASE_BUILD, SoundSource.BLOCKS, 1.5f, 1.0f);
			}
			case "boss" -> {
				BossKind kind = BossKind.byId(id);
				if (kind != null) {
					Manifestations.begin(level, ahead(player, 12).above(), kind, 40, null);
				}
			}
			case "arena" -> {
				BossKind kind = BossKind.byId(id);
				if (kind != null) {
					BlockPos origin = ahead(player, Structures.radius(kind.arena()) + 3);
					BlockPos focus = Structures.build(level, kind.arena(), origin, facingPlayer(player));
					level.playSound(null, origin, ModSounds.SHOWCASE_BUILD, SoundSource.BLOCKS, 1.5f, 1.0f);
					Manifestations.begin(level, focus, kind, 60, null);
				}
			}
			case "creature" -> {
				EntityType<? extends Mob> type = creature(id);
				BlockPos at = ahead(player, 4).above();
				Mob mob = type.spawn(level, at, EntitySpawnReason.COMMAND);
				if (mob != null) {
					mob.setYRot(player.getYRot() + 180.0f);
				}
			}
			case "replay" -> {
				EventType type = EventType.byId(id);
				BlockPos origin = ahead(player, Structures.radius("projector_stage") + 3);
				BlockPos projectorPos = Structures.build(level, "projector_stage", origin, facingPlayer(player));
				EchoMemory memory = findMemory(level, projectorPos, type);
				if (memory == null) {
					return Component.translatable("showcase.echoes_of_the_past.replay_missing", name);
				}
				if (level.getBlockEntity(projectorPos) instanceof EchoProjectorBlockEntity projector) {
					ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
					echo.set(ModComponents.ECHO_MEMORY, memory);
					projector.insert(level, echo);
					projector.start(level);
				}
				return Component.translatable("showcase.echoes_of_the_past.replaying", memory.title());
			}
			case "gear" -> gear(player, id);
			default -> {
				return null;
			}
		}
		return Component.translatable("showcase.echoes_of_the_past.done", name);
	}

	/** A point on the ground {@code distance} blocks ahead of the player. */
	static BlockPos ahead(ServerPlayer player, int distance) {
		Vec3 look = player.getLookAngle().multiply(1, 0, 1);
		if (look.lengthSqr() < 1.0E-4) {
			look = new Vec3(0, 0, 1);
		}
		look = look.normalize();
		BlockPos feet = player.blockPosition();
		return new BlockPos(Mth.floor(player.getX() + look.x * distance), feet.getY() - 1, Mth.floor(player.getZ() + look.z * distance));
	}

	/** The direction pointing back at the player from a structure ahead of them. */
	static Direction facingPlayer(ServerPlayer player) {
		return player.getDirection().getOpposite();
	}

	static EntityType<? extends Mob> creature(String id) {
		return switch (id) {
			case "memory_moth" -> ModEntities.MEMORY_MOTH;
			case "echo_knight" -> ModEntities.ECHO_KNIGHT;
			case "spectral_archer" -> ModEntities.SPECTRAL_ARCHER;
			case "ash_revenant" -> ModEntities.ASH_REVENANT;
			case "dawn_wisp" -> ModEntities.DAWN_WISP;
			case "shard_crawler" -> ModEntities.SHARD_CRAWLER;
			default -> ModEntities.LINGERER;
		};
	}

	/**
	 * Finds the nearest chunk whose history remembers an event of the given kind, and returns the memory of it. The
	 * search stays in the same terrain as the player so the echo is a true memory of the land around them.
	 */
	public static @Nullable EchoMemory findMemory(ServerLevel level, BlockPos near, EventType type) {
		ChunkPos home = ChunkPos.containing(near);
		Terrain terrain = TerrainClassifier.classifyChunk(level, home);
		long seed = level.getSeed();
		String dimension = level.dimension().identifier().toString();
		for (int r = 0; r <= 24; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
						continue;
					}
					int cx = home.x() + dx;
					int cz = home.z() + dz;
					for (Era era : Era.values()) {
						HistoricEvent event = HistoryGenerator.generate(seed, cx, cz, era, terrain);
						if (event.type() == type) {
							return new EchoMemory(dimension, cx, cz, era.representativeY(), terrain.name(), type.id(), event.title());
						}
					}
				}
			}
		}
		return null;
	}

	private static void gear(ServerPlayer player, String id) {
		List<Item> items = switch (id) {
			case "explorer_kit" -> List.of(ModItems.GUIDE_BOOK, ModItems.ARCHAEOLOGIST_CHISEL, Items.BRUSH, ModItems.RESONANCE_COMPASS,
					io.github.adpulsipher.echoes.registry.ModBlocks.ECHO_PROJECTOR.asItem(), ModItems.ECHO_DUST, ModItems.HEIRLOOM_LOCKET, ModItems.FESTIVAL_CHARM);
			case "wyrmscale" -> List.of(ModItems.WYRMSCALE_HELMET, ModItems.WYRMSCALE_CHESTPLATE, ModItems.WYRMSCALE_LEGGINGS, ModItems.WYRMSCALE_BOOTS, ModItems.ECHOING_BLADE);
			case "spectral_knight" -> List.of(ModItems.SPECTRAL_KNIGHT_HELMET, ModItems.SPECTRAL_KNIGHT_CHESTPLATE, ModItems.SPECTRAL_KNIGHT_LEGGINGS, ModItems.SPECTRAL_KNIGHT_BOOTS, ModItems.SPECTRAL_LONGSWORD);
			case "cindersteel" -> List.of(ModItems.CINDERSTEEL_HELMET, ModItems.CINDERSTEEL_CHESTPLATE, ModItems.CINDERSTEEL_LEGGINGS, ModItems.CINDERSTEEL_BOOTS, ModItems.ASHEN_CLEAVER);
			case "colossus" -> List.of(ModItems.COLOSSUS_HELMET, ModItems.COLOSSUS_CHESTPLATE, ModItems.COLOSSUS_LEGGINGS, ModItems.COLOSSUS_BOOTS, ModItems.SIEGEBREAKER);
			case "dawnweave" -> List.of(ModItems.DAWNWEAVE_HOOD, ModItems.DAWNWEAVE_ROBE, ModItems.DAWNWEAVE_LEGGINGS, ModItems.DAWNWEAVE_SLIPPERS, ModItems.DAWN_STAFF);
			case "weapons" -> List.of(ModItems.LEGIONNAIRE_BLADE, ModItems.SPECTRAL_LONGSWORD, ModItems.ASHEN_CLEAVER, ModItems.CROWNBREAKER, ModItems.SIEGEBREAKER,
					ModItems.DAWN_STAFF, ModItems.ECHOING_BLADE, ModItems.HOLLOW_CROWN);
			case "keystones" -> List.of(ModItems.KEYSTONE_OF_CROWNS, ModItems.KEYSTONE_OF_IRON, ModItems.KEYSTONE_OF_DRAGONS, ModItems.KEYSTONE_OF_DAWN, io.github.adpulsipher.echoes.registry.ModBlocks.ECHO_PROJECTOR.asItem());
			default -> List.of();
		};
		for (Item item : items) {
			Gifts.give(player, new ItemStack(item, item == ModItems.ECHO_DUST ? 8 : 1));
		}
		if (id.equals("explorer_kit")) {
			// A few echoes of this very place to get started.
			for (Era era : new Era[]{Era.HEARTHS, Era.CROWNS, Era.DRAGONS}) {
				ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
				echo.set(ModComponents.ECHO_MEMORY, io.github.adpulsipher.echoes.projection.EchoLore.memoryAt((ServerLevel) player.level(), player.blockPosition().atY(era.representativeY())));
				Gifts.give(player, echo);
			}
		}
	}
}
