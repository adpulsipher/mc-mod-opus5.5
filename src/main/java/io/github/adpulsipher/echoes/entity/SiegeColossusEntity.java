package io.github.adpulsipher.echoes.entity;

import io.github.adpulsipher.echoes.combat.Hazards;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Siege Colossus: a war engine of the Age of Iron and Ash, built to breach walls that no longer stand. It
 * slams the earth with both fists, calls down mortar fire on anyone who keeps their distance, and when its
 * furnace heart overheats it burns everything around it. Its plating turns arrows aside.
 */
public class SiegeColossusEntity extends EchoBoss {
	public static final int WALK = 0;
	public static final int SLAM = 1;
	public static final int BARRAGE = 2;
	public static final int STOMP = 3;

	private int slamCooldown = 20;
	private int barrageCooldown = 120;
	private int stompCooldown = 40;

	public SiegeColossusEntity(EntityType<? extends SiegeColossusEntity> type, Level level) {
		super(type, level, BossEvent.BossBarColor.RED);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 500.0)
				.add(Attributes.ARMOR, 18.0)
				.add(Attributes.ARMOR_TOUGHNESS, 6.0)
				.add(Attributes.ATTACK_DAMAGE, 18.0)
				.add(Attributes.FOLLOW_RANGE, 48.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.2)
				.add(Attributes.STEP_HEIGHT, 2.0);
	}

	@Override
	protected String slainAdvancement() {
		return "the_walls_fall";
	}

	@Override
	protected int themeColor() {
		return 0xFF8A3A;
	}

	@Override
	protected void bossTick(ServerLevel level, @Nullable LivingEntity target) {
		slamCooldown--;
		barrageCooldown--;
		stompCooldown--;
		switch (getAttackState()) {
			case WALK -> walk(level, target);
			case SLAM -> slam(level, target);
			case BARRAGE -> barrage(level, target);
			case STOMP -> stomp(level);
			default -> setAttackState(WALK);
		}
		if (isEnraged() && tickCount % 4 == 0) {
			level.sendParticles(ParticleTypes.FLAME, getX(), getY(0.55), getZ(), 3, 0.6, 0.6, 0.6, 0.01);
			level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(1.0), getZ(), 1, 0.4, 0.1, 0.4, 0.02);
		}
	}

	private void walk(ServerLevel level, @Nullable LivingEntity target) {
		if (target == null) {
			if (home != null && distanceToSqr(Vec3.atCenterOf(home)) > 16) {
				getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.8);
			}
			return;
		}
		getLookControl().setLookAt(target, 20.0f, 20.0f);
		double distance = distanceTo(target);
		if (distance < 2.6 && stompCooldown <= 0) {
			getNavigation().stop();
			setAttackState(STOMP);
		} else if (distance < 5.5 && slamCooldown <= 0) {
			getNavigation().stop();
			setAttackState(SLAM);
		} else if (distance > 7 && barrageCooldown <= 0) {
			getNavigation().stop();
			setAttackState(BARRAGE);
		} else if (tickCount % 6 == 0) {
			getNavigation().moveTo(target, isEnraged() ? 1.3 : 1.0);
		}
		if (walkAnimation.speed() > 0.1f && tickCount % 14 == 0) {
			playSound(ModSounds.COLOSSUS_STEP, 1.6f, 0.7f);
		}
	}

	private void slam(ServerLevel level, @Nullable LivingEntity target) {
		if (target != null && stateTicks < 18) {
			faceTowards(target.position());
		}
		if (stateTicks == 2) {
			playSound(ModSounds.COLOSSUS_GROAN, 2.5f, 0.8f);
		}
		if (stateTicks == 24) {
			Vec3 facing = Vec3.directionFromRotation(0, getYRot());
			Vec3 impact = position().add(facing.scale(2.8));
			shockwave(level, impact, 5.0, 16.0f, 0.8);
		}
		if (stateTicks >= 40) {
			slamCooldown = isEnraged() ? 30 : 50;
			setAttackState(WALK);
		}
	}

	private void stomp(ServerLevel level) {
		if (stateTicks == 10) {
			shockwave(level, position(), 3.8, 10.0f, 1.0);
		}
		if (stateTicks >= 22) {
			stompCooldown = 60;
			setAttackState(WALK);
		}
	}

	private void barrage(ServerLevel level, @Nullable LivingEntity target) {
		if (target != null) {
			faceTowards(target.position());
		}
		if (stateTicks == 2) {
			playSound(ModSounds.COLOSSUS_GROAN, 2.5f, 1.1f);
			announce(level, Component.translatable("boss.echoes_of_the_past.siege_colossus.barrage"));
		}
		if (stateTicks > 10 && stateTicks < 24) {
			level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() + 0.5, getZ(), 3, 0.5, 0.2, 0.5, 0.05);
		}
		if (stateTicks == 22 && target != null) {
			int shells = isEnraged() ? 9 : 6;
			for (int i = 0; i < shells; i++) {
				Vec3 at = target.position();
				if (i > 0) {
					double angle = random.nextDouble() * Math.PI * 2;
					double r = 1.5 + random.nextDouble() * 4.5;
					at = at.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
				}
				int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(at.x), Mth.floor(at.z));
				double y = Math.abs(ground - at.y) < 6 ? ground : at.y;
				Hazards.schedule(level, this, new Vec3(at.x, y, at.z), 2.6, 11.0f, 30 + i * 5, Hazards.Style.MORTAR);
			}
		}
		if (stateTicks >= 50) {
			barrageCooldown = isEnraged() ? 90 : 150;
			setAttackState(WALK);
		}
	}

	private void shockwave(ServerLevel level, Vec3 impact, double radius, float damage, double lift) {
		playSound(ModSounds.COLOSSUS_SLAM, 3.0f, 0.8f);
		BlockState dust = level.getBlockState(BlockPos.containing(impact).below());
		if (dust.isAir()) {
			dust = Blocks.STONE.defaultBlockState();
		}
		BlockParticleOption debris = new BlockParticleOption(ParticleTypes.BLOCK, dust);
		for (int ring = 1; ring <= 3; ring++) {
			double r = radius * ring / 3.0;
			int points = 12 * ring;
			for (int i = 0; i < points; i++) {
				double angle = i * Math.PI * 2 / points;
				level.sendParticles(debris, impact.x + Math.cos(angle) * r, impact.y + 0.2, impact.z + Math.sin(angle) * r, 3, 0.2, 0.2, 0.2, 0.15);
			}
		}
		level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y + 0.5, impact.z, 2, 0.6, 0.1, 0.6, 0.0);
		AABB area = new AABB(impact, impact).inflate(radius, 2.0, radius);
		for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this && e.isAlive() && !Spectral.allied(this, e))) {
			double dx = victim.getX() - impact.x;
			double dz = victim.getZ() - impact.z;
			double d = Math.sqrt(dx * dx + dz * dz);
			if (d > radius) {
				continue;
			}
			victim.hurtServer(level, damageSources().mobAttack(this), damage * (float) (1.0 - 0.4 * d / radius));
			double len = Math.max(0.5, d);
			victim.push(dx / len * 0.8, lift, dz / len * 0.8);
			if (isEnraged()) {
				victim.igniteForSeconds(3.0f);
			}
		}
	}

	@Override
	protected void onEnrage(ServerLevel level) {
		announce(level, Component.translatable("boss.echoes_of_the_past.siege_colossus.overheat"));
		playSound(ModSounds.COLOSSUS_GROAN, 3.0f, 0.6f);
		level.sendParticles(ParticleTypes.LAVA, getX(), getY(0.6), getZ(), 30, 1.0, 1.0, 1.0, 0.0);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypeTags.IS_PROJECTILE)) {
			// Siege plating turns arrows aside.
			level.sendParticles(ParticleTypes.CRIT, getX(), getY(0.6), getZ(), 6, 0.6, 0.8, 0.6, 0.1);
			amount *= 0.4f;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.COLOSSUS_GROAN;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.COLOSSUS_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.COLOSSUS_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 3.0f;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 260;
	}
}
