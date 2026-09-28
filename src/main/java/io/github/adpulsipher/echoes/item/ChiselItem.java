package io.github.adpulsipher.echoes.item;

import io.github.adpulsipher.echoes.component.EchoMemory;
import io.github.adpulsipher.echoes.projection.EchoLore;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import io.github.adpulsipher.echoes.registry.ModBlocks;
import io.github.adpulsipher.echoes.registry.ModComponents;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.registry.ModSounds;
import io.github.adpulsipher.echoes.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The Archaeologist's Chisel. Mining an echo deposit shatters the memory into shards; chiselling it free, slowly
 * and carefully, preserves it whole as an echo block.
 */
public class ChiselItem extends Item {
	/** Ticks of careful work needed to free an echo. */
	public static final int WORK_TICKS = 60;
	private static final int MAX_USE = 72000;

	public ChiselItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (player != null && state.is(ModTags.ECHO_DEPOSITS)) {
			player.startUsingItem(context.getHand());
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BRUSH;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return MAX_USE;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (!(entity instanceof Player player)) {
			entity.releaseUsingItem();
			return;
		}
		HitResult hit = player.pick(player.blockInteractionRange(), 0.0f, false);
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
			player.releaseUsingItem();
			return;
		}
		BlockPos pos = blockHit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!state.is(ModTags.ECHO_DEPOSITS)) {
			player.releaseUsingItem();
			return;
		}

		int elapsed = MAX_USE - remainingUseDuration;
		if (elapsed % 6 == 0) {
			level.playSound(player, pos, ModSounds.CHISEL_TAP, SoundSource.PLAYERS, 0.7f, 0.9f + level.getRandom().nextFloat() * 0.3f);
			if (level instanceof ServerLevel serverLevel) {
				var face = blockHit.getLocation();
				serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), face.x, face.y, face.z, 4, 0.1, 0.1, 0.1, 0.05);
				serverLevel.sendParticles(ParticleTypes.END_ROD, face.x, face.y, face.z, 1, 0.1, 0.1, 0.1, 0.02);
			}
		}

		if (elapsed >= WORK_TICKS && level instanceof ServerLevel serverLevel) {
			extract(serverLevel, pos, state, player, stack);
			player.releaseUsingItem();
		}
	}

	private void extract(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack chisel) {
		EchoMemory memory = EchoLore.memoryAt(level, pos);
		Block remainder = state.is(ModBlocks.DEEPSLATE_ECHO_DEPOSIT) ? Blocks.COBBLED_DEEPSLATE : Blocks.COBBLESTONE;
		level.setBlock(pos, remainder.defaultBlockState(), Block.UPDATE_ALL);

		ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
		echo.set(ModComponents.ECHO_MEMORY, memory);
		Block.popResource(level, pos, echo);

		level.playSound(null, pos, ModSounds.ECHO_EXTRACT, SoundSource.PLAYERS, 1.0f, 1.0f);
		level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.35, 0.35, 0.35, 0.06);
		chisel.hurtAndBreak(1, player, player.getUsedItemHand());
		if (player instanceof ServerPlayer serverPlayer) {
			ReplayOutcome.grant(serverPlayer, "careful_hands", "extracted");
		}
	}
}
