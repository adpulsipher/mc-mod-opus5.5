package io.github.adpulsipher.echoes.history;

/**
 * Broad landscape categories. The land shapes what history happened on it.
 */
public enum Terrain {
	PLAINS("meadows"),
	FOREST("old woods"),
	MOUNTAINS("high crags"),
	DESERT("dunes"),
	SNOW("frozen wastes"),
	SWAMP("mires"),
	COAST("shores"),
	JUNGLE("green deeps"),
	BADLANDS("red mesas"),
	UNDERGROUND("caverns");

	private final String landName;

	Terrain(String landName) {
		this.landName = landName;
	}

	/** Poetic plural noun for the land, e.g. "the high crags". */
	public String landName() {
		return landName;
	}
}
