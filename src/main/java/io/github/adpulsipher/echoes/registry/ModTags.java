package io.github.adpulsipher.echoes.registry;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
	public static final TagKey<Block> ECHO_DEPOSITS = TagKey.create(Registries.BLOCK, EchoesOfThePast.id("echo_deposits"));
	public static final TagKey<Block> INCORRECT_FOR_RELIC_TOOL = TagKey.create(Registries.BLOCK, EchoesOfThePast.id("incorrect_for_relic_tool"));
	public static final TagKey<Item> SPECTRAL_WEAPONS = TagKey.create(Registries.ITEM, EchoesOfThePast.id("spectral_weapons"));
	public static final TagKey<Item> REPAIRS_WYRMSCALE = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_wyrmscale"));
	public static final TagKey<Item> REPAIRS_RELIC = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_relic"));
	public static final TagKey<Item> REPAIRS_SPECTRAL = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_spectral"));
	public static final TagKey<Item> REPAIRS_CINDERSTEEL = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_cindersteel"));
	public static final TagKey<Item> REPAIRS_ROYAL = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_royal"));
	public static final TagKey<Item> REPAIRS_COLOSSUS = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_colossus"));
	public static final TagKey<Item> REPAIRS_DAWNWEAVE = TagKey.create(Registries.ITEM, EchoesOfThePast.id("repairs_dawnweave"));
	public static final TagKey<Item> CROWNS = TagKey.create(Registries.ITEM, EchoesOfThePast.id("crowns"));

	private ModTags() {
	}
}
