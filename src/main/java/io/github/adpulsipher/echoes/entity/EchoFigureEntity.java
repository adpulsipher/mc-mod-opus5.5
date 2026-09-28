package io.github.adpulsipher.echoes.entity;

import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.Role;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A ghostly figure in a historic replay. It has no will of its own: the replay director moves it every tick,
 * and it quietly dissolves if the director ever stops caring for it.
 */
public class EchoFigureEntity extends Mob {
	private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(EchoFigureEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TINT = SynchedEntityData.defineId(EchoFigureEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(EchoFigureEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FADING = SynchedEntityData.defineId(EchoFigureEntity.class, EntityDataSerializers.BOOLEAN);

	/** Ticks without a director before an orphaned figure dissolves. */
	private static final int ORPHAN_TIMEOUT = 60;

	private int lastDirected;
	/** Client-side: how long the figure has been fading, and when its current pose began. */
	private int fadeTicks;
	private int poseStartTick;

	public EchoFigureEntity(EntityType<? extends EchoFigureEntity> type, Level level) {
		super(type, level);
		this.setNoAi(true);
		this.setNoGravity(true);
		this.setSilent(true);
		this.noPhysics = true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ROLE, 0);
		builder.define(TINT, 0xBFEFFF);
		builder.define(POSE, 0);
		builder.define(FADING, false);
	}

	public Role getRole() {
		return Role.byId(this.entityData.get(ROLE));
	}

	public void setRole(Role role) {
		this.entityData.set(ROLE, role.ordinal());
	}

	public int getTint() {
		return this.entityData.get(TINT);
	}

	public void setTint(int tint) {
		this.entityData.set(TINT, tint);
	}

	public Pose getFigurePose() {
		return Pose.byId(this.entityData.get(POSE));
	}

	public void setFigurePose(Pose pose) {
		if (pose.ordinal() != this.entityData.get(POSE)) {
			this.entityData.set(POSE, pose.ordinal());
		}
	}

	public boolean isFading() {
		return this.entityData.get(FADING);
	}

	public void startFading() {
		this.entityData.set(FADING, true);
	}

	public int getFadeTicks() {
		return fadeTicks;
	}

	public int getPoseStartTick() {
		return poseStartTick;
	}

	/** Called by the replay director every tick while the figure is part of a running replay. */
	public void direct() {
		this.lastDirected = this.tickCount;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);
		if (POSE.equals(data)) {
			this.poseStartTick = this.tickCount;
		}
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();
		if (level.isClientSide()) {
			if (isFading()) {
				fadeTicks++;
			}
			if (this.random.nextInt(getRole() == Role.DRAGON ? 1 : 5) == 0) {
				int tint = getTint();
				double spread = getRole() == Role.DRAGON ? 2.5 : 0.4;
				level.addParticle(new DustParticleOptions(tint, 0.7f),
						getX() + (random.nextDouble() - 0.5) * spread,
						getY() + random.nextDouble() * getBbHeight() * (getRole() == Role.DRAGON ? 2.0 : 1.0),
						getZ() + (random.nextDouble() - 0.5) * spread,
						0, 0.02, 0);
			}
		} else {
			if (this.tickCount - lastDirected > ORPHAN_TIMEOUT) {
				this.discard();
			}
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(net.minecraft.world.entity.Entity entity) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("figure_role", this.entityData.get(ROLE));
		output.putInt("figure_tint", getTint());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(ROLE, input.getIntOr("figure_role", 0));
		this.entityData.set(TINT, input.getIntOr("figure_tint", 0xBFEFFF));
	}
}
