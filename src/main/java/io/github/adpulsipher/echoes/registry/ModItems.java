package io.github.adpulsipher.echoes.registry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.item.ChiselItem;
import io.github.adpulsipher.echoes.item.EchoingBladeItem;
import io.github.adpulsipher.echoes.item.HeirloomLocketItem;
import io.github.adpulsipher.echoes.item.ResonanceCompassItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class ModItems {
	public static final ToolMaterial RELIC_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 900, 7.0f, 2.5f, 18, ModTags.REPAIRS_RELIC);
	public static final ToolMaterial ECHO_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 9.0f, 4.0f, 22, ModTags.REPAIRS_WYRMSCALE);

	public static final ResourceKey<EquipmentAsset> WYRMSCALE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, EchoesOfThePast.id("wyrmscale"));
	public static final ResourceKey<EquipmentAsset> CROWN_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, EchoesOfThePast.id("tarnished_crown"));

	public static final ArmorMaterial WYRMSCALE_ARMOR = new ArmorMaterial(35,
			Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3, ArmorType.BODY, 11),
			20, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f, ModTags.REPAIRS_WYRMSCALE, WYRMSCALE_ASSET);
	public static final ArmorMaterial CROWN_ARMOR = new ArmorMaterial(12,
			Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 2, ArmorType.LEGGINGS, 2, ArmorType.BOOTS, 1, ArmorType.BODY, 2),
			25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0f, 0.0f, ModTags.REPAIRS_RELIC, CROWN_ASSET);

	public static final ResourceKey<JukeboxSong> ECHOES_SONG = ResourceKey.create(Registries.JUKEBOX_SONG, EchoesOfThePast.id("echoes"));

	// Materials
	public static final Item ECHO_SHARD = register("echo_shard", Item::new, new Item.Properties());
	public static final Item ECHO_DUST = register("echo_dust", Item::new, new Item.Properties());
	public static final Item ECHO_BLOCK = register("echo_block", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	public static final Item ANCIENT_COIN = register("ancient_coin", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item WYRMSCALE = register("wyrmscale", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
	public static final Item WYRM_HEART = register("wyrm_heart", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

	// Tools
	public static final Item ARCHAEOLOGIST_CHISEL = register("archaeologist_chisel", ChiselItem::new, new Item.Properties().durability(128));
	public static final Item RESONANCE_COMPASS = register("resonance_compass", ResonanceCompassItem::new, new Item.Properties().stacksTo(1));

	// Relics
	public static final Item LEGIONNAIRE_BLADE = register("legionnaire_blade", Item::new,
			new Item.Properties().sword(RELIC_MATERIAL, 3.0f, -2.2f).rarity(Rarity.UNCOMMON));
	public static final Item TARNISHED_CROWN = register("tarnished_crown", Item::new,
			new Item.Properties().humanoidArmor(CROWN_ARMOR, ArmorType.HELMET).durability(ArmorType.HELMET.getDurability(12)).rarity(Rarity.RARE));
	public static final Item HEIRLOOM_LOCKET = register("heirloom_locket", HeirloomLocketItem::new, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
	public static final Item FESTIVAL_CHARM = register("festival_charm", Item::new, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)
			.component(DataComponents.CONSUMABLE, Consumable.builder()
					.consumeSeconds(0.8f)
					.animation(ItemUseAnimation.TOOT_HORN)
					.sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_CHIME))
					.hasConsumeParticles(false)
					.onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
							new MobEffectInstance(MobEffects.REGENERATION, 20 * 12, 0),
							new MobEffectInstance(MobEffects.LUCK, 20 * 180, 0),
							new MobEffectInstance(MobEffects.SPEED, 20 * 60, 0))))
					.build()));

	// Wyrm gear
	public static final Item ECHOING_BLADE = register("echoing_blade", EchoingBladeItem::new,
			new Item.Properties().sword(ECHO_MATERIAL, 3.0f, -2.4f).rarity(Rarity.EPIC).fireResistant());
	public static final Item WYRMSCALE_HELMET = register("wyrmscale_helmet", Item::new, armor(ArmorType.HELMET));
	public static final Item WYRMSCALE_CHESTPLATE = register("wyrmscale_chestplate", Item::new, armor(ArmorType.CHESTPLATE));
	public static final Item WYRMSCALE_LEGGINGS = register("wyrmscale_leggings", Item::new, armor(ArmorType.LEGGINGS));
	public static final Item WYRMSCALE_BOOTS = register("wyrmscale_boots", Item::new, armor(ArmorType.BOOTS));
	public static final Item MUSIC_DISC_ECHOES = register("music_disc_echoes", Item::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ECHOES_SONG));

	// Spawn eggs
	public static final Item LINGERER_SPAWN_EGG = register("lingerer_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.LINGERER));
	public static final Item MEMORY_MOTH_SPAWN_EGG = register("memory_moth_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.MEMORY_MOTH));
	public static final Item ECHO_WYRM_SPAWN_EGG = register("echo_wyrm_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.ECHO_WYRM));

	private ModItems() {
	}

	private static Item.Properties armor(ArmorType type) {
		return new Item.Properties().humanoidArmor(WYRMSCALE_ARMOR, type).durability(type.getDurability(35)).rarity(Rarity.RARE).fireResistant();
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, EchoesOfThePast.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
	}
}
