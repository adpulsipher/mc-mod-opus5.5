package io.github.adpulsipher.echoes.history;

import java.util.List;

/**
 * A single remembered event in the history of a chunk.
 *
 * @param seed deterministic seed of this event, used to stage the replay and pick relics
 * @param type what happened
 * @param era when it happened
 * @param terrain the land it happened on
 * @param chunkX chunk the memory belongs to
 * @param chunkZ chunk the memory belongs to
 * @param yearsAgo how long ago it happened
 * @param yearOfEra the year within its era, as the chroniclers of the time counted it
 * @param title a short title, e.g. "The Siege of Ashvale"
 * @param place the settlement or landmark where it happened
 * @param factionA the first party (usually the defenders or hosts)
 * @param factionB the second party (usually the attackers or guests)
 * @param hero the leading figure of the first party
 * @param foe the leading figure of the second party, or the beast
 * @param outcome how it ended
 * @param narration short lines spoken during the replay, one per act
 * @param chronicle long-form account used for the transcript book, one paragraph per entry
 * @param relicHint a sentence describing what was left behind in the ground
 */
public record HistoricEvent(
		long seed,
		EventType type,
		Era era,
		Terrain terrain,
		int chunkX,
		int chunkZ,
		int yearsAgo,
		int yearOfEra,
		String title,
		String place,
		Faction factionA,
		Faction factionB,
		String hero,
		String foe,
		Outcome outcome,
		List<String> narration,
		List<String> chronicle,
		String relicHint
) {
	public enum Outcome {
		VICTORY,
		DEFEAT,
		STALEMATE,
		SURVIVED,
		TRIUMPH,
		CATASTROPHE
	}

	/** e.g. "Year 412 of the Age of Dragons" */
	public String dateLine() {
		return "Year " + yearOfEra + " of " + era.title().replaceFirst("^The ", "the ");
	}

	/** Whether replaying this memory can tear open a rift and let something through. */
	public boolean unstable() {
		return type.violent() && outcome != Outcome.STALEMATE;
	}

	/** Deterministic offset in [0,16) of where relics of this event lie buried in the chunk. */
	public int relicOffsetX() {
		return (int) Math.floorMod(seed >>> 7, 12L) + 2;
	}

	public int relicOffsetZ() {
		return (int) Math.floorMod(seed >>> 19, 12L) + 2;
	}
}
