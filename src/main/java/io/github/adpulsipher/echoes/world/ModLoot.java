package io.github.adpulsipher.echoes.world;

import java.util.Set;

import io.github.adpulsipher.echoes.registry.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

/**
 * Scatters a few traces of the past through vanilla's ruins and dungeons.
 */
public final class ModLoot {
	private static final Set<ResourceKey<LootTable>> RUINS = Set.of(
			BuiltInLootTables.SIMPLE_DUNGEON,
			BuiltInLootTables.ABANDONED_MINESHAFT,
			BuiltInLootTables.ANCIENT_CITY,
			BuiltInLootTables.STRONGHOLD_LIBRARY,
			BuiltInLootTables.DESERT_PYRAMID,
			BuiltInLootTables.JUNGLE_TEMPLE,
			BuiltInLootTables.WOODLAND_MANSION
	);

	private ModLoot() {
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			if (!source.isBuiltin() || !RUINS.contains(key)) {
				return;
			}
			builder.withPool(LootPool.lootPool()
					.add(EmptyLootItem.emptyItem().setWeight(12))
					.add(LootItem.lootTableItem(ModItems.ECHO_SHARD).setWeight(5))
					.add(LootItem.lootTableItem(ModItems.ANCIENT_COIN).setWeight(4))
					.add(LootItem.lootTableItem(ModItems.ARCHAEOLOGIST_CHISEL).setWeight(2))
					.add(LootItem.lootTableItem(ModItems.RESONANCE_COMPASS).setWeight(1))
					.add(LootItem.lootTableItem(ModItems.HEIRLOOM_LOCKET).setWeight(1)));
		});
	}
}
