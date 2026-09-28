package io.github.adpulsipher.echoes.replay;

/**
 * An instruction in a replay script, fired at a given tick.
 */
public sealed interface Cue permits Cue.Move, Cue.SetPose, Cue.Face, Cue.FaceActor, Cue.Die, Cue.Fade, Cue.Fly, Cue.Effect, Cue.Sound, Cue.Subtitle {
	int tick();

	/** Moves an actor along the ground to (x, z) at {@code speed} blocks/tick, then takes {@code arrivalPose}. */
	record Move(int tick, int actor, double x, double z, double speed, Pose movingPose, Pose arrivalPose) implements Cue {
	}

	record SetPose(int tick, int actor, Pose pose) implements Cue {
	}

	/** Turns an actor to face a point on the stage. */
	record Face(int tick, int actor, double x, double z) implements Cue {
	}

	/** Turns an actor to keep facing another actor. */
	record FaceActor(int tick, int actor, int target) implements Cue {
	}

	/** The actor falls and slowly fades. */
	record Die(int tick, int actor) implements Cue {
	}

	/** The actor dissolves into motes of light. */
	record Fade(int tick, int actor) implements Cue {
	}

	/** Moves a flying actor through the air to (x, y, z) relative to the stage floor. */
	record Fly(int tick, int actor, double x, double y, double z, double speed) implements Cue {
	}

	record Effect(int tick, EffectKind kind, double x, double y, double z, double radius, int duration) implements Cue {
	}

	record Sound(int tick, SoundKind kind, double x, double z) implements Cue {
	}

	/** A narration line. {@code index} refers to the event's narration beats. */
	record Subtitle(int tick, int index) implements Cue {
	}
}
