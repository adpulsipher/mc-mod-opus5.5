package io.github.adpulsipher.echoes.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A cleaver of cindersteel that never quite cools. Whatever it cuts catches fire.
 */
public class AshenCleaverItem extends Item {
	public AshenCleaverItem(Properties properties) {
		super(properties);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		target.igniteForSeconds(4.0f);
		if (attacker.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.5), target.getZ(), 10, 0.3, 0.4, 0.3, 0.03);
		}
	}
}
