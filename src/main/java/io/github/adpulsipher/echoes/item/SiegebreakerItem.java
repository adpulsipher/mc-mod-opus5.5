package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A maul hammered from the Siege Colossus's furnace core. Its blows hurl foes back, and brought down on the earth
 * it sends out a shockwave like the Colossus's own.
 */
public class SiegebreakerItem extends Item {
	public SiegebreakerItem(Properties properties) {
		super(properties);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		double dx = target.getX() - attacker.getX();
		double dz = target.getZ() - attacker.getZ();
		double len = Math.max(0.3, Math.sqrt(dx * dx + dz * dz));
		target.push(dx / len * 0.6, 0.25, dz / len * 0.6);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!player.onGround()) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel) {
			BlockState ground = serverLevel.getBlockState(player.blockPosition().below());
			BlockParticleOption debris = new BlockParticleOption(ParticleTypes.BLOCK, ground.isAir() ? Blocks.STONE.defaultBlockState() : ground);
			for (int ring = 1; ring <= 3; ring++) {
				for (int i = 0; i < 12 * ring; i++) {
					double angle = i * Math.PI * 2 / (12 * ring);
					double r = ring * 1.4;
					serverLevel.sendParticles(debris, player.getX() + Math.cos(angle) * r, player.getY() + 0.2, player.getZ() + Math.sin(angle) * r, 3, 0.1, 0.1, 0.1, 0.15);
				}
			}
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.COLOSSUS_SLAM, SoundSource.PLAYERS, 1.2f, 1.2f);
			for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.5, 1.5, 4.5), e -> e != player && e.isAlive())) {
				double dx = victim.getX() - player.getX();
				double dz = victim.getZ() - player.getZ();
				double len = Math.max(0.5, Math.sqrt(dx * dx + dz * dz));
				victim.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 8.0f);
				victim.push(dx / len * 1.1, 0.7, dz / len * 1.1);
			}
			stack.hurtAndBreak(3, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
		}
		player.getCooldowns().addCooldown(stack, 20 * 8);
		return InteractionResult.SUCCESS;
	}
}
