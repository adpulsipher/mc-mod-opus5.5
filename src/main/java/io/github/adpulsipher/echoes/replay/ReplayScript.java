package io.github.adpulsipher.echoes.replay;

import java.util.Comparator;
import java.util.List;

/**
 * A fully staged replay: who appears, and everything that happens, tick by tick.
 */
public record ReplayScript(List<ActorSpec> actors, List<Cue> cues, int duration) {
	public ReplayScript {
		actors = List.copyOf(actors);
		cues = cues.stream().sorted(Comparator.comparingInt(Cue::tick)).toList();
	}
}
