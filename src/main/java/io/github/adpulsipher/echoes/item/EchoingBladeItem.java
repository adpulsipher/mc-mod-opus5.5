package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.EchoStrikes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A blade forged around the heart of an Echo Wyrm. Every blow it lands is remembered, and struck again a moment
 * later by a ghostly afterimage.
 */
public class EchoingBladeItem extends Item {
	public EchoingBladeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (attacker.level() instanceof ServerLevel level) {
			EchoStrikes.schedule(level, target, attacker, 5.0f);
		}
	}
}
