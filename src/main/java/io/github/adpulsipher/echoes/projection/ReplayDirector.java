package io.github.adpulsipher.echoes.projection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.adpulsipher.echoes.entity.EchoFigureEntity;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModSounds;
import io.github.adpulsipher.echoes.replay.ActorSpec;
import io.github.adpulsipher.echoes.replay.Choreographer;
import io.github.adpulsipher.echoes.replay.Cue;
import io.github.adpulsipher.echoes.replay.EffectKind;
import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.ReplayScript;
import io.github.adpulsipher.echoes.replay.Role;
import io.github.adpulsipher.echoes.replay.SoundKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Runs a replay on the server: spawns the ghostly figures around a projector and drives them through their
 * script, conjuring effects and narrating as it goes.
 */
public class ReplayDirector {
	/** Ticks of projector spin-up before the first figure appears. */
	public static final int INTRO = 50;
	public static final double AUDIENCE_RADIUS = 40.0;

	private final ServerLevel level;
	private final BlockPos origin;
	private final HistoricEvent event;
	private final ReplayScript script;
	private final boolean resonant;
	private final RandomSource random;

	private final Map<Integer, Actor> actors = new HashMap<>();
	private final List<ActiveEffect> effects = new ArrayList<>();
	private final Map<Long, Double> groundCache = new HashMap<>();
	private int cueIndex;
	private int tick;
	private int currentBeat = -1;
	private boolean finished;
	private boolean riftPending;

	public ReplayDirector(ServerLevel level, BlockPos origin, HistoricEvent event, boolean resonant) {
		this.level = level;
		this.origin = origin;
		this.event = event;
		this.script = Choreographer.stage(event);
		this.resonant = resonant;
		this.random = RandomSource.create(event.seed() ^ level.getGameTime());
	}

	public HistoricEvent event() {
		return event;
	}

	public boolean resonant() {
		return resonant;
	}

	public int tick() {
		return tick;
	}

	public int totalDuration() {
		return INTRO + script.duration();
	}

	public boolean finished() {
		return finished;
	}

	public boolean riftPending() {
		return riftPending;
	}

	/** Advances the replay by one tick. Returns true once it has finished. */
	public boolean advance() {
		if (finished) {
			return true;
		}
		if (tick < INTRO) {
			intro();
		} else {
			int t = tick - INTRO;
			spawnActors(t);
			runCues(t);
			updateActors();
			updateEffects();
			narrate(t);
			if (t == Choreographer.BEATS[4] - 20 && event.unstable()) {
				double chance = switch (event.type()) {
					case DRAGON_ATTACK -> event.era().ordinal() >= 3 ? 0.55 : 0.2;
					case RITUAL -> 0.35;
					default -> 0.25;
				};
				if (random.nextDouble() < chance) {
					riftPending = true;
					warnRift();
				}
			}
			if (t >= script.duration()) {
				finished = true;
				cleanup();
				return true;
			}
		}
		tick++;
		return false;
	}

	/** Stops the replay immediately, dissolving every figure. */
	public void abort() {
		finished = true;
		cleanup();
	}

	// ------------------------------------------------------------------ stage

	private Vec3 stagePos(double x, double z) {
		double wx = origin.getX() + 0.5 + x;
		double wz = origin.getZ() + 0.5 + z;
		return new Vec3(wx, groundY(wx, wz), wz);
	}

	/** Finds the floor under a stage position, so ghosts walk over the terrain rather than through it. */
	private double groundY(double x, double z) {
		int bx = Mth.floor(x);
		int bz = Mth.floor(z);
		long key = BlockPos.asLong(bx, 0, bz);
		Double cached = groundCache.get(key);
		if (cached != null) {
			return cached;
		}
		double result = origin.getY();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dy = 3; dy >= -5; dy--) {
			pos.set(bx, origin.getY() + dy, bz);
			BlockState here = level.getBlockState(pos);
			BlockState below = level.getBlockState(pos.below());
			if (here.getCollisionShape(level, pos).isEmpty() && !below.getCollisionShape(level, pos.below()).isEmpty()) {
				result = pos.getY() + Math.max(0, below.getCollisionShape(level, pos.below()).max(net.minecraft.core.Direction.Axis.Y) - 1.0);
				break;
			}
		}
		groundCache.put(key, result);
		return result;
	}

	private void intro() {
		Vec3 center = Vec3.atBottomCenterOf(origin);
		if (tick == 0) {
			level.playSound(null, origin, ModSounds.PROJECTOR_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.0f);
		}
		if (tick % 3 == 0) {
			double height = 1.0 + tick / (double) INTRO * 3.0;
			for (int i = 0; i < 6; i++) {
				double angle = (tick * 0.35) + i * Math.PI / 3;
				double r = 0.6 + tick / (double) INTRO * 7.5;
				level.sendParticles(dust(event.era().tint(), 1.0f), center.x + Math.cos(angle) * r, center.y + 0.3, center.z + Math.sin(angle) * r, 1, 0, 0, 0, 0);
			}
			level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + height, center.z, 2, 0.1, 0.3, 0.1, 0.01);
		}
		if (tick == INTRO - 10) {
			level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF), center.x, center.y + 1.5, center.z, 1, 0.0, 0.0, 0.0, 0.0);
			level.playSound(null, origin, ModSounds.REPLAY_WHISPER, SoundSource.AMBIENT, 1.0f, 0.8f);
			showTitle();
		}
	}

	private void showTitle() {
		Component title = Component.literal(event.title()).withStyle(Style.EMPTY.withColor(event.era().tint()));
		Component subtitle = Component.literal(event.dateLine() + " — " + event.yearsAgo() + " years ago").withStyle(Style.EMPTY.withColor(0xC8C8DC).withItalic(true));
		for (ServerPlayer player : audience()) {
			player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 70, 25));
			player.connection.send(new ClientboundSetTitleTextPacket(title));
			player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
		}
	}

	private void narrate(int t) {
		for (int i = Choreographer.BEATS.length - 1; i >= 0; i--) {
			if (t >= Choreographer.BEATS[i]) {
				if (i != currentBeat) {
					currentBeat = i;
				}
				int since = t - Choreographer.BEATS[i];
				// The action bar fades after a few seconds, so keep the line up for most of the act.
				if (since % 40 == 0 && since < 120) {
					Component line = Component.literal(event.narration().get(i)).withStyle(Style.EMPTY.withColor(0xDDF6FF).withItalic(true));
					for (ServerPlayer player : audience()) {
						player.connection.send(new ClientboundSetActionBarTextPacket(line));
					}
				}
				break;
			}
		}
	}

	private void warnRift() {
		level.playSound(null, origin, ModSounds.RIFT_WARNING, SoundSource.HOSTILE, 1.5f, 0.8f);
		Component warning = Component.translatable("replay.echoes_of_the_past.rift_warning").withStyle(Style.EMPTY.withColor(0xFF7A9A).withBold(true));
		for (ServerPlayer player : audience()) {
			player.sendSystemMessage(warning);
		}
		effects.add(new ActiveEffect(EffectKind.RIFT, Vec3.atBottomCenterOf(origin).add(0, 3, 0), 1.0, 160));
	}

	public List<ServerPlayer> audience() {
		Vec3 center = Vec3.atCenterOf(origin);
		List<ServerPlayer> players = new ArrayList<>();
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceToSqr(center) < AUDIENCE_RADIUS * AUDIENCE_RADIUS) {
				players.add(player);
			}
		}
		return players;
	}

	// ------------------------------------------------------------------ actors

	private void spawnActors(int t) {
		for (ActorSpec spec : script.actors()) {
			if (spec.appearTick() == t && !actors.containsKey(spec.id())) {
				EchoFigureEntity figure = new EchoFigureEntity(ModEntities.ECHO_FIGURE, level);
				Vec3 pos = stagePos(spec.x(), spec.z()).add(0, spec.y(), 0);
				figure.setPos(pos.x, pos.y, pos.z);
				figure.setYRot(spec.yaw());
				figure.setYHeadRot(spec.yaw());
				figure.setYBodyRot(spec.yaw());
				figure.setRole(spec.role());
				figure.setTint(spec.tint());
				figure.setFigurePose(spec.pose());
				figure.direct();
				level.addFreshEntity(figure);
				Actor actor = new Actor(spec, figure);
				actor.x = spec.x();
				actor.y = spec.y();
				actor.z = spec.z();
				actor.yaw = spec.yaw();
				actors.put(spec.id(), actor);
				level.sendParticles(dust(spec.tint(), 1.2f), pos.x, pos.y + 1, pos.z, 12, 0.25, 0.6, 0.25, 0.01);
			}
		}
	}

	private void runCues(int t) {
		List<Cue> cues = script.cues();
		while (cueIndex < cues.size() && cues.get(cueIndex).tick() <= t) {
			apply(cues.get(cueIndex));
			cueIndex++;
		}
	}

	private void apply(Cue cue) {
		switch (cue) {
			case Cue.Move move -> withActor(move.actor(), actor -> {
				actor.targetX = move.x();
				actor.targetZ = move.z();
				actor.targetY = 0;
				actor.speed = move.speed();
				actor.flying = false;
				actor.movingPose = move.movingPose();
				actor.arrivalPose = move.arrivalPose();
				actor.moving = true;
				actor.faceTarget = -1;
			});
			case Cue.Fly fly -> withActor(fly.actor(), actor -> {
				actor.targetX = fly.x();
				actor.targetY = fly.y();
				actor.targetZ = fly.z();
				actor.speed = fly.speed();
				actor.flying = true;
				actor.movingPose = actor.figure.getFigurePose() == Pose.BREATHE ? Pose.BREATHE : Pose.FLY;
				actor.arrivalPose = actor.movingPose;
				actor.moving = true;
			});
			case Cue.SetPose setPose -> withActor(setPose.actor(), actor -> {
				if (!actor.moving) {
					actor.figure.setFigurePose(setPose.pose());
				} else {
					actor.arrivalPose = setPose.pose();
					if (setPose.pose() == Pose.BREATHE || setPose.pose() == Pose.FLY) {
						actor.movingPose = setPose.pose();
						actor.figure.setFigurePose(setPose.pose());
					}
				}
			});
			case Cue.Face face -> withActor(face.actor(), actor -> {
				actor.faceTarget = -1;
				actor.yaw = (float) (Math.toDegrees(Math.atan2(face.z() - actor.z, face.x() - actor.x)) - 90.0);
			});
			case Cue.FaceActor faceActor -> withActor(faceActor.actor(), actor -> actor.faceTarget = faceActor.target());
			case Cue.Die die -> withActor(die.actor(), actor -> {
				actor.moving = false;
				actor.dead = true;
				actor.figure.setFigurePose(Pose.DEAD);
				actor.fadeAt = tick + 70;
				Vec3 pos = actor.figure.position();
				level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 1, pos.z, 4, 0.2, 0.4, 0.2, 0.02);
				if (actor.spec.role() == Role.DRAGON) {
					level.playSound(null, pos.x, pos.y, pos.z, ModSounds.WYRM_DEATH, SoundSource.AMBIENT, 1.5f, 1.2f);
				}
			});
			case Cue.Fade fade -> withActor(fade.actor(), actor -> {
				if (!actor.fading) {
					actor.fading = true;
					actor.figure.startFading();
					actor.removeAt = tick + 30;
				}
			});
			case Cue.Effect effect -> {
				Vec3 base = stagePos(effect.x(), effect.z()).add(0, effect.y(), 0);
				effects.add(new ActiveEffect(effect.kind(), base, effect.radius(), effect.duration()));
				if (effect.kind() == EffectKind.EXPLOSION) {
					level.playSound(null, base.x, base.y, base.z, ModSounds.REPLAY_RUMBLE, SoundSource.AMBIENT, 1.2f, 0.6f);
				}
			}
			case Cue.Sound sound -> {
				Vec3 pos = stagePos(sound.x(), sound.z());
				level.playSound(null, pos.x, pos.y + 1, pos.z, soundFor(sound.kind()), SoundSource.AMBIENT, 0.9f, 0.9f + random.nextFloat() * 0.2f);
			}
			case Cue.Subtitle subtitle -> {
				// Narration is handled in narrate(), keyed on the beat schedule.
			}
		}
	}

	private void withActor(int id, java.util.function.Consumer<Actor> action) {
		Actor actor = actors.get(id);
		if (actor != null && !actor.removed) {
			action.accept(actor);
		}
	}

	private void updateActors() {
		for (Actor actor : actors.values()) {
			if (actor.removed) {
				continue;
			}
			EchoFigureEntity figure = actor.figure;
			if (figure.isRemoved()) {
				actor.removed = true;
				continue;
			}
			figure.direct();

			if (actor.dead && !actor.fading && tick >= actor.fadeAt) {
				actor.fading = true;
				figure.startFading();
				actor.removeAt = tick + 30;
			}
			if (actor.fading && tick >= actor.removeAt) {
				Vec3 pos = figure.position();
				level.sendParticles(dust(actor.spec.tint(), 1.0f), pos.x, pos.y + 0.8, pos.z, 10, 0.3, 0.5, 0.3, 0.02);
				figure.discard();
				actor.removed = true;
				continue;
			}

			if (actor.moving) {
				double dx = actor.targetX - actor.x;
				double dy = actor.targetY - actor.y;
				double dz = actor.targetZ - actor.z;
				double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
				if (dist <= actor.speed || dist < 1.0E-3) {
					actor.x = actor.targetX;
					actor.y = actor.targetY;
					actor.z = actor.targetZ;
					actor.moving = false;
					figure.setFigurePose(actor.arrivalPose);
				} else {
					actor.x += dx / dist * actor.speed;
					actor.y += dy / dist * actor.speed;
					actor.z += dz / dist * actor.speed;
					if (Math.abs(dx) + Math.abs(dz) > 1.0E-3) {
						actor.yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
					}
					figure.setFigurePose(actor.movingPose);
				}
			}

			if (!actor.moving && actor.faceTarget >= 0) {
				Actor target = actors.get(actor.faceTarget);
				if (target != null) {
					actor.yaw = (float) (Math.toDegrees(Math.atan2(target.z - actor.z, target.x - actor.x)) - 90.0);
				}
			}

			Vec3 ground = stagePos(actor.x, actor.z);
			double y = actor.spec.role() == Role.DRAGON || actor.flying ? origin.getY() + actor.y : ground.y;
			if (actor.dead && actor.spec.role() == Role.DRAGON) {
				y = Math.max(ground.y, y - 0.08);
				actor.y = y - origin.getY();
			}
			figure.setPos(ground.x, y, ground.z);
			float smoothYaw = Mth.approachDegrees(figure.getYRot(), actor.yaw, 18.0f);
			figure.setYRot(smoothYaw);
			figure.setYHeadRot(smoothYaw);
			figure.setYBodyRot(smoothYaw);

			if (actor.spec.role() == Role.DRAGON && tick % 18 == 0 && !actor.dead) {
				level.playSound(null, ground.x, y, ground.z, ModSounds.WYRM_FLAP, SoundSource.AMBIENT, 0.8f, 1.1f);
			}
			if (figure.getFigurePose() == Pose.BREATHE && !actor.dead) {
				breathe(figure);
			}
		}
	}

	private void breathe(EchoFigureEntity dragon) {
		Vec3 look = Vec3.directionFromRotation(20.0f, dragon.getYRot());
		Vec3 mouth = dragon.position().add(look.scale(3.0)).add(0, 1.5, 0);
		for (int i = 0; i < 6; i++) {
			double s = 0.3 + random.nextDouble() * 0.5;
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, mouth.x, mouth.y, mouth.z, 0,
					look.x * s + (random.nextDouble() - 0.5) * 0.2, look.y * s - 0.2, look.z * s + (random.nextDouble() - 0.5) * 0.2, 1.0);
		}
		if (tick % 12 == 0) {
			level.playSound(null, mouth.x, mouth.y, mouth.z, ModSounds.REPLAY_FIRE, SoundSource.AMBIENT, 1.0f, 0.8f);
		}
	}

	// ------------------------------------------------------------------ effects

	private void updateEffects() {
		effects.removeIf(effect -> effect.remaining-- <= 0);
		for (ActiveEffect effect : effects) {
			Vec3 p = effect.pos;
			double r = effect.radius;
			switch (effect.kind) {
				case FIRE_BURST -> {
					level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 0.3, p.z, 4, r * 0.5, 0.3, r * 0.5, 0.02);
					if (tick % 3 == 0) {
						level.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 0.8, p.z, 2, r * 0.5, 0.4, r * 0.5, 0.01);
					}
				}
				case SMOKE -> {
					if (tick % 4 == 0) {
						level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.x, p.y, p.z, 1, r * 0.6, 0.1, r * 0.6, 0.005);
					}
				}
				case LIGHT_PILLAR -> {
					for (int i = 0; i < 4; i++) {
						level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + random.nextDouble() * 14, p.z, 1, r * 0.3, 0, r * 0.3, 0.0);
					}
					level.sendParticles(dust(event.era().tint(), 1.5f), p.x, p.y + random.nextDouble() * 10, p.z, 2, r * 0.4, 0.2, r * 0.4, 0.0);
				}
				case SPARKS -> {
					if (tick % 2 == 0) {
						level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 2, r * 0.5, 0.4, r * 0.5, 0.1);
						level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, r * 0.5, 0.3, r * 0.5, 0.2);
					}
				}
				case DUST_FALL -> level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()),
						p.x, p.y, p.z, 3, r * 0.5, 0.1, r * 0.5, 0.0);
				case EXPLOSION -> {
					if (effect.remaining == effect.duration - 1) {
						level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.x, p.y, p.z, 1, 0, 0, 0, 0);
						level.sendParticles(ParticleTypes.SCULK_SOUL, p.x, p.y, p.z, 20, r, r, r, 0.05);
					}
				}
				case BONFIRE -> {
					level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 0.4, p.z, 3, 0.35, 0.3, 0.35, 0.01);
					if (tick % 5 == 0) {
						level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.x, p.y + 1.2, p.z, 1, 0.1, 0.1, 0.1, 0.01);
					}
				}
				case ARROW_VOLLEY -> level.sendParticles(ParticleTypes.CRIT, p.x, p.y + 2, p.z, 5, r, 1.0, r, 0.4);
				case CONFETTI -> {
					int color = pastel(random.nextFloat());
					level.sendParticles(dust(color, 1.0f), p.x, p.y, p.z, 3, r, 0.5, r, 0.0);
					if (tick % 6 == 0) {
						level.sendParticles(ParticleTypes.FIREWORK, p.x, p.y + 1, p.z, 2, r * 0.5, 0.5, r * 0.5, 0.02);
					}
				}
				case RIFT -> {
					level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 8, r, r, r, 0.05);
					if (tick % 4 == 0) {
						level.sendParticles(ParticleTypes.SCULK_SOUL, p.x, p.y, p.z, 1, r * 0.5, r * 0.5, r * 0.5, 0.02);
					}
				}
			}
		}
	}

	/** A soft pastel color around the hue wheel. */
	private static int pastel(float hue) {
		float h = (hue % 1.0f) * 6.0f;
		int sector = (int) h;
		float f = h - sector;
		float lo = 0.55f;
		float up = lo + (1.0f - lo) * f;
		float down = 1.0f - (1.0f - lo) * f;
		float[] rgb = switch (sector) {
			case 0 -> new float[] {1, up, lo};
			case 1 -> new float[] {down, 1, lo};
			case 2 -> new float[] {lo, 1, up};
			case 3 -> new float[] {lo, down, 1};
			case 4 -> new float[] {up, lo, 1};
			default -> new float[] {1, lo, down};
		};
		return ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
	}

	private static ParticleOptions dust(int color, float scale) {
		return new DustParticleOptions(color, scale);
	}

	private static SoundEvent soundFor(SoundKind kind) {
		return switch (kind) {
			case HORN -> ModSounds.REPLAY_HORN;
			case CLASH -> ModSounds.REPLAY_CLASH;
			case ROAR -> ModSounds.REPLAY_ROAR;
			case CHEER -> ModSounds.REPLAY_CHEER;
			case BELL -> ModSounds.REPLAY_BELL;
			case CHANT -> ModSounds.REPLAY_CHANT;
			case RUMBLE -> ModSounds.REPLAY_RUMBLE;
			case FIRE -> ModSounds.REPLAY_FIRE;
			case FANFARE -> ModSounds.REPLAY_FANFARE;
			case MUSIC -> ModSounds.REPLAY_MUSIC;
			case WHISPER -> ModSounds.REPLAY_WHISPER;
		};
	}

	private void cleanup() {
		for (Actor actor : actors.values()) {
			if (!actor.removed && !actor.figure.isRemoved()) {
				Vec3 pos = actor.figure.position();
				level.sendParticles(dust(actor.spec.tint(), 1.0f), pos.x, pos.y + 0.8, pos.z, 8, 0.3, 0.5, 0.3, 0.02);
				actor.figure.discard();
			}
			actor.removed = true;
		}
		effects.clear();
	}

	private static final class Actor {
		final ActorSpec spec;
		final EchoFigureEntity figure;
		double x;
		double y;
		double z;
		float yaw;
		double targetX;
		double targetY;
		double targetZ;
		double speed;
		boolean moving;
		boolean flying;
		Pose movingPose = Pose.WALK;
		Pose arrivalPose = Pose.IDLE;
		int faceTarget = -1;
		boolean dead;
		boolean fading;
		boolean removed;
		int fadeAt;
		int removeAt;

		Actor(ActorSpec spec, EchoFigureEntity figure) {
			this.spec = spec;
			this.figure = figure;
		}
	}

	private static final class ActiveEffect {
		final EffectKind kind;
		final Vec3 pos;
		final double radius;
		final int duration;
		int remaining;

		ActiveEffect(EffectKind kind, Vec3 pos, double radius, int duration) {
			this.kind = kind;
			this.pos = pos;
			this.radius = radius;
			this.duration = duration;
			this.remaining = duration;
		}
	}
}
