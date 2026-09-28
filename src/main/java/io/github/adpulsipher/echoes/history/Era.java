package io.github.adpulsipher.echoes.history;

/**
 * Ages of the world's forgotten past. The deeper an echo forms, the older the memory it holds.
 */
public enum Era {
	HEARTHS("The Age of Hearths", 150, 400, 48, 0x9fe8ff),
	CROWNS("The Age of Crowns", 400, 900, 16, 0xa9c8ff),
	IRON_AND_ASH("The Age of Iron and Ash", 900, 1600, -16, 0xc2b2ff),
	DRAGONS("The Age of Dragons", 1600, 3000, -48, 0xe0a8ff),
	ELDER_DAWN("The Elder Dawn", 3000, 6000, Integer.MIN_VALUE, 0xffb8e8);

	private final String title;
	private final int minYearsAgo;
	private final int maxYearsAgo;
	private final int minY;
	private final int tint;

	Era(String title, int minYearsAgo, int maxYearsAgo, int minY, int tint) {
		this.title = title;
		this.minYearsAgo = minYearsAgo;
		this.maxYearsAgo = maxYearsAgo;
		this.minY = minY;
		this.tint = tint;
	}

	public String title() {
		return title;
	}

	public int minYearsAgo() {
		return minYearsAgo;
	}

	public int maxYearsAgo() {
		return maxYearsAgo;
	}

	/** Hologram tint associated with memories of this era. */
	public int tint() {
		return tint;
	}

	/** The lowest block height (inclusive) at which echoes of this era form. */
	public int minY() {
		return minY;
	}

	public static Era fromDepth(int y) {
		for (Era era : values()) {
			if (y >= era.minY) {
				return era;
			}
		}
		return ELDER_DAWN;
	}

	/** The block height at which echoes of this era are most typical, used when sampling a chunk's history. */
	public int representativeY() {
		return switch (this) {
			case HEARTHS -> 60;
			case CROWNS -> 30;
			case IRON_AND_ASH -> 0;
			case DRAGONS -> -30;
			case ELDER_DAWN -> -56;
		};
	}
}
