package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Hollow King's greatsword. Raised high, it issues a Royal Decree: every hostile creature nearby is humbled
 * (slowed and weakened) while its bearer is emboldened.
 */
public class CrownbreakerItem extends Item {
	private static final DustParticleOptions ROYAL = new DustParticleOptions(0xFFD35A, 1.4f);

	public CrownbreakerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 40; i++) {
				double angle = i * Math.PI * 2 / 40;
				serverLevel.sendParticles(ROYAL, player.getX() + Math.cos(angle) * 8, player.getY() + 0.3, player.getZ() + Math.sin(angle) * 8, 1, 0, 0.2, 0, 0);
			}
			serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(1.2), player.getZ(), 20, 0.3, 0.6, 0.3, 0.1);
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.KING_DECREE, SoundSource.PLAYERS, 1.2f, 1.2f);
			for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0), e -> e instanceof Enemy && e.isAlive())) {
				victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), player);
				victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1), player);
				serverLevel.sendParticles(ROYAL, victim.getX(), victim.getY(1.0), victim.getZ(), 8, 0.3, 0.3, 0.3, 0);
			}
			player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0));
			stack.hurtAndBreak(2, player, hand == InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
		}
		player.getCooldowns().addCooldown(stack, 20 * 20);
		return InteractionResult.SUCCESS;
	}
}
