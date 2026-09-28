package io.github.adpulsipher.echoes.world;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldgen {
	public static final ResourceKey<PlacedFeature> ECHO_DEPOSITS = ResourceKey.create(Registries.PLACED_FEATURE, EchoesOfThePast.id("echo_deposits"));
	public static final ResourceKey<PlacedFeature> ECHO_DEPOSITS_DEEP = ResourceKey.create(Registries.PLACED_FEATURE, EchoesOfThePast.id("echo_deposits_deep"));

	private ModWorldgen() {
	}

	public static void init() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, ECHO_DEPOSITS);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, ECHO_DEPOSITS_DEEP);
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, ModEntities.LINGERER, 12, 1, 2);
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.AMBIENT, ModEntities.MEMORY_MOTH, 10, 1, 3);
	}
}
