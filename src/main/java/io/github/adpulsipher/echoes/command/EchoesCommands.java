package io.github.adpulsipher.echoes.command;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.adpulsipher.echoes.component.EchoMemory;
import io.github.adpulsipher.echoes.history.Era;
import io.github.adpulsipher.echoes.history.HistoricEvent;
import io.github.adpulsipher.echoes.history.HistoryGenerator;
import io.github.adpulsipher.echoes.history.Terrain;
import io.github.adpulsipher.echoes.projection.EchoLore;
import io.github.adpulsipher.echoes.registry.ModComponents;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.showcase.ShowcaseActions;
import io.github.adpulsipher.echoes.showcase.ShowcaseCatalog;
import io.github.adpulsipher.echoes.world.TerrainClassifier;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

/**
 * {@code /echoes history} tells anyone the remembered history of the chunk they stand in.
 * {@code /echoes give <era>} lets operators conjure an echo of this chunk from any era.
 */
public final class EchoesCommands {
	private EchoesCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("echoes")
						.then(Commands.literal("history").executes(EchoesCommands::history))
						.then(Commands.literal("give")
								.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
								.then(Commands.argument("era", StringArgumentType.word())
										.suggests((context, builder) -> {
											for (Era era : Era.values()) {
												builder.suggest(era.name().toLowerCase(Locale.ROOT));
											}
											return builder.buildFuture();
										})
										.executes(EchoesCommands::give)))
						.then(Commands.literal("replay")
								.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
								.then(Commands.argument("pos", BlockPosArgument.blockPos())
										.then(Commands.argument("era", StringArgumentType.word())
												.suggests((context, builder) -> {
													for (Era era : Era.values()) {
														builder.suggest(era.name().toLowerCase(Locale.ROOT));
													}
													return builder.buildFuture();
												})
												.executes(EchoesCommands::replay))))
						.then(Commands.literal("showcase")
								.then(Commands.argument("category", StringArgumentType.word())
										.suggests((context, builder) -> {
											ShowcaseCatalog.CATEGORIES.keySet().forEach(builder::suggest);
											return builder.buildFuture();
										})
										.then(Commands.argument("id", StringArgumentType.word())
												.suggests((context, builder) -> {
													String category = StringArgumentType.getString(context, "category");
													ShowcaseCatalog.CATEGORIES.getOrDefault(category, java.util.List.of()).forEach(builder::suggest);
													return builder.buildFuture();
												})
												.executes(EchoesCommands::showcase))))
		));
	}

	/** Runs an entry of the Codex of Echoes. Open to operators and to anyone in creative mode. */
	private static int showcase(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		if (!player.isCreative() && !source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			source.sendFailure(Component.translatable("showcase.echoes_of_the_past.creative_only"));
			return 0;
		}
		String category = StringArgumentType.getString(context, "category");
		String id = StringArgumentType.getString(context, "id");
		Component result = ShowcaseActions.run(player, category, id);
		if (result == null) {
			source.sendFailure(Component.translatable("showcase.echoes_of_the_past.unknown", category + " " + id));
			return 0;
		}
		source.sendSuccess(() -> result, false);
		return 1;
	}

	/** Starts the replay of this place's memory from the given era in the projector at {@code pos}. */
	private static int replay(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
		Era era;
		try {
			era = Era.valueOf(StringArgumentType.getString(context, "era").toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.translatable("command.echoes_of_the_past.give.unknown_era", StringArgumentType.getString(context, "era")));
			return 0;
		}
		if (!(level.getBlockEntity(pos) instanceof EchoProjectorBlockEntity projector) || projector.hasEcho()) {
			source.sendFailure(Component.translatable("command.echoes_of_the_past.replay.no_projector"));
			return 0;
		}
		EchoMemory memory = EchoLore.memoryAt(level, pos.atY(era.representativeY()));
		ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
		echo.set(ModComponents.ECHO_MEMORY, memory);
		projector.insert(level, echo);
		source.sendSuccess(() -> Component.translatable("command.echoes_of_the_past.give.success", memory.title()), true);
		return 1;
	}

	private static int history(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		BlockPos pos = BlockPos.containing(source.getPosition());
		ChunkPos chunk = ChunkPos.containing(pos);
		Terrain terrain = TerrainClassifier.classifyChunk(level, chunk);
		source.sendSuccess(() -> Component.translatable("command.echoes_of_the_past.history.header", chunk.x(), chunk.z())
				.withStyle(Style.EMPTY.withColor(0x7FE9FF).withBold(true)), false);
		for (HistoricEvent event : HistoryGenerator.chunkHistory(level.getSeed(), chunk.x(), chunk.z(), terrain)) {
			Component line = Component.literal("  " + event.era().title() + ": ").withStyle(Style.EMPTY.withColor(event.era().tint()))
					.append(Component.literal(event.title()).withStyle(Style.EMPTY.withColor(0xFFFFFF)))
					.append(Component.literal("  (" + event.yearsAgo() + " years ago)").withStyle(Style.EMPTY.withColor(0x8888A0).withItalic(true)));
			source.sendSuccess(() -> line, false);
		}
		return 1;
	}

	private static int give(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();
		String eraName = StringArgumentType.getString(context, "era").toUpperCase(Locale.ROOT);
		Era era;
		try {
			era = Era.valueOf(eraName);
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.translatable("command.echoes_of_the_past.give.unknown_era", eraName));
			return 0;
		}
		BlockPos pos = player.blockPosition();
		BlockPos formed = new BlockPos(pos.getX(), era.representativeY(), pos.getZ());
		EchoMemory memory = EchoLore.memoryAt(source.getLevel(), formed);
		ItemStack echo = new ItemStack(ModItems.ECHO_BLOCK);
		echo.set(ModComponents.ECHO_MEMORY, memory);
		io.github.adpulsipher.echoes.Gifts.give(player, echo);
		source.sendSuccess(() -> Component.translatable("command.echoes_of_the_past.give.success", memory.title()), true);
		return 1;
	}
}
