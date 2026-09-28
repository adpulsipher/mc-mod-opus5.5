package io.github.adpulsipher.echoes.block;

import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stone in which centuries of memory have crystallized. Glows faintly and hums when no one is listening.
 */
public class EchoDepositBlock extends DropExperienceBlock {
	private static final DustParticleOptions ECHO_DUST = new DustParticleOptions(0x6FE6FF, 0.8f);

	public EchoDepositBlock(Properties properties) {
		super(UniformInt.of(3, 7), properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(4) == 0) {
			double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.3;
			double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 1.3;
			double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.3;
			level.addParticle(ECHO_DUST, x, y, z, 0, 0.02, 0);
		}
		if (random.nextInt(12) == 0) {
			level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, 0.01, 0);
		}
		if (random.nextInt(180) == 0) {
			level.playLocalSound(pos, ModSounds.ECHO_DEPOSIT_CHIME, SoundSource.BLOCKS, 0.6f, 0.7f + random.nextFloat() * 0.6f, false);
		}
	}
}
