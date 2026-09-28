package io.github.adpulsipher.echoes.registry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.entity.BossKind;
import io.github.adpulsipher.echoes.guide.GuideBook;
import io.github.adpulsipher.echoes.item.AshenCleaverItem;
import io.github.adpulsipher.echoes.item.ChiselItem;
import io.github.adpulsipher.echoes.item.CrownbreakerItem;
import io.github.adpulsipher.echoes.item.DawnStaffItem;
import io.github.adpulsipher.echoes.item.GuideBookItem;
import io.github.adpulsipher.echoes.item.KeystoneItem;
import io.github.adpulsipher.echoes.item.ShowcaseBookItem;
import io.github.adpulsipher.echoes.item.SiegebreakerItem;
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

	public static final ToolMaterial SPECTRAL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1000, 7.0f, 3.0f, 16, ModTags.REPAIRS_SPECTRAL);
	public static final ToolMaterial CINDERSTEEL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1100, 7.5f, 3.0f, 12, ModTags.REPAIRS_CINDERSTEEL);
	public static final ToolMaterial ROYAL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 8.0f, 4.0f, 22, ModTags.REPAIRS_ROYAL);
	public static final ToolMaterial COLOSSUS_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 8.0f, 4.0f, 10, ModTags.REPAIRS_COLOSSUS);

	public static final ResourceKey<EquipmentAsset> WYRMSCALE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, EchoesOfThePast.id("wyrmscale"));
	public static final ResourceKey<EquipmentAsset> CROWN_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, EchoesOfThePast.id("tarnished_crown"));

	public static final ResourceKey<EquipmentAsset> SPECTRAL_KNIGHT_ASSET = asset("spectral_knight");
	public static final ResourceKey<EquipmentAsset> CINDERSTEEL_ASSET = asset("cindersteel");
	public static final ResourceKey<EquipmentAsset> COLOSSUS_ASSET = asset("colossus");
	public static final ResourceKey<EquipmentAsset> DAWNWEAVE_ASSET = asset("dawnweave");
	public static final ResourceKey<EquipmentAsset> HOLLOW_CROWN_ASSET = asset("hollow_crown");

	public static final ArmorMaterial SPECTRAL_KNIGHT_ARMOR = new ArmorMaterial(25,
			Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 7, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2, ArmorType.BODY, 6),
			16, SoundEvents.ARMOR_EQUIP_CHAIN, 1.0f, 0.0f, ModTags.REPAIRS_SPECTRAL, SPECTRAL_KNIGHT_ASSET);
	public static final ArmorMaterial CINDERSTEEL_ARMOR = new ArmorMaterial(28,
			Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 7, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2, ArmorType.BODY, 7),
			12, SoundEvents.ARMOR_EQUIP_IRON, 1.5f, 0.0f, ModTags.REPAIRS_CINDERSTEEL, CINDERSTEEL_ASSET);
	public static final ArmorMaterial COLOSSUS_ARMOR = new ArmorMaterial(38,
			Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3, ArmorType.BODY, 11),
			10, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.5f, 0.2f, ModTags.REPAIRS_COLOSSUS, COLOSSUS_ASSET);
	public static final ArmorMaterial DAWNWEAVE_ARMOR = new ArmorMaterial(30,
			Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 7, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 3, ArmorType.BODY, 8),
			25, SoundEvents.ARMOR_EQUIP_LEATHER, 2.0f, 0.0f, ModTags.REPAIRS_DAWNWEAVE, DAWNWEAVE_ASSET);
	public static final ArmorMaterial HOLLOW_CROWN_ARMOR = new ArmorMaterial(30,
			Map.of(ArmorType.HELMET, 4, ArmorType.CHESTPLATE, 4, ArmorType.LEGGINGS, 4, ArmorType.BOOTS, 4, ArmorType.BODY, 4),
			25, SoundEvents.ARMOR_EQUIP_GOLD, 2.0f, 0.1f, ModTags.REPAIRS_ROYAL, HOLLOW_CROWN_ASSET);

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
	public static final Item WYRMSCALE_HELMET = register("wyrmscale_helmet", Item::new, armor(WYRMSCALE_ARMOR, ArmorType.HELMET, 35, Rarity.RARE).fireResistant());
	public static final Item WYRMSCALE_CHESTPLATE = register("wyrmscale_chestplate", Item::new, armor(WYRMSCALE_ARMOR, ArmorType.CHESTPLATE, 35, Rarity.RARE).fireResistant());
	public static final Item WYRMSCALE_LEGGINGS = register("wyrmscale_leggings", Item::new, armor(WYRMSCALE_ARMOR, ArmorType.LEGGINGS, 35, Rarity.RARE).fireResistant());
	public static final Item WYRMSCALE_BOOTS = register("wyrmscale_boots", Item::new, armor(WYRMSCALE_ARMOR, ArmorType.BOOTS, 35, Rarity.RARE).fireResistant());
	public static final Item MUSIC_DISC_ECHOES = register("music_disc_echoes", Item::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ECHOES_SONG));

	// Materials of the great echoes and their servants
	public static final Item SPECTRAL_PLATE = register("spectral_plate", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item REVENANT_ASH = register("revenant_ash", Item::new, new Item.Properties());
	public static final Item CINDERSTEEL_INGOT = register("cindersteel_ingot", Item::new, new Item.Properties().fireResistant());
	public static final Item COLOSSUS_PLATING = register("colossus_plating", Item::new, new Item.Properties().rarity(Rarity.RARE).fireResistant());
	public static final Item COLOSSUS_CORE = register("colossus_core", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
	public static final Item DAWNSTONE = register("dawnstone", Item::new, new Item.Properties().rarity(Rarity.RARE));
	public static final Item ROYAL_SIGIL = register("royal_sigil", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

	// Weapons
	public static final Item SPECTRAL_LONGSWORD = register("spectral_longsword", Item::new,
			new Item.Properties().sword(SPECTRAL_MATERIAL, 3.0f, -2.4f).rarity(Rarity.UNCOMMON));
	public static final Item ASHEN_CLEAVER = register("ashen_cleaver", AshenCleaverItem::new,
			new Item.Properties().sword(CINDERSTEEL_MATERIAL, 4.0f, -2.8f).rarity(Rarity.UNCOMMON).fireResistant());
	public static final Item CROWNBREAKER = register("crownbreaker", CrownbreakerItem::new,
			new Item.Properties().sword(ROYAL_MATERIAL, 5.0f, -2.8f).rarity(Rarity.EPIC));
	public static final Item SIEGEBREAKER = register("siegebreaker", SiegebreakerItem::new,
			new Item.Properties().sword(COLOSSUS_MATERIAL, 6.0f, -3.1f).rarity(Rarity.EPIC).fireResistant());
	public static final Item DAWN_STAFF = register("dawn_staff", DawnStaffItem::new,
			new Item.Properties().durability(500).rarity(Rarity.EPIC).repairable(ModTags.REPAIRS_DAWNWEAVE));

	// Armor sets
	public static final Item HOLLOW_CROWN = register("hollow_crown", Item::new, armor(HOLLOW_CROWN_ARMOR, ArmorType.HELMET, 30, Rarity.EPIC));
	public static final Item SPECTRAL_KNIGHT_HELMET = register("spectral_knight_helmet", Item::new, armor(SPECTRAL_KNIGHT_ARMOR, ArmorType.HELMET, 25, Rarity.UNCOMMON));
	public static final Item SPECTRAL_KNIGHT_CHESTPLATE = register("spectral_knight_chestplate", Item::new, armor(SPECTRAL_KNIGHT_ARMOR, ArmorType.CHESTPLATE, 25, Rarity.UNCOMMON));
	public static final Item SPECTRAL_KNIGHT_LEGGINGS = register("spectral_knight_leggings", Item::new, armor(SPECTRAL_KNIGHT_ARMOR, ArmorType.LEGGINGS, 25, Rarity.UNCOMMON));
	public static final Item SPECTRAL_KNIGHT_BOOTS = register("spectral_knight_boots", Item::new, armor(SPECTRAL_KNIGHT_ARMOR, ArmorType.BOOTS, 25, Rarity.UNCOMMON));
	public static final Item CINDERSTEEL_HELMET = register("cindersteel_helmet", Item::new, armor(CINDERSTEEL_ARMOR, ArmorType.HELMET, 28, Rarity.UNCOMMON).fireResistant());
	public static final Item CINDERSTEEL_CHESTPLATE = register("cindersteel_chestplate", Item::new, armor(CINDERSTEEL_ARMOR, ArmorType.CHESTPLATE, 28, Rarity.UNCOMMON).fireResistant());
	public static final Item CINDERSTEEL_LEGGINGS = register("cindersteel_leggings", Item::new, armor(CINDERSTEEL_ARMOR, ArmorType.LEGGINGS, 28, Rarity.UNCOMMON).fireResistant());
	public static final Item CINDERSTEEL_BOOTS = register("cindersteel_boots", Item::new, armor(CINDERSTEEL_ARMOR, ArmorType.BOOTS, 28, Rarity.UNCOMMON).fireResistant());
	public static final Item COLOSSUS_HELMET = register("colossus_helmet", Item::new, armor(COLOSSUS_ARMOR, ArmorType.HELMET, 38, Rarity.RARE).fireResistant());
	public static final Item COLOSSUS_CHESTPLATE = register("colossus_chestplate", Item::new, armor(COLOSSUS_ARMOR, ArmorType.CHESTPLATE, 38, Rarity.RARE).fireResistant());
	public static final Item COLOSSUS_LEGGINGS = register("colossus_leggings", Item::new, armor(COLOSSUS_ARMOR, ArmorType.LEGGINGS, 38, Rarity.RARE).fireResistant());
	public static final Item COLOSSUS_BOOTS = register("colossus_boots", Item::new, armor(COLOSSUS_ARMOR, ArmorType.BOOTS, 38, Rarity.RARE).fireResistant());
	public static final Item DAWNWEAVE_HOOD = register("dawnweave_hood", Item::new, armor(DAWNWEAVE_ARMOR, ArmorType.HELMET, 30, Rarity.RARE));
	public static final Item DAWNWEAVE_ROBE = register("dawnweave_robe", Item::new, armor(DAWNWEAVE_ARMOR, ArmorType.CHESTPLATE, 30, Rarity.RARE));
	public static final Item DAWNWEAVE_LEGGINGS = register("dawnweave_leggings", Item::new, armor(DAWNWEAVE_ARMOR, ArmorType.LEGGINGS, 30, Rarity.RARE));
	public static final Item DAWNWEAVE_SLIPPERS = register("dawnweave_slippers", Item::new, armor(DAWNWEAVE_ARMOR, ArmorType.BOOTS, 30, Rarity.RARE));

	// Keystones
	public static final Item KEYSTONE_OF_CROWNS = register("keystone_of_crowns", p -> new KeystoneItem(BossKind.HOLLOW_KING, p), keystone());
	public static final Item KEYSTONE_OF_IRON = register("keystone_of_iron", p -> new KeystoneItem(BossKind.SIEGE_COLOSSUS, p), keystone());
	public static final Item KEYSTONE_OF_DRAGONS = register("keystone_of_dragons", p -> new KeystoneItem(BossKind.ECHO_WYRM, p), keystone());
	public static final Item KEYSTONE_OF_DAWN = register("keystone_of_dawn", p -> new KeystoneItem(BossKind.HIEROPHANT, p), keystone());

	// Books
	public static final Item GUIDE_BOOK = register("guide_book", GuideBookItem::new, new Item.Properties().stacksTo(1)
			.component(DataComponents.WRITTEN_BOOK_CONTENT, GuideBook.content()));
	public static final Item SHOWCASE_BOOK = register("showcase_book", ShowcaseBookItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

	// Spawn eggs
	public static final Item LINGERER_SPAWN_EGG = register("lingerer_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.LINGERER));
	public static final Item MEMORY_MOTH_SPAWN_EGG = register("memory_moth_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.MEMORY_MOTH));
	public static final Item ECHO_WYRM_SPAWN_EGG = register("echo_wyrm_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.ECHO_WYRM));
	public static final Item ECHO_KNIGHT_SPAWN_EGG = register("echo_knight_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.ECHO_KNIGHT));
	public static final Item SPECTRAL_ARCHER_SPAWN_EGG = register("spectral_archer_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SPECTRAL_ARCHER));
	public static final Item ASH_REVENANT_SPAWN_EGG = register("ash_revenant_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.ASH_REVENANT));
	public static final Item DAWN_WISP_SPAWN_EGG = register("dawn_wisp_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.DAWN_WISP));
	public static final Item SHARD_CRAWLER_SPAWN_EGG = register("shard_crawler_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SHARD_CRAWLER));
	public static final Item HOLLOW_KING_SPAWN_EGG = register("hollow_king_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.HOLLOW_KING));
	public static final Item SIEGE_COLOSSUS_SPAWN_EGG = register("siege_colossus_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SIEGE_COLOSSUS));
	public static final Item HIEROPHANT_SPAWN_EGG = register("hierophant_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.HIEROPHANT));

	private ModItems() {
	}

	private static ResourceKey<EquipmentAsset> asset(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, EchoesOfThePast.id(name));
	}

	private static Item.Properties armor(ArmorMaterial material, ArmorType type, int durability, Rarity rarity) {
		return new Item.Properties().humanoidArmor(material, type).durability(type.getDurability(durability)).rarity(rarity);
	}

	private static Item.Properties keystone() {
		return new Item.Properties().stacksTo(16).rarity(Rarity.EPIC);
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, EchoesOfThePast.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
	}
}
