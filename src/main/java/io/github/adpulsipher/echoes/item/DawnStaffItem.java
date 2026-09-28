package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.combat.SpectralBolts;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Staff of the Elder Dawn fires bolts of the first light, which strike across time like any spectral weapon.
 */
public class DawnStaffItem extends Item {
	public DawnStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel serverLevel) {
			Vec3 from = player.getEyePosition().add(player.getLookAngle().scale(0.8)).add(0, -0.2, 0);
			Vec3 at = player.getEyePosition().add(player.getLookAngle().scale(40));
			SpectralBolts.fire(serverLevel, player, from, at, 2.2, 9.0f, SpectralBolts.Style.DAWN);
			stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
		}
		player.getCooldowns().addCooldown(stack, 16);
		return InteractionResult.SUCCESS;
	}
}
