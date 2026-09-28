package io.github.adpulsipher.echoes.replay;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.history.HistoricEvent.Outcome;

/**
 * Stages a {@link HistoricEvent} as a replay: places the ghostly actors on a stage around the projector and
 * scripts their movements, effects and narration.
 *
 * <p>Coordinates are in blocks relative to the projector. The stage is roughly a disc of radius 8.
 */
public final class Choreographer {
	/** Ticks at which each of the five narration beats is spoken. */
	public static final int[] BEATS = {10, 130, 270, 420, 570};
	public static final int DURATION = 720;

	private static final double WALK = 0.07;
	private static final double RUN = 0.17;
	private static final int NEUTRAL = 0xbfefff;

	private final SplittableRandom random;
	private final List<ActorSpec> actors = new ArrayList<>();
	private final List<Cue> cues = new ArrayList<>();

	private Choreographer(long seed) {
		this.random = new SplittableRandom(seed ^ 0x57A6EL);
	}

	public static ReplayScript stage(HistoricEvent event) {
		Choreographer c = new Choreographer(event.seed());
		for (int i = 0; i < BEATS.length; i++) {
			c.cues.add(new Cue.Subtitle(BEATS[i], i));
		}
		switch (event.type()) {
			case BATTLE -> c.battle(event);
			case SIEGE -> c.siege(event);
			case DRAGON_ATTACK -> c.dragonAttack(event);
			case MARKET_DAY -> c.market(event);
			case CORONATION -> c.coronation(event);
			case RITUAL -> c.ritual(event);
			case DUEL -> c.duel(event);
			case FESTIVAL -> c.festival(event);
			case EXODUS -> c.exodus(event);
			case CAVE_IN -> c.caveIn(event);
		}
		return new ReplayScript(c.actors, c.cues, DURATION);
	}

	// ---------------------------------------------------------------- helpers

	private double jitter(double amount) {
		return (random.nextDouble() * 2 - 1) * amount;
	}

	private int between(int min, int max) {
		return min + random.nextInt(Math.max(1, max - min + 1));
	}

	private static float yawTowards(double fromX, double fromZ, double toX, double toZ) {
		return (float) (Math.toDegrees(Math.atan2(toZ - fromZ, toX - fromX)) - 90.0);
	}

	private int actor(Role role, int tint, double x, double z, float yaw, Pose pose, int appear) {
		int id = actors.size();
		actors.add(new ActorSpec(id, role, tint, x, 0, z, yaw, pose, appear));
		return id;
	}

	private int flyer(Role role, int tint, double x, double y, double z, float yaw, int appear) {
		int id = actors.size();
		actors.add(new ActorSpec(id, role, tint, x, y, z, yaw, Pose.FLY, appear));
		return id;
	}

	private void move(int tick, int id, double x, double z, double speed, Pose arrival) {
		cues.add(new Cue.Move(tick, id, x, z, speed, speed > 0.12 ? Pose.RUN : Pose.WALK, arrival));
	}

	private void carry(int tick, int id, double x, double z, double speed) {
		cues.add(new Cue.Move(tick, id, x, z, speed, Pose.CARRY, Pose.CARRY));
	}

	private void pose(int tick, int id, Pose pose) {
		cues.add(new Cue.SetPose(tick, id, pose));
	}

	private void face(int tick, int id, double x, double z) {
		cues.add(new Cue.Face(tick, id, x, z));
	}

	private void faceActor(int tick, int id, int target) {
		cues.add(new Cue.FaceActor(tick, id, target));
	}

	private void die(int tick, int id) {
		cues.add(new Cue.Die(tick, id));
	}

	private void fade(int tick, int id) {
		cues.add(new Cue.Fade(tick, id));
	}

	private void fly(int tick, int id, double x, double y, double z, double speed) {
		cues.add(new Cue.Fly(tick, id, x, y, z, speed));
	}

	private void effect(int tick, EffectKind kind, double x, double y, double z, double radius, int duration) {
		cues.add(new Cue.Effect(tick, kind, x, y, z, radius, duration));
	}

	private void sound(int tick, SoundKind kind, double x, double z) {
		cues.add(new Cue.Sound(tick, kind, x, z));
	}

	/** Everyone left on stage dissolves at the end. */
	private void fadeAll(int from) {
		for (ActorSpec actor : actors) {
			fade(from + random.nextInt(40), actor.id());
		}
	}

	private Role soldierRole() {
		int roll = random.nextInt(10);
		return roll < 6 ? Role.SOLDIER : roll < 8 ? Role.KNIGHT : Role.ARCHER;
	}

	private Role commonerRole() {
		int roll = random.nextInt(10);
		return roll < 6 ? Role.PEASANT : roll < 8 ? Role.CHILD : Role.MERCHANT;
	}

	// ---------------------------------------------------------------- events

	private void battle(HistoricEvent event) {
		int tintA = event.factionA().tint();
		int tintB = event.factionB().tint();
		boolean aWins = event.outcome() != Outcome.DEFEAT;
		int perSide = between(5, 7);

		List<Integer> sideA = new ArrayList<>();
		List<Integer> sideB = new ArrayList<>();
		int leaderA = actor(Role.KNIGHT, tintA, -7, 0, -90, Pose.IDLE, 0);
		int leaderB = actor(Role.KNIGHT, tintB, 7, 0, 90, Pose.IDLE, 0);
		sideA.add(leaderA);
		sideB.add(leaderB);
		for (int i = 0; i < perSide; i++) {
			double z = (i - perSide / 2.0) * 1.6 + jitter(0.3);
			sideA.add(actor(soldierRole(), tintA, -6 + jitter(0.6), z, -90, Pose.IDLE, between(0, 30)));
			sideB.add(actor(soldierRole(), tintB, 6 + jitter(0.6), z, 90, Pose.IDLE, between(0, 30)));
		}

		// Beat 1: the armies face off. Archers loose volleys.
		sound(120, SoundKind.HORN, -7, 0);
		sound(150, SoundKind.HORN, 7, 0);
		for (int id : sideA) {
			if (actors.get(id).role() == Role.ARCHER) {
				pose(140, id, Pose.SHOOT);
			}
		}
		for (int id : sideB) {
			if (actors.get(id).role() == Role.ARCHER) {
				pose(150, id, Pose.SHOOT);
			}
		}
		effect(170, EffectKind.ARROW_VOLLEY, 0, 4, 0, 5, 40);

		// Beat 2: the charge. Lines meet in the middle and fight.
		sound(265, SoundKind.HORN, -7, 0);
		for (int i = 0; i < sideA.size(); i++) {
			int a = sideA.get(i);
			int b = sideB.get(Math.min(i, sideB.size() - 1));
			ActorSpec as = actors.get(a);
			ActorSpec bs = actors.get(b);
			double meetZ = (as.z() + bs.z()) / 2 + jitter(0.4);
			if (as.role() != Role.ARCHER) {
				move(250 + between(0, 20), a, -0.8 + jitter(0.2), meetZ, RUN, Pose.ATTACK);
				faceActor(300, a, b);
			}
			if (bs.role() != Role.ARCHER) {
				move(250 + between(0, 20), b, 0.8 + jitter(0.2), meetZ, RUN, Pose.ATTACK);
				faceActor(300, b, a);
			}
		}
		for (int t = 300; t < 540; t += 35) {
			sound(t + between(0, 10), SoundKind.CLASH, jitter(2), jitter(4));
		}
		effect(330, EffectKind.SPARKS, 0, 1, 0, 4, 200);

		// Beat 3/4: the losing side falls, one by one.
		List<Integer> losers = aWins ? sideB : sideA;
		List<Integer> winners = aWins ? sideA : sideB;
		int t = 360;
		for (int id : losers) {
			if (random.nextInt(4) != 0 || id == (aWins ? leaderB : leaderA)) {
				die(t, id);
				t += between(18, 34);
			} else {
				move(t, id, aWins ? 10 : -10, jitter(6), RUN, Pose.RUN);
				fade(t + 50, id);
			}
		}
		for (int id : winners) {
			if (random.nextInt(5) == 0) {
				die(between(380, 520), id);
			}
		}

		// Beat 5: the victors cheer.
		for (int id : winners) {
			pose(575 + between(0, 15), id, Pose.CHEER);
		}
		sound(580, SoundKind.CHEER, aWins ? -2 : 2, 0);
		effect(560, EffectKind.SMOKE, 0, 0.5, 0, 5, 120);
		fadeAll(660);
	}

	private void siege(HistoricEvent event) {
		int defenders = event.factionA().tint();
		int attackers = event.factionB().tint();
		boolean held = event.outcome() == Outcome.VICTORY;

		// The wall is implied by a line of defenders along x = -3; the gate at z = 0.
		List<Integer> walls = new ArrayList<>();
		int lord = actor(Role.NOBLE, defenders, -6, 0, -90, Pose.IDLE, 0);
		for (int i = 0; i < 5; i++) {
			double z = -4 + i * 2;
			if (Math.abs(z) < 0.5) {
				continue;
			}
			walls.add(actor(i % 2 == 0 ? Role.ARCHER : Role.SOLDIER, defenders, -3, z, -90, Pose.IDLE, between(0, 20)));
		}
		List<Integer> host = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			host.add(actor(soldierRole(), attackers, 6 + jitter(1.5), jitter(5), 90, Pose.IDLE, between(0, 40)));
		}
		int ramA = actor(Role.SOLDIER, attackers, 7, -0.6, 90, Pose.CARRY, 20);
		int ramB = actor(Role.SOLDIER, attackers, 8, 0.6, 90, Pose.CARRY, 20);
		int captain = actor(Role.KNIGHT, attackers, 8.5, 0, 90, Pose.IDLE, 0);

		effect(20, EffectKind.SMOKE, 7, 0.5, 0, 4, 200);
		for (int id : walls) {
			if (actors.get(id).role() == Role.ARCHER) {
				pose(150, id, Pose.SHOOT);
			}
		}
		effect(160, EffectKind.ARROW_VOLLEY, 3, 3, 0, 4, 80);

		// Beat 3: the ram advances on the gate.
		carry(270, ramA, -2.2, -0.6, WALK);
		carry(270, ramB, -1.2, 0.6, WALK);
		for (int t = 360; t < 420; t += 14) {
			sound(t, SoundKind.RUMBLE, -2.5, 0);
			effect(t, EffectKind.DUST_FALL, -3, 2, 0, 1, 10);
		}
		for (int id : host) {
			move(280 + between(0, 40), id, 1 + jitter(1.5), jitter(3.5), WALK, Pose.ATTACK);
		}

		if (held) {
			// Beat 4: the sally.
			move(420, lord, 1, 0, RUN, Pose.ATTACK);
			for (int id : walls) {
				move(425 + between(0, 10), id, 0.5 + jitter(1), jitter(3), RUN, Pose.ATTACK);
			}
			die(460, ramA);
			die(470, ramB);
			int t = 480;
			for (int id : host) {
				if (random.nextBoolean()) {
					die(t, id);
				} else {
					move(t, id, 11, jitter(6), RUN, Pose.RUN);
					fade(t + 60, id);
				}
				t += 12;
			}
			move(500, captain, 12, 2, RUN, Pose.RUN);
			fade(560, captain);
			pose(590, lord, Pose.CHEER);
			sound(590, SoundKind.CHEER, 0, 0);
		} else {
			// Beat 4: the gate breaks.
			effect(420, EffectKind.EXPLOSION, -3, 1, 0, 1.5, 5);
			sound(420, SoundKind.RUMBLE, -3, 0);
			move(430, captain, -5, 0, RUN, Pose.ATTACK);
			for (int id : host) {
				move(430 + between(0, 20), id, -4 + jitter(2), jitter(3), RUN, Pose.ATTACK);
			}
			int t = 450;
			for (int id : walls) {
				die(t, id);
				t += 16;
			}
			pose(520, lord, Pose.KNEEL);
			effect(560, EffectKind.FIRE_BURST, -5, 0.5, jitter(2), 3, 140);
			sound(560, SoundKind.FIRE, -5, 0);
			pose(590, captain, Pose.CHEER);
		}
		fadeAll(660);
	}

	private void dragonAttack(HistoricEvent event) {
		int villagers = event.factionA().tint();
		Outcome outcome = event.outcome();

		List<Integer> folk = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double r = 2 + random.nextDouble() * 4;
			int id = actor(commonerRole(), villagers, Math.cos(angle) * r, Math.sin(angle) * r, random.nextFloat() * 360, Pose.IDLE, between(0, 20));
			folk.add(id);
			move(30 + between(0, 60), id, Math.cos(angle + 1) * r, Math.sin(angle + 1) * r, WALK, Pose.IDLE);
		}
		int hero = actor(Role.KNIGHT, villagers, -1, 0, 0, Pose.IDLE, 0);
		List<Integer> archers = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			archers.add(actor(Role.ARCHER, villagers, -5, -3 + i * 3, -90, Pose.IDLE, between(0, 20)));
		}
		int dragon = flyer(Role.DRAGON, 0xd8a6ff, 18, 14, -10, 45, 110);

		// Beat 2: the shadow arrives.
		sound(130, SoundKind.BELL, 0, 0);
		sound(150, SoundKind.ROAR, 10, -6);
		fly(115, dragon, 6, 9, 4, 0.35);
		fly(190, dragon, -6, 8, 2, 0.35);
		for (int id : folk) {
			pose(160 + between(0, 20), id, Pose.COWER);
		}

		// Beat 3: fire; the people run.
		fly(250, dragon, 2, 6, -2, 0.3);
		cues.add(new Cue.SetPose(280, dragon, Pose.BREATHE));
		effect(285, EffectKind.FIRE_BURST, 1, 0.3, 1, 3, 70);
		sound(285, SoundKind.FIRE, 1, 1);
		sound(290, SoundKind.ROAR, 2, -2);
		cues.add(new Cue.SetPose(360, dragon, Pose.FLY));
		for (int id : folk) {
			move(290 + between(0, 30), id, -9 + jitter(2), jitter(5), RUN, Pose.COWER);
		}
		for (int i = 0; i < folk.size(); i++) {
			if (i % 3 == 0) {
				die(300 + i * 5, folk.get(i));
			}
		}

		// Beat 4: the stand.
		fly(360, dragon, 5, 7, 5, 0.3);
		move(420, hero, -2, 0, RUN, Pose.SHOOT);
		faceActor(430, hero, dragon);
		for (int id : archers) {
			pose(430 + between(0, 10), id, Pose.SHOOT);
			faceActor(430, id, dragon);
		}
		effect(440, EffectKind.ARROW_VOLLEY, 3, 5, 3, 3, 60);
		fly(440, dragon, 0, 5, 0, 0.25);
		cues.add(new Cue.SetPose(470, dragon, Pose.BREATHE));
		effect(475, EffectKind.FIRE_BURST, -4, 0.3, 0, 3, 60);
		die(490, archers.get(0));
		cues.add(new Cue.SetPose(530, dragon, Pose.FLY));

		// Beat 5: the outcome.
		switch (outcome) {
			case TRIUMPH -> {
				sound(570, SoundKind.ROAR, 0, 0);
				fly(575, dragon, 3, 0.5, 3, 0.2);
				die(600, dragon);
				effect(600, EffectKind.EXPLOSION, 3, 1, 3, 2, 5);
				pose(610, hero, Pose.CHEER);
				for (int id : archers) {
					pose(615, id, Pose.CHEER);
				}
				sound(620, SoundKind.CHEER, -3, 0);
			}
			case CATASTROPHE -> {
				effect(560, EffectKind.FIRE_BURST, 0, 0.3, 0, 6, 120);
				sound(560, SoundKind.FIRE, 0, 0);
				die(580, hero);
				for (int id : archers) {
					die(590 + between(0, 20), id);
				}
				fly(600, dragon, 0, 3, 0, 0.2);
				cues.add(new Cue.SetPose(640, dragon, Pose.IDLE));
			}
			default -> {
				sound(570, SoundKind.ROAR, 4, 4);
				fly(575, dragon, 20, 18, 20, 0.45);
				fade(640, dragon);
				pose(600, hero, Pose.CHEER);
			}
		}
		effect(580, EffectKind.SMOKE, 0, 0.5, 0, 6, 120);
		fadeAll(670);
	}

	private void market(HistoricEvent event) {
		int locals = event.factionA().tint();
		int traders = event.factionB().tint();

		// Stalls in a ring; merchants stand behind them.
		List<Integer> merchants = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			double angle = i * Math.PI / 2 + Math.PI / 4;
			double x = Math.cos(angle) * 4.5;
			double z = Math.sin(angle) * 4.5;
			merchants.add(actor(Role.MERCHANT, locals, x, z, yawTowards(x, z, 0, 0), Pose.TRADE, 0));
		}
		List<Integer> crowd = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			crowd.add(actor(commonerRole(), locals, jitter(3), jitter(3), random.nextFloat() * 360, Pose.IDLE, between(0, 40)));
		}
		// Beat 2: the caravan arrives from the east.
		List<Integer> caravan = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			int id = actor(i == 0 ? Role.MERCHANT : Role.PEASANT, traders, 10 + i * 1.5, 1 - i * 0.3, 90, Pose.CARRY, 120);
			caravan.add(id);
			carry(135 + i * 10, id, 2.5 + i * 0.8, 0.5 - i * 1.2, WALK);
			pose(260, id, Pose.TRADE);
		}
		sound(140, SoundKind.BELL, 8, 0);

		// The crowd wanders from stall to stall.
		for (int id : crowd) {
			for (int t = 60 + between(0, 40); t < 640; t += between(90, 140)) {
				int stall = merchants.get(random.nextInt(merchants.size()));
				ActorSpec s = actors.get(stall);
				double x = s.x() * 0.62 + jitter(0.4);
				double z = s.z() * 0.62 + jitter(0.4);
				move(t, id, x, z, WALK, Pose.TRADE);
				face(t + 60, id, s.x(), s.z());
			}
		}
		// Beat 4: an entertainer and running children.
		int juggler = actor(Role.PEASANT, locals, 0, 0, 0, Pose.DANCE, 400);
		effect(420, EffectKind.CONFETTI, 0, 2, 0, 1.5, 100);
		sound(420, SoundKind.MUSIC, 0, 0);
		for (int id : crowd) {
			if (actors.get(id).role() == Role.CHILD) {
				move(430, id, 1.5, 1.5, RUN, Pose.DANCE);
				faceActor(470, id, juggler);
			}
		}
		for (int t = 300; t < 640; t += 60) {
			sound(t, SoundKind.WHISPER, jitter(4), jitter(4));
		}
		sound(590, SoundKind.CHEER, 0, 0);
		fadeAll(660);
	}

	private void coronation(HistoricEvent event) {
		int court = event.factionA().tint();
		boolean betrayal = event.outcome() == Outcome.CATASTROPHE;

		// Two rows of kneeling lords along the aisle (the z axis), the throne at z = -6.
		List<Integer> lords = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			double z = 4 - i * 2.2;
			lords.add(actor(Role.NOBLE, court, -1.8, z, -90, Pose.IDLE, between(0, 30)));
			lords.add(actor(i % 2 == 0 ? Role.KNIGHT : Role.NOBLE, court, 1.8, z, 90, Pose.IDLE, between(0, 30)));
		}
		int priest = actor(Role.MAGE, court, 0, -6.5, 0, Pose.IDLE, 0);
		int ruler = actor(Role.NOBLE, court, 0, 8, 180, Pose.IDLE, 60);
		for (int id : lords) {
			pose(140 + between(0, 30), id, Pose.KNEEL);
		}
		sound(140, SoundKind.FANFARE, 0, 6);
		// Beat 3: the procession.
		move(270, ruler, 0, -5, 0.05, Pose.KNEEL);
		face(270, ruler, 0, -6.5);
		sound(280, SoundKind.CHANT, 0, 0);
		// Beat 4: the crowning.
		pose(420, priest, Pose.CAST);
		effect(440, EffectKind.LIGHT_PILLAR, 0, 0, -5, 0.6, 80);
		sound(440, SoundKind.BELL, 0, -5);
		if (!betrayal) {
			pose(520, ruler, Pose.CHEER);
			face(520, ruler, 0, 5);
			for (int id : lords) {
				pose(575 + between(0, 20), id, Pose.CHEER);
			}
			sound(575, SoundKind.CHEER, 0, 0);
			effect(580, EffectKind.CONFETTI, 0, 3, 0, 4, 100);
			sound(585, SoundKind.FANFARE, 0, 0);
		} else {
			int traitor = lords.get(random.nextInt(lords.size()));
			move(540, traitor, 0.6, -4.3, RUN, Pose.ATTACK);
			faceActor(560, traitor, ruler);
			die(585, ruler);
			for (int id : lords) {
				if (id != traitor) {
					pose(590 + between(0, 15), id, Pose.COWER);
				}
			}
			sound(586, SoundKind.CLASH, 0, -5);
			sound(600, SoundKind.WHISPER, 0, 0);
		}
		fadeAll(665);
	}

	private void ritual(HistoricEvent event) {
		int circle = event.factionA().tint();
		boolean success = event.outcome() == Outcome.TRIUMPH;

		List<Integer> mages = new ArrayList<>();
		int count = 6;
		for (int i = 0; i < count; i++) {
			double angle = i * Math.PI * 2 / count;
			double x = Math.cos(angle) * 4;
			double z = Math.sin(angle) * 4;
			mages.add(actor(i == 0 ? Role.NOBLE : Role.MAGE, circle, x * 1.8, z * 1.8, yawTowards(x, z, 0, 0), Pose.IDLE, between(0, 40)));
			move(40 + i * 8, mages.get(i), x, z, WALK, Pose.IDLE);
			face(120, mages.get(i), 0, 0);
		}
		// Beat 2: the chant.
		for (int id : mages) {
			pose(135 + between(0, 10), id, Pose.CAST);
		}
		for (int t = 140; t < 560; t += 70) {
			sound(t, SoundKind.CHANT, 0, 0);
		}
		// Beat 3: light.
		effect(270, EffectKind.LIGHT_PILLAR, 0, 0, 0, 0.8, 280);
		sound(275, SoundKind.RUMBLE, 0, 0);
		// Beat 4: something answers.
		effect(420, EffectKind.RIFT, 0, 3, 0, 1.5, 150);
		sound(425, SoundKind.WHISPER, 0, 0);
		sound(440, SoundKind.ROAR, 0, 0);
		if (success) {
			effect(560, EffectKind.EXPLOSION, 0, 2, 0, 1, 5);
			for (int id : mages) {
				pose(575, id, Pose.KNEEL);
			}
			pose(600, mages.get(0), Pose.CHEER);
		} else {
			effect(560, EffectKind.EXPLOSION, 0, 1, 0, 3, 5);
			sound(560, SoundKind.RUMBLE, 0, 0);
			int t = 565;
			for (int id : mages) {
				die(t, id);
				t += 6;
			}
			effect(570, EffectKind.RIFT, 0, 2, 0, 3, 90);
		}
		fadeAll(670);
	}

	private void duel(HistoricEvent event) {
		int tintA = event.factionA().tint();
		int tintB = event.factionB().tint();
		boolean aWins = event.outcome() == Outcome.VICTORY;

		List<Integer> crowd = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			double angle = i * Math.PI * 2 / 10 + jitter(0.1);
			double x = Math.cos(angle) * 6.2;
			double z = Math.sin(angle) * 6.2;
			crowd.add(actor(i % 2 == 0 ? commonerRole() : Role.SOLDIER, i < 5 ? tintA : tintB, x, z, yawTowards(x, z, 0, 0), Pose.IDLE, between(0, 40)));
		}
		int a = actor(Role.KNIGHT, tintA, -3.5, 0, -90, Pose.IDLE, 20);
		int b = actor(Role.KNIGHT, tintB, 3.5, 0, 90, Pose.IDLE, 20);
		pose(130, a, Pose.BOW);
		pose(140, b, Pose.BOW);
		sound(135, SoundKind.HORN, 0, 0);
		// Beat 3: they circle and clash.
		move(270, a, -1, -0.8, RUN, Pose.ATTACK);
		move(270, b, 1, 0.8, RUN, Pose.ATTACK);
		faceActor(275, a, b);
		faceActor(275, b, a);
		for (int t = 300; t < 420; t += 22) {
			sound(t, SoundKind.CLASH, 0, 0);
		}
		move(350, a, -0.8, 1, WALK, Pose.ATTACK);
		move(350, b, 1, -0.6, WALK, Pose.ATTACK);
		effect(320, EffectKind.SPARKS, 0, 1.2, 0, 0.8, 120);
		// Beat 4: one stumbles.
		int loser = aWins ? b : a;
		int winner = aWins ? a : b;
		pose(420, loser, Pose.KNEEL);
		for (int id : crowd) {
			pose(425, id, Pose.IDLE);
		}
		move(470, winner, aWins ? 0.2 : -0.2, 0, WALK, Pose.ATTACK);
		sound(490, SoundKind.CLASH, 0, 0);
		die(495, loser);
		// Beat 5: the victor stands alone.
		pose(575, winner, Pose.CHEER);
		for (int id : crowd) {
			boolean sameSide = actors.get(id).tint() == actors.get(winner).tint();
			pose(580 + between(0, 10), id, sameSide ? Pose.CHEER : Pose.COWER);
		}
		sound(580, SoundKind.CHEER, 0, 0);
		fadeAll(665);
	}

	private void festival(HistoricEvent event) {
		int folk = event.factionA().tint();
		effect(130, EffectKind.BONFIRE, 0, 0, 0, 1, 560);
		sound(130, SoundKind.FIRE, 0, 0);
		int fiddler = actor(Role.PEASANT, folk, 0, -6, 0, Pose.IDLE, 0);
		pose(270, fiddler, Pose.DANCE);
		for (int t = 270; t < 640; t += 60) {
			sound(t, SoundKind.MUSIC, 0, -6);
		}
		List<Integer> dancers = new ArrayList<>();
		int count = 9;
		for (int i = 0; i < count; i++) {
			double angle = i * Math.PI * 2 / count;
			dancers.add(actor(commonerRole(), i % 3 == 0 ? NEUTRAL : folk, Math.cos(angle) * 8, Math.sin(angle) * 8, 0, Pose.IDLE, between(0, 50)));
			move(140 + between(0, 30), dancers.get(i), Math.cos(angle) * 3.2, Math.sin(angle) * 3.2, WALK, Pose.IDLE);
		}
		// The ring dance: dancers step around the fire in a circle.
		for (int step = 0; step < 12; step++) {
			int t = 280 + step * 26;
			for (int i = 0; i < count; i++) {
				double angle = i * Math.PI * 2 / count + (step + 1) * Math.PI * 2 / 12;
				move(t, dancers.get(i), Math.cos(angle) * 3.2, Math.sin(angle) * 3.2, 0.1, Pose.DANCE);
			}
		}
		effect(430, EffectKind.CONFETTI, 0, 4, 0, 4, 160);
		for (int id : dancers) {
			pose(600, id, Pose.CHEER);
		}
		sound(600, SoundKind.CHEER, 0, 0);
		fadeAll(670);
	}

	private void exodus(HistoricEvent event) {
		int people = event.factionA().tint();
		boolean lost = event.outcome() == Outcome.CATASTROPHE;
		int leader = actor(Role.NOBLE, people, 9, 0, 90, Pose.IDLE, 0);
		move(130, leader, -10, 0.5, 0.045, Pose.IDLE);
		List<Integer> column = new ArrayList<>();
		for (int i = 0; i < 11; i++) {
			Role role = i % 4 == 0 ? Role.SOLDIER : commonerRole();
			int id = actor(role, people, 10 + i * 1.3, jitter(1.2), 90, i % 2 == 0 ? Pose.CARRY : Pose.IDLE, 20 + i * 12);
			column.add(id);
			double speed = 0.04 + random.nextDouble() * 0.01;
			if (i % 2 == 0) {
				carry(140 + i * 10, id, -10, jitter(1.5), speed);
			} else {
				move(140 + i * 10, id, -10, jitter(1.5), speed, Pose.IDLE);
			}
		}
		// Beat 4: stragglers fall behind.
		int fallen = lost ? 5 : 2;
		for (int i = 0; i < fallen; i++) {
			int id = column.get(column.size() - 1 - i * 2);
			die(430 + i * 35, id);
		}
		effect(20, EffectKind.SMOKE, 9, 0.5, 0, 3, 200);
		sound(140, SoundKind.BELL, 8, 0);
		for (int t = 200; t < 640; t += 90) {
			sound(t, SoundKind.WHISPER, jitter(5), jitter(2));
		}
		for (int id : column) {
			fade(620 + between(0, 50), id);
		}
		fade(640, leader);
	}

	private void caveIn(HistoricEvent event) {
		int miners = event.factionA().tint();
		boolean survived = event.outcome() == Outcome.SURVIVED;
		List<Integer> crew = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			double x = -5 + i * 2 + jitter(0.4);
			double z = (i % 2 == 0 ? -1.2 : 1.2) + jitter(0.3);
			int id = actor(Role.MINER, miners, x, z, i % 2 == 0 ? 180 : 0, Pose.MINE, between(0, 30));
			crew.add(id);
		}
		int foreman = actor(Role.MINER, miners, 6.5, 0, 90, Pose.IDLE, 0);
		for (int t = 40; t < 400; t += 24) {
			effect(t, EffectKind.SPARKS, jitter(5), 1, jitter(1.5), 0.3, 6);
		}
		// Beat 3: the rock groans.
		sound(270, SoundKind.RUMBLE, 0, 0);
		for (int t = 280; t < 420; t += 20) {
			effect(t, EffectKind.DUST_FALL, jitter(5), 3, jitter(2), 1.5, 15);
		}
		for (int id : crew) {
			pose(300 + between(0, 20), id, Pose.IDLE);
			face(310, id, 0, 0);
		}
		// Beat 4: collapse.
		pose(420, foreman, Pose.CHEER);
		sound(425, SoundKind.RUMBLE, -2, 0);
		effect(430, EffectKind.DUST_FALL, -3, 3, 0, 4, 100);
		effect(440, EffectKind.EXPLOSION, -4, 1, 0, 1, 3);
		for (int i = 0; i < crew.size(); i++) {
			int id = crew.get(i);
			boolean escapes = survived ? i >= 1 : i >= 4;
			if (escapes) {
				move(430 + between(0, 15), id, 9, jitter(1.5), RUN, Pose.RUN);
				fade(520 + between(0, 30), id);
			} else {
				move(430, id, actors.get(id).x() + 1.5, actors.get(id).z(), RUN, Pose.COWER);
				die(470 + i * 8, id);
			}
		}
		move(470, foreman, 9, 0, RUN, Pose.KNEEL);
		pose(580, foreman, Pose.KNEEL);
		sound(600, SoundKind.WHISPER, 0, 0);
		fadeAll(660);
	}
}
