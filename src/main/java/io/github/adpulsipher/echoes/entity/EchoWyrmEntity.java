package io.github.adpulsipher.echoes.entity;

import java.util.List;
import java.util.UUID;

import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A dragon remembered so vividly that it tore its way out of an echo and into the present. It circles its
 * ancient hunting ground, diving at intruders and breathing spectral fire.
 */
public class EchoWyrmEntity extends Monster implements io.github.adpulsipher.echoes.combat.Echoborn {
	private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(EchoWyrmEntity.class, EntityDataSerializers.INT);
	private static final DustParticleOptions WYRM_DUST = new DustParticleOptions(0xD8A6FF, 1.6f);

	public enum Phase {
		CIRCLE, SWOOP, BREATH, ROAR
	}

	private final ServerBossEvent bossEvent;
	@Nullable
	private BlockPos home;
	private int phaseTicks;
	private boolean summoned;
	private double orbitAngle;
	private String wyrmName = "";

	public EchoWyrmEntity(EntityType<? extends EchoWyrmEntity> type, Level level) {
		super(type, level);
		this.xpReward = 250;
		this.setNoGravity(true);
		this.bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
		this.bossEvent.setDarkenScreen(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 300.0)
				.add(Attributes.ARMOR, 10.0)
				.add(Attributes.ATTACK_DAMAGE, 12.0)
				.add(Attributes.FOLLOW_RANGE, 64.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PHASE, Phase.CIRCLE.ordinal());
	}

	public Phase getPhase() {
		return Phase.values()[Mth.clamp(this.entityData.get(PHASE), 0, Phase.values().length - 1)];
	}

	private void setPhase(Phase phase) {
		this.entityData.set(PHASE, phase.ordinal());
		this.phaseTicks = 0;
	}

	public void setHome(BlockPos home) {
		this.home = home;
	}

	public void setWyrmName(String name) {
		this.wyrmName = name;
		this.setCustomName(Component.literal(name));
		this.bossEvent.setName(this.getDisplayName());
	}

	private BlockPos home() {
		if (home == null) {
			home = blockPosition();
		}
		return home;
	}

	@Override
	protected void registerGoals() {
		// The wyrm's behaviour is a hand-written state machine in customServerAiStep.
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isNoAi()) {
			return;
		}
		bossEvent.setProgress(getHealth() / getMaxHealth());
		phaseTicks++;

		LivingEntity target = getTarget();
		if (target == null || !target.isAlive() || target.distanceToSqr(this) > 64 * 64 || isIgnorable(target)) {
			target = findTarget(level);
			setTarget(target);
		}

		if (!summoned && getHealth() < getMaxHealth() * 0.5f) {
			summoned = true;
			setPhase(Phase.ROAR);
		}

		switch (getPhase()) {
			case CIRCLE -> circle(target);
			case SWOOP -> swoop(level, target);
			case BREATH -> breath(level, target);
			case ROAR -> roar(level, target);
		}
		faceMovement();
	}

	private boolean isIgnorable(LivingEntity target) {
		return target instanceof Player player && (player.isCreative() || player.isSpectator());
	}

	private @Nullable LivingEntity findTarget(ServerLevel level) {
		Player nearest = null;
		double best = 48 * 48;
		for (Player player : level.players()) {
			if (!player.isAlive() || isIgnorable(player)) {
				continue;
			}
			double d = player.distanceToSqr(this);
			if (d < best) {
				best = d;
				nearest = player;
			}
		}
		return nearest;
	}

	private void steer(Vec3 destination, double speed) {
		Vec3 delta = destination.subtract(position());
		double length = delta.length();
		Vec3 desired = length < 1.0E-4 ? Vec3.ZERO : delta.scale(Math.min(speed, length) / length);
		setDeltaMovement(getDeltaMovement().scale(0.8).add(desired.scale(0.2)));
	}

	private void faceMovement() {
		Vec3 motion = getDeltaMovement();
		if (motion.horizontalDistanceSqr() > 1.0E-4) {
			float yaw = (float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0f;
			setYRot(Mth.approachDegrees(getYRot(), yaw, 8.0f));
			yBodyRot = getYRot();
			yHeadRot = getYRot();
		}
		setXRot((float) Mth.clamp(-motion.y * 60.0, -40.0, 40.0));
	}

	private void circle(@Nullable LivingEntity target) {
		orbitAngle += 0.035;
		BlockPos center = home();
		double radius = 16.0;
		int ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ());
		double altitude = Math.max(center.getY(), ground + 8) + Math.sin(orbitAngle * 2) * 2.0;
		steer(new Vec3(center.getX() + Math.cos(orbitAngle) * radius, altitude, center.getZ() + Math.sin(orbitAngle) * radius), 0.6);
		if (tickCount % 24 == 0) {
			playSound(ModSounds.WYRM_FLAP, 2.0f, 0.9f);
		}
		if (target != null && phaseTicks > 90 + random.nextInt(60)) {
			setPhase(random.nextInt(3) == 0 ? Phase.BREATH : Phase.SWOOP);
			playSound(ModSounds.WYRM_ROAR, 3.0f, 1.0f);
		}
	}

	private void swoop(ServerLevel level, @Nullable LivingEntity target) {
		if (target == null || phaseTicks > 120) {
			setPhase(Phase.CIRCLE);
			return;
		}
		steer(target.position().add(0, target.getBbHeight() * 0.5, 0), 1.1);
		if (getBoundingBox().inflate(1.5).intersects(target.getBoundingBox())) {
			doHurtTarget(level, target);
			target.push(getDeltaMovement().x * 1.2, 0.6, getDeltaMovement().z * 1.2);
			setDeltaMovement(getDeltaMovement().add(0, 0.8, 0));
			setPhase(Phase.CIRCLE);
		}
	}

	private void breath(ServerLevel level, @Nullable LivingEntity target) {
		if (target == null || phaseTicks > 90) {
			setPhase(Phase.CIRCLE);
			return;
		}
		Vec3 away = position().subtract(target.position()).multiply(1, 0, 1);
		if (away.lengthSqr() < 1.0E-3) {
			away = new Vec3(1, 0, 0);
		}
		Vec3 hover = target.position().add(away.normalize().scale(9.0)).add(0, 5.0, 0);
		steer(hover, 0.4);
		// Always face the victim while breathing.
		Vec3 look = target.getEyePosition().subtract(getEyePosition());
		float yaw = (float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0f;
		setYRot(yaw);
		yBodyRot = yaw;
		yHeadRot = yaw;

		if (phaseTicks > 20) {
			Vec3 mouth = position().add(0, 1.2, 0).add(Vec3.directionFromRotation(0, yaw).scale(3.0));
			Vec3 dir = target.getEyePosition().subtract(mouth).normalize();
			for (int i = 0; i < 8; i++) {
				double s = 0.6 + random.nextDouble() * 0.6;
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, mouth.x, mouth.y, mouth.z, 0,
						dir.x * s + (random.nextDouble() - 0.5) * 0.25, dir.y * s + (random.nextDouble() - 0.5) * 0.25, dir.z * s + (random.nextDouble() - 0.5) * 0.25, 1.0);
			}
			if (phaseTicks % 10 == 0) {
				level.playSound(null, mouth.x, mouth.y, mouth.z, ModSounds.WYRM_BREATH, SoundSource.HOSTILE, 2.0f, 0.9f);
				AABB cone = new AABB(mouth, mouth).expandTowards(dir.scale(12)).inflate(2.0);
				List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, cone, e -> e != this && e.isAlive() && !io.github.adpulsipher.echoes.combat.Spectral.allied(this, e));
				for (LivingEntity victim : victims) {
					Vec3 to = victim.position().subtract(mouth);
					if (to.normalize().dot(dir) > 0.85) {
						victim.hurtServer(level, damageSources().mobAttack(this), 5.0f);
						victim.igniteForSeconds(4.0f);
					}
				}
			}
		}
	}

	private void roar(ServerLevel level, @Nullable LivingEntity target) {
		steer(position(), 0.0);
		if (phaseTicks == 1) {
			level.playSound(null, getX(), getY(), getZ(), ModSounds.WYRM_ROAR, SoundSource.HOSTILE, 4.0f, 0.6f);
			level.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY(1.0), getZ(), 1, 0, 0, 0, 0);
		}
		if (phaseTicks == 30) {
			BlockPos around = target != null ? target.blockPosition() : home();
			for (int i = 0; i < 3; i++) {
				LingererEntity lingerer = new LingererEntity(ModEntities.LINGERER, level);
				double angle = i * Math.PI * 2 / 3;
				double x = around.getX() + 0.5 + Math.cos(angle) * 3;
				double z = around.getZ() + 0.5 + Math.sin(angle) * 3;
				int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
				lingerer.setPos(x, y, z);
				lingerer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
				level.addFreshEntity(lingerer);
				level.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1, z, 12, 0.3, 0.6, 0.3, 0.02);
			}
		}
		if (phaseTicks > 50) {
			setPhase(Phase.CIRCLE);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			for (int i = 0; i < 2; i++) {
				level().addParticle(WYRM_DUST, getRandomX(2.0), getRandomY(), getRandomZ(2.0), 0, 0.01, 0);
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean spectral = io.github.adpulsipher.echoes.combat.Spectral.isSpectralHit(source);
		return super.hurtServer(level, source, spectral ? amount * 1.5f : amount);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 2, 1, 1, 1, 0);
			level.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 120, 2, 2, 2, 0.2);
			for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
				ReplayOutcome.grant(player, "wyrmslayer", "slain");
				ReplayOutcome.grant(player, "echo_hunter", "echo_wyrm");
			}
		}
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (!isNoAi()) {
			bossEvent.addPlayer(player);
		}
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossEvent.removePlayer(player);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.WYRM_ROAR;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.WYRM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.WYRM_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 4.0f;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 240;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (home != null) {
			output.store("home", BlockPos.CODEC, home);
		}
		output.putBoolean("summoned", summoned);
		output.putString("wyrm_name", wyrmName);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.home = input.read("home", BlockPos.CODEC).orElse(null);
		this.summoned = input.getBooleanOr("summoned", false);
		this.wyrmName = input.getStringOr("wyrm_name", "");
		if (hasCustomName()) {
			bossEvent.setName(getDisplayName());
		}
	}
}
