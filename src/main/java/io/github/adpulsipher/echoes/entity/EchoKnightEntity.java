package io.github.adpulsipher.echoes.entity;

import io.github.adpulsipher.echoes.combat.Echoborn;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;

/**
 * A sworn knight of the Age of Crowns, still keeping an oath to a king long dead. Echo Knights raise their
 * spectral shields against blows from the front, and lower their lances to charge anyone who keeps their distance.
 */
public class EchoKnightEntity extends Monster implements Echoborn {
	private static final DustParticleOptions OATH = new DustParticleOptions(0xA8C8FF, 0.9f);
	private int chargeCooldown = 60;
	private int charging;

	public EchoKnightEntity(EntityType<? extends EchoKnightEntity> type, Level level) {
		super(type, level);
		this.xpReward = 12;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 36.0)
				.add(Attributes.ATTACK_DAMAGE, 7.0)
				.add(Attributes.ARMOR, 10.0)
				.add(Attributes.ARMOR_TOUGHNESS, 2.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
				.add(Attributes.MOVEMENT_SPEED, 0.25)
				.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	public static boolean checkKnightSpawnRules(EntityType<EchoKnightEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < 16 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				(target, level) -> !LingererEntity.wearsCrown(target)));
	}

	/** Whether the knight is charging, for its animation. */
	public boolean isCharging() {
		return charging > 0;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			if (random.nextInt(4) == 0) {
				level().addParticle(OATH, getRandomX(0.6), getRandomY(), getRandomZ(0.6), 0, 0.02, 0);
			}
			return;
		}
		if (isNoAi()) {
			return;
		}
		chargeCooldown--;
		LivingEntity target = getTarget();
		if (charging > 0) {
			charging--;
			Vec3 facing = Vec3.directionFromRotation(0, getYRot());
			setDeltaMovement(facing.x * 0.55, getDeltaMovement().y, facing.z * 0.55);
			if (level() instanceof ServerLevel level) {
				level.sendParticles(OATH, getX(), getY(0.5), getZ(), 2, 0.3, 0.5, 0.3, 0.0);
				if (target != null && getBoundingBox().inflate(0.6).intersects(target.getBoundingBox())) {
					target.hurtServer(level, damageSources().mobAttack(this), 9.0f);
					target.push(facing.x * 1.2, 0.4, facing.z * 1.2);
					charging = 0;
				}
			}
			return;
		}
		if (target != null && chargeCooldown <= 0 && onGround()) {
			double distance = distanceTo(target);
			if (distance > 5 && distance < 12 && hasLineOfSight(target)) {
				Vec3 to = target.position().subtract(position());
				float yaw = (float) (Math.atan2(to.z, to.x) * (180 / Math.PI)) - 90.0f;
				setYRot(yaw);
				yBodyRot = yaw;
				charging = 16;
				chargeCooldown = 120 + random.nextInt(60);
				playSound(ModSounds.KNIGHT_CHARGE, 1.0f, 1.0f);
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean spectral = Spectral.isSpectralHit(source);
		// Blows from the front glance off the spectral shield.
		if (!spectral && source.getEntity() instanceof LivingEntity attacker && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
			Vec3 facing = Vec3.directionFromRotation(0, yBodyRot);
			Vec3 to = attacker.position().subtract(position()).multiply(1, 0, 1);
			if (to.lengthSqr() > 1.0E-4 && to.normalize().dot(facing) > 0.5 && random.nextFloat() < 0.6f) {
				level.sendParticles(ParticleTypes.ENCHANTED_HIT, getX(), getY(0.6), getZ(), 8, 0.3, 0.4, 0.3, 0.1);
				playSound(ModSounds.KNIGHT_BLOCK, 1.0f, 0.9f + random.nextFloat() * 0.2f);
				amount *= 0.25f;
			}
		}
		return super.hurtServer(level, source, spectral ? amount * 1.5f : amount);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.KNIGHT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.KNIGHT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.LINGERER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(ModSounds.KNIGHT_STEP, 0.3f, 1.0f);
	}
}
