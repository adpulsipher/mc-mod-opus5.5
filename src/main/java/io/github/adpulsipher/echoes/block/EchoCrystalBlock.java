package io.github.adpulsipher.echoes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A block of pure crystallized memory. Decorative and luminous. */
public class EchoCrystalBlock extends Block {
	public EchoCrystalBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(10) == 0) {
			level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(), 0, 0.015, 0);
		}
	}
}
