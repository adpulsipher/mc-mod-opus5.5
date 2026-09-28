package io.github.adpulsipher.echoes.entity;

import java.util.EnumSet;
import java.util.Optional;

import io.github.adpulsipher.echoes.item.ResonanceCompassItem;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.registry.ModSounds;
import io.github.adpulsipher.echoes.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A pale moth drawn to the glow of crystallized memory. Feed it echo dust and it will remember you, and lead
 * you to the nearest echo deposit.
 */
public class MemoryMothEntity extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> ATTUNED = SynchedEntityData.defineId(MemoryMothEntity.class, EntityDataSerializers.BOOLEAN);
	private static final DustParticleOptions TRAIL = new DustParticleOptions(0xBFF6FF, 0.6f);

	private BlockPos deposit;
	private int searchCooldown;

	public MemoryMothEntity(EntityType<? extends MemoryMothEntity> type, Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl<>(this, 20, true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 4.0)
				.add(Attributes.FLYING_SPEED, 0.55)
				.add(Attributes.MOVEMENT_SPEED, 0.25)
				.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	public static boolean checkMothSpawnRules(EntityType<MemoryMothEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < 50 && level.getBrightness(LightLayer.SKY, pos) == 0 && level.getBlockState(pos).isAir();
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
		navigation.setCanOpenDoors(false);
		navigation.setCanFloat(true);
		return navigation;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new SeekEchoGoal());
		this.goalSelector.addGoal(3, new FlutterGoal());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTUNED, false);
	}

	public boolean isAttuned() {
		return this.entityData.get(ATTUNED);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(ModItems.ECHO_DUST) || isAttuned()) {
			return super.mobInteract(player, hand);
		}
		if (!level().isClientSide()) {
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			this.entityData.set(ATTUNED, true);
			this.setPersistenceRequired();
			this.searchCooldown = 0;
			playSound(ModSounds.MOTH_ATTUNE, 1.0f, 1.2f);
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 12, 0.3, 0.3, 0.3, 0.05);
			}
			if (player instanceof ServerPlayer serverPlayer) {
				ReplayOutcome.grant(serverPlayer, "moth_to_a_flame", "attuned");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide() && random.nextInt(isAttuned() ? 2 : 6) == 0) {
			level().addParticle(TRAIL, getRandomX(0.3), getY(0.3), getRandomZ(0.3), 0, -0.01, 0);
		}
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
	}

	@Override
	public boolean isFlapping() {
		return true;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.MOTH_FLUTTER;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return !isAttuned() && super.removeWhenFarAway(distance);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("attuned", isAttuned());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(ATTUNED, input.getBooleanOr("attuned", false));
	}

	/** Attuned moths fly to the nearest echo deposit and circle it. */
	private class SeekEchoGoal extends Goal {
		SeekEchoGoal() {
			setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (!isAttuned() || !(level() instanceof ServerLevel level)) {
				return false;
			}
			if (deposit != null && !level.getBlockState(deposit).is(ModTags.ECHO_DEPOSITS)) {
				deposit = null;
			}
			if (deposit == null && --searchCooldown <= 0) {
				searchCooldown = 100;
				Optional<BlockPos> found = ResonanceCompassItem.findNearest(level, blockPosition());
				deposit = found.orElse(null);
			}
			return deposit != null;
		}

		@Override
		public void tick() {
			if (deposit == null) {
				return;
			}
			double angle = tickCount * 0.12;
			Vec3 orbit = Vec3.atCenterOf(deposit).add(Math.cos(angle) * 1.4, 0.8 + Math.sin(tickCount * 0.07) * 0.4, Math.sin(angle) * 1.4);
			getMoveControl().setWantedPosition(orbit.x, orbit.y, orbit.z, 1.0);
		}
	}

	/** Aimless fluttering. */
	private class FlutterGoal extends Goal {
		FlutterGoal() {
			setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return getNavigation().isDone() && random.nextInt(8) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return getNavigation().isInProgress();
		}

		@Override
		public void start() {
			for (int i = 0; i < 6; i++) {
				BlockPos target = blockPosition().offset(random.nextInt(13) - 6, random.nextInt(7) - 3, random.nextInt(13) - 6);
				if (level().getBlockState(target).isAir()) {
					getNavigation().moveTo(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 1.0);
					return;
				}
			}
		}
	}
}
