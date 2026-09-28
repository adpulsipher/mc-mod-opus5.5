package io.github.adpulsipher.echoes.guide;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * The text of the Archaeologist's Field Guide, which every new player receives. Its table of contents links to
 * each chapter.
 */
public final class GuideBook {
	public static final String TITLE = "Field Guide to the Past";
	public static final String AUTHOR = "The Last Chronicler";

	private static final int INK = 0x2A2A3A;
	private static final int ACCENT = 0x1B6F8A;

	/** A chapter: a heading and one or more pages of body text. */
	record Chapter(String heading, int color, List<String> pages) {
	}

	static final List<Chapter> CHAPTERS = List.of(
			new Chapter("Finding Echoes", 0x1B6F8A, List.of(
					"Echo Deposits glow faintly in stone and deepslate, chiming when you draw near. They form at every depth.\n\n"
							+ "Mining one with a pickaxe shatters it into Echo Shards.",
					"A Resonance Compass (a compass ringed by four shards) points to the nearest deposit.\n\n"
							+ "Feed Echo Dust to a Memory Moth and it will lead you to one.")),
			new Chapter("Extraction", 0x1B6F8A, List.of(
					"To keep a memory whole, hold use on a deposit with an Archaeologist's Chisel (iron, copper and a stick).\n\n"
							+ "After a few careful seconds you free an Echo Block.")),
			new Chapter("The Projector", 0x1B6F8A, List.of(
					"Place an Echo Block in an Echo Projector, then use it with an empty hand.\n\n"
							+ "Ghosts of the past will replay the event that happened there. Sneak-use to take the echo back.",
					"Everyone watching receives an Echo Transcript, a book telling the whole story.\n\n"
							+ "Try every kind of event: battles, sieges, markets, rituals and more.")),
			new Chapter("The Eras", 0x7A4DB0, List.of(
					"The deeper an echo forms, the older it is:\n\n"
							+ "Y 48+  Hearths\nY 16+  Crowns\nY -16+ Iron and Ash\nY -48+ Dragons\nBelow  Elder Dawn")),
			new Chapter("Relics", 0x9A6418, List.of(
					"Replay an echo in the chunk where it formed and it settles into the earth.\n\n"
							+ "Disturbed Earth or Stone appears where relics lie. Brush it to recover blades, crowns, lockets and coins.")),
			new Chapter("Rifts", 0xA02A3C, List.of(
					"Violent memories are unstable. A replay that warns it is destabilizing may open a rift.\n\n"
							+ "Lingerers, Knights, Revenants, Wisps or even a great echo may step out.")),
			new Chapter("Creatures", 0x2A7A4A, List.of(
					"Lingerer: phases out of time; only spectral weapons touch it then.\n\n"
							+ "Echo Knight: shields its front and charges. Strike from behind.",
					"Spectral Archer: keeps its distance, slowing bolts.\n\n"
							+ "Ash Revenant: burns what it strikes, bursts with embers.",
					"Dawn Wisp: a floating light in the deepest dark.\n\n"
							+ "Shard Crawler: a shy beetle that grazes on deposits. It drops Echo Shards.\n\n"
							+ "Memory Moth: harmless, and a fine guide.")),
			new Chapter("Great Echoes", 0xA02A3C, List.of(
					"The Hollow King (Crowns): cleaves, charges and calls his knights.\n\n"
							+ "The Siege Colossus (Iron and Ash): slams and fires mortars. Arrows glance off it.",
					"The Echo Wyrm (Dragons): swoops and breathes spectral fire.\n\n"
							+ "The Hierophant (Elder Dawn): shielded while its Dawn Wisps live. Destroy them first!")),
			new Chapter("Keystones", 0x7A4DB0, List.of(
					"Memory Keystones call a great echo on demand. Use one on an idle Echo Projector.\n\n"
							+ "Crowns: crown, gold\nIron: blade, cindersteel\nDragons: wyrmscale\nDawn: wyrm heart")),
			new Chapter("Arms & Armor", 0x5E6573, List.of(
					"Crownbreaker: use to issue a Royal Decree.\nSiegebreaker: use to slam the ground.\nStaff of the Elder Dawn: fires bolts of light.\nAshen Cleaver: sets foes alight.",
					"Spectral Longsword and the Echoing Blade strike across time.\n\n"
							+ "Armor: Spectral Knight plate (Knights), Cindersteel (Revenants), Colossus plate, Dawnweave and Wyrmscale.")),
			new Chapter("Set Bonuses", 0x5E6573, List.of(
					"Wear a full set:\n\nSpectral Knight: speed, every blow spectral.\nCindersteel: strength, fire immunity.\nColossus: resistance.",
					"Dawnweave: night vision and gentle healing.\nWyrmscale: fire immunity and slow falling.")),
			new Chapter("Last Words", 0x1B6F8A, List.of(
					"/echoes history tells the story of the ground beneath you.\n\n"
							+ "Every chunk remembers something. Go and listen.\n\n  - The Last Chronicler"))
	);

	private GuideBook() {
	}

	private static MutableComponent text(String s, int color) {
		return Component.literal(s).withStyle(Style.EMPTY.withColor(color));
	}

	public static List<Component> pages() {
		List<Component> pages = new ArrayList<>();
		pages.add(Component.empty()
				.append(text("\n Echoes of the Past\n\n", ACCENT).withStyle(s -> s.withUnderlined(true)))
				.append(text(" An Archaeologist's\n    Field Guide\n\n", INK).withStyle(s -> s.withItalic(true)))
				.append(text("The world remembers. Beneath your feet, centuries of history have crystallized into echoes.", INK)));

		// Page numbers are 1-based: cover, contents, then the chapters.
		List<Integer> starts = new ArrayList<>();
		int page = 3;
		for (Chapter chapter : CHAPTERS) {
			starts.add(page);
			page += chapter.pages().size();
		}
		MutableComponent contents = text("Contents\n\n", ACCENT).withStyle(s -> s.withBold(true));
		for (int i = 0; i < CHAPTERS.size(); i++) {
			Chapter chapter = CHAPTERS.get(i);
			int target = starts.get(i);
			contents.append(text((i + 1) + ". " + chapter.heading() + "\n", chapter.color()).withStyle(s -> s
					.withClickEvent(new ClickEvent.ChangePage(target))
					.withHoverEvent(new HoverEvent.ShowText(Component.literal("Page " + target)))));
		}
		pages.add(contents);

		for (Chapter chapter : CHAPTERS) {
			for (int i = 0; i < chapter.pages().size(); i++) {
				MutableComponent p = Component.empty();
				if (i == 0) {
					p.append(text(chapter.heading() + "\n\n", chapter.color()).withStyle(s -> s.withBold(true)));
				}
				p.append(text(chapter.pages().get(i), INK));
				pages.add(p);
			}
		}
		return pages;
	}

	public static WrittenBookContent content() {
		List<Filterable<Component>> pages = new ArrayList<>();
		for (Component page : pages()) {
			pages.add(Filterable.passThrough(page));
		}
		return new WrittenBookContent(Filterable.passThrough(TITLE), AUTHOR, 0, pages, true);
	}
}
