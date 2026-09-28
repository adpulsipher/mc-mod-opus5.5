package io.github.adpulsipher.echoes.history;

import java.util.Locale;

/**
 * The kinds of historic events an echo can remember.
 */
public enum EventType {
	BATTLE("battle", true),
	SIEGE("siege", true),
	DRAGON_ATTACK("dragon_attack", true),
	MARKET_DAY("market_day", false),
	CORONATION("coronation", false),
	RITUAL("ritual", true),
	DUEL("duel", false),
	FESTIVAL("festival", false),
	EXODUS("exodus", false),
	CAVE_IN("cave_in", false);

	private final String id;
	private final boolean violent;

	EventType(String id, boolean violent) {
		this.id = id;
		this.violent = violent;
	}

	public String id() {
		return id;
	}

	/** Violent memories are unstable and may tear open a rift when replayed. */
	public boolean violent() {
		return violent;
	}

	public static EventType byId(String id) {
		for (EventType type : values()) {
			if (type.id.equals(id)) {
				return type;
			}
		}
		return BATTLE;
	}

	public String displayName() {
		String name = id.replace('_', ' ');
		return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(Locale.ROOT);
	}

	/**
	 * Relative likelihood of this event in the given era and terrain.
	 */
	public int weight(Era era, Terrain terrain) {
		int w = switch (this) {
			case BATTLE -> 12;
			case SIEGE -> 7;
			case DRAGON_ATTACK -> 4;
			case MARKET_DAY -> 10;
			case CORONATION -> 5;
			case RITUAL -> 5;
			case DUEL -> 7;
			case FESTIVAL -> 8;
			case EXODUS -> 6;
			case CAVE_IN -> 2;
		};

		w += switch (era) {
			case HEARTHS -> switch (this) {
				case MARKET_DAY, FESTIVAL -> 8;
				case DRAGON_ATTACK -> -3;
				case RITUAL -> -2;
				default -> 0;
			};
			case CROWNS -> switch (this) {
				case CORONATION, SIEGE, DUEL -> 5;
				default -> 0;
			};
			case IRON_AND_ASH -> switch (this) {
				case BATTLE, SIEGE -> 7;
				case EXODUS, CAVE_IN -> 4;
				default -> 0;
			};
			case DRAGONS -> switch (this) {
				case DRAGON_ATTACK -> 16;
				case EXODUS -> 4;
				case MARKET_DAY -> -5;
				default -> 0;
			};
			case ELDER_DAWN -> switch (this) {
				case RITUAL -> 14;
				case DRAGON_ATTACK -> 9;
				case MARKET_DAY, CORONATION -> -4;
				default -> 0;
			};
		};

		w += switch (terrain) {
			case PLAINS -> switch (this) {
				case BATTLE, FESTIVAL, MARKET_DAY -> 4;
				default -> 0;
			};
			case FOREST -> switch (this) {
				case RITUAL, DUEL -> 5;
				default -> 0;
			};
			case MOUNTAINS -> switch (this) {
				case DRAGON_ATTACK -> 8;
				case SIEGE, CAVE_IN -> 5;
				default -> 0;
			};
			case DESERT -> switch (this) {
				case EXODUS, MARKET_DAY -> 6;
				case FESTIVAL -> -3;
				default -> 0;
			};
			case SNOW -> switch (this) {
				case SIEGE, EXODUS -> 5;
				default -> 0;
			};
			case SWAMP -> switch (this) {
				case RITUAL -> 8;
				case EXODUS -> 3;
				default -> 0;
			};
			case COAST -> switch (this) {
				case MARKET_DAY, EXODUS -> 5;
				default -> 0;
			};
			case JUNGLE -> switch (this) {
				case RITUAL -> 6;
				case FESTIVAL -> 3;
				default -> 0;
			};
			case BADLANDS -> switch (this) {
				case CAVE_IN -> 8;
				case DUEL -> 4;
				default -> 0;
			};
			case UNDERGROUND -> switch (this) {
				case CAVE_IN -> 14;
				case RITUAL -> 6;
				case FESTIVAL, CORONATION -> -4;
				default -> 0;
			};
		};

		return Math.max(1, w);
	}
}
