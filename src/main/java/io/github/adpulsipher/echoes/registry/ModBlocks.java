package io.github.adpulsipher.echoes.registry;

import java.util.function.Function;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.block.EchoCrystalBlock;
import io.github.adpulsipher.echoes.block.EchoDepositBlock;
import io.github.adpulsipher.echoes.block.EchoProjectorBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
	public static final Block ECHO_DEPOSIT = register("echo_deposit", EchoDepositBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE).lightLevel(state -> 4).sound(SoundType.AMETHYST));
	public static final Block DEEPSLATE_ECHO_DEPOSIT = register("deepslate_echo_deposit", EchoDepositBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE).lightLevel(state -> 5).sound(SoundType.AMETHYST));
	public static final Block ECHO_CRYSTAL_BLOCK = register("echo_crystal_block", EchoCrystalBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.5f).sound(SoundType.AMETHYST).lightLevel(state -> 10).requiresCorrectToolForDrops());
	public static final Block ECHO_PROJECTOR = register("echo_projector", EchoProjectorBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.0f, 6.0f).sound(SoundType.COPPER).noOcclusion()
					.requiresCorrectToolForDrops().lightLevel(state -> state.getValue(EchoProjectorBlock.ACTIVE) ? 13 : 4));
	public static final Block ECHO_LANTERN = register("echo_lantern", LanternBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN).lightLevel(state -> 14));
	public static final Block RELIC_CACHE_SOIL = register("relic_cache_soil",
			props -> new BrushableBlock(Blocks.COARSE_DIRT, SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, props),
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.6f).sound(SoundType.SUSPICIOUS_GRAVEL).pushReaction(PushReaction.DESTROY));
	public static final Block RELIC_CACHE_STONE = register("relic_cache_stone",
			props -> new BrushableBlock(Blocks.COBBLESTONE, SoundEvents.BRUSH_GRAVEL, SoundEvents.BRUSH_GRAVEL_COMPLETED, props),
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.9f).sound(SoundType.SUSPICIOUS_GRAVEL).pushReaction(PushReaction.DESTROY));

	private ModBlocks() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		Identifier id = EchoesOfThePast.id(name);
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.setId(blockKey)));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
		return block;
	}

	public static void init() {
	}
}
