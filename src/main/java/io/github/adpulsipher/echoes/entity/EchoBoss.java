package io.github.adpulsipher.echoes.entity;

import java.util.UUID;

import io.github.adpulsipher.echoes.combat.Echoborn;
import io.github.adpulsipher.echoes.combat.Spectral;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Shared machinery for the great echoes: a boss bar, a home to defend, a synced attack state for animations,
 * a second phase at half health and the rites of their passing.
 */
public abstract class EchoBoss extends Monster implements Echoborn {
	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(EchoBoss.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(EchoBoss.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> STATE_START = SynchedEntityData.defineId(EchoBoss.class, EntityDataSerializers.INT);

	protected final ServerBossEvent bossEvent;
	@Nullable
	protected BlockPos home;
	protected int stateTicks;

	protected EchoBoss(EntityType<? extends EchoBoss> type, Level level, BossEvent.BossBarColor color) {
		super(type, level);
		this.xpReward = 300;
		this.bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
		this.bossEvent.setDarkenScreen(true);
		this.setPersistenceRequired();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STATE, 0);
		builder.define(ENRAGED, false);
		builder.define(STATE_START, 0);
	}

	/** The id of the current attack, for animation. */
	public int getAttackState() {
		return this.entityData.get(STATE);
	}

	/** Ticks since the current attack began, as seen by the client. */
	public float attackTime(float partialTick) {
		return tickCount - this.entityData.get(STATE_START) + partialTick;
	}

	protected void setAttackState(int state) {
		this.entityData.set(STATE, state);
		this.entityData.set(STATE_START, tickCount);
		this.stateTicks = 0;
	}

	public boolean isEnraged() {
		return this.entityData.get(ENRAGED);
	}

	public void setHome(BlockPos home) {
		this.home = home;
	}

	protected BlockPos home() {
		if (home == null) {
			home = blockPosition();
		}
		return home;
	}

	/** The advancement granted to everyone nearby when this boss falls. */
	protected abstract String slainAdvancement();

	/** Colour of the boss's name in announcements. */
	protected abstract int themeColor();

	/** Runs the boss's attack logic for one tick. */
	protected abstract void bossTick(ServerLevel level, @Nullable LivingEntity target);

	/** Called once when the boss first falls below half health. */
	protected void onEnrage(ServerLevel level) {
	}

	@Override
	protected void registerGoals() {
		// Bosses are driven by the state machines in bossTick.
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isNoAi()) {
			return;
		}
		bossEvent.setProgress(getHealth() / getMaxHealth());
		stateTicks++;

		LivingEntity target = getTarget();
		if (target == null || !target.isAlive() || target.distanceToSqr(this) > 64 * 64 || isIgnorable(target)) {
			target = findTarget(level, 40.0);
			setTarget(target);
		}
		if (!isEnraged() && getHealth() < getMaxHealth() * 0.5f) {
			this.entityData.set(ENRAGED, true);
			announce(level, Component.translatable("boss.echoes_of_the_past.enraged", getDisplayName()));
			onEnrage(level);
		}
		bossTick(level, target);
	}

	protected static boolean isIgnorable(LivingEntity target) {
		return target instanceof Player player && (player.isCreative() || player.isSpectator());
	}

	protected @Nullable LivingEntity findTarget(ServerLevel level, double range) {
		Player nearest = null;
		double best = range * range;
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

	protected void announce(ServerLevel level, Component message) {
		Component styled = message.copy().withStyle(Style.EMPTY.withColor(themeColor()));
		for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
			player.sendSystemMessage(styled);
		}
	}

	/** Turns the boss to face a point instantly. */
	protected void faceTowards(Vec3 point) {
		Vec3 look = point.subtract(position());
		float yaw = (float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0f;
		setYRot(yaw);
		yBodyRot = yaw;
		yHeadRot = yaw;
	}

	/** Whether {@code victim} stands within the arc in front of the boss. */
	protected boolean inFront(LivingEntity victim, double minDot) {
		Vec3 facing = Vec3.directionFromRotation(0, getYRot());
		Vec3 to = victim.position().subtract(position()).multiply(1, 0, 1);
		if (to.lengthSqr() < 1.0E-4) {
			return true;
		}
		return to.normalize().dot(facing) >= minDot;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return super.hurtServer(level, source, Spectral.isSpectralHit(source) ? amount * 1.5f : amount);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(0.5), getZ(), 2, 1, 1, 1, 0);
			level.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 120, 1.5, 2, 1.5, 0.2);
			level.sendParticles(ParticleTypes.SOUL, getX(), getY(0.5), getZ(), 60, 1.5, 2, 1.5, 0.05);
			announce(level, Component.translatable("boss.echoes_of_the_past.slain", getDisplayName()));
			for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
				ReplayOutcome.grant(player, slainAdvancement(), "slain");
				ReplayOutcome.grant(player, "echo_hunter", EntityType.getKey(getType()).getPath());
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
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		this.bossEvent.setName(this.getDisplayName());
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
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (home != null) {
			output.store("home", BlockPos.CODEC, home);
		}
		output.putBoolean("enraged", isEnraged());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.home = input.read("home", BlockPos.CODEC).orElse(null);
		this.entityData.set(ENRAGED, input.getBooleanOr("enraged", false));
		if (hasCustomName()) {
			bossEvent.setName(getDisplayName());
		}
	}
}
