package io.github.adpulsipher.echoes.replay;

/**
 * The part a ghostly figure plays in a replay. Each role has its own look.
 */
public enum Role {
	SOLDIER,
	KNIGHT,
	ARCHER,
	PEASANT,
	MERCHANT,
	NOBLE,
	MAGE,
	MINER,
	CHILD,
	DRAGON;

	private static final Role[] VALUES = values();

	public static Role byId(int id) {
		return VALUES[Math.floorMod(id, VALUES.length)];
	}

	public boolean humanoid() {
		return this != DRAGON;
	}

	public String textureName() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}
}
