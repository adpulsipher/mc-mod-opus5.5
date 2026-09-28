package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.guide.GuideBook;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Archaeologist's Field Guide. Opens like a written book; its text is always the latest edition.
 */
public class GuideBookItem extends Item {
	public GuideBookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			stack.set(DataComponents.WRITTEN_BOOK_CONTENT, GuideBook.content());
			player.openItemGui(stack, hand);
		}
		return InteractionResult.SUCCESS;
	}
}
