package io.github.adpulsipher.echoes.guide;

import io.github.adpulsipher.echoes.Gifts;
import io.github.adpulsipher.echoes.registry.ModAttachments;
import io.github.adpulsipher.echoes.registry.ModItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Every player receives the Field Guide the first time they join a world.
 */
public final class Onboarding {
	private Onboarding() {
	}

	public static void init() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> welcome(handler.getPlayer()));
	}

	public static void welcome(ServerPlayer player) {
		if (Boolean.TRUE.equals(player.getAttachedOrElse(ModAttachments.RECEIVED_GUIDE, false))) {
			return;
		}
		player.setAttached(ModAttachments.RECEIVED_GUIDE, true);
		Gifts.give(player, new ItemStack(ModItems.GUIDE_BOOK));
		player.sendSystemMessage(Component.translatable("guide.echoes_of_the_past.welcome").withStyle(Style.EMPTY.withColor(0x7FE9FF).withItalic(true)));
	}
}
