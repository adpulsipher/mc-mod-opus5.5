package io.github.adpulsipher.echoes.history;

import java.util.Locale;
import java.util.SplittableRandom;

/**
 * Procedural names for the people, places and powers of the forgotten past.
 */
public final class NameGen {
	private static final String[] PERSON_START = {
		"Al", "Bran", "Cae", "Dag", "El", "Fen", "Gor", "Hal", "Is", "Jor", "Kae", "Ly", "Mor", "Ny", "Os",
		"Per", "Quen", "Ro", "Syl", "Tor", "Ul", "Val", "Wy", "Ys", "Zar", "Ath", "Bel", "Cor", "Dru", "Eir",
		"Ger", "Hild", "Ing", "Kes", "Lor", "Mae", "Oth", "Rhia", "Sig", "Thal", "Ves", "Wen"
	};
	private static final String[] PERSON_MID = {"", "", "", "a", "e", "i", "o", "an", "el", "ri", "ra", "or", "is", "en"};
	private static final String[] PERSON_END = {
		"dric", "wyn", "mund", "ra", "thas", "ric", "da", "vain", "gar", "lith", "mar", "ne", "sa", "wald", "ya",
		"dor", "helm", "iel", "ka", "ros", "stan", "tha", "via", "wen", "rik", "len", "gund", "mira", "bert", "dis"
	};

	private static final String[] PLACE_START = {
		"Ash", "Bright", "Cold", "Dun", "Elder", "Fair", "Grey", "Hollow", "Iron", "Kings", "Long", "Mist", "North",
		"Oak", "Raven", "Stone", "Thorn", "Wolf", "Amber", "Black", "Bram", "Crag", "Dusk", "Frost", "Gold", "Harrow",
		"Low", "Marsh", "Red", "Salt", "Silver", "Storm", "Wither", "Wind", "Ember", "Glimmer"
	};
	private static final String[] PLACE_END = {
		"vale", "ford", "moor", "hold", "wick", "stead", "haven", "mere", "gate", "fell", "barrow", "crest",
		"dale", "hollow", "reach", "watch", "bridge", "field", "march", "spire", "wold", "keep", "brook", "rest"
	};

	private static final String[] DRAGON_START = {"Vor", "Kal", "Syth", "Ygg", "Mal", "Thar", "Xer", "Ghor", "Nyx", "Aur", "Skar", "Veh"};
	private static final String[] DRAGON_MID = {"a", "ae", "o", "u", "y", "ith", "or", "ra"};
	private static final String[] DRAGON_END = {"thrax", "gorn", "vex", "mauth", "zir", "drak", "roth", "nax", "gul", "thyr"};
	private static final String[] DRAGON_EPITHET = {
		"the Cinder-Wing", "the Pale Devourer", "the Stormcaller", "Ashen-Crowned", "the Many-Eyed",
		"the Unburied", "Night's Tooth", "the Hollow Flame", "Bane of Hearths", "the Glass Serpent"
	};

	private static final String[] RULER_TITLES = {"King", "Queen", "High Lord", "Lady", "Warlord", "Prince", "Princess", "Thane", "Matriarch", "Archon"};
	private static final String[] WARRIOR_TITLES = {"Captain", "Sir", "Dame", "Marshal", "Commander", "Champion", "Warden", "Knight-Errant"};
	private static final String[] MAGE_TITLES = {"Archmage", "Seer", "Oracle", "Hierophant", "Witch", "Loremaster", "Sorcerer", "Sister"};
	private static final String[] COMMON_TITLES = {"the smith", "the miller", "the weaver", "the cartwright", "the brewer", "the shepherd", "the potter", "the fletcher"};

	private static final String[] EPITHETS = {
		"the Unbroken", "the Bold", "the Grey", "Oathkeeper", "the Just", "the Silent", "Ironhand", "the Young",
		"the Cruel", "Stormborn", "the Wise", "Half-Hand", "the Fair", "Wolfsbane", "the Last", "the Twice-Crowned"
	};

	private static final String[] ORDER_ADJ = {
		"Ashen", "Iron", "Crimson", "Sunward", "Silent", "Gilded", "Obsidian", "Verdant", "Frost", "Storm",
		"Hollow", "Radiant", "Black", "Silver", "Thorned", "Shattered", "Amber", "Veiled"
	};
	private static final String[] ORDER_NOUN = {
		"Legion", "Pact", "Brotherhood", "Covenant", "Host", "Company", "Order", "Vanguard", "Wardens", "Circle",
		"Banners", "Crown", "Council", "Guard", "Reavers", "Kindred"
	};
	private static final String[] SIGIL_BEAST = {"stag", "wolf", "raven", "serpent", "lion", "bear", "owl", "boar", "hawk", "salmon", "moth", "dragon"};
	private static final String[] SIGIL_COLOR = {"silver", "golden", "black", "crimson", "white", "azure", "green", "violet"};
	private static final int[] FACTION_TINTS = {
		0x7fe9ff, 0xffc36b, 0xff7a7a, 0x9dff9d, 0xc59bff, 0xffffff, 0x7fb4ff, 0xffa3d6
	};

	private NameGen() {
	}

	private static String pick(SplittableRandom random, String[] options) {
		return options[random.nextInt(options.length)];
	}

	private static String capitalize(String s) {
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
	}

	public static String givenName(SplittableRandom random) {
		String name = pick(random, PERSON_START) + pick(random, PERSON_MID) + pick(random, PERSON_END);
		return capitalize(name);
	}

	public static String settlement(SplittableRandom random) {
		String start = pick(random, PLACE_START);
		String end = pick(random, PLACE_END);
		return start + end;
	}

	public static String dragon(SplittableRandom random) {
		String name = capitalize(pick(random, DRAGON_START) + pick(random, DRAGON_MID) + pick(random, DRAGON_END));
		return name + " " + pick(random, DRAGON_EPITHET);
	}

	public static String ruler(SplittableRandom random) {
		return pick(random, RULER_TITLES) + " " + givenName(random) + " " + pick(random, EPITHETS);
	}

	public static String warrior(SplittableRandom random) {
		return pick(random, WARRIOR_TITLES) + " " + givenName(random);
	}

	public static String mage(SplittableRandom random) {
		return pick(random, MAGE_TITLES) + " " + givenName(random);
	}

	public static String commoner(SplittableRandom random) {
		return givenName(random) + " " + pick(random, COMMON_TITLES);
	}

	public static String house(SplittableRandom random) {
		return "House " + capitalize(pick(random, PERSON_START) + pick(random, PERSON_END));
	}

	/**
	 * Creates a faction. {@code tintOffset} lets callers force two factions of the same event to differ in color.
	 */
	public static Faction faction(SplittableRandom random, int tintOffset) {
		String shortName;
		String name;
		if (random.nextInt(3) == 0) {
			shortName = house(random);
			name = shortName;
		} else {
			shortName = pick(random, ORDER_ADJ) + " " + pick(random, ORDER_NOUN);
			name = "the " + shortName;
		}
		String sigil = "a " + pick(random, SIGIL_COLOR) + " " + pick(random, SIGIL_BEAST);
		int tint = FACTION_TINTS[Math.floorMod(random.nextInt(FACTION_TINTS.length) + tintOffset, FACTION_TINTS.length)];
		return new Faction(name, shortName, tint, sigil);
	}

	/** A second faction guaranteed to be distinguishable from {@code other}. */
	public static Faction rival(SplittableRandom random, Faction other) {
		for (int i = 0; i < 8; i++) {
			Faction candidate = faction(random, i);
			if (candidate.tint() != other.tint() && !candidate.shortName().equals(other.shortName())) {
				return candidate;
			}
		}
		return new Faction("the Nameless Host", "Nameless Host", other.tint() == 0xff7a7a ? 0x7fe9ff : 0xff7a7a, "a broken wheel");
	}
}
