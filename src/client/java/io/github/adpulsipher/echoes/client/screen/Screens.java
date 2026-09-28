package io.github.adpulsipher.echoes.client.screen;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Opens a screen. The method that does this on {@link Minecraft} has been renamed across versions, so it is found
 * once by its shape (a public instance method taking a single {@link Screen}) rather than by name.
 */
public final class Screens {
	private static Method open;

	private Screens() {
	}

	public static void open(Screen screen) {
		try {
			if (open == null) {
				open = find();
				EchoesOfThePast.LOGGER.info("Opening screens with Minecraft#{}", open.getName());
			}
			open.invoke(Minecraft.getInstance(), screen);
		} catch (ReflectiveOperationException e) {
			EchoesOfThePast.LOGGER.error("Could not open the Codex of Echoes", e);
		}
	}

	private static Method find() throws NoSuchMethodException {
		Method best = null;
		int bestScore = Integer.MIN_VALUE;
		for (Method method : Minecraft.class.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1 || method.getParameterTypes()[0] != Screen.class) {
				continue;
			}
			String name = method.getName().toLowerCase();
			EchoesOfThePast.LOGGER.info("Minecraft screen method candidate: {}", method);
			int score = 0;
			if (name.contains("screen")) {
				score += 10;
			}
			if (name.startsWith("set") || name.startsWith("show") || name.startsWith("open")) {
				score += 5;
			}
			if (name.contains("force") || name.contains("overlay") || name.contains("disconnect")) {
				score -= 20;
			}
			if (score > bestScore) {
				bestScore = score;
				best = method;
			}
		}
		if (best == null) {
			throw new NoSuchMethodException("no Minecraft method accepts a Screen");
		}
		return best;
	}
}
