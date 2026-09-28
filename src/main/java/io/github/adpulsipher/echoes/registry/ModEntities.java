package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.entity.EchoFigureEntity;
import io.github.adpulsipher.echoes.entity.EchoWyrmEntity;
import io.github.adpulsipher.echoes.entity.LingererEntity;
import io.github.adpulsipher.echoes.entity.MemoryMothEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
	public static final EntityType<EchoFigureEntity> ECHO_FIGURE = register("echo_figure",
			FabricEntityType.Builder.createMob(EchoFigureEntity::new, MobCategory.MISC, b -> b.defaultAttributes(EchoFigureEntity::createAttributes))
					.sized(0.6f, 1.8f)
					.clientTrackingRange(10)
					.updateInterval(1));

	public static final EntityType<LingererEntity> LINGERER = register("lingerer",
			FabricEntityType.Builder.createMob(LingererEntity::new, MobCategory.MONSTER, b -> b
							.defaultAttributes(LingererEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LingererEntity::checkLingererSpawnRules))
					.sized(0.6f, 1.95f)
					.clientTrackingRange(8));

	public static final EntityType<MemoryMothEntity> MEMORY_MOTH = register("memory_moth",
			FabricEntityType.Builder.createMob(MemoryMothEntity::new, MobCategory.AMBIENT, b -> b
							.defaultAttributes(MemoryMothEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MemoryMothEntity::checkMothSpawnRules))
					.sized(0.5f, 0.4f)
					.clientTrackingRange(8));

	public static final EntityType<EchoWyrmEntity> ECHO_WYRM = register("echo_wyrm",
			FabricEntityType.Builder.createMob(EchoWyrmEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(EchoWyrmEntity::createAttributes))
					.sized(3.0f, 2.2f)
					.fireImmune()
					.clientTrackingRange(16)
					.updateInterval(1));

	private ModEntities() {
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, EchoesOfThePast.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
	}
}
