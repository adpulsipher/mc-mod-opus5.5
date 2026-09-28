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
				output.accept(ModItems.GUIDE_BOOK);
				output.accept(ModItems.SHOWCASE_BOOK);
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
				output.accept(ModItems.SPECTRAL_PLATE);
				output.accept(ModItems.REVENANT_ASH);
				output.accept(ModItems.CINDERSTEEL_INGOT);
				output.accept(ModItems.COLOSSUS_PLATING);
				output.accept(ModItems.COLOSSUS_CORE);
				output.accept(ModItems.DAWNSTONE);
				output.accept(ModItems.ROYAL_SIGIL);
				output.accept(ModItems.SPECTRAL_LONGSWORD);
				output.accept(ModItems.ASHEN_CLEAVER);
				output.accept(ModItems.CROWNBREAKER);
				output.accept(ModItems.SIEGEBREAKER);
				output.accept(ModItems.DAWN_STAFF);
				output.accept(ModItems.HOLLOW_CROWN);
				output.accept(ModItems.SPECTRAL_KNIGHT_HELMET);
				output.accept(ModItems.SPECTRAL_KNIGHT_CHESTPLATE);
				output.accept(ModItems.SPECTRAL_KNIGHT_LEGGINGS);
				output.accept(ModItems.SPECTRAL_KNIGHT_BOOTS);
				output.accept(ModItems.CINDERSTEEL_HELMET);
				output.accept(ModItems.CINDERSTEEL_CHESTPLATE);
				output.accept(ModItems.CINDERSTEEL_LEGGINGS);
				output.accept(ModItems.CINDERSTEEL_BOOTS);
				output.accept(ModItems.COLOSSUS_HELMET);
				output.accept(ModItems.COLOSSUS_CHESTPLATE);
				output.accept(ModItems.COLOSSUS_LEGGINGS);
				output.accept(ModItems.COLOSSUS_BOOTS);
				output.accept(ModItems.DAWNWEAVE_HOOD);
				output.accept(ModItems.DAWNWEAVE_ROBE);
				output.accept(ModItems.DAWNWEAVE_LEGGINGS);
				output.accept(ModItems.DAWNWEAVE_SLIPPERS);
				output.accept(ModItems.KEYSTONE_OF_CROWNS);
				output.accept(ModItems.KEYSTONE_OF_IRON);
				output.accept(ModItems.KEYSTONE_OF_DRAGONS);
				output.accept(ModItems.KEYSTONE_OF_DAWN);
				output.accept(ModItems.LINGERER_SPAWN_EGG);
				output.accept(ModItems.MEMORY_MOTH_SPAWN_EGG);
				output.accept(ModItems.ECHO_WYRM_SPAWN_EGG);
				output.accept(ModItems.ECHO_KNIGHT_SPAWN_EGG);
				output.accept(ModItems.SPECTRAL_ARCHER_SPAWN_EGG);
				output.accept(ModItems.ASH_REVENANT_SPAWN_EGG);
				output.accept(ModItems.DAWN_WISP_SPAWN_EGG);
				output.accept(ModItems.SHARD_CRAWLER_SPAWN_EGG);
				output.accept(ModItems.HOLLOW_KING_SPAWN_EGG);
				output.accept(ModItems.SIEGE_COLOSSUS_SPAWN_EGG);
				output.accept(ModItems.HIEROPHANT_SPAWN_EGG);
			})
			.build();

	private ModCreativeTab() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, TAB);
	}
}
