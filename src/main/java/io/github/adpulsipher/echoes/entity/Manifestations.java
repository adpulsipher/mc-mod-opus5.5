package io.github.adpulsipher.echoes.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.adpulsipher.echoes.registry.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The ritual of a great echo tearing into the present: light spirals up around the point for a few seconds,
 * the ground shakes, and then the boss steps out.
 */
public final class Manifestations {
	private static final List<Manifestation> PENDING = new ArrayList<>();

	private Manifestations() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<Manifestation> it = PENDING.iterator();
			while (it.hasNext()) {
				if (it.next().step()) {
					it.remove();
				}
			}
		});
	}

	/** Begins manifesting {@code kind} at {@code pos} after {@code delay} ticks. */
	public static void begin(ServerLevel level, BlockPos pos, BossKind kind, int delay, @Nullable String name) {
		PENDING.add(new Manifestation(level, pos, kind, Math.max(1, delay), name));
		Component title = Component.translatable("boss.echoes_of_the_past.stirs").withStyle(Style.EMPTY.withColor(kind.color()));
		Component subtitle = Component.translatable("entity.echoes_of_the_past." + kind.id()).withStyle(Style.EMPTY.withColor(0xC8C8DC).withItalic(true));
		for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(48))) {
			player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
			player.connection.send(new ClientboundSetTitleTextPacket(title));
			player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
		}
		level.playSound(null, pos, ModSounds.BOSS_MANIFEST, SoundSource.HOSTILE, 3.0f, 1.0f);
	}

	/** Spawns the boss right away. */
	public static @Nullable Mob spawn(ServerLevel level, BlockPos pos, BossKind kind, @Nullable String name) {
		Mob boss = kind.type().create(level, EntitySpawnReason.MOB_SUMMONED);
		if (boss == null) {
			return null;
		}
		double y = pos.getY() + (kind == BossKind.ECHO_WYRM ? 8 : kind == BossKind.HIEROPHANT ? 2 : 0);
		boss.setPos(pos.getX() + 0.5, y, pos.getZ() + 0.5);
		boss.setYRot(level.getRandom().nextFloat() * 360.0f);
		if (boss instanceof EchoBoss echoBoss) {
			echoBoss.setHome(pos);
		} else if (boss instanceof EchoWyrmEntity wyrm) {
			wyrm.setHome(pos.above(10));
			if (name != null) {
				wyrm.setWyrmName(name);
			}
		}
		if (name != null && !(boss instanceof EchoWyrmEntity)) {
			boss.setCustomName(Component.literal(name));
		}
		boss.setPersistenceRequired();
		level.addFreshEntity(boss);
		return boss;
	}

	private static final class Manifestation {
		final ServerLevel level;
		final BlockPos pos;
		final BossKind kind;
		final int total;
		final @Nullable String name;
		int tick;

		Manifestation(ServerLevel level, BlockPos pos, BossKind kind, int total, @Nullable String name) {
			this.level = level;
			this.pos = pos;
			this.kind = kind;
			this.total = total;
			this.name = name;
		}

		boolean step() {
			tick++;
			Vec3 center = Vec3.atBottomCenterOf(pos);
			DustParticleOptions dust = new DustParticleOptions(kind.color(), 1.5f);
			double progress = tick / (double) total;
			for (int i = 0; i < 4; i++) {
				double angle = tick * 0.3 + i * Math.PI / 2;
				double r = 3.5 * (1.0 - progress) + 0.5;
				double h = (tick % 20) * 0.25;
				level.sendParticles(dust, center.x + Math.cos(angle) * r, center.y + h, center.z + Math.sin(angle) * r, 1, 0, 0, 0, 0);
			}
			level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.5, center.z, 4, 0.8, 1.2, 0.8, 0.05);
			if (tick % 20 == 0) {
				level.playSound(null, pos, ModSounds.REPLAY_RUMBLE, SoundSource.HOSTILE, 2.0f, 0.6f + (float) progress * 0.4f);
			}
			if (tick < total) {
				return false;
			}
			level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | kind.color()), center.x, center.y + 1.5, center.z, 1, 0, 0, 0, 0);
			level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 1.5, center.z, 1, 0, 0, 0, 0);
			level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.5, center.z, 80, 1.0, 1.5, 1.0, 0.2);
			level.playSound(null, pos, ModSounds.RIFT_OPEN, SoundSource.HOSTILE, 3.0f, 0.6f);
			spawn(level, pos, kind, name);
			return true;
		}
	}
}
