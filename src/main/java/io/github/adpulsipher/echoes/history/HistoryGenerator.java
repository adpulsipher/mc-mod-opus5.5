package io.github.adpulsipher.echoes.history;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Deterministically derives the history of any chunk from the world seed.
 *
 * <p>Every chunk has one remembered event per {@link Era}. The same world, chunk and era always produce the same
 * event, so history is consistent for every player and across restarts without storing anything.
 */
public final class HistoryGenerator {
	private HistoryGenerator() {
	}

	/** SplitMix64 finalizer, used to decorrelate the combined seed. */
	static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
		z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
		return z ^ (z >>> 31);
	}

	public static long eventSeed(long worldSeed, int chunkX, int chunkZ, Era era) {
		long seed = mix(worldSeed ^ 0x5eed_ec40L);
		seed = mix(seed + chunkX * 0x9E3779B97F4A7C15L);
		seed = mix(seed + chunkZ * 0xC2B2AE3D27D4EB4FL);
		return mix(seed + era.ordinal() * 0x165667B19E3779F9L);
	}

	/**
	 * Settlements span several chunks, so the place name is derived from a coarser region grid.
	 */
	static String placeName(long worldSeed, int chunkX, int chunkZ) {
		long seed = mix(mix(worldSeed ^ 0x91ace) + Math.floorDiv(chunkX, 6) * 0x9E3779B97F4A7C15L);
		seed = mix(seed + Math.floorDiv(chunkZ, 6) * 0xC2B2AE3D27D4EB4FL);
		return NameGen.settlement(new SplittableRandom(seed));
	}

	public static HistoricEvent generate(long worldSeed, int chunkX, int chunkZ, Era era, Terrain terrain) {
		long seed = eventSeed(worldSeed, chunkX, chunkZ, era);
		SplittableRandom random = new SplittableRandom(seed);

		EventType type = pickType(random, era, terrain);
		int yearsAgo = era.minYearsAgo() + random.nextInt(era.maxYearsAgo() - era.minYearsAgo());
		int yearOfEra = 1 + random.nextInt(900);
		String place = placeName(worldSeed, chunkX, chunkZ);
		Faction a = NameGen.faction(random, 0);
		Faction b = NameGen.rival(random, a);

		String date = "Year " + yearOfEra + " of " + era.title().replaceFirst("^The ", "the ");
		Chronicler.Story story = Chronicler.write(random, type, era, terrain, place, a, b, date);

		return new HistoricEvent(seed, type, era, terrain, chunkX, chunkZ, yearsAgo, yearOfEra, story.title(), place, a, b,
				story.hero(), story.foe(), story.outcome(), story.narration(), story.chronicle(), story.relicHint());
	}

	/** Convenience: the event an echo formed at block height {@code y} remembers. */
	public static HistoricEvent generateAtDepth(long worldSeed, int chunkX, int chunkZ, int y, Terrain terrain) {
		return generate(worldSeed, chunkX, chunkZ, Era.fromDepth(y), terrain);
	}

	/** The full history of a chunk, oldest first. */
	public static List<HistoricEvent> chunkHistory(long worldSeed, int chunkX, int chunkZ, Terrain terrain) {
		List<HistoricEvent> events = new ArrayList<>();
		Era[] eras = Era.values();
		for (int i = eras.length - 1; i >= 0; i--) {
			events.add(generate(worldSeed, chunkX, chunkZ, eras[i], terrain));
		}
		return events;
	}

	static EventType pickType(SplittableRandom random, Era era, Terrain terrain) {
		EventType[] types = EventType.values();
		int total = 0;
		for (EventType type : types) {
			total += type.weight(era, terrain);
		}
		int roll = random.nextInt(total);
		for (EventType type : types) {
			roll -= type.weight(era, terrain);
			if (roll < 0) {
				return type;
			}
		}
		return EventType.BATTLE;
	}
}
