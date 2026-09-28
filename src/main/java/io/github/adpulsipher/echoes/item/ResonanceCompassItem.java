package io.github.adpulsipher.echoes.item;

import java.util.Optional;

import io.github.adpulsipher.echoes.registry.ModSounds;
import io.github.adpulsipher.echoes.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

/**
 * A compass whose needle is a sliver of echo crystal. When shaken, it listens for the hum of nearby deposits
 * and points to the closest one.
 */
public class ResonanceCompassItem extends Item {
	public static final int RADIUS = 24;
	public static final int VERTICAL = 32;

	public ResonanceCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		Optional<BlockPos> nearest = findNearest(serverLevel, player.blockPosition());
		if (nearest.isPresent()) {
			BlockPos pos = nearest.get();
			stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), pos)), false));
			int distance = (int) Math.sqrt(pos.distSqr(player.blockPosition()));
			player.sendOverlayMessage(Component.translatable("item.echoes_of_the_past.resonance_compass.found", distance));
			level.playSound(null, player.blockPosition(), ModSounds.COMPASS_PING, SoundSource.PLAYERS, 0.8f, 1.6f - Math.min(distance, 32) / 40.0f);
		} else {
			stack.remove(DataComponents.LODESTONE_TRACKER);
			player.sendOverlayMessage(Component.translatable("item.echoes_of_the_past.resonance_compass.silent"));
			level.playSound(null, player.blockPosition(), ModSounds.COMPASS_PING, SoundSource.PLAYERS, 0.4f, 0.5f);
		}
		player.getCooldowns().addCooldown(stack, 30);
		return InteractionResult.SUCCESS;
	}

	public static Optional<BlockPos> findNearest(ServerLevel level, BlockPos center) {
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int minY = Math.max(level.getMinY(), center.getY() - VERTICAL);
		int maxY = Math.min(level.getMaxY(), center.getY() + VERTICAL);
		for (int x = center.getX() - RADIUS; x <= center.getX() + RADIUS; x++) {
			for (int z = center.getZ() - RADIUS; z <= center.getZ() + RADIUS; z++) {
				for (int y = minY; y <= maxY; y++) {
					pos.set(x, y, z);
					if (level.getBlockState(pos).is(ModTags.ECHO_DEPOSITS)) {
						double d = pos.distSqr(center);
						if (d < bestDist) {
							bestDist = d;
							best = pos.immutable();
						}
					}
				}
			}
		}
		return Optional.ofNullable(best);
	}
}
