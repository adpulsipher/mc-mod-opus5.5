package io.github.adpulsipher.echoes.showcase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the Codex of Echoes can conjure, by category. Shared by the {@code /echoes showcase} command and the
 * codex screen. Titles and descriptions live in the language file under
 * {@code showcase.echoes_of_the_past.<category>.<id>} and {@code ...<id>.desc}.
 */
public final class ShowcaseCatalog {
	public static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();

	static {
		CATEGORIES.put("structure", List.of("projector_stage", "dig_site", "throne_hall", "siege_ruin", "wyrm_roost", "dawn_altar", "bestiary", "armory"));
		CATEGORIES.put("boss", List.of("hollow_king", "siege_colossus", "echo_wyrm", "hierophant"));
		CATEGORIES.put("arena", List.of("hollow_king", "siege_colossus", "echo_wyrm", "hierophant"));
		CATEGORIES.put("creature", List.of("lingerer", "memory_moth", "echo_knight", "spectral_archer", "ash_revenant", "dawn_wisp", "shard_crawler"));
		CATEGORIES.put("replay", List.of("battle", "siege", "dragon_attack", "market_day", "coronation", "ritual", "duel", "festival", "exodus", "cave_in"));
		CATEGORIES.put("gear", List.of("explorer_kit", "wyrmscale", "spectral_knight", "cindersteel", "colossus", "dawnweave", "weapons", "keystones"));
	}

	private ShowcaseCatalog() {
	}

	public static boolean contains(String category, String id) {
		List<String> ids = CATEGORIES.get(category);
		return ids != null && ids.contains(id);
	}

	public static String command(String category, String id) {
		return "echoes showcase " + category + " " + id;
	}
}
