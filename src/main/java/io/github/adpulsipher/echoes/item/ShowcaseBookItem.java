package io.github.adpulsipher.echoes.item;

import java.util.function.Consumer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The Codex of Echoes, a creative-mode showcase. Opening it shows a menu that builds the mod's structures, summons
 * its bosses and creatures, stages replays and hands out gear. The menu itself lives on the client.
 */
public class ShowcaseBookItem extends Item {
	/** Set by the client to open the codex screen. */
	public static Consumer<Player> openScreen = player -> {
	};

	public ShowcaseBookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			openScreen.accept(player);
		}
		return InteractionResult.SUCCESS;
	}
}
