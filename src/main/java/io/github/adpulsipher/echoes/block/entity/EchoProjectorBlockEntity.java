package io.github.adpulsipher.echoes.block.entity;

import io.github.adpulsipher.echoes.block.EchoProjectorBlock;
import io.github.adpulsipher.echoes.component.EchoMemory;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.projection.EchoLore;
import io.github.adpulsipher.echoes.projection.ReplayDirector;
import io.github.adpulsipher.echoes.projection.ReplayOutcome;
import io.github.adpulsipher.echoes.registry.ModBlockEntities;
import io.github.adpulsipher.echoes.registry.ModComponents;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Holds an echo block and, while projecting, the director running its replay.
 */
public class EchoProjectorBlockEntity extends BlockEntity {
	private ItemStack echo = ItemStack.EMPTY;
	@Nullable
	private ReplayDirector director;
	/** Synced to clients for rendering: 0 when idle, otherwise ticks since the replay began. */
	private int projectionTicks;
	private int era = -1;

	public EchoProjectorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ECHO_PROJECTOR, pos, state);
	}

	public ItemStack getEcho() {
		return echo;
	}

	public boolean hasEcho() {
		return !echo.isEmpty();
	}

	public boolean isProjecting() {
		return director != null || projectionTicks > 0;
	}

	public int getProjectionTicks() {
		return projectionTicks;
	}

	/** Index of the era being replayed, for tinting on the client. -1 when idle. */
	public int getEraIndex() {
		return era;
	}

	/** Places an echo in the projector and begins its replay. */
	public boolean insert(ServerLevel level, ItemStack stack) {
		if (hasEcho() || !stack.has(ModComponents.ECHO_MEMORY)) {
			return false;
		}
		this.echo = stack.copyWithCount(1);
		level.playSound(null, worldPosition, ModSounds.PROJECTOR_INSERT, SoundSource.BLOCKS, 1.0f, 1.0f);
		start(level);
		return true;
	}

	public void start(ServerLevel level) {
		EchoMemory memory = echo.get(ModComponents.ECHO_MEMORY);
		if (memory == null || director != null) {
			return;
		}
		HistoricEvent event = EchoLore.eventOf(level, memory);
		boolean resonant = EchoLore.isResonant(level, worldPosition, memory);
		this.director = new ReplayDirector(level, worldPosition, event, resonant);
		this.projectionTicks = 1;
		this.era = event.era().ordinal();
		level.setBlock(worldPosition, getBlockState().setValue(EchoProjectorBlock.ACTIVE, true), Block.UPDATE_ALL);
		sync();
	}

	/** Returns the echo to the player, if the projector is idle. */
	public ItemStack eject() {
		if (director != null) {
			return ItemStack.EMPTY;
		}
		ItemStack out = echo;
		echo = ItemStack.EMPTY;
		sync();
		return out;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EchoProjectorBlockEntity projector) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (projector.director == null) {
			// A replay interrupted by the world unloading: settle back to idle, keeping the echo.
			if (state.getValue(EchoProjectorBlock.ACTIVE)) {
				level.setBlock(pos, state.setValue(EchoProjectorBlock.ACTIVE, false), Block.UPDATE_ALL);
				projector.projectionTicks = 0;
				projector.sync();
			}
			return;
		}
		ReplayDirector director = projector.director;
		projector.projectionTicks++;
		if (projector.projectionTicks % 60 == 0) {
			level.playSound(null, pos, ModSounds.PROJECTOR_HUM, SoundSource.BLOCKS, 0.5f, 1.0f);
		}
		if (director.advance()) {
			projector.finish(serverLevel);
		}
	}

	private void finish(ServerLevel level) {
		ReplayDirector finished = this.director;
		this.director = null;
		this.projectionTicks = 0;
		this.era = -1;
		if (finished != null) {
			ReplayOutcome.conclude(level, worldPosition, finished);
			if (finished.resonant()) {
				// The memory returns to the land it came from. Only dust remains.
				echo = ItemStack.EMPTY;
				Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
						new ItemStack(ModItems.ECHO_DUST, 2 + level.getRandom().nextInt(3)));
				level.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 30, 0.3, 0.6, 0.3, 0.05);
			}
		}
		BlockState state = level.getBlockState(worldPosition);
		if (state.hasProperty(EchoProjectorBlock.ACTIVE)) {
			level.setBlock(worldPosition, state.setValue(EchoProjectorBlock.ACTIVE, false), Block.UPDATE_ALL);
		}
		sync();
	}

	/** Called when the projector is broken. */
	public void shutdown(ServerLevel level) {
		if (director != null) {
			director.abort();
			director = null;
		}
		projectionTicks = 0;
		if (!echo.isEmpty()) {
			Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, echo);
			echo = ItemStack.EMPTY;
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level instanceof ServerLevel serverLevel) {
			shutdown(serverLevel);
		}
	}

	private void sync() {
		setChanged();
		if (level != null) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!echo.isEmpty()) {
			output.store("echo", ItemStack.CODEC, echo);
		}
		output.putInt("projection_ticks", projectionTicks);
		output.putInt("era", era);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.echo = input.read("echo", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		// A replay interrupted by unloading is not resumed on the server, but the client keeps its visual state.
		this.projectionTicks = input.getIntOr("projection_ticks", 0);
		this.era = input.getIntOr("era", -1);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (!level.isClientSide() && director == null) {
			projectionTicks = 0;
			era = -1;
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	/** Client-side animation tick. */
	public static void clientTick(Level level, BlockPos pos, BlockState state, EchoProjectorBlockEntity projector) {
		if (projector.projectionTicks > 0) {
			projector.projectionTicks++;
		}
	}
}
