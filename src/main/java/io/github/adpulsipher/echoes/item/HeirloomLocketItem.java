package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.entity.LingererEntity;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A locket some family buried when they fled. Shown to a Lingerer, it reminds the ghost of home, and it finally
 * rests.
 */
public class HeirloomLocketItem extends Item {
	public HeirloomLocketItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof LingererEntity lingerer) || !lingerer.isAlive()) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			lingerer.layToRest(serverPlayer);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			ReplayOutcome.grant(serverPlayer, "laid_to_rest", "rested");
		}
		return InteractionResult.SUCCESS;
	}
}
