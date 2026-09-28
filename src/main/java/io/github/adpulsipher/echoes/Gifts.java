package io.github.adpulsipher.echoes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Hands items to players, dropping them at their feet when their inventory is full. */
public final class Gifts {
	private Gifts() {
	}

	public static void give(Player player, ItemStack stack) {
		if (!player.getInventory().add(stack) && !stack.isEmpty()) {
			Block.popResource(player.level(), player.blockPosition(), stack);
		}
	}
}
