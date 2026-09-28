package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.entity.AshRevenantEntity;
import io.github.adpulsipher.echoes.entity.DawnWispEntity;
import io.github.adpulsipher.echoes.entity.EchoFigureEntity;
import io.github.adpulsipher.echoes.entity.EchoKnightEntity;
import io.github.adpulsipher.echoes.entity.HierophantEntity;
import io.github.adpulsipher.echoes.entity.HollowKingEntity;
import io.github.adpulsipher.echoes.entity.ShardCrawlerEntity;
import io.github.adpulsipher.echoes.entity.SiegeColossusEntity;
import io.github.adpulsipher.echoes.entity.SpectralArcherEntity;
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

	public static final EntityType<EchoKnightEntity> ECHO_KNIGHT = register("echo_knight",
			FabricEntityType.Builder.createMob(EchoKnightEntity::new, MobCategory.MONSTER, b -> b
							.defaultAttributes(EchoKnightEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EchoKnightEntity::checkKnightSpawnRules))
					.sized(0.7f, 2.0f)
					.clientTrackingRange(8));

	public static final EntityType<SpectralArcherEntity> SPECTRAL_ARCHER = register("spectral_archer",
			FabricEntityType.Builder.createMob(SpectralArcherEntity::new, MobCategory.MONSTER, b -> b
							.defaultAttributes(SpectralArcherEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpectralArcherEntity::checkArcherSpawnRules))
					.sized(0.6f, 1.95f)
					.clientTrackingRange(8));

	public static final EntityType<AshRevenantEntity> ASH_REVENANT = register("ash_revenant",
			FabricEntityType.Builder.createMob(AshRevenantEntity::new, MobCategory.MONSTER, b -> b
							.defaultAttributes(AshRevenantEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AshRevenantEntity::checkRevenantSpawnRules))
					.sized(0.6f, 1.95f)
					.fireImmune()
					.clientTrackingRange(8));

	public static final EntityType<DawnWispEntity> DAWN_WISP = register("dawn_wisp",
			FabricEntityType.Builder.createMob(DawnWispEntity::new, MobCategory.MONSTER, b -> b
							.defaultAttributes(DawnWispEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DawnWispEntity::checkWispSpawnRules))
					.sized(0.6f, 0.6f)
					.fireImmune()
					.clientTrackingRange(8));

	public static final EntityType<ShardCrawlerEntity> SHARD_CRAWLER = register("shard_crawler",
			FabricEntityType.Builder.createMob(ShardCrawlerEntity::new, MobCategory.MONSTER, b -> b
							.defaultAttributes(ShardCrawlerEntity::createAttributes)
							.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ShardCrawlerEntity::checkCrawlerSpawnRules))
					.sized(0.8f, 0.5f)
					.clientTrackingRange(8));

	public static final EntityType<HollowKingEntity> HOLLOW_KING = register("hollow_king",
			FabricEntityType.Builder.createMob(HollowKingEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(HollowKingEntity::createAttributes))
					.sized(1.4f, 4.4f)
					.clientTrackingRange(16)
					.updateInterval(1));

	public static final EntityType<SiegeColossusEntity> SIEGE_COLOSSUS = register("siege_colossus",
			FabricEntityType.Builder.createMob(SiegeColossusEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(SiegeColossusEntity::createAttributes))
					.sized(2.8f, 5.4f)
					.fireImmune()
					.clientTrackingRange(16)
					.updateInterval(1));

	public static final EntityType<HierophantEntity> HIEROPHANT = register("hierophant",
			FabricEntityType.Builder.createMob(HierophantEntity::new, MobCategory.MONSTER, b -> b.defaultAttributes(HierophantEntity::createAttributes))
					.sized(1.1f, 3.4f)
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
