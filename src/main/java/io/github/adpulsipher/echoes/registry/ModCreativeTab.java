package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
	public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), EchoesOfThePast.id("echoes"));

	public static final CreativeModeTab TAB = FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(ModBlocks.ECHO_PROJECTOR))
			.title(Component.translatable("itemGroup.echoes_of_the_past"))
			.displayItems((params, output) -> {
				output.accept(ModBlocks.ECHO_DEPOSIT);
				output.accept(ModBlocks.DEEPSLATE_ECHO_DEPOSIT);
				output.accept(ModBlocks.ECHO_PROJECTOR);
				output.accept(ModBlocks.ECHO_CRYSTAL_BLOCK);
				output.accept(ModBlocks.ECHO_LANTERN);
				output.accept(ModBlocks.RELIC_CACHE_SOIL);
				output.accept(ModBlocks.RELIC_CACHE_STONE);
				output.accept(ModItems.ECHO_SHARD);
				output.accept(ModItems.ECHO_DUST);
				output.accept(ModItems.ARCHAEOLOGIST_CHISEL);
				output.accept(ModItems.RESONANCE_COMPASS);
				output.accept(ModItems.ANCIENT_COIN);
				output.accept(ModItems.LEGIONNAIRE_BLADE);
				output.accept(ModItems.TARNISHED_CROWN);
				output.accept(ModItems.HEIRLOOM_LOCKET);
				output.accept(ModItems.FESTIVAL_CHARM);
				output.accept(ModItems.WYRMSCALE);
				output.accept(ModItems.WYRM_HEART);
				output.accept(ModItems.ECHOING_BLADE);
				output.accept(ModItems.WYRMSCALE_HELMET);
				output.accept(ModItems.WYRMSCALE_CHESTPLATE);
				output.accept(ModItems.WYRMSCALE_LEGGINGS);
				output.accept(ModItems.WYRMSCALE_BOOTS);
				output.accept(ModItems.MUSIC_DISC_ECHOES);
				output.accept(ModItems.LINGERER_SPAWN_EGG);
				output.accept(ModItems.MEMORY_MOTH_SPAWN_EGG);
				output.accept(ModItems.ECHO_WYRM_SPAWN_EGG);
			})
			.build();

	private ModCreativeTab() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, TAB);
	}
}
