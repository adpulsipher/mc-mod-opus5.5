package io.github.adpulsipher.echoes.history;

/**
 * A people, house or company that took part in a historic event.
 *
 * @param name full name with article, e.g. "the Ashen Legion"
 * @param shortName name without article, e.g. "Ashen Legion"
 * @param tint the RGB tint used to render this faction's ghosts
 * @param sigil a heraldic device, e.g. "a silver stag"
 */
public record Faction(String name, String shortName, int tint, String sigil) {
	public String capitalizedName() {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
}
