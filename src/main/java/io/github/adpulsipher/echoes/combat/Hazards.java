package io.github.adpulsipher.echoes.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.adpulsipher.echoes.registry.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Telegraphed area attacks: a warning circle is drawn on the ground, and when it closes the blow lands. Used
 * by the Siege Colossus's mortar barrage, the Hierophant's starfall and the Hollow King's decree.
 */
public final class Hazards {
	public enum Style {
		/** A siege stone falls from the sky. */
		MORTAR,
		/** A shaft of dawn light spears down. */
		STARFALL,
		/** A ring of spectral force erupts from the ground. */
		DECREE
	}

	private static final List<Hazard> PENDING = new ArrayList<>();

	private Hazards() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
	}

	public static void schedule(ServerLevel level, LivingEntity owner, Vec3 at, double radius, float damage, int delay, Style style) {
		PENDING.add(new Hazard(level, owner, at, radius, damage, delay, style));
	}

	private static void tick() {
		Iterator<Hazard> it = PENDING.iterator();
		while (it.hasNext()) {
			Hazard hazard = it.next();
			if (hazard.step()) {
				it.remove();
			}
		}
	}

	private static final class Hazard {
		final ServerLevel level;
		final LivingEntity owner;
		final Vec3 at;
		final double radius;
		final float damage;
		final int total;
		final Style style;
		int remaining;

		Hazard(ServerLevel level, LivingEntity owner, Vec3 at, double radius, float damage, int delay, Style style) {
			this.level = level;
			this.owner = owner;
			this.at = at;
			this.radius = radius;
			this.damage = damage;
			this.total = Math.max(1, delay);
			this.remaining = this.total;
			this.style = style;
		}

		boolean step() {
			remaining--;
			float progress = 1.0f - remaining / (float) total;
			int color = switch (style) {
				case MORTAR -> 0xFF6A2A;
				case STARFALL -> 0xFFE7A0;
				case DECREE -> 0xB08CFF;
			};
			if (remaining % 2 == 0) {
				// The warning circle closes as the blow approaches.
				int points = (int) (10 + radius * 6);
				double r = radius * (1.15 - 0.15 * progress);
				for (int i = 0; i < points; i++) {
					double angle = i * Math.PI * 2 / points + remaining * 0.05;
					level.sendParticles(new DustParticleOptions(color, 1.0f), at.x + Math.cos(angle) * r, at.y + 0.15, at.z + Math.sin(angle) * r, 1, 0, 0, 0, 0);
				}
			}
			switch (style) {
				case MORTAR -> {
					if (remaining < 12) {
						double h = remaining * 1.6;
						level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + h, at.z, 2, 0.15, 0.2, 0.15, 0.0);
						level.sendParticles(ParticleTypes.FLAME, at.x, at.y + h, at.z, 2, 0.15, 0.2, 0.15, 0.0);
					}
				}
				case STARFALL -> {
					if (remaining < 14) {
						double h = remaining * 1.4;
						level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + h, at.z, 3, 0.05, 0.3, 0.05, 0.0);
					}
				}
				case DECREE -> level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 0.2, at.z, 1, radius * 0.5, 0.05, radius * 0.5, 0.0);
			}
			if (remaining > 0) {
				return false;
			}
			land(color);
			return true;
		}

		void land(int color) {
			switch (style) {
				case MORTAR -> {
					level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 2, 0.5, 0.3, 0.5, 0.0);
					level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 8, 0.6, 0.2, 0.6, 0.0);
					level.playSound(null, at.x, at.y, at.z, ModSounds.MORTAR_IMPACT, SoundSource.HOSTILE, 1.6f, 0.8f + level.getRandom().nextFloat() * 0.3f);
				}
				case STARFALL -> {
					level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFE7A0), at.x, at.y + 0.5, at.z, 1, 0, 0, 0, 0);
					level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.3, at.z, 24, 0.4, 0.2, 0.4, 0.15);
					level.playSound(null, at.x, at.y, at.z, ModSounds.STARFALL, SoundSource.HOSTILE, 1.2f, 1.0f + level.getRandom().nextFloat() * 0.4f);
				}
				case DECREE -> {
					for (int i = 0; i < 48; i++) {
						double angle = i * Math.PI * 2 / 48;
						level.sendParticles(new DustParticleOptions(color, 1.6f), at.x + Math.cos(angle) * radius, at.y + 0.3, at.z + Math.sin(angle) * radius, 2, 0.1, 0.3, 0.1, 0.0);
					}
					level.sendParticles(ParticleTypes.SONIC_BOOM, at.x, at.y + 1, at.z, 1, 0, 0, 0, 0);
				}
			}
			AABB area = new AABB(at, at).inflate(radius, 2.5, radius);
			for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != owner && e.isAlive() && !Spectral.allied(owner, e))) {
				double dx = victim.getX() - at.x;
				double dz = victim.getZ() - at.z;
				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				victim.hurtServer(level, level.damageSources().mobAttack(owner), damage);
				double len = Math.max(0.3, Math.sqrt(dx * dx + dz * dz));
				victim.push(dx / len * 0.6, style == Style.MORTAR ? 0.5 : 0.35, dz / len * 0.6);
				if (style == Style.MORTAR) {
					victim.igniteForSeconds(2.0f);
				} else if (style == Style.DECREE) {
					victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), owner);
					victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), owner);
				}
			}
		}
	}
}
