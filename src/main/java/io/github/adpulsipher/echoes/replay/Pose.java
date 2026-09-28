package io.github.adpulsipher.echoes.replay;

/**
 * What a ghostly figure is doing. Poses are purely visual and drive the figure's animation.
 */
public enum Pose {
	IDLE,
	WALK,
	RUN,
	ATTACK,
	SHOOT,
	CAST,
	KNEEL,
	CHEER,
	DEAD,
	COWER,
	DANCE,
	TRADE,
	MINE,
	BOW,
	CARRY,
	FLY,
	BREATHE;

	private static final Pose[] VALUES = values();

	public static Pose byId(int id) {
		return VALUES[Math.floorMod(id, VALUES.length)];
	}

	public boolean moving() {
		return this == WALK || this == RUN || this == FLY || this == CARRY;
	}
}
