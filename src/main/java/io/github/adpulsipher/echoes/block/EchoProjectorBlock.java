package io.github.adpulsipher.echoes.block;

import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import io.github.adpulsipher.echoes.registry.ModBlockEntities;
import io.github.adpulsipher.echoes.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Echo Projector: a brass and amethyst apparatus that draws the memory out of an echo block and casts it
 * across the land as a ghostly replay.
 */
public class EchoProjectorBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(1, 0, 1, 15, 3, 15),
			Block.box(3, 3, 3, 13, 9, 13),
			Block.box(4, 9, 4, 12, 14, 12)
	);

	public EchoProjectorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIVE);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EchoProjectorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide()
				? createTickerHelper(type, ModBlockEntities.ECHO_PROJECTOR, EchoProjectorBlockEntity::clientTick)
				: createTickerHelper(type, ModBlockEntities.ECHO_PROJECTOR, EchoProjectorBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.has(ModComponents.ECHO_MEMORY)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!(level.getBlockEntity(pos) instanceof EchoProjectorBlockEntity projector)) {
			return InteractionResult.PASS;
		}
		if (projector.hasEcho()) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("block.echoes_of_the_past.echo_projector.occupied"));
			}
			return InteractionResult.CONSUME;
		}
		if (level instanceof ServerLevel serverLevel && projector.insert(serverLevel, stack)) {
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof EchoProjectorBlockEntity projector)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (projector.isProjecting()) {
			player.sendOverlayMessage(Component.translatable("block.echoes_of_the_past.echo_projector.busy"));
		} else if (projector.hasEcho()) {
			if (player.isShiftKeyDown()) {
				ItemStack echo = projector.eject();
				player.getInventory().placeItemBackInInventory(echo);
			} else {
				projector.start((ServerLevel) level);
			}
		} else {
			player.sendOverlayMessage(Component.translatable("block.echoes_of_the_past.echo_projector.empty"));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.95;
		double z = pos.getZ() + 0.5;
		if (state.getValue(ACTIVE)) {
			for (int i = 0; i < 2; i++) {
				level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5) * 0.3, y, z + (random.nextDouble() - 0.5) * 0.3,
						0, 0.05 + random.nextDouble() * 0.05, 0);
			}
			level.addParticle(new DustParticleOptions(0x7FE9FF, 1.0f), x + (random.nextDouble() - 0.5), y + random.nextDouble() * 2, z + (random.nextDouble() - 0.5), 0, 0, 0);
		} else if (random.nextInt(5) == 0) {
			level.addParticle(new DustParticleOptions(0x9FD8FF, 0.5f), x + (random.nextDouble() - 0.5) * 0.4, y, z + (random.nextDouble() - 0.5) * 0.4, 0, 0.01, 0);
		}
	}
}
