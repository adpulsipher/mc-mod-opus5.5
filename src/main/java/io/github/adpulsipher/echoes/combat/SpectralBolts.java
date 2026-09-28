package io.github.adpulsipher.echoes.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Bolts of remembered light. They are simulated on the server rather than being entities: each tick a bolt
 * advances, leaves a trail of particles and checks what it passed through.
 */
public final class SpectralBolts {
	public enum Style {
		/** Ghostly arrows of the Spectral Archers: they chill and slow. */
		SPECTRAL(new DustParticleOptions(0x8FEFFF, 1.0f), ParticleTypes.SOUL_FIRE_FLAME),
		/** Motes of the Elder Dawn, fired by wisps, the Hierophant and the Staff of the Elder Dawn. */
		DAWN(new DustParticleOptions(0xFFE7A0, 1.2f), ParticleTypes.END_ROD),
		/** Embers of the Age of Iron and Ash. */
		EMBER(new DustParticleOptions(0xFF8A3A, 1.1f), ParticleTypes.FLAME);

		final ParticleOptions trail;
		final ParticleOptions spark;

		Style(ParticleOptions trail, ParticleOptions spark) {
			this.trail = trail;
			this.spark = spark;
		}
	}

	private static final List<Bolt> BOLTS = new ArrayList<>();

	private SpectralBolts() {
	}

	public static void init() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> tick());
	}

	/** Fires a bolt from {@code from} toward {@code at}. */
	public static void fire(ServerLevel level, LivingEntity owner, Vec3 from, Vec3 at, double speed, float damage, Style style) {
		Vec3 direction = at.subtract(from);
		if (direction.lengthSqr() < 1.0E-6) {
			direction = owner.getLookAngle();
		}
		BOLTS.add(new Bolt(level, owner, from, direction.normalize().scale(speed), damage, style));
		level.playSound(null, from.x, from.y, from.z, ModSounds.BOLT_FIRE, SoundSource.HOSTILE, 0.8f, style == Style.DAWN ? 1.4f : 1.0f);
	}

	public static int active() {
		return BOLTS.size();
	}

	private static void tick() {
		Iterator<Bolt> it = BOLTS.iterator();
		while (it.hasNext()) {
			Bolt bolt = it.next();
			if (bolt.step()) {
				it.remove();
			}
		}
	}

	private static final class Bolt {
		final ServerLevel level;
		final LivingEntity owner;
		Vec3 pos;
		final Vec3 velocity;
		final float damage;
		final Style style;
		int life = 60;

		Bolt(ServerLevel level, LivingEntity owner, Vec3 pos, Vec3 velocity, float damage, Style style) {
			this.level = level;
			this.owner = owner;
			this.pos = pos;
			this.velocity = velocity;
			this.damage = damage;
			this.style = style;
		}

		/** Returns true once the bolt is spent. */
		boolean step() {
			if (--life <= 0 || owner.isRemoved() && owner.level() != level) {
				return true;
			}
			Vec3 next = pos.add(velocity);
			BlockHitResult blockHit = level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
			Vec3 end = blockHit.getType() == HitResult.Type.MISS ? next : blockHit.getLocation();

			LivingEntity victim = null;
			double best = Double.MAX_VALUE;
			AABB sweep = new AABB(pos, end).inflate(0.8);
			for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, sweep, e -> e != owner && e.isAlive() && !e.isSpectator() && !Spectral.allied(owner, e))) {
				Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3).clip(pos, end);
				if (hit.isPresent()) {
					double d = hit.get().distanceToSqr(pos);
					if (d < best) {
						best = d;
						victim = candidate;
					}
				}
			}

			// Trail
			int steps = Math.max(2, (int) (end.distanceTo(pos) * 2.5));
			for (int i = 0; i < steps; i++) {
				Vec3 p = pos.lerp(end, i / (double) steps);
				level.sendParticles(style.trail, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
			}
			level.sendParticles(style.spark, end.x, end.y, end.z, 1, 0.05, 0.05, 0.05, 0.005);

			if (victim != null) {
				victim.hurtServer(level, level.damageSources().indirectMagic(owner, owner), damage);
				switch (style) {
					case SPECTRAL -> victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), owner);
					case EMBER -> victim.igniteForSeconds(3.0f);
					case DAWN -> victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0), owner);
				}
				burst(victim.position().add(0, victim.getBbHeight() * 0.5, 0));
				return true;
			}
			if (blockHit.getType() != HitResult.Type.MISS) {
				burst(end);
				return true;
			}
			pos = end;
			return false;
		}

		void burst(Vec3 at) {
			level.sendParticles(style.spark, at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.05);
			level.sendParticles(style.trail, at.x, at.y, at.z, 8, 0.3, 0.3, 0.3, 0.0);
			level.playSound(null, at.x, at.y, at.z, ModSounds.BOLT_HIT, SoundSource.HOSTILE, 0.7f, 1.2f);
		}
	}
}
