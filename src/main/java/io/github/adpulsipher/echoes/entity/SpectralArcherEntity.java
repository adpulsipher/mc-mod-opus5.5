package io.github.adpulsipher.echoes.entity;

import java.util.EnumSet;

import io.github.adpulsipher.echoes.combat.Echoborn;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.combat.SpectralBolts;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * An archer who still holds the wall. Spectral Archers keep their distance and loose bolts of chilling ghost-light
 * that slow whatever they strike.
 */
public class SpectralArcherEntity extends Monster implements Echoborn {
	private static final DustParticleOptions MIST = new DustParticleOptions(0x8FEFFF, 0.8f);
	private int drawTicks;

	public SpectralArcherEntity(EntityType<? extends SpectralArcherEntity> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 24.0)
				.add(Attributes.ATTACK_DAMAGE, 3.0)
				.add(Attributes.ARMOR, 2.0)
				.add(Attributes.MOVEMENT_SPEED, 0.27)
				.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	public static boolean checkArcherSpawnRules(EntityType<SpectralArcherEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < 32 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new VolleyGoal());
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				(target, level) -> !LingererEntity.wearsCrown(target)));
	}

	/** Whether the archer is drawing its bow, for its animation. */
	public boolean isDrawing() {
		return isAggressive() && getTarget() != null;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide() && random.nextInt(4) == 0) {
			level().addParticle(MIST, getRandomX(0.6), getRandomY(), getRandomZ(0.6), 0, 0.02, 0);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return super.hurtServer(level, source, Spectral.isSpectralHit(source) ? amount * 1.5f : amount);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.LINGERER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.LINGERER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.LINGERER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}

	/** Keeps a comfortable range, then draws and looses a spectral bolt. */
	private class VolleyGoal extends Goal {
		private int strafeTicks;
		private boolean strafeLeft;

		VolleyGoal() {
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = getTarget();
			return target != null && target.isAlive();
		}

		@Override
		public void start() {
			setAggressive(true);
			drawTicks = 0;
		}

		@Override
		public void stop() {
			setAggressive(false);
			getNavigation().stop();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity target = getTarget();
			if (target == null) {
				return;
			}
			getLookControl().setLookAt(target, 30.0f, 30.0f);
			double distance = distanceTo(target);
			boolean sees = hasLineOfSight(target);
			if (distance > 14 || !sees) {
				getNavigation().moveTo(target, 1.0);
			} else if (distance < 6) {
				Vec3 away = position().subtract(target.position()).normalize().scale(4);
				getNavigation().moveTo(getX() + away.x, getY(), getZ() + away.z, 1.1);
			} else {
				getNavigation().stop();
				if (++strafeTicks > 30) {
					strafeTicks = 0;
					strafeLeft = !strafeLeft;
				}
				getMoveControl().strafe(0.0f, strafeLeft ? 0.4f : -0.4f);
			}
			if (sees && distance < 20) {
				if (++drawTicks >= 35 && level() instanceof ServerLevel level) {
					drawTicks = 0;
					Vec3 from = getEyePosition().add(getLookAngle().scale(0.6));
					Vec3 at = target.getEyePosition().add(target.getDeltaMovement().scale(distance / 1.6));
					SpectralBolts.fire(level, SpectralArcherEntity.this, from, at.add(0, -0.3, 0), 1.6, 5.0f, SpectralBolts.Style.SPECTRAL);
				}
			} else {
				drawTicks = Math.max(0, drawTicks - 1);
			}
		}
	}
}
