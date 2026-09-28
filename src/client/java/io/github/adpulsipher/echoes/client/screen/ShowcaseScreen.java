package io.github.adpulsipher.echoes.client.screen;

import java.util.List;

import io.github.adpulsipher.echoes.showcase.ShowcaseCatalog;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * The Codex of Echoes: a creative-mode menu that builds showcase structures, summons bosses and creatures, stages
 * replays and hands out gear. Every button runs the matching {@code /echoes showcase} command.
 */
public class ShowcaseScreen extends Screen {
	private static final List<String> TABS = List.of("structure", "boss", "creature", "replay", "gear");
	private static String tab = "structure";

	public ShowcaseScreen() {
		super(Component.translatable("showcase.echoes_of_the_past.title"));
	}

	@Override
	protected void init() {
		int center = this.width / 2;
		addRenderableWidget(new StringWidget(center - 150, 12, 300, 12,
				this.title.copy().withStyle(Style.EMPTY.withColor(0x7FE9FF).withBold(true)), this.font));
		addRenderableWidget(new StringWidget(center - 150, 24, 300, 12,
				Component.translatable("showcase.echoes_of_the_past.subtitle").withStyle(Style.EMPTY.withColor(0xA0A0B8).withItalic(true)), this.font));

		int tabWidth = 64;
		int tabsLeft = center - (TABS.size() * (tabWidth + 4) - 4) / 2;
		for (int i = 0; i < TABS.size(); i++) {
			String id = TABS.get(i);
			Button button = Button.builder(Component.translatable("showcase.echoes_of_the_past.tab." + id), b -> {
				tab = id;
				rebuildWidgets();
			}).bounds(tabsLeft + i * (tabWidth + 4), 42, tabWidth, 20).build();
			button.active = !id.equals(tab);
			addRenderableWidget(button);
		}

		int top = 72;
		if (tab.equals("boss")) {
			List<String> bosses = ShowcaseCatalog.CATEGORIES.get("boss");
			for (int i = 0; i < bosses.size(); i++) {
				String id = bosses.get(i);
				int y = top + i * 26;
				addRenderableWidget(entry("boss", id, center - 154, y, 150));
				addRenderableWidget(Button.builder(Component.translatable("showcase.echoes_of_the_past.arena_button"), b -> run("arena", id))
						.bounds(center + 4, y, 150, 20)
						.tooltip(Tooltip.create(Component.translatable("showcase.echoes_of_the_past.arena." + id + ".desc")))
						.build());
			}
		} else {
			List<String> ids = ShowcaseCatalog.CATEGORIES.get(tab);
			for (int i = 0; i < ids.size(); i++) {
				int column = i % 2;
				int row = i / 2;
				addRenderableWidget(entry(tab, ids.get(i), column == 0 ? center - 154 : center + 4, top + row * 24, 150));
			}
		}

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
				.bounds(center - 50, this.height - 28, 100, 20).build());
	}

	private Button entry(String category, String id, int x, int y, int width) {
		String key = "showcase.echoes_of_the_past." + category + "." + id;
		return Button.builder(Component.translatable(key), b -> run(category, id))
				.bounds(x, y, width, 20)
				.tooltip(Tooltip.create(Component.translatable(key + ".desc")))
				.build();
	}

	private void run(String category, String id) {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.connection.sendCommand(ShowcaseCatalog.command(category, id));
		}
		onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
