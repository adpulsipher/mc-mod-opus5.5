package io.github.adpulsipher.echoes.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.adpulsipher.echoes.combat.Hazards;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Hierophant of the Elder Dawn, the oldest memory the world keeps. It drifts above its altar, shielded by
 * wards of living light: while any of its Dawn Wisps survive, blows barely reach it. It spears foes with lances
 * of dawn, calls down falling stars, and flashes across the altar in bursts of radiance.
 */
public class HierophantEntity extends EchoBoss {
	public static final int DRIFT = 0;
	public static final int LANCE = 1;
	public static final int STARFALL = 2;
	public static final int NOVA = 3;
	public static final int CONJURE = 4;

	private static final EntityDataAccessor<Integer> WARDS = SynchedEntityData.defineId(HierophantEntity.class, EntityDataSerializers.INT);
	private static final DustParticleOptions DAWN = new DustParticleOptions(0xFFE7A0, 1.3f);
	private static final DustParticleOptions LANCE_DUST = new DustParticleOptions(0xFFF6D0, 0.9f);

	private final List<UUID> wards = new ArrayList<>();
	private int conjures;
	private int attackCooldown = 60;
	private double orbit;
	private Vec3 lanceFrom = Vec3.ZERO;
	private Vec3 lanceTo = Vec3.ZERO;

	public HierophantEntity(EntityType<? extends HierophantEntity> type, Level level) {
		super(type, level, BossEvent.BossBarColor.WHITE);
		this.setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 350.0)
				.add(Attributes.ARMOR, 8.0)
				.add(Attributes.ATTACK_DAMAGE, 10.0)
				.add(Attributes.FOLLOW_RANGE, 48.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(WARDS, 0);
	}

	public int getWardCount() {
		return this.entityData.get(WARDS);
	}

	@Override
	protected String slainAdvancement() {
		return "dawnbreaker";
	}

	@Override
	protected int themeColor() {
		return 0xFFE7A0;
	}

	@Override
	protected void bossTick(ServerLevel level, @Nullable LivingEntity target) {
		updateWards(level);
		attackCooldown--;

		float health = getHealth() / getMaxHealth();
		boolean due = conjures == 0 && target != null || conjures == 1 && health < 0.6f || conjures == 2 && health < 0.25f;
		if (getAttackState() == DRIFT && due) {
			conjures++;
			setAttackState(CONJURE);
		}

		switch (getAttackState()) {
			case DRIFT -> drift(level, target);
			case LANCE -> lance(level, target);
			case STARFALL -> starfall(level, target);
			case NOVA -> nova(level);
			case CONJURE -> conjure(level);
			default -> setAttackState(DRIFT);
		}
	}

	private void updateWards(ServerLevel level) {
		wards.removeIf(id -> {
			Entity ward = level.getEntity(id);
			return ward == null || !ward.isAlive();
		});
		if (getWardCount() != wards.size()) {
			this.entityData.set(WARDS, wards.size());
			if (wards.isEmpty()) {
				playSound(ModSounds.HIEROPHANT_WARD_BREAK, 2.0f, 0.8f);
				level.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 40, 0.8, 1.2, 0.8, 0.15);
			}
		}
	}

	private void steer(Vec3 destination, double speed) {
		Vec3 delta = destination.subtract(position());
		double length = delta.length();
		Vec3 desired = length < 1.0E-4 ? Vec3.ZERO : delta.scale(Math.min(speed, length) / length);
		setDeltaMovement(getDeltaMovement().scale(0.75).add(desired.scale(0.25)));
	}

	private double hoverHeight(double x, double z) {
		BlockPos center = home();
		int ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
		return Math.max(ground + 3.5, center.getY() + 2.5);
	}

	private void drift(ServerLevel level, @Nullable LivingEntity target) {
		orbit += isEnraged() ? 0.03 : 0.02;
		BlockPos center = home();
		double r = 7.0;
		double x = center.getX() + 0.5 + Math.cos(orbit) * r;
		double z = center.getZ() + 0.5 + Math.sin(orbit) * r;
		steer(new Vec3(x, hoverHeight(x, z) + Math.sin(tickCount * 0.08) * 0.6, z), 0.3);
		if (target != null) {
			faceTowards(target.position());
			if (attackCooldown <= 0) {
				int roll = random.nextInt(isEnraged() ? 4 : 3);
				setAttackState(roll == 0 ? STARFALL : roll == 3 ? NOVA : LANCE);
				if (distanceTo(target) < 4.0) {
					setAttackState(NOVA);
				}
			}
		} else if (tickCount % 40 == 0 && home != null) {
			heal(2.0f);
		}
	}

	private void lance(ServerLevel level, @Nullable LivingEntity target) {
		steer(position(), 0.0);
		if (target == null && stateTicks < 18) {
			finish(40);
			return;
		}
		if (stateTicks < 18 && target != null) {
			faceTowards(target.position());
			lanceFrom = position().add(0, getBbHeight() * 0.8, 0);
			lanceTo = target.getEyePosition();
			if (stateTicks == 1) {
				playSound(ModSounds.HIEROPHANT_CAST, 2.0f, 1.0f);
			}
		}
		if (stateTicks >= 18 && stateTicks < 30) {
			// Telegraph: the path of the lance glimmers before it strikes.
			Vec3 dir = lanceTo.subtract(lanceFrom);
			double length = Math.min(32.0, dir.length());
			Vec3 unit = dir.normalize();
			for (double d = 0; d < length; d += 0.8) {
				Vec3 p = lanceFrom.add(unit.scale(d));
				level.sendParticles(LANCE_DUST, p.x, p.y, p.z, 1, 0, 0, 0, 0);
			}
		}
		if (stateTicks == 30) {
			Vec3 unit = lanceTo.subtract(lanceFrom).normalize();
			Vec3 end = lanceFrom.add(unit.scale(32.0));
			var clip = level.clip(new ClipContext(lanceFrom, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			end = clip.getLocation();
			double length = end.distanceTo(lanceFrom);
			for (double d = 0; d < length; d += 0.5) {
				Vec3 p = lanceFrom.add(unit.scale(d));
				level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.02);
			}
			level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFF6D0), end.x, end.y, end.z, 1, 0, 0, 0, 0);
			level.playSound(null, lanceFrom.x, lanceFrom.y, lanceFrom.z, ModSounds.HIEROPHANT_LANCE, SoundSource.HOSTILE, 2.5f, 1.0f);
			AABB swept = new AABB(lanceFrom, end).inflate(1.2);
			for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, swept, e -> e != this && e.isAlive() && !Spectral.allied(this, e))) {
				if (victim.getBoundingBox().inflate(0.6).clip(lanceFrom, end).isPresent()) {
					victim.hurtServer(level, damageSources().indirectMagic(this, this), isEnraged() ? 14.0f : 11.0f);
					victim.igniteForSeconds(2.0f);
				}
			}
		}
		if (stateTicks >= 38) {
			finish(isEnraged() ? 35 : 55);
		}
	}

	private void starfall(ServerLevel level, @Nullable LivingEntity target) {
		steer(new Vec3(getX(), hoverHeight(getX(), getZ()) + 2, getZ()), 0.2);
		if (stateTicks == 1) {
			playSound(ModSounds.HIEROPHANT_CAST, 2.0f, 0.7f);
			announce(level, Component.translatable("boss.echoes_of_the_past.hierophant.starfall"));
		}
		if (target != null && stateTicks % 6 == 0 && stateTicks <= 48) {
			Vec3 at = target.position();
			if (stateTicks % 12 != 0) {
				at = at.add(random.nextGaussian() * 2.5, 0, random.nextGaussian() * 2.5);
			}
			int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(at.x), Mth.floor(at.z));
			double y = Math.abs(ground - at.y) < 6 ? ground : at.y;
			Hazards.schedule(level, this, new Vec3(at.x, y, at.z), 1.9, 8.0f, 24, Hazards.Style.STARFALL);
		}
		if (stateTicks >= 70) {
			finish(isEnraged() ? 40 : 70);
		}
	}

	private void nova(ServerLevel level) {
		if (stateTicks == 1) {
			// Flash to a new point of the altar.
			level.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 30, 0.5, 1.0, 0.5, 0.1);
			BlockPos center = home();
			double angle = random.nextDouble() * Math.PI * 2;
			double x = center.getX() + 0.5 + Math.cos(angle) * 5;
			double z = center.getZ() + 0.5 + Math.sin(angle) * 5;
			teleportTo(x, hoverHeight(x, z), z);
			playSound(ModSounds.HIEROPHANT_WARD_BREAK, 1.5f, 1.6f);
		}
		steer(position(), 0.0);
		if (stateTicks >= 1 && stateTicks <= 12) {
			double r = stateTicks * 0.6;
			for (int i = 0; i < 24; i++) {
				double a = i * Math.PI * 2 / 24;
				level.sendParticles(DAWN, getX() + Math.cos(a) * r, getY(0.5), getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
			}
		}
		if (stateTicks == 12) {
			level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFE7A0), getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
			playSound(ModSounds.HIEROPHANT_LANCE, 2.5f, 0.7f);
			for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(7.0), e -> e != this && e.isAlive() && !Spectral.allied(this, e))) {
				Vec3 away = victim.position().subtract(position()).multiply(1, 0, 1);
				double len = Math.max(0.5, away.length());
				victim.hurtServer(level, damageSources().indirectMagic(this, this), 8.0f);
				victim.push(away.x / len * 1.4, 0.5, away.z / len * 1.4);
			}
		}
		if (stateTicks >= 24) {
			finish(40);
		}
	}

	private void conjure(ServerLevel level) {
		steer(position(), 0.0);
		if (stateTicks == 1) {
			playSound(ModSounds.HIEROPHANT_CAST, 2.5f, 0.5f);
			announce(level, Component.translatable("boss.echoes_of_the_past.hierophant.conjure"));
		}
		if (stateTicks < 30) {
			double a = stateTicks * 0.5;
			for (int i = 0; i < 4; i++) {
				double b = a + i * Math.PI / 2;
				level.sendParticles(DAWN, getX() + Math.cos(b) * 2.5, getY(0.5) + Math.sin(stateTicks * 0.3) * 0.5, getZ() + Math.sin(b) * 2.5, 1, 0, 0, 0, 0);
			}
		}
		if (stateTicks == 30) {
			int count = conjures >= 3 ? 5 : 4;
			for (int i = 0; i < count; i++) {
				DawnWispEntity wisp = ModEntities.DAWN_WISP.create(level, EntitySpawnReason.MOB_SUMMONED);
				if (wisp == null) {
					continue;
				}
				double b = i * Math.PI * 2 / count;
				wisp.setPos(getX() + Math.cos(b) * 3, getY(0.6), getZ() + Math.sin(b) * 3);
				wisp.bindTo(this, i, count);
				level.addFreshEntity(wisp);
				wards.add(wisp.getUUID());
				level.sendParticles(ParticleTypes.END_ROD, wisp.getX(), wisp.getY(), wisp.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
			}
			this.entityData.set(WARDS, wards.size());
		}
		if (stateTicks >= 45) {
			finish(30);
		}
	}

	private void finish(int cooldown) {
		attackCooldown = cooldown;
		setAttackState(DRIFT);
	}

	@Override
	protected void onEnrage(ServerLevel level) {
		playSound(ModSounds.HIEROPHANT_CAST, 3.0f, 0.4f);
		level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFE7A0), getX(), getY(0.5), getZ(), 1, 0, 0, 0, 0);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (getWardCount() > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			level.sendParticles(DAWN, getX(), getY(0.5), getZ(), 16, 0.8, 1.2, 0.8, 0.0);
			playSound(ModSounds.HIEROPHANT_WARD_HIT, 1.2f, 1.2f);
			amount *= 0.15f;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			level().addParticle(DAWN, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0, -0.02, 0);
			if (getWardCount() > 0 && random.nextInt(3) == 0) {
				double a = random.nextDouble() * Math.PI * 2;
				level().addParticle(ParticleTypes.END_ROD, getX() + Math.cos(a) * 1.3, getY(random.nextDouble()), getZ() + Math.sin(a) * 1.3, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return false;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.HIEROPHANT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.HIEROPHANT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.HIEROPHANT_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 2.5f;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 180;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("conjures", conjures);
		output.store("wards", UUIDUtil.CODEC.listOf(), List.copyOf(wards));
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		conjures = input.getIntOr("conjures", 0);
		wards.clear();
		input.read("wards", UUIDUtil.CODEC.listOf()).ifPresent(wards::addAll);
	}
}
