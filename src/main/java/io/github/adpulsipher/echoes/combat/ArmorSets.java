package io.github.adpulsipher.echoes.combat;

import java.util.List;
import java.util.function.Consumer;

import io.github.adpulsipher.echoes.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/**
 * Full-set bonuses. Every second, anyone wearing a complete set is granted its blessings.
 */
public final class ArmorSets {
	public record ArmorSet(String id, Item helmet, Item chestplate, Item leggings, Item boots, int color, Consumer<ServerPlayer> bonus) {
		public boolean isWornBy(LivingEntity entity) {
			return entity.getItemBySlot(EquipmentSlot.HEAD).is(helmet)
					&& entity.getItemBySlot(EquipmentSlot.CHEST).is(chestplate)
					&& entity.getItemBySlot(EquipmentSlot.LEGS).is(leggings)
					&& entity.getItemBySlot(EquipmentSlot.FEET).is(boots);
		}

		public Component bonusDescription() {
			return Component.translatable("armor_set.echoes_of_the_past." + id).withStyle(Style.EMPTY.withColor(color));
		}
	}

	public static final ArmorSet WYRMSCALE = new ArmorSet("wyrmscale", ModItems.WYRMSCALE_HELMET, ModItems.WYRMSCALE_CHESTPLATE,
			ModItems.WYRMSCALE_LEGGINGS, ModItems.WYRMSCALE_BOOTS, 0xC3A8FF, player -> {
				give(player, MobEffects.FIRE_RESISTANCE, 0);
				if (player.fallDistance > 3.0 && !player.isShiftKeyDown()) {
					give(player, MobEffects.SLOW_FALLING, 0);
				}
			});
	public static final ArmorSet SPECTRAL_KNIGHT = new ArmorSet("spectral_knight", ModItems.SPECTRAL_KNIGHT_HELMET, ModItems.SPECTRAL_KNIGHT_CHESTPLATE,
			ModItems.SPECTRAL_KNIGHT_LEGGINGS, ModItems.SPECTRAL_KNIGHT_BOOTS, 0x8FEFFF, player -> give(player, MobEffects.SPEED, 0));
	public static final ArmorSet CINDERSTEEL = new ArmorSet("cindersteel", ModItems.CINDERSTEEL_HELMET, ModItems.CINDERSTEEL_CHESTPLATE,
			ModItems.CINDERSTEEL_LEGGINGS, ModItems.CINDERSTEEL_BOOTS, 0xFF9A4A, player -> {
				give(player, MobEffects.FIRE_RESISTANCE, 0);
				give(player, MobEffects.STRENGTH, 0);
			});
	public static final ArmorSet COLOSSUS = new ArmorSet("colossus", ModItems.COLOSSUS_HELMET, ModItems.COLOSSUS_CHESTPLATE,
			ModItems.COLOSSUS_LEGGINGS, ModItems.COLOSSUS_BOOTS, 0xB8C0CC, player -> give(player, MobEffects.RESISTANCE, 0));
	public static final ArmorSet DAWNWEAVE = new ArmorSet("dawnweave", ModItems.DAWNWEAVE_HOOD, ModItems.DAWNWEAVE_ROBE,
			ModItems.DAWNWEAVE_LEGGINGS, ModItems.DAWNWEAVE_SLIPPERS, 0xFFE7A0, player -> {
				player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 15, 0, true, false, true));
				if (player.tickCount % 160 < 20) {
					give(player, MobEffects.REGENERATION, 0);
				}
			});

	public static final List<ArmorSet> ALL = List.of(WYRMSCALE, SPECTRAL_KNIGHT, CINDERSTEEL, COLOSSUS, DAWNWEAVE);

	private ArmorSets() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!player.isAlive()) {
					continue;
				}
				for (ArmorSet set : ALL) {
					if (set.isWornBy(player)) {
						set.bonus().accept(player);
					}
				}
			}
		});
	}

	public static boolean wearsFull(LivingEntity entity, ArmorSet set) {
		return set.isWornBy(entity);
	}

	private static void give(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
		player.addEffect(new MobEffectInstance(effect, 50, amplifier, true, false, true));
	}
}
