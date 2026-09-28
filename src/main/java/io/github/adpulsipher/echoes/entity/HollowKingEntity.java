package io.github.adpulsipher.echoes.entity;

import io.github.adpulsipher.echoes.combat.Hazards;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Hollow King, last monarch of the Age of Crowns. Betrayed on the day of his coronation, he still holds court
 * over an empty hall: he cleaves at anyone who approaches the throne, issues decrees that bend the knees of the
 * living, charges down those who flee and calls his sworn knights back from the dead.
 */
public class HollowKingEntity extends EchoBoss {
	public static final int STALK = 0;
	public static final int CLEAVE = 1;
	public static final int DECREE = 2;
	public static final int CHARGE = 3;
	public static final int SUMMON = 4;

	private static final DustParticleOptions ROYAL = new DustParticleOptions(0xFFD35A, 1.4f);
	private static final DustParticleOptions HOLLOW = new DustParticleOptions(0xB08CFF, 1.2f);

	private int cleaveCooldown;
	private int decreeCooldown = 160;
	private int chargeCooldown = 100;
	private int summons;
	private Vec3 chargeDirection = Vec3.ZERO;

	public HollowKingEntity(EntityType<? extends HollowKingEntity> type, Level level) {
		super(type, level, BossEvent.BossBarColor.YELLOW);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 400.0)
				.add(Attributes.ARMOR, 14.0)
				.add(Attributes.ARMOR_TOUGHNESS, 4.0)
				.add(Attributes.ATTACK_DAMAGE, 14.0)
				.add(Attributes.FOLLOW_RANGE, 48.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
				.add(Attributes.MOVEMENT_SPEED, 0.26)
				.add(Attributes.STEP_HEIGHT, 1.5);
	}

	@Override
	protected String slainAdvancement() {
		return "regicide";
	}

	@Override
	protected int themeColor() {
		return 0xFFD35A;
	}

	private float speedFactor() {
		return isEnraged() ? 1.3f : 1.0f;
	}

	@Override
	protected void bossTick(ServerLevel level, @Nullable LivingEntity target) {
		cleaveCooldown--;
		decreeCooldown--;
		chargeCooldown--;

		// Summon the sworn knights at two thirds and one third of his strength.
		float health = getHealth() / getMaxHealth();
		if (getAttackState() == STALK && (summons == 0 && health < 0.67f || summons == 1 && health < 0.34f)) {
			summons++;
			setAttackState(SUMMON);
		}

		switch (getAttackState()) {
			case STALK -> stalk(level, target);
			case CLEAVE -> cleave(level, target);
			case DECREE -> decree(level, target);
			case CHARGE -> charge(level, target);
			case SUMMON -> summon(level, target);
			default -> setAttackState(STALK);
		}
	}

	private void stalk(ServerLevel level, @Nullable LivingEntity target) {
		if (target == null) {
			if (home != null && distanceToSqr(Vec3.atCenterOf(home)) > 9) {
				getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.8);
			}
			return;
		}
		getLookControl().setLookAt(target, 30.0f, 30.0f);
		double distance = distanceTo(target);
		if (distance < 4.2 && cleaveCooldown <= 0) {
			getNavigation().stop();
			setAttackState(CLEAVE);
			return;
		}
		if (decreeCooldown <= 0 && distance < 10) {
			getNavigation().stop();
			setAttackState(DECREE);
			return;
		}
		if (chargeCooldown <= 0 && distance > 8 && distance < 24 && hasLineOfSight(target)) {
			getNavigation().stop();
			setAttackState(CHARGE);
			return;
		}
		if (tickCount % 5 == 0) {
			getNavigation().moveTo(target, 1.0 * speedFactor());
		}
	}

	private void cleave(ServerLevel level, @Nullable LivingEntity target) {
		if (target != null && stateTicks < 10) {
			faceTowards(target.position());
		}
		if (stateTicks == 4) {
			playSound(ModSounds.KING_CLEAVE, 1.5f, 0.8f);
		}
		if (stateTicks == 12) {
			Vec3 facing = Vec3.directionFromRotation(0, getYRot());
			Vec3 center = position().add(facing.scale(2.2));
			level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, getY(0.5), center.z, 3, 1.0, 0.3, 1.0, 0.0);
			level.sendParticles(ROYAL, center.x, getY(0.4), center.z, 20, 1.5, 0.4, 1.5, 0.0);
			playSound(ModSounds.REPLAY_CLASH, 2.0f, 0.6f);
			AABB area = getBoundingBox().inflate(4.5, 1.0, 4.5);
			for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this && e.isAlive() && !Spectral.allied(this, e))) {
				if (inFront(victim, 0.1) && victim.distanceTo(this) < 5.2) {
					victim.hurtServer(level, damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
					victim.push(facing.x * 0.9, 0.35, facing.z * 0.9);
				}
			}
		}
		if (stateTicks >= 22) {
			cleaveCooldown = isEnraged() ? 16 : 28;
			setAttackState(STALK);
		}
	}

	private void decree(ServerLevel level, @Nullable LivingEntity target) {
		if (target != null) {
			faceTowards(target.position());
		}
		if (stateTicks == 1) {
			playSound(ModSounds.KING_DECREE, 3.0f, 0.8f);
			announce(level, Component.translatable("boss.echoes_of_the_past.hollow_king.decree"));
		}
		if (stateTicks < 30) {
			for (int i = 0; i < 3; i++) {
				double angle = (stateTicks * 0.4) + i * Math.PI * 2 / 3;
				double r = 6.0 - stateTicks * 0.18;
				level.sendParticles(HOLLOW, getX() + Math.cos(angle) * r, getY() + 0.3 + stateTicks * 0.08, getZ() + Math.sin(angle) * r, 1, 0, 0, 0, 0);
			}
		}
		if (stateTicks == 30) {
			Hazards.schedule(level, this, position(), 8.0, 9.0f, 1, Hazards.Style.DECREE);
			if (isEnraged() && target != null) {
				// In his fury the decree echoes out a second time beneath the fleeing.
				Hazards.schedule(level, this, target.position(), 4.0, 8.0f, 25, Hazards.Style.DECREE);
			}
		}
		if (stateTicks >= 45) {
			decreeCooldown = isEnraged() ? 140 : 220;
			setAttackState(STALK);
		}
	}

	private void charge(ServerLevel level, @Nullable LivingEntity target) {
		if (stateTicks < 15) {
			if (target == null) {
				setAttackState(STALK);
				return;
			}
			faceTowards(target.position());
			chargeDirection = target.position().subtract(position()).multiply(1, 0, 1).normalize();
			if (stateTicks == 1) {
				playSound(ModSounds.KING_ROAR, 2.5f, 0.9f);
			}
			level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1, getZ(), 2, 0.6, 0.0, 0.6, 0.02);
			return;
		}
		double speed = 0.85 * speedFactor();
		setDeltaMovement(chargeDirection.x * speed, getDeltaMovement().y, chargeDirection.z * speed);
		level.sendParticles(ROYAL, getX(), getY(0.5), getZ(), 4, 0.6, 1.0, 0.6, 0.0);
		for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.8), e -> e != this && e.isAlive() && !Spectral.allied(this, e))) {
			victim.hurtServer(level, damageSources().mobAttack(this), 12.0f);
			victim.push(chargeDirection.x * 1.5, 0.6, chargeDirection.z * 1.5);
			stateTicks = 100;
		}
		if (horizontalCollision && stateTicks > 18) {
			level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
			playSound(ModSounds.REPLAY_CLASH, 2.0f, 0.5f);
			stateTicks = 100;
		}
		if (stateTicks >= 40) {
			setDeltaMovement(getDeltaMovement().multiply(0.2, 1, 0.2));
			chargeCooldown = isEnraged() ? 80 : 140;
			setAttackState(STALK);
		}
	}

	private void summon(ServerLevel level, @Nullable LivingEntity target) {
		getNavigation().stop();
		if (stateTicks == 1) {
			playSound(ModSounds.REPLAY_HORN, 3.0f, 0.7f);
			announce(level, Component.translatable("boss.echoes_of_the_past.hollow_king.summon"));
		}
		if (stateTicks == 20) {
			int count = isEnraged() ? 4 : 3;
			for (int i = 0; i < count; i++) {
				double angle = i * Math.PI * 2 / count + random.nextDouble();
				double x = getX() + Math.cos(angle) * 4;
				double z = getZ() + Math.sin(angle) * 4;
				int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
				if (Math.abs(y - getY()) > 4) {
					y = Mth.floor(getY());
				}
				Mob minion;
				if (i == count - 1 && isEnraged()) {
					minion = ModEntities.SPECTRAL_ARCHER.create(level, EntitySpawnReason.MOB_SUMMONED);
				} else {
					minion = ModEntities.ECHO_KNIGHT.create(level, EntitySpawnReason.MOB_SUMMONED);
				}
				if (minion != null) {
					minion.setPos(x, y, z);
					minion.setYRot(random.nextFloat() * 360.0f);
					minion.setTarget(target);
					level.addFreshEntity(minion);
					level.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1, z, 16, 0.3, 0.8, 0.3, 0.03);
				}
			}
		}
		if (stateTicks >= 40) {
			setAttackState(STALK);
		}
	}

	@Override
	protected void onEnrage(ServerLevel level) {
		playSound(ModSounds.KING_ROAR, 3.0f, 0.7f);
		level.sendParticles(ROYAL, getX(), getY(1.0), getZ(), 60, 0.8, 0.4, 0.8, 0.1);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			if (random.nextInt(2) == 0) {
				level().addParticle(isEnraged() ? ROYAL : HOLLOW, getRandomX(1.0), getRandomY(), getRandomZ(1.0), 0, 0.02, 0);
			}
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.KING_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.KING_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.KING_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 2.5f;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("summons", summons);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		summons = input.getIntOr("summons", 0);
	}
}
