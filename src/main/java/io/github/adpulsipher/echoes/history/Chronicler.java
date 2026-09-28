package io.github.adpulsipher.echoes.history;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import io.github.adpulsipher.echoes.history.HistoricEvent.Outcome;

/**
 * Writes the story of a historic event: its title, the five narration beats spoken during a replay,
 * and the long-form chronicle bound into transcript books.
 */
final class Chronicler {
	private Chronicler() {
	}

	record Story(String title, String hero, String foe, Outcome outcome, List<String> narration, List<String> chronicle, String relicHint) {
	}

	private static String pick(SplittableRandom random, String... options) {
		return options[random.nextInt(options.length)];
	}

	private static String cap(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	static Story write(SplittableRandom random, EventType type, Era era, Terrain terrain, String place, Faction a, Faction b, String date) {
		return switch (type) {
			case BATTLE -> battle(random, era, terrain, place, a, b, date);
			case SIEGE -> siege(random, era, terrain, place, a, b, date);
			case DRAGON_ATTACK -> dragon(random, era, terrain, place, a, date);
			case MARKET_DAY -> market(random, terrain, place, a, b, date);
			case CORONATION -> coronation(random, place, a, date);
			case RITUAL -> ritual(random, era, terrain, place, a, date);
			case DUEL -> duel(random, place, a, b, date);
			case FESTIVAL -> festival(random, terrain, place, a, date);
			case EXODUS -> exodus(random, era, terrain, place, a, date);
			case CAVE_IN -> caveIn(random, place, a, date);
		};
	}

	private static Story battle(SplittableRandom r, Era era, Terrain terrain, String place, Faction a, Faction b, String date) {
		String hero = NameGen.warrior(r);
		String foe = NameGen.warrior(r);
		Outcome outcome = r.nextInt(3) == 0 ? Outcome.DEFEAT : Outcome.VICTORY;
		String cause = pick(r,
				"a quarrel over the salt roads",
				"the murder of an envoy",
				"a broken marriage-pact",
				"the theft of a holy relic",
				"hunger, after three failed harvests",
				"an insult spoken at a royal feast");
		String title = pick(r, "The Battle of ", "The Rout at ", "The Clash at ", "The Bloodying of ") + place;
		Faction winner = outcome == Outcome.VICTORY ? a : b;
		Faction loser = outcome == Outcome.VICTORY ? b : a;
		List<String> narration = List.of(
				date + ". The " + terrain.landName() + " near " + place + ".",
				cap(a.name()) + " and " + b.name() + " meet, bound for war by " + cause + ".",
				hero + " sounds the horn. The lines break upon one another.",
				foe + " falls" + pick(r, " beneath the banner of " + winner.shortName() + ".", ". The field is lost for " + loser.shortName() + ".", ", and the fighting falters."),
				cap(winner.name()) + " hold" + (winner.name().startsWith("House") ? "s" : "") + " the field. The rest is silence.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nIn the " + terrain.landName() + " beside " + place + ", two hosts made war for " + cause + ".");
		chronicle.add(cap(a.name()) + ", who bore " + a.sigil() + ", were led by " + hero + ". Against them marched " + b.name() + " under " + b.sigil() + ", with " + foe + " at their head.");
		chronicle.add("The armies met at dawn. Arrows darkened the sky before the shield-walls closed, and for an hour neither side would yield.");
		chronicle.add(outcome == Outcome.VICTORY
				? hero + " broke the enemy line and " + foe + " was struck down. " + cap(b.name()) + " fled into the " + terrain.landName() + "."
				: foe + " turned the flank and " + hero + " fell among the standard-bearers. " + cap(a.name()) + " were scattered and " + place + " was burned.");
		chronicle.add("The dead were never counted. Their arms still lie beneath this ground, and some say their anger lingers with them.");
		String relic = "Weapons and coin of the fallen lie buried where the lines met.";
		return new Story(title, hero, foe, outcome, narration, chronicle, relic);
	}

	private static Story siege(SplittableRandom r, Era era, Terrain terrain, String place, Faction a, Faction b, String date) {
		String hero = NameGen.ruler(r);
		String foe = NameGen.warrior(r);
		Outcome outcome = r.nextInt(2) == 0 ? Outcome.VICTORY : Outcome.DEFEAT;
		String keep = place + " " + pick(r, "Keep", "Tower", "Hold", "Citadel", "Wall");
		String title = "The Siege of " + keep;
		int days = 20 + r.nextInt(200);
		List<String> narration = List.of(
				date + ". " + keep + " stands against the " + terrain.landName() + ".",
				cap(b.name()) + " have encircled the walls for " + days + " days.",
				"The ram is brought forward. The defenders loose their last arrows.",
				outcome == Outcome.VICTORY ? "The gate holds. " + hero + " leads the sally." : "The gate splinters. " + foe + " pours through.",
				outcome == Outcome.VICTORY ? "The besiegers break and flee." : cap(keep) + " falls, and is not rebuilt.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\n" + keep + " was the seat of " + hero + " and the pride of " + a.name() + ".");
		chronicle.add("In the " + pick(r, "autumn", "deep winter", "spring thaw", "summer drought") + ", " + b.name() + " came with engines and ladders, led by " + foe + ".");
		chronicle.add("For " + days + " days the walls were held. Wells were fouled, the granaries emptied, and the defenders ate their horses.");
		chronicle.add(outcome == Outcome.VICTORY
				? "When the ram came at last, " + hero + " threw open the gate and charged. The besiegers, sick and starving themselves, broke."
				: "When the ram came at last, the gate gave way. " + hero + " was taken in chains, and the stones of the keep were carried off.");
		chronicle.add("Nothing of the walls remains above ground. Below, the rubble still holds what the defenders hid.");
		return new Story(title, hero, foe, outcome, narration, chronicle, "The defenders buried their treasury beneath the gatehouse.");
	}

	private static Story dragon(SplittableRandom r, Era era, Terrain terrain, String place, Faction a, String date) {
		String dragon = NameGen.dragon(r);
		String hero = NameGen.warrior(r);
		Outcome outcome = switch (r.nextInt(4)) {
			case 0 -> Outcome.TRIUMPH;
			case 1 -> Outcome.CATASTROPHE;
			default -> Outcome.SURVIVED;
		};
		String dragonShort = dragon.substring(0, dragon.indexOf(' '));
		String title = pick(r, "The Burning of ", "The Wyrmfall at ", "The Dragon's Harvest at ", "The Ashing of ") + place;
		List<String> narration = List.of(
				date + ". An ordinary evening in " + place + ".",
				"A shadow crosses the " + terrain.landName() + ". Bells ring out. " + dragon + " has come.",
				"Fire falls from the sky. The people of " + place + " run for the caves.",
				hero + " and the archers of " + a.shortName() + " stand their ground.",
				switch (outcome) {
					case TRIUMPH -> dragonShort + " is struck through the eye, and falls screaming into the " + terrain.landName() + ".";
					case CATASTROPHE -> "Nothing remains of " + place + " but ash. " + dragonShort + " sleeps where it fed.";
					default -> dragonShort + " is driven off, wounded. " + place + " will rebuild.";
				});
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\n" + place + " was a quiet settlement of " + a.name() + ", keepers of herds and hearths.");
		chronicle.add("At dusk the sky darkened, and " + dragon + " descended upon the village. It was said its wings blotted out the moon.");
		chronicle.add("Roofs burned like kindling. Those who could fled to the caves, while " + hero + " gathered every bow in the village.");
		chronicle.add(switch (outcome) {
			case TRIUMPH -> "By the grace of some forgotten god an arrow found its eye. The beast fell, and its bones were buried where it landed.";
			case CATASTROPHE -> "The arrows were as rain on stone. By morning " + place + " was ash, and the survivors never spoke its name again.";
			default -> "Wounded and furious, the beast wheeled away into the " + terrain.landName() + ". It did not return in their lifetimes.";
		});
		chronicle.add("Scales as large as shields have been found in this soil. Some are still warm.");
		return new Story(title, hero, dragon, outcome, narration, chronicle, "Shed scales of the beast are buried in the scorched earth.");
	}

	private static Story market(SplittableRandom r, Terrain terrain, String place, Faction a, Faction b, String date) {
		String hero = NameGen.commoner(r);
		String foe = "the merchants of " + b.shortName();
		String good = pick(r, "amber and furs", "salt and spice", "glassware from the south", "dyed wool", "iron ingots", "honey and wax", "silk and saffron");
		String title = pick(r, "Market Day at ", "The Great Fair of ", "The Caravan Days of ") + place;
		List<String> narration = List.of(
				date + ". The market square of " + place + ".",
				"Caravans of " + b.shortName() + " arrive, laden with " + good + ".",
				hero + " haggles loudly. Coins change hands.",
				"A " + pick(r, "juggler", "bard", "fire-eater", "storyteller") + " draws a crowd. Children run between the stalls.",
				"The day ends well. For a time, " + place + " was prosperous.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nOnce each season the roads opened, and " + place + " became the richest market in the " + terrain.landName() + ".");
		chronicle.add("Traders of " + b.name() + " brought " + good + ", and " + a.name() + " paid in silver stamped with " + a.sigil() + ".");
		chronicle.add(hero + " kept the finest stall, and it was said no bargain in " + place + " was struck without their blessing.");
		chronicle.add("The fairs ended when the roads grew dangerous, and the square was swallowed by grass. Dropped coins remain in the dirt.");
		return new Story(title, hero, foe, Outcome.TRIUMPH, narration, chronicle, "Coins dropped in the crowd lie scattered under the old square.");
	}

	private static Story coronation(SplittableRandom r, String place, Faction a, String date) {
		String ruler = NameGen.ruler(r);
		String hero = ruler;
		String foe = NameGen.mage(r);
		Outcome outcome = r.nextInt(5) == 0 ? Outcome.CATASTROPHE : Outcome.TRIUMPH;
		String title = "The Crowning of " + ruler;
		List<String> narration = List.of(
				date + ". The great hall of " + place + ".",
				"The lords of " + a.shortName() + " kneel in two long rows.",
				ruler + " walks between them to the high seat.",
				foe + " raises the crown of " + a.shortName() + ".",
				outcome == Outcome.TRIUMPH ? "\"Long may they reign.\" The hall roars its oath." : "A blade flashes. The reign lasts but a single breath.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nWhen the old ruler died without heir, the lords of " + a.name() + " gathered at " + place + " to choose another.");
		chronicle.add("They chose " + ruler + ". The crown, set with " + pick(r, "seven emeralds", "a single sapphire", "the teeth of a wolf", "white gold from the mountains") + ", was brought from the vaults.");
		chronicle.add(foe + " placed the crown, and every lord swore upon " + a.sigil() + ".");
		chronicle.add(outcome == Outcome.TRIUMPH
				? "The reign that followed was remembered as a golden one, and " + place + " grew great."
				: "But a traitor stood among the kneeling lords. The new ruler died upon the dais, and the crown was lost in the chaos.");
		return new Story(title, hero, foe, outcome, narration, chronicle, "Regalia of the crowning were hidden beneath the hall's foundations.");
	}

	private static Story ritual(SplittableRandom r, Era era, Terrain terrain, String place, Faction a, String date) {
		String hero = NameGen.mage(r);
		String foe = pick(r, "the Hollow Star", "the Thing Beneath", "the Pale Choir", "the Unmaking", "the Eye Between Worlds", "the Last Dream");
		Outcome outcome = r.nextInt(3) == 0 ? Outcome.CATASTROPHE : Outcome.TRIUMPH;
		String title = pick(r, "The Rite of ", "The Summoning at ", "The Long Night of ") + place;
		List<String> narration = List.of(
				date + ". A circle of standing stones near " + place + ".",
				hero + " and the circle of " + a.shortName() + " begin the chant.",
				"The air thickens. Light pours upward from the center of the ring.",
				"Something answers. " + cap(foe) + " opens its eye.",
				outcome == Outcome.TRIUMPH ? "The rite holds. " + cap(foe) + " is bound beneath the stones." : "The circle breaks. None who stood within it are seen again.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nIn the " + terrain.landName() + " near " + place + ", the mages of " + a.name() + " raised a ring of stones.");
		chronicle.add(hero + " believed " + foe + " could be called down and bound, and its power drawn up like water from a well.");
		chronicle.add("On the longest night they chanted until their voices failed. A pillar of light rose so high it was seen in distant lands.");
		chronicle.add(outcome == Outcome.TRIUMPH
				? "The binding held. " + hero + " emerged white-haired and silent, and never spoke of what they saw."
				: "The binding failed. The ring was found empty at dawn, the stones cracked through, the grass burned in a perfect circle.");
		chronicle.add("Those who dig here report strange warmth, and voices at the edge of hearing.");
		return new Story(title, hero, foe, outcome, narration, chronicle, "Ritual implements were buried within the ring when it was abandoned.");
	}

	private static Story duel(SplittableRandom r, String place, Faction a, Faction b, String date) {
		String hero = NameGen.warrior(r);
		String foe = NameGen.warrior(r);
		Outcome outcome = r.nextBoolean() ? Outcome.VICTORY : Outcome.DEFEAT;
		String stake = pick(r, "the hand of a princess", "the ownership of the valley", "a stolen sword", "the honor of a murdered father", "the right to the throne", "a single disputed well");
		String title = "The Duel of " + hero.substring(hero.indexOf(' ') + 1) + " and " + foe.substring(foe.indexOf(' ') + 1);
		List<String> narration = List.of(
				date + ". A ring of onlookers outside " + place + ".",
				hero + " of " + a.shortName() + " faces " + foe + " of " + b.shortName() + ".",
				"They fight for " + stake + ". Steel rings on steel.",
				"The crowd falls silent. One champion stumbles.",
				(outcome == Outcome.VICTORY ? hero : foe) + " stands alone in the ring.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nRather than spend a thousand lives on war, " + a.name() + " and " + b.name() + " agreed to settle their quarrel by single combat.");
		chronicle.add("The quarrel was over " + stake + ". " + hero + " and " + foe + " were chosen as champions, and a ring was drawn outside " + place + ".");
		chronicle.add("They fought until the sun was low. Both bled from a dozen wounds.");
		chronicle.add((outcome == Outcome.VICTORY ? hero : foe) + " was the victor. The loser's blade was buried where they fell, as was the custom.");
		return new Story(title, hero, foe, outcome, narration, chronicle, "The fallen champion's blade was buried where they fell.");
	}

	private static Story festival(SplittableRandom r, Terrain terrain, String place, Faction a, String date) {
		String hero = NameGen.commoner(r);
		String feast = pick(r, "the Harvest Moon", "Midsummer", "the First Snow", "the Lantern Night", "the Return of the Swallows", "the Longest Day");
		String title = "The Festival of " + feast.replace("the ", "");
		List<String> narration = List.of(
				date + ". The green of " + place + ", on the eve of " + feast + ".",
				"A great bonfire is lit. The people of " + a.shortName() + " gather close.",
				hero + " strikes up a tune. The dancing begins.",
				"Round and round they go, beneath a sky full of stars.",
				"For one night, the " + terrain.landName() + " knew nothing but joy.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nEvery year " + a.name() + " of " + place + " celebrated " + feast + " with fire, song and too much cider.");
		chronicle.add(hero + " was the finest fiddler in the " + terrain.landName() + ", and no festival began until they arrived.");
		chronicle.add("That year was remembered as the best of all: the harvest was rich, the night was warm, and no one quarreled until dawn.");
		chronicle.add("Trinkets, charms and ribbons were lost in the dancing grass, and the earth has kept them.");
		return new Story(title, hero, feast, Outcome.TRIUMPH, narration, chronicle, "Festival charms and trinkets were lost in the dancing grass.");
	}

	private static Story exodus(SplittableRandom r, Era era, Terrain terrain, String place, Faction a, String date) {
		String hero = NameGen.ruler(r);
		String cause = pick(r, "the plague", "the long winter", "the rising sea", "the coming of the dragons", "a war they could not win", "the failing of the wells");
		Outcome outcome = r.nextInt(3) == 0 ? Outcome.CATASTROPHE : Outcome.SURVIVED;
		String title = "The Leaving of " + place;
		List<String> narration = List.of(
				date + ". The road out of " + place + ".",
				"Fleeing " + cause + ", " + a.name() + " abandon their homes.",
				hero + " leads the long column through the " + terrain.landName() + ".",
				"The old and the sick fall behind. Some are left where they lie.",
				outcome == Outcome.SURVIVED ? "They cross the far hills, and are never heard of here again." : "The road is long. Few reach its end.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\n" + place + " had stood for three hundred years when " + cause + " came.");
		chronicle.add(hero + " ordered the village emptied. Carts were loaded with what could be carried; the rest was buried, in hope of return.");
		chronicle.add("The column stretched for a mile along the road through the " + terrain.landName() + ". Mothers carried children; the old leaned on the young.");
		chronicle.add("No one ever came back for what was buried.");
		return new Story(title, hero, cause, outcome, narration, chronicle, "Belongings were buried by families who hoped to return.");
	}

	private static Story caveIn(SplittableRandom r, String place, Faction a, String date) {
		String hero = NameGen.givenName(r) + " the foreman";
		String vein = pick(r, "silver", "gold", "copper", "iron", "amethyst", "star-metal");
		Outcome outcome = r.nextBoolean() ? Outcome.CATASTROPHE : Outcome.SURVIVED;
		String title = "The Collapse of the " + place + " " + cap(vein) + " Mine";
		List<String> narration = List.of(
				date + ". Deep beneath " + place + ", in the " + vein + " mine.",
				"The miners of " + a.shortName() + " work the richest vein they have ever found.",
				"A groan in the rock. Dust sifts from the ceiling.",
				"The tunnel comes down. " + hero + " shouts for the others to run.",
				outcome == Outcome.SURVIVED ? "Most reach the surface. The vein is sealed forever." : "The mountain keeps them. Their lamps still burn in the dark, somewhere below.");
		List<String> chronicle = new ArrayList<>();
		chronicle.add(title + ".\n" + date + ".\n\nThe " + vein + " of " + place + " made " + a.name() + " rich for a generation.");
		chronicle.add("Greed drove the tunnels ever deeper. " + hero + " warned that the supports were rotten, but no one listened.");
		chronicle.add("When the collapse came it was sudden. The roar was heard in the village above.");
		chronicle.add(outcome == Outcome.SURVIVED
				? "By luck or providence most of the miners escaped. The mine was sealed, its " + vein + " abandoned."
				: "Forty went down that morning. Four came back up. The mine was sealed with its dead inside.");
		return new Story(title, hero, vein, outcome, narration, chronicle, "Tools and ore were abandoned in the collapsed tunnels.");
	}
}
