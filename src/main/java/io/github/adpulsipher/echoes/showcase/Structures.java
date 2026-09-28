package io.github.adpulsipher.echoes.showcase;

import java.util.List;

import io.github.adpulsipher.echoes.block.EchoProjectorBlock;
import io.github.adpulsipher.echoes.history.EventType;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import io.github.adpulsipher.echoes.registry.ModBlocks;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/**
 * The showcase builds: projector stages, a dig site, an arena for each great echo, a bestiary and an armory.
 * Every build method returns the point of interest of the structure (where a replay or boss belongs).
 */
public final class Structures {
	private Structures() {
	}

	/** How far from the player the centre of each structure is placed, so it never lands on top of them. */
	public static int radius(String id) {
		return switch (id) {
			case "projector_stage" -> 6;
			case "dig_site" -> 9;
			case "throne_hall" -> 14;
			case "siege_ruin", "wyrm_roost", "dawn_altar" -> 14;
			case "bestiary" -> 14;
			case "armory" -> 8;
			default -> 8;
		};
	}

	public static BlockPos build(ServerLevel level, String id, BlockPos origin, Direction toward) {
		Builder b = new Builder(level, origin, origin.asLong() ^ id.hashCode());
		BlockPos focus = switch (id) {
			case "projector_stage" -> projectorStage(b, toward);
			case "dig_site" -> digSite(b);
			case "throne_hall" -> throneHall(b);
			case "siege_ruin" -> siegeRuin(b);
			case "wyrm_roost" -> wyrmRoost(b);
			case "dawn_altar" -> dawnAltar(b);
			case "bestiary" -> bestiary(b);
			case "armory" -> armory(b);
			default -> origin;
		};
		b.finish();
		if (id.equals("bestiary")) {
			populateBestiary(b);
		} else if (id.equals("armory")) {
			populateArmory(b);
		}
		return focus;
	}

	// ------------------------------------------------------------------ projector stage

	static BlockPos projectorStage(Builder b, Direction toward) {
		b.clearSite(6, 6, 10, true, () -> Blocks.POLISHED_DEEPSLATE.defaultBlockState(), () -> Blocks.DEEPSLATE_BRICKS.defaultBlockState());
		for (int x = -6; x <= 6; x++) {
			for (int z = -6; z <= 6; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 6.5) {
					continue;
				}
				if (d >= 5.3) {
					b.set(x, 0, z, Blocks.CHISELED_DEEPSLATE);
				} else if (d < 2.2) {
					b.set(x, 0, z, Blocks.SMOOTH_QUARTZ);
				} else if ((int) (d * 2) % 3 == 0) {
					b.set(x, 0, z, Blocks.DEEPSLATE_TILES);
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			double a = Math.PI / 4 + i * Math.PI / 2;
			int x = (int) Math.round(Math.cos(a) * 4.6);
			int z = (int) Math.round(Math.sin(a) * 4.6);
			b.set(x, 1, z, Blocks.POLISHED_DEEPSLATE_WALL);
			b.set(x, 2, z, Blocks.POLISHED_DEEPSLATE_WALL);
			b.set(x, 3, z, ModBlocks.ECHO_LANTERN);
		}
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4 + Math.PI / 8;
			b.set((int) Math.round(Math.cos(a) * 3.2), 1, (int) Math.round(Math.sin(a) * 3.2), Builder.facing(Blocks.AMETHYST_CLUSTER, Direction.UP));
		}
		b.set(0, 1, 0, ModBlocks.ECHO_PROJECTOR.defaultBlockState().setValue(EchoProjectorBlock.FACING, toward));
		return b.at(0, 1, 0);
	}

	// ------------------------------------------------------------------ dig site

	static BlockPos digSite(Builder b) {
		b.clearSite(9, 9, 10, false, b.mix(Blocks.COARSE_DIRT, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.DIRT_PATH), b.mix(Blocks.DIRT));
		// Strata beneath the site, visible in the pit walls.
		for (int y = -1; y >= -5; y--) {
			Block[] layer = switch (-y) {
				case 1 -> new Block[]{Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT};
				case 2 -> new Block[]{Blocks.PACKED_MUD, Blocks.DIRT, Blocks.MUD_BRICKS};
				case 3 -> new Block[]{Blocks.STONE, Blocks.ANDESITE, Blocks.TUFF};
				case 4 -> new Block[]{Blocks.STONE, Blocks.TUFF, Blocks.COBBLESTONE};
				default -> new Block[]{Blocks.DEEPSLATE, Blocks.TUFF};
			};
			b.fill(-6, y, -6, 6, y, 6, b.mix(layer));
		}
		// The pit, cut in terraces.
		for (int depth = 0; depth < 4; depth++) {
			int r = 4 - depth;
			b.fill(-r, -depth, -r, r, 0 - depth, r, Blocks.AIR.defaultBlockState());
		}
		b.fill(-1, -4, -1, 1, -4, 1, Blocks.AIR.defaultBlockState());
		// Echoes in the walls, relics on the floor.
		b.set(-4, -1, 2, ModBlocks.ECHO_DEPOSIT);
		b.set(3, -2, -3, ModBlocks.ECHO_DEPOSIT);
		b.set(-2, -3, -2, ModBlocks.ECHO_DEPOSIT);
		b.set(2, -4, 2, ModBlocks.DEEPSLATE_ECHO_DEPOSIT);
		b.set(0, -5, 0, ModBlocks.DEEPSLATE_ECHO_DEPOSIT);
		List<EventType> kinds = List.of(EventType.values());
		int[][] caches = {{-1, -5, -1}, {1, -5, 1}, {1, -5, -1}, {-3, -3, 3}, {3, -3, 0}};
		for (int i = 0; i < caches.length; i++) {
			int[] c = caches[i];
			boolean stone = c[1] <= -5;
			b.set(c[0], c[1], c[2], stone ? ModBlocks.RELIC_CACHE_STONE : ModBlocks.RELIC_CACHE_SOIL);
			if (b.level.getBlockEntity(b.at(c[0], c[1], c[2])) instanceof BrushableBlockEntity brushable) {
				brushable.setLootTable(ReplayOutcome.relicLootTable(kinds.get(b.random.nextInt(kinds.size()))), b.random.nextLong());
			}
		}
		// Rope fence around the pit with lanterns at the corners.
		for (int i = -6; i <= 6; i++) {
			if (Math.abs(i) != 1 && i != 0) {
				b.set(i, 1, -6, Blocks.OAK_FENCE);
				b.set(-6, 1, i, Blocks.OAK_FENCE);
				b.set(6, 1, i, Blocks.OAK_FENCE);
			}
			b.set(i, 1, 6, Blocks.OAK_FENCE);
		}
		for (int[] c : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}}) {
			b.set(c[0], 2, c[1], Blocks.OAK_FENCE);
			b.set(c[0], 3, c[1], Blocks.LANTERN);
		}
		// Scaffolding tower over the pit edge.
		b.fill(5, 1, 5, 5, 3, 5, Blocks.STRIPPED_OAK_LOG);
		b.fill(4, 3, 4, 5, 3, 5, Blocks.OAK_SLAB);
		b.set(4, 4, 4, Blocks.LANTERN);
		// The archaeologist's tent.
		for (int x = -9; x <= -7; x++) {
			for (int z = 2; z <= 7; z++) {
				int roof = x == -8 ? 4 : 3;
				b.set(x, roof, z, Builder.byId(x == -8 ? "white_wool" : "light_gray_wool"));
			}
		}
		for (int z : new int[]{2, 7}) {
			b.fill(-9, 1, z, -9, 2, z, Blocks.OAK_FENCE.defaultBlockState());
			b.fill(-7, 1, z, -7, 2, z, Blocks.OAK_FENCE.defaultBlockState());
			b.fill(-8, 1, z, -8, 3, z, Blocks.OAK_FENCE.defaultBlockState());
		}
		b.set(-8, 1, 3, Blocks.CRAFTING_TABLE);
		b.set(-8, 1, 4, Builder.facing(Blocks.CHEST, Direction.EAST));
		b.set(-8, 1, 5, Blocks.BARREL);
		b.set(-8, 1, 6, ModBlocks.ECHO_PROJECTOR.defaultBlockState().setValue(EchoProjectorBlock.FACING, Direction.EAST));
		b.set(-9, 1, 4, Blocks.LANTERN);
		if (b.level.getBlockEntity(b.at(-8, 1, 4)) instanceof Container chest) {
			chest.setItem(0, new ItemStack(ModItems.ARCHAEOLOGIST_CHISEL));
			chest.setItem(1, new ItemStack(Items.BRUSH));
			chest.setItem(2, new ItemStack(ModItems.RESONANCE_COMPASS));
			chest.setItem(3, new ItemStack(ModItems.GUIDE_BOOK));
			chest.setItem(4, new ItemStack(ModItems.ECHO_SHARD, 8));
			chest.setItem(5, new ItemStack(ModItems.ECHO_DUST, 4));
			chest.setItem(9, new ItemStack(Items.TORCH, 16));
			chest.setItem(10, new ItemStack(Items.BREAD, 6));
		}
		// Spoil heaps.
		for (int[] c : new int[][]{{7, 3}, {7, -3}, {-3, -8}, {2, 8}}) {
			b.set(c[0], 1, c[1], Blocks.COARSE_DIRT);
			b.set(c[0] + 1, 1, c[1], Blocks.DIRT);
			b.set(c[0], 2, c[1], Blocks.COARSE_DIRT);
		}
		return b.at(0, -4, 0);
	}

	// ------------------------------------------------------------------ throne hall of the Hollow King

	static BlockPos throneHall(Builder b) {
		b.clearSite(10, 15, 14, false, b.mix(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS),
				() -> Blocks.BLACKSTONE.defaultBlockState());
		// Checkered nave and a red carpet up the aisle.
		for (int x = -7; x <= 7; x++) {
			for (int z = -13; z <= 13; z++) {
				if ((x + z) % 4 == 0) {
					b.set(x, 0, z, Blocks.POLISHED_BLACKSTONE);
				}
			}
		}
		b.fill(-1, 1, -13, 1, 1, 8, Builder.byId("red_carpet"));
		b.fill(-2, 1, -13, -2, 1, 8, Builder.byId("yellow_carpet"));
		b.fill(2, 1, -13, 2, 1, 8, Builder.byId("yellow_carpet"));
		// Ruined walls with tall windows.
		for (int z = -14; z <= 14; z++) {
			for (int side : new int[]{-9, 9}) {
				int height = 8 - (b.random.nextInt(6) == 0 ? 2 + b.random.nextInt(4) : 0);
				for (int y = 1; y <= height; y++) {
					boolean window = (z + 14) % 5 == 2 && y >= 3 && y <= 5;
					if (!window && b.random.nextInt(12) != 0) {
						b.set(side, y, z, b.random.nextInt(5) == 0 ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
					}
				}
				if (height == 8) {
					b.set(side, 9, z, (z & 1) == 0 ? Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState() : Blocks.AIR.defaultBlockState());
				}
			}
		}
		for (int x = -8; x <= 8; x++) {
			int height = Math.abs(x) <= 2 ? 0 : 8 - b.random.nextInt(3);
			for (int y = 1; y <= height; y++) {
				b.set(x, y, 14, Blocks.DEEPSLATE_BRICKS);
			}
			if (Math.abs(x) > 2) {
				for (int y = 1; y <= 3 + b.random.nextInt(3); y++) {
					b.set(x, y, -14, b.random.nextInt(4) == 0 ? Blocks.CRACKED_DEEPSLATE_BRICKS : Blocks.DEEPSLATE_BRICKS);
				}
			}
		}
		// Two rows of pillars with soul lanterns.
		for (int z = -10; z <= 5; z += 5) {
			for (int x : new int[]{-5, 5}) {
				b.fill(x, 1, z, x, 6, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
				b.set(x, 7, z, Blocks.CHISELED_DEEPSLATE);
				b.set(x, 8, z, Blocks.SOUL_LANTERN);
				b.set(x, 1, z + 1, Builder.stairs(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH));
				b.set(x, 1, z - 1, Builder.stairs(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.SOUTH));
			}
		}
		// The dais and the empty throne.
		b.fill(-5, 1, 9, 5, 1, 9, Builder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH));
		b.fill(-5, 1, 10, 5, 1, 13, Blocks.POLISHED_BLACKSTONE_BRICKS);
		b.fill(-3, 2, 10, 3, 2, 10, Builder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH));
		b.fill(-3, 2, 11, 3, 2, 13, Blocks.POLISHED_BLACKSTONE);
		b.fill(-1, 3, 11, 1, 3, 11, Builder.byId("red_carpet"));
		b.set(0, 3, 12, Builder.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.SOUTH));
		b.set(-1, 3, 12, Blocks.GOLD_BLOCK);
		b.set(1, 3, 12, Blocks.GOLD_BLOCK);
		b.fill(0, 3, 13, 0, 5, 13, Blocks.POLISHED_BLACKSTONE_BRICKS);
		b.set(-1, 4, 13, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
		b.set(1, 4, 13, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
		b.set(0, 6, 13, Blocks.GOLD_BLOCK);
		b.set(-1, 5, 13, Blocks.GOLD_BLOCK);
		b.set(1, 5, 13, Blocks.GOLD_BLOCK);
		b.set(-4, 2, 12, Builder.byId("purple_banner"));
		b.set(4, 2, 12, Builder.byId("purple_banner"));
		b.set(-5, 2, 10, Blocks.SOUL_LANTERN);
		b.set(5, 2, 10, Blocks.SOUL_LANTERN);
		// Rubble and fallen masonry.
		for (int i = 0; i < 24; i++) {
			int x = b.random.nextInt(15) - 7;
			int z = b.random.nextInt(22) - 13;
			if (Math.abs(x) > 2) {
				b.set(x, 1, z, b.random.nextBoolean() ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState());
			}
		}
		return b.at(0, 1, 3);
	}

	// ------------------------------------------------------------------ siege ruin of the Colossus

	static BlockPos siegeRuin(Builder b) {
		b.clearSite(14, 14, 14, true, b.mix(Blocks.COARSE_DIRT, Blocks.COBBLESTONE, Blocks.PACKED_MUD, Blocks.COARSE_DIRT, Blocks.MOSSY_COBBLESTONE),
				() -> Blocks.COBBLESTONE.defaultBlockState());
		for (int x = -14; x <= 14; x++) {
			for (int z = -14; z <= 14; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d < 11.5 || d > 13.8) {
					continue;
				}
				boolean gate = z > 8 && Math.abs(x) <= 2;
				if (gate) {
					continue;
				}
				double angle = Math.atan2(z, x);
				int height = 2 + (int) (Math.abs(Math.sin(angle * 3.0 + 1.0)) * 5) - (b.random.nextInt(4) == 0 ? 2 : 0);
				for (int y = 1; y <= height; y++) {
					b.set(x, y, z, b.mix(Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS).get());
				}
				if (height >= 5 && d > 12.7 && (x + z) % 2 == 0) {
					b.set(x, height + 1, z, Blocks.STONE_BRICK_WALL);
				}
			}
		}
		// Gatehouse towers.
		for (int side : new int[]{-4, 4}) {
			b.fill(side - 1, 1, 11, side + 1, 9, 13, Blocks.STONE_BRICKS);
			b.fill(side, 2, 11, side, 8, 11, Blocks.AIR.defaultBlockState());
			for (int dx = -1; dx <= 1; dx += 2) {
				b.set(side + dx, 10, 11, Blocks.STONE_BRICK_WALL);
				b.set(side + dx, 10, 13, Blocks.STONE_BRICK_WALL);
			}
			b.set(side, 9, 12, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false));
		}
		b.fill(-3, 8, 12, 3, 9, 12, Blocks.STONE_BRICKS);
		b.fill(-2, 5, 12, 2, 7, 12, Blocks.IRON_BARS.defaultBlockState());
		// The spoils of the siege: a broken ram, anvils, chains and scattered rubble.
		b.fill(-3, 1, -3, 3, 1, -3, Builder.facing(Blocks.STRIPPED_DARK_OAK_LOG, Direction.EAST));
		b.set(-3, 2, -3, Blocks.DARK_OAK_FENCE);
		b.set(3, 2, -3, Blocks.DARK_OAK_FENCE);
		b.set(-3, 3, -3, Blocks.IRON_BARS);
		b.set(3, 3, -3, Blocks.IRON_BARS);
		b.set(4, 1, -3, Blocks.IRON_BLOCK);
		b.set(-6, 1, 4, Blocks.DAMAGED_ANVIL);
		b.set(6, 1, 5, Blocks.CHIPPED_ANVIL);
		b.set(7, 1, -6, Blocks.HAY_BLOCK);
		b.set(7, 2, -6, Blocks.HAY_BLOCK);
		b.set(8, 1, -5, Blocks.HAY_BLOCK);
		b.set(-7, 1, -6, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false));
		for (int i = 0; i < 40; i++) {
			double a = b.random.nextDouble() * Math.PI * 2;
			double r = 4 + b.random.nextDouble() * 7;
			int x = (int) Math.round(Math.cos(a) * r);
			int z = (int) Math.round(Math.sin(a) * r);
			b.set(x, 1, z, b.mix(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.STONE_BRICK_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.CRACKED_STONE_BRICKS).get());
		}
		for (int[] c : new int[][]{{-9, -2}, {9, 2}, {0, -10}}) {
			b.set(c[0], 1, c[1], Blocks.COBBLESTONE_WALL);
			b.set(c[0], 2, c[1], Blocks.LANTERN);
		}
		return b.at(0, 1, 2);
	}

	// ------------------------------------------------------------------ wyrm roost

	static BlockPos wyrmRoost(Builder b) {
		b.clearSite(14, 14, 18, true, b.mix(Blocks.BLACKSTONE, Blocks.BASALT, Blocks.BLACKSTONE, Blocks.SMOOTH_BASALT), () -> Blocks.BLACKSTONE.defaultBlockState());
		// A crag rising towards the nest.
		for (int x = -14; x <= 14; x++) {
			for (int z = -14; z <= 14; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 14.5) {
					continue;
				}
				int height = d < 7 ? 2 : d < 10 ? 1 : 0;
				for (int y = 1; y <= height; y++) {
					b.set(x, y, z, b.mix(Blocks.BLACKSTONE, Blocks.BASALT, Blocks.GILDED_BLACKSTONE, Blocks.BLACKSTONE, Blocks.BLACKSTONE).get());
				}
				if (d >= 12.5 && b.random.nextInt(3) == 0) {
					int spike = 1 + b.random.nextInt(4);
					for (int y = 1; y <= spike; y++) {
						b.set(x, y, z, Blocks.BASALT);
					}
				}
			}
		}
		// Great bone ribs arching over the nest.
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4;
			for (int step = 0; step <= 12; step++) {
				double t = step / 12.0;
				double r = 8.5 - 5.5 * t * t;
				int y = 3 + (int) Math.round(Math.sin(t * Math.PI * 0.5) * 7);
				b.set((int) Math.round(Math.cos(a) * r), y, (int) Math.round(Math.sin(a) * r), Blocks.BONE_BLOCK);
			}
		}
		// The hoard.
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				if (x * x + z * z <= 10) {
					b.set(x, 3, z, b.mix(Blocks.HAY_BLOCK, Blocks.HAY_BLOCK, Blocks.BONE_BLOCK).get());
				}
			}
		}
		b.set(0, 3, 0, Blocks.GOLD_BLOCK);
		b.set(1, 3, 1, Blocks.RAW_GOLD_BLOCK);
		b.set(-1, 3, 0, ModBlocks.ECHO_CRYSTAL_BLOCK);
		// Soul-fire braziers.
		for (int i = 0; i < 4; i++) {
			double a = i * Math.PI / 2 + Math.PI / 4;
			int x = (int) Math.round(Math.cos(a) * 10);
			int z = (int) Math.round(Math.sin(a) * 10);
			b.set(x, 1, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
			b.set(x, 2, z, Blocks.SOUL_SOIL);
			b.set(x, 3, z, Blocks.SOUL_FIRE);
		}
		return b.at(0, 4, 0);
	}

	// ------------------------------------------------------------------ dawn altar

	static BlockPos dawnAltar(Builder b) {
		b.clearSite(14, 14, 14, true, () -> Blocks.CALCITE.defaultBlockState(), () -> Blocks.SMOOTH_STONE.defaultBlockState());
		for (int x = -14; x <= 14; x++) {
			for (int z = -14; z <= 14; z++) {
				double d = Math.sqrt(x * x + z * z);
				if (d > 14.5) {
					continue;
				}
				int ring = (int) Math.round(d);
				if (ring == 4 || ring == 8) {
					b.set(x, 0, z, Blocks.SMOOTH_QUARTZ);
				} else if (ring == 12) {
					b.set(x, 0, z, Blocks.AMETHYST_BLOCK);
				} else if (d < 3) {
					b.set(x, 0, z, Blocks.QUARTZ_BRICKS);
				}
			}
		}
		// Standing stones crowned with light.
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4;
			int x = (int) Math.round(Math.cos(a) * 10.5);
			int z = (int) Math.round(Math.sin(a) * 10.5);
			int height = 4 + (i % 2) * 2;
			b.fill(x, 1, z, x, height, z, Blocks.CALCITE.defaultBlockState());
			b.set(x, height + 1, z, Blocks.AMETHYST_BLOCK);
			b.set(x, height + 2, z, Builder.facing(Blocks.END_ROD, Direction.UP));
			b.set(x, 1, z + (z > 0 ? -1 : 1), Builder.facing(Blocks.AMETHYST_CLUSTER, Direction.UP));
		}
		// The altar.
		b.fill(-2, 1, -2, 2, 1, 2, Blocks.CHISELED_QUARTZ_BLOCK);
		b.fill(-1, 2, -1, 1, 2, 1, Blocks.QUARTZ_PILLAR);
		b.set(0, 3, 0, ModBlocks.ECHO_CRYSTAL_BLOCK);
		for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
			b.set(c[0], 2, c[1], Builder.facing(Blocks.END_ROD, Direction.UP));
		}
		for (int i = 0; i < 12; i++) {
			double a = i * Math.PI / 6;
			b.set((int) Math.round(Math.cos(a) * 6), 1, (int) Math.round(Math.sin(a) * 6), Builder.facing(Blocks.MEDIUM_AMETHYST_BUD, Direction.UP));
		}
		return b.at(0, 5, 0);
	}

	// ------------------------------------------------------------------ bestiary

	private static final List<EntityType<? extends Mob>> SMALL_EXHIBITS = List.of(ModEntities.LINGERER, ModEntities.MEMORY_MOTH, ModEntities.ECHO_KNIGHT,
			ModEntities.SPECTRAL_ARCHER, ModEntities.ASH_REVENANT, ModEntities.DAWN_WISP, ModEntities.SHARD_CRAWLER);
	private static final List<EntityType<? extends Mob>> GREAT_EXHIBITS = List.of(ModEntities.HOLLOW_KING, ModEntities.SIEGE_COLOSSUS, ModEntities.ECHO_WYRM,
			ModEntities.HIEROPHANT);

	static BlockPos bestiary(Builder b) {
		int smallWidth = SMALL_EXHIBITS.size() * 5 + 1;
		int greatWidth = GREAT_EXHIBITS.size() * 9 + 1;
		int half = Math.max(smallWidth, greatWidth) / 2 + 1;
		b.clearSite(half, 14, 12, false, b.mix(Blocks.POLISHED_ANDESITE, Blocks.POLISHED_ANDESITE, Blocks.POLISHED_DIORITE), () -> Blocks.STONE_BRICKS.defaultBlockState());
		// Small exhibits: 4x4 glass pens along the north side.
		int x0 = -smallWidth / 2;
		for (int i = 0; i < SMALL_EXHIBITS.size(); i++) {
			int x = x0 + i * 5;
			pen(b, x, -12, x + 5, -7, 4);
		}
		// Great echoes: 8x8 glass halls along the south side.
		x0 = -greatWidth / 2;
		for (int i = 0; i < GREAT_EXHIBITS.size(); i++) {
			int x = x0 + i * 9;
			pen(b, x, 3, x + 9, 12, 9);
		}
		b.fill(-2, 1, -3, 2, 1, -1, Builder.byId("red_carpet"));
		return b.at(0, 1, -2);
	}

	private static void pen(Builder b, int x0, int z0, int x1, int z1, int height) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
				b.set(x, 0, z, edge ? Blocks.POLISHED_DEEPSLATE : Blocks.MOSS_BLOCK);
				for (int y = 1; y <= height; y++) {
					if (edge) {
						boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
						b.set(x, y, z, corner ? Blocks.POLISHED_DEEPSLATE : Blocks.GLASS);
					}
				}
				b.set(x, height + 1, z, edge ? Blocks.POLISHED_DEEPSLATE : Blocks.GLASS);
			}
		}
		b.set((x0 + x1) / 2, height + 1, (z0 + z1) / 2, ModBlocks.ECHO_LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true));
		b.set((x0 + x1) / 2, height + 2, (z0 + z1) / 2, Blocks.POLISHED_DEEPSLATE);
	}

	private static void populateBestiary(Builder b) {
		int smallWidth = SMALL_EXHIBITS.size() * 5 + 1;
		int x0 = -smallWidth / 2;
		for (int i = 0; i < SMALL_EXHIBITS.size(); i++) {
			exhibit(b, SMALL_EXHIBITS.get(i), x0 + i * 5 + 2.5, SMALL_EXHIBITS.get(i) == ModEntities.MEMORY_MOTH || SMALL_EXHIBITS.get(i) == ModEntities.DAWN_WISP ? 2.5 : 1, -9.5);
		}
		int greatWidth = GREAT_EXHIBITS.size() * 9 + 1;
		x0 = -greatWidth / 2;
		for (int i = 0; i < GREAT_EXHIBITS.size(); i++) {
			EntityType<? extends Mob> type = GREAT_EXHIBITS.get(i);
			exhibit(b, type, x0 + i * 9 + 4.5, type == ModEntities.ECHO_WYRM ? 4 : type == ModEntities.HIEROPHANT ? 2 : 1, 7.5);
		}
	}

	private static void exhibit(Builder b, EntityType<? extends Mob> type, double x, double y, double z) {
		Mob mob = type.create(b.level, EntitySpawnReason.COMMAND);
		if (mob == null) {
			return;
		}
		mob.setPos(b.origin.getX() + x, b.origin.getY() + y, b.origin.getZ() + z);
		mob.setYRot(0.0f);
		mob.setYHeadRot(0.0f);
		mob.setYBodyRot(0.0f);
		mob.setNoAi(true);
		mob.setSilent(true);
		mob.setPersistenceRequired();
		mob.setCustomName(Component.translatable(type.getDescriptionId()).withStyle(Style.EMPTY.withColor(0xBFF6FF)));
		mob.setCustomNameVisible(true);
		b.level.addFreshEntity(mob);
	}

	// ------------------------------------------------------------------ armory

	private record Display(Item head, Item chest, Item legs, Item feet, Item weapon) {
	}

	private static final List<Display> DISPLAYS = List.of(
			new Display(ModItems.SPECTRAL_KNIGHT_HELMET, ModItems.SPECTRAL_KNIGHT_CHESTPLATE, ModItems.SPECTRAL_KNIGHT_LEGGINGS, ModItems.SPECTRAL_KNIGHT_BOOTS, ModItems.SPECTRAL_LONGSWORD),
			new Display(ModItems.CINDERSTEEL_HELMET, ModItems.CINDERSTEEL_CHESTPLATE, ModItems.CINDERSTEEL_LEGGINGS, ModItems.CINDERSTEEL_BOOTS, ModItems.ASHEN_CLEAVER),
			new Display(ModItems.HOLLOW_CROWN, ModItems.SPECTRAL_KNIGHT_CHESTPLATE, ModItems.SPECTRAL_KNIGHT_LEGGINGS, ModItems.SPECTRAL_KNIGHT_BOOTS, ModItems.CROWNBREAKER),
			new Display(ModItems.COLOSSUS_HELMET, ModItems.COLOSSUS_CHESTPLATE, ModItems.COLOSSUS_LEGGINGS, ModItems.COLOSSUS_BOOTS, ModItems.SIEGEBREAKER),
			new Display(ModItems.WYRMSCALE_HELMET, ModItems.WYRMSCALE_CHESTPLATE, ModItems.WYRMSCALE_LEGGINGS, ModItems.WYRMSCALE_BOOTS, ModItems.ECHOING_BLADE),
			new Display(ModItems.DAWNWEAVE_HOOD, ModItems.DAWNWEAVE_ROBE, ModItems.DAWNWEAVE_LEGGINGS, ModItems.DAWNWEAVE_SLIPPERS, ModItems.DAWN_STAFF)
	);

	static BlockPos armory(Builder b) {
		b.clearSite(10, 6, 8, false, b.mix(Blocks.DARK_OAK_PLANKS), () -> Blocks.STONE_BRICKS.defaultBlockState());
		for (int x = -10; x <= 10; x++) {
			b.set(x, 0, -6, Blocks.POLISHED_DEEPSLATE);
			b.set(x, 0, 6, Blocks.POLISHED_DEEPSLATE);
			for (int y = 1; y <= 6; y++) {
				b.set(x, y, 6, y == 6 ? Blocks.CHISELED_STONE_BRICKS : Blocks.STONE_BRICKS);
			}
		}
		for (int z = -6; z <= 6; z++) {
			for (int y = 1; y <= 6; y++) {
				b.set(-10, y, z, Blocks.STONE_BRICKS);
				b.set(10, y, z, Blocks.STONE_BRICKS);
			}
		}
		b.fill(-9, 1, -1, 9, 1, 1, Builder.byId("red_carpet"));
		for (int i = 0; i < DISPLAYS.size(); i++) {
			int x = -8 + i * 3 + 1;
			b.set(x, 1, 4, Blocks.POLISHED_BLACKSTONE);
			b.set(x, 4, 5, ModBlocks.ECHO_LANTERN);
		}
		for (int x = -9; x <= 9; x += 3) {
			b.set(x, 3, -5, Blocks.LANTERN);
			b.set(x, 2, -5, Blocks.POLISHED_DEEPSLATE_WALL);
			b.set(x, 1, -5, Blocks.POLISHED_DEEPSLATE_WALL);
		}
		return b.at(0, 1, 0);
	}

	private static String id(Item item) {
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
	}

	private static void populateArmory(Builder b) {
		MinecraftServer server = b.level.getServer();
		for (int i = 0; i < DISPLAYS.size(); i++) {
			Display display = DISPLAYS.get(i);
			double x = b.origin.getX() - 8 + i * 3 + 1 + 0.5;
			double y = b.origin.getY() + 2;
			double z = b.origin.getZ() + 4 + 0.5;
			String equipment = String.format("equipment:{head:{id:\"%s\",count:1},chest:{id:\"%s\",count:1},legs:{id:\"%s\",count:1},feet:{id:\"%s\",count:1},mainhand:{id:\"%s\",count:1}}",
					id(display.head()), id(display.chest()), id(display.legs()), id(display.feet()), id(display.weapon()));
			String command = String.format(java.util.Locale.ROOT, "summon minecraft:armor_stand %.2f %.2f %.2f {ShowArms:1b,NoBasePlate:1b,Rotation:[180f,0f],Tags:[\"echoes_showcase\"],%s}", x, y, z, equipment);
			server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withLevel(b.level).withSuppressedOutput(), command);
			for (LivingEntity stand : b.level.getEntitiesOfClass(LivingEntity.class, new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 2.5, z + 0.5),
					e -> EntityType.getKey(e.getType()).getPath().equals("armor_stand"))) {
				stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(display.head()));
				stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(display.chest()));
				stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(display.legs()));
				stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(display.feet()));
				stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(display.weapon()));
			}
		}
	}
}
