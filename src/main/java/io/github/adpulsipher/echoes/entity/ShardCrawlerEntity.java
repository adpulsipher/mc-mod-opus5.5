package io.github.adpulsipher.echoes.entity;

import java.util.EnumSet;
import java.util.Optional;

import io.github.adpulsipher.echoes.item.ResonanceCompassItem;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A small beetle whose shell has grown through with echo crystal from grazing on deposits. Shard Crawlers are
 * shy: they only fight when provoked, and when badly hurt they burrow away through the stone. They are the
 * surest source of echo shards for a young archaeologist.
 */
public class ShardCrawlerEntity extends Monster {
	private static final DustParticleOptions SHARD = new DustParticleOptions(0x6FE6FF, 0.7f);
	private boolean burrowed;

	public ShardCrawlerEntity(EntityType<? extends ShardCrawlerEntity> type, Level level) {
		super(type, level);
		this.xpReward = 4;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 12.0)
				.add(Attributes.ATTACK_DAMAGE, 3.0)
				.add(Attributes.ARMOR, 6.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.FOLLOW_RANGE, 16.0);
	}

	public static boolean checkCrawlerSpawnRules(EntityType<ShardCrawlerEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < 48 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
		this.goalSelector.addGoal(4, new GrazeGoal());
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide() && random.nextInt(8) == 0) {
			level().addParticle(SHARD, getRandomX(0.5), getY(0.8), getRandomZ(0.5), 0, 0.02, 0);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && isAlive() && !burrowed && getHealth() < getMaxHealth() * 0.4f) {
			burrow(level);
		}
		return hurt;
	}

	/** Digs away through the stone and resurfaces a short distance off. */
	private void burrow(ServerLevel level) {
		BlockState below = level.getBlockState(blockPosition().below());
		BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, below.isAir() ? Blocks.STONE.defaultBlockState() : below);
		for (int attempt = 0; attempt < 16; attempt++) {
			double x = getX() + (random.nextDouble() - 0.5) * 16;
			double z = getZ() + (random.nextDouble() - 0.5) * 16;
			BlockPos spot = BlockPos.containing(x, getY(), z);
			for (int dy = 3; dy >= -3; dy--) {
				BlockPos p = spot.above(dy);
				if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolid()
						&& level.noCollision(this, getBoundingBox().move(x - getX(), p.getY() - getY(), z - getZ()))) {
					level.sendParticles(dust, getX(), getY() + 0.2, getZ(), 30, 0.3, 0.1, 0.3, 0.1);
					playSound(ModSounds.CRAWLER_BURROW, 1.0f, 1.0f);
					teleportTo(x, p.getY(), z);
					level.sendParticles(dust, getX(), getY() + 0.2, getZ(), 30, 0.3, 0.1, 0.3, 0.1);
					burrowed = true;
					setTarget(null);
					getNavigation().stop();
					return;
				}
			}
		}
		burrowed = true;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.CRAWLER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.CRAWLER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.CRAWLER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(ModSounds.CRAWLER_STEP, 0.15f, 1.0f);
	}

	@Override
	protected float getSoundVolume() {
		return 0.6f;
	}

	/** Wanders to a nearby echo deposit and nibbles at it. */
	private class GrazeGoal extends Goal {
		private BlockPos deposit;
		private int cooldown;
		private int ticks;

		GrazeGoal() {
			setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (getTarget() != null || --cooldown > 0 || !(level() instanceof ServerLevel level)) {
				return false;
			}
			cooldown = 200 + random.nextInt(200);
			Optional<BlockPos> found = ResonanceCompassItem.findNearest(level, blockPosition());
			deposit = found.filter(p -> p.distSqr(blockPosition()) < 16 * 16).orElse(null);
			return deposit != null;
		}

		@Override
		public boolean canContinueToUse() {
			return deposit != null && getTarget() == null && ticks < 200;
		}

		@Override
		public void start() {
			ticks = 0;
			getNavigation().moveTo(deposit.getX() + 0.5, deposit.getY(), deposit.getZ() + 0.5, 0.9);
		}

		@Override
		public void tick() {
			ticks++;
			if (deposit != null && distanceToSqr(deposit.getX() + 0.5, deposit.getY() + 0.5, deposit.getZ() + 0.5) < 4.0 && level() instanceof ServerLevel level) {
				getLookControl().setLookAt(deposit.getX() + 0.5, deposit.getY() + 0.5, deposit.getZ() + 0.5);
				if (ticks % 10 == 0) {
					level.sendParticles(SHARD, deposit.getX() + 0.5, deposit.getY() + 0.5, deposit.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
					playSound(ModSounds.CHISEL_TAP, 0.3f, 1.8f + Mth.sin(ticks) * 0.1f);
				}
			}
		}

		@Override
		public void stop() {
			deposit = null;
		}
	}
}
