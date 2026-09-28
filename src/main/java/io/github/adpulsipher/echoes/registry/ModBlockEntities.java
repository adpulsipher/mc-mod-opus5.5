package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public final class ModBlockEntities {
	public static final BlockEntityType<EchoProjectorBlockEntity> ECHO_PROJECTOR = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			EchoesOfThePast.id("echo_projector"),
			FabricBlockEntityTypeBuilder.<EchoProjectorBlockEntity>create(EchoProjectorBlockEntity::new, ModBlocks.ECHO_PROJECTOR).build()
	);

	private ModBlockEntities() {
	}

	public static void init() {
		// Relic caches reuse vanilla's brushable block entity, so the vanilla brush and renderer just work.
		BlockEntityTypes.BRUSHABLE_BLOCK.addValidBlock(ModBlocks.RELIC_CACHE_SOIL);
		BlockEntityTypes.BRUSHABLE_BLOCK.addValidBlock(ModBlocks.RELIC_CACHE_STONE);
	}
}
