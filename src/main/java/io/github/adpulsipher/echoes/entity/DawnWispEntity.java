package io.github.adpulsipher.echoes.entity;

import java.util.EnumSet;
import java.util.UUID;

import io.github.adpulsipher.echoes.combat.Echoborn;
import io.github.adpulsipher.echoes.combat.SpectralBolts;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A mote of the Elder Dawn: the first light the world remembers, still drifting in the deepest dark. Wisps hover
 * at a distance and fire bolts of dawn. Those conjured by the Hierophant circle it as living wards.
 */
public class DawnWispEntity extends Monster implements Echoborn {
	private static final DustParticleOptions GLOW = new DustParticleOptions(0xFFE7A0, 0.8f);

	@Nullable
	private UUID master;
	private int slot;
	private int slots = 4;
	private int shotCooldown = 40;

	public DawnWispEntity(EntityType<? extends DawnWispEntity> type, Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl<>(this, 20, true);
		this.setNoGravity(true);
		this.xpReward = 6;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 14.0)
				.add(Attributes.ATTACK_DAMAGE, 3.0)
				.add(Attributes.FLYING_SPEED, 0.5)
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	public static boolean checkWispSpawnRules(EntityType<DawnWispEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < -40 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
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
		this.goalSelector.addGoal(1, new WardGoal());
		this.goalSelector.addGoal(2, new HoverAndShootGoal());
		this.goalSelector.addGoal(4, new DriftGoal());
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	/** Binds this wisp to the Hierophant as one of {@code slots} orbiting wards. */
	public void bindTo(HierophantEntity hierophant, int slot, int slots) {
		this.master = hierophant.getUUID();
		this.slot = slot;
		this.slots = slots;
		this.setPersistenceRequired();
	}

	private @Nullable HierophantEntity master() {
		if (master == null || !(level() instanceof ServerLevel level)) {
			return null;
		}
		Entity entity = level.getEntity(master);
		return entity instanceof HierophantEntity hierophant && hierophant.isAlive() ? hierophant : null;
	}

	private void shootAt(LivingEntity target) {
		if (level() instanceof ServerLevel level && hasLineOfSight(target)) {
			SpectralBolts.fire(level, this, position().add(0, getBbHeight() * 0.5, 0), target.getEyePosition().add(0, -0.4, 0), 1.1, 4.0f, SpectralBolts.Style.DAWN);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			level().addParticle(GLOW, getRandomX(0.4), getRandomY(), getRandomZ(0.4), 0, -0.01, 0);
			if (random.nextInt(5) == 0) {
				level().addParticle(ParticleTypes.END_ROD, getRandomX(0.3), getRandomY(), getRandomZ(0.3), 0, 0, 0);
			}
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
	public boolean removeWhenFarAway(double distance) {
		return master == null && super.removeWhenFarAway(distance);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.WISP_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.WISP_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.WISP_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6f;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (master != null) {
			output.store("master", UUIDUtil.CODEC, master);
		}
		output.putInt("slot", slot);
		output.putInt("slots", slots);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		master = input.read("master", UUIDUtil.CODEC).orElse(null);
		slot = input.getIntOr("slot", 0);
		slots = input.getIntOr("slots", 4);
	}

	/** Circles the Hierophant and fires on whatever it fights. */
	private class WardGoal extends Goal {
		WardGoal() {
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return master() != null;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			HierophantEntity hierophant = master();
			if (hierophant == null) {
				return;
			}
			double angle = tickCount * 0.05 + slot * Math.PI * 2 / Math.max(1, slots);
			Vec3 orbit = hierophant.position().add(Math.cos(angle) * 3.2, hierophant.getBbHeight() * 0.55 + Math.sin(tickCount * 0.1 + slot) * 0.4, Math.sin(angle) * 3.2);
			getMoveControl().setWantedPosition(orbit.x, orbit.y, orbit.z, 1.4);
			LivingEntity target = hierophant.getTarget();
			if (target != null) {
				getLookControl().setLookAt(target);
				if (--shotCooldown <= 0) {
					shotCooldown = 70 + random.nextInt(30);
					shootAt(target);
				}
			}
		}
	}

	/** Keeps its distance from its target and fires bolts of dawn. */
	private class HoverAndShootGoal extends Goal {
		HoverAndShootGoal() {
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = getTarget();
			return target != null && target.isAlive();
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
			getLookControl().setLookAt(target);
			Vec3 away = position().subtract(target.position()).multiply(1, 0, 1);
			if (away.lengthSqr() < 0.01) {
				away = new Vec3(1, 0, 0);
			}
			Vec3 spot = target.position().add(away.normalize().scale(6.0)).add(0, 3.0 + Math.sin(tickCount * 0.08), 0);
			getMoveControl().setWantedPosition(spot.x, spot.y, spot.z, 1.0);
			if (--shotCooldown <= 0) {
				shotCooldown = 45 + random.nextInt(20);
				shootAt(target);
			}
		}
	}

	/** Aimless drifting through the dark. */
	private class DriftGoal extends Goal {
		DriftGoal() {
			setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return getNavigation().isDone() && random.nextInt(10) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return getNavigation().isInProgress();
		}

		@Override
		public void start() {
			for (int i = 0; i < 6; i++) {
				BlockPos spot = blockPosition().offset(random.nextInt(11) - 5, random.nextInt(5) - 2, random.nextInt(11) - 5);
				if (level().getBlockState(spot).isAir()) {
					getNavigation().moveTo(spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5, 0.8);
					return;
				}
			}
		}
	}
}
