package io.github.adpulsipher.echoes.entity;

import io.github.adpulsipher.echoes.combat.Echoborn;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A smith of the Age of Iron and Ash who died at the forge and never stopped burning. Revenants set whatever they
 * strike alight, and every so often the embers inside them burst outward.
 */
public class AshRevenantEntity extends Monster implements Echoborn {
	private int burstCooldown = 80;

	public AshRevenantEntity(EntityType<? extends AshRevenantEntity> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 30.0)
				.add(Attributes.ATTACK_DAMAGE, 6.0)
				.add(Attributes.ARMOR, 6.0)
				.add(Attributes.MOVEMENT_SPEED, 0.28)
				.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	public static boolean checkRevenantSpawnRules(EntityType<AshRevenantEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < 0 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			if (random.nextInt(2) == 0) {
				level().addParticle(ParticleTypes.FLAME, getRandomX(0.5), getRandomY(), getRandomZ(0.5), 0, 0.02, 0);
			}
			if (random.nextInt(6) == 0) {
				level().addParticle(ParticleTypes.SMOKE, getRandomX(0.5), getY(1.0), getRandomZ(0.5), 0, 0.04, 0);
			}
			return;
		}
		if (isNoAi() || !(level() instanceof ServerLevel level)) {
			return;
		}
		LivingEntity target = getTarget();
		if (--burstCooldown <= 0 && target != null && distanceTo(target) < 3.5) {
			burstCooldown = 100 + random.nextInt(40);
			emberBurst(level, 5.0f);
		}
	}

	private void emberBurst(ServerLevel level, float damage) {
		level.sendParticles(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 40, 0.4, 0.6, 0.4, 0.15);
		level.sendParticles(ParticleTypes.LAVA, getX(), getY(0.5), getZ(), 6, 0.4, 0.4, 0.4, 0.0);
		playSound(ModSounds.REVENANT_BURST, 1.2f, 1.0f);
		for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5), e -> e != this && e.isAlive() && !Spectral.allied(this, e))) {
			victim.hurtServer(level, damageSources().mobAttack(this), damage);
			victim.igniteForSeconds(4.0f);
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			target.igniteForSeconds(4.0f);
		}
		return hit;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return super.hurtServer(level, source, Spectral.isSpectralHit(source) ? amount * 1.5f : amount);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel level) {
			emberBurst(level, 3.0f);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.REVENANT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.REVENANT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.REVENANT_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}
}
