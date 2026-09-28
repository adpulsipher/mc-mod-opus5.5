package io.github.adpulsipher.echoes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Delayed "echo strikes" from the Echoing Blade: the ghost of a blow that lands a moment after the real one.
 */
public final class EchoStrikes {
	/** Just longer than the invulnerability window after a hit, so the echo always lands. */
	private static final int DELAY = 12;
	private static final List<Strike> PENDING = new ArrayList<>();

	private EchoStrikes() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
	}

	public static void schedule(ServerLevel level, LivingEntity target, LivingEntity attacker, float damage) {
		PENDING.add(new Strike(level, target, attacker, damage, DELAY));
	}

	private static void tick() {
		Iterator<Strike> it = PENDING.iterator();
		while (it.hasNext()) {
			Strike strike = it.next();
			if (--strike.remaining > 0) {
				continue;
			}
			it.remove();
			LivingEntity target = strike.target;
			if (!target.isAlive() || target.level() != strike.level) {
				continue;
			}
			strike.level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.5), target.getZ(), 1, 0, 0, 0, 0);
			strike.level.sendParticles(new DustParticleOptions(0x7FE9FF, 1.2f), target.getX(), target.getY(0.5), target.getZ(), 12, 0.4, 0.5, 0.4, 0.02);
			strike.level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7f, 1.6f);
			target.hurtServer(strike.level, strike.level.damageSources().indirectMagic(strike.attacker, strike.attacker), strike.damage);
		}
	}

	private static final class Strike {
		final ServerLevel level;
		final LivingEntity target;
		final LivingEntity attacker;
		final float damage;
		int remaining;

		Strike(ServerLevel level, LivingEntity target, LivingEntity attacker, float damage, int remaining) {
			this.level = level;
			this.target = target;
			this.attacker = attacker;
			this.damage = damage;
			this.remaining = remaining;
		}
	}
}
