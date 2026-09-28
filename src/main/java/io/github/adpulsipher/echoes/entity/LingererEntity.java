package io.github.adpulsipher.echoes.entity;

import io.github.adpulsipher.echoes.history.EventType;
import io.github.adpulsipher.echoes.history.HistoricEvent;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A soldier who never left the battlefield. Lingerers flicker between the past and the present: while "phased"
 * they cannot be harmed by ordinary weapons, and they can step through time to close the distance to their foe.
 * They will not raise a blade against anyone who wears a crown.
 */
public class LingererEntity extends Monster {
	private static final EntityDataAccessor<Boolean> PHASED = SynchedEntityData.defineId(LingererEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int PHASE_DURATION = 30;
	private static final DustParticleOptions GHOST_DUST = new DustParticleOptions(0x8FEFFF, 0.9f);

	private int phaseCooldown = 100;
	private int phaseTicks;

	public LingererEntity(EntityType<? extends LingererEntity> type, Level level) {
		super(type, level);
		this.xpReward = 8;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 28.0)
				.add(Attributes.ATTACK_DAMAGE, 5.0)
				.add(Attributes.MOVEMENT_SPEED, 0.27)
				.add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.ARMOR, 4.0);
	}

	public static boolean checkLingererSpawnRules(EntityType<LingererEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return pos.getY() < 40 && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				(target, level) -> !wearsCrown(target)));
	}

	public static boolean wearsCrown(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModTags.CROWNS);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PHASED, false);
	}

	public boolean isPhased() {
		return this.entityData.get(PHASED);
	}

	private void setPhased(boolean phased) {
		this.entityData.set(PHASED, phased);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
		SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
		RandomSource random = level.getRandom();
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextInt(4) == 0 ? Items.STONE_SWORD : Items.IRON_SWORD));
		return result;
	}

	/** Arms a lingerer that stepped out of a replay according to the memory it came from. */
	public void equipFromEcho(HistoricEvent event) {
		boolean veteran = event.type() == EventType.SIEGE || event.type() == EventType.DUEL || random.nextInt(4) == 0;
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(veteran ? ModItems.LEGIONNAIRE_BLADE : Items.IRON_SWORD));
		this.setPersistenceRequired();
	}

	@Override
	public void aiStep() {
		super.aiStep();
		Level level = this.level();
		if (level.isClientSide()) {
			int chance = isPhased() ? 1 : 4;
			if (random.nextInt(chance) == 0) {
				level.addParticle(GHOST_DUST, getRandomX(0.6), getRandomY(), getRandomZ(0.6), 0, 0.02, 0);
			}
			return;
		}

		if (isPhased()) {
			if (--phaseTicks <= 0) {
				setPhased(false);
				blink();
			}
		} else if (getTarget() != null && --phaseCooldown <= 0) {
			setPhased(true);
			phaseTicks = PHASE_DURATION;
			phaseCooldown = 90 + random.nextInt(80);
			playSound(ModSounds.LINGERER_PHASE, 1.0f, 0.9f + random.nextFloat() * 0.2f);
		}

		// Ghosts thin away in daylight.
		if (level.isBrightOutside() && level.canSeeSky(blockPosition()) && tickCount % 20 == 0 && !isPersistenceRequired()) {
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.SOUL, getX(), getY(0.6), getZ(), 3, 0.3, 0.4, 0.3, 0.01);
				hurtServer(serverLevel, damageSources().magic(), 1.0f);
			}
		}
	}

	/** Steps through time to reappear beside its target. */
	private void blink() {
		LivingEntity target = getTarget();
		if (target == null || !(level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 toTarget = target.position().subtract(position());
		double distance = toTarget.length();
		if (distance < 3.0 || distance > 20.0) {
			return;
		}
		Vec3 destination = position().add(toTarget.normalize().scale(Math.min(distance - 1.5, 6.0)));
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY(0.5), getZ(), 20, 0.3, 0.6, 0.3, 0.05);
		if (level.noCollision(this, getBoundingBox().move(destination.subtract(position())))) {
			teleportTo(destination.x, destination.y, destination.z);
			level.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY(0.5), getZ(), 8, 0.3, 0.5, 0.3, 0.02);
			level.playSound(null, getX(), getY(), getZ(), ModSounds.LINGERER_PHASE, SoundSource.HOSTILE, 0.8f, 1.4f);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean spectral = source.getWeaponItem() != null && source.getWeaponItem().is(ModTags.SPECTRAL_WEAPONS);
		if (isPhased() && !spectral && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			level.sendParticles(ParticleTypes.ENCHANTED_HIT, getX(), getY(0.6), getZ(), 6, 0.3, 0.4, 0.3, 0.1);
			return false;
		}
		return super.hurtServer(level, source, spectral ? amount * 1.5f : amount);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (isPhased()) {
			return false;
		}
		return super.doHurtTarget(level, target);
	}

	/** Shown an heirloom, the ghost remembers home and finally rests. */
	public void layToRest(ServerPlayer player) {
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY(0.5), getZ(), 30, 0.3, 0.8, 0.3, 0.05);
		level.sendParticles(GHOST_DUST, getX(), getY(0.5), getZ(), 20, 0.4, 0.8, 0.4, 0.02);
		level.playSound(null, getX(), getY(), getZ(), ModSounds.MOTH_ATTUNE, SoundSource.NEUTRAL, 1.0f, 0.7f);
		spawnAtLocation(level, new ItemStack(ModItems.ECHO_DUST, 2 + random.nextInt(3)));
		spawnAtLocation(level, new ItemStack(ModItems.ANCIENT_COIN, 1 + random.nextInt(2)));
		discard();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.LINGERER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.LINGERER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.LINGERER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		// Ghosts make no footsteps.
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("phase_cooldown", phaseCooldown);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		phaseCooldown = input.getIntOr("phase_cooldown", 100);
	}
}
