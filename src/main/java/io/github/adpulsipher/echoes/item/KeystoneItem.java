package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import io.github.adpulsipher.echoes.entity.BossKind;
import io.github.adpulsipher.echoes.entity.Manifestations;
import io.github.adpulsipher.echoes.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * A memory keystone: a crystallized fragment of one of the great echoes. Set into an idle Echo Projector, it
 * overloads the lens and calls that echo into the present.
 */
public class KeystoneItem extends Item {
	private final BossKind kind;

	public KeystoneItem(BossKind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	public BossKind kind() {
		return kind;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		if (!context.getLevel().getBlockState(pos).is(ModBlocks.ECHO_PROJECTOR)) {
			return InteractionResult.PASS;
		}
		Player player = context.getPlayer();
		if (context.getLevel() instanceof ServerLevel level) {
			if (level.getBlockEntity(pos) instanceof EchoProjectorBlockEntity projector && projector.isProjecting()) {
				if (player != null) {
					player.sendOverlayMessage(Component.translatable("block.echoes_of_the_past.echo_projector.busy"));
				}
				return InteractionResult.FAIL;
			}
			Manifestations.begin(level, pos.above(), kind, 100, null);
			if (player == null || !player.getAbilities().instabuild) {
				context.getItemInHand().shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
