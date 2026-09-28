package io.github.adpulsipher.echoes.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

/**
 * Stages a few scenes in a fresh world and takes screenshots of them, so the art and the holograms can be
 * checked visually in CI.
 */
@SuppressWarnings("UnstableApiUsage")
public class EchoesClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			TestServerContext server = singleplayer.getServer();
			BlockPos p = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().blockPosition());
			int x = p.getX();
			int y = p.getY() + 30;
			int z = p.getZ();

			// A floating stage so the terrain never gets in the way.
			server.runCommand("gamemode creative @a");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand(String.format("fill %d %d %d %d %d %d minecraft:grass_block", x - 14, y - 1, z - 14, x + 14, y - 1, z + 14));
			server.runCommand(String.format("fill %d %d %d %d %d %d minecraft:air", x - 14, y, z - 14, x + 14, y + 12, z + 14));

			// Showcase row of blocks
			String[] blocks = {"echo_deposit", "deepslate_echo_deposit", "echo_crystal_block", "echo_projector", "echo_lantern", "relic_cache_soil", "relic_cache_stone"};
			for (int i = 0; i < blocks.length; i++) {
				server.runCommand(String.format("setblock %d %d %d echoes_of_the_past:%s", x - 6 + i * 2, y, z - 4, blocks[i]));
			}
			server.runCommand(String.format("summon echoes_of_the_past:lingerer %d %d %d {NoAI:1b,Rotation:[180f,0f]}", x - 4, y, z - 7));
			server.runCommand(String.format("summon echoes_of_the_past:memory_moth %d %d %d {NoAI:1b}", x + 1, y + 2, z - 6));
			server.runCommand(String.format("summon echoes_of_the_past:echo_wyrm %d %d %d {NoAI:1b,Rotation:[210f,0f]}", x + 6, y + 3, z - 10));

			server.runCommand(String.format("tp @a %d %d %d 180 20", x, y + 2, z + 3));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("echoes-showcase");

			// A replay at the projector, seen at dusk so the ghosts glow.
			server.runCommand("kill @e[type=echoes_of_the_past:echo_wyrm]");
			server.runCommand("time set 13000");
			server.runCommand(String.format("setblock %d %d %d echoes_of_the_past:echo_projector", x, y, z + 12));
			server.runCommand(String.format("echoes replay %d %d %d crowns", x, y, z + 12));
			server.runCommand(String.format("tp @a %d %d %d 0 25", x, y + 4, z + 1));
			context.waitTicks(150);
			context.takeScreenshot("echoes-replay-1");
			context.waitTicks(250);
			context.takeScreenshot("echoes-replay-2");
			context.waitTicks(200);
			context.takeScreenshot("echoes-replay-3");

			// A dragon-era memory elsewhere on the stage.
			server.runCommand(String.format("setblock %d %d %d echoes_of_the_past:echo_projector", x - 10, y, z));
			server.runCommand(String.format("echoes replay %d %d %d dragons", x - 10, y, z));
			server.runCommand(String.format("tp @a %d %d %d 90 15", x + 2, y + 3, z));
			context.waitTicks(260);
			context.takeScreenshot("echoes-replay-dragons");

			// The expansion: showcase structures and the great echoes, built by the Codex of Echoes.
			server.runCommand("time set noon");
			scene(context, singleplayer, server, x + 120, y, z, "structure bestiary", 0, 12, 4, 30, 80, "echoes-bestiary");
			scene(context, singleplayer, server, x + 220, y, z, "structure armory", 0, 3, 6, 15, 60, "echoes-armory");
			scene(context, singleplayer, server, x + 320, y, z, "structure dig_site", 0, 7, 2, 35, 60, "echoes-dig-site");
			server.runCommand("time set 12800");
			scene(context, singleplayer, server, x + 420, y, z, "arena hollow_king", 0, 5, 0, 12, 180, "echoes-hollow-king");
			scene(context, singleplayer, server, x + 520, y, z, "arena siege_colossus", 0, 7, 4, 12, 180, "echoes-siege-colossus");
			scene(context, singleplayer, server, x + 620, y, z, "arena hierophant", 0, 5, 3, 10, 180, "echoes-hierophant");
			scene(context, singleplayer, server, x + 720, y, z, "arena echo_wyrm", 0, 8, 3, 15, 180, "echoes-wyrm-roost");

			// The two books.
			server.runOnServer(s -> {
				net.minecraft.server.level.ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
				player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(io.github.adpulsipher.echoes.registry.ModItems.GUIDE_BOOK));
				player.openItemGui(player.getMainHandItem(), net.minecraft.world.InteractionHand.MAIN_HAND);
			});
			context.waitTicks(20);
			context.takeScreenshot("echoes-guide-book");
			context.setScreen(() -> new io.github.adpulsipher.echoes.client.screen.ShowcaseScreen());
			context.waitTicks(20);
			context.takeScreenshot("echoes-codex");
			context.setScreen(() -> null);
		}
	}

	/**
	 * Stands the player on a pillar at (x, y, z) facing north, runs a Codex entry, then moves the camera up and back
	 * to look at what it made.
	 */
	private static void scene(ClientGameTestContext context, TestSingleplayerContext singleplayer, TestServerContext server, int x, int y, int z,
			String entry, int camX, int camY, int camZ, int pitch, int wait, String name) {
		server.runCommand(String.format("tp @a %d %d %d 180 0", x, y + 1, z));
		server.runCommand(String.format("setblock %d %d %d minecraft:glass", x, y, z));
		context.waitTicks(20);
		singleplayer.getConnection().waitForChunksRender();
		server.runCommand(String.format("tp @a %d %d %d 180 0", x, y + 1, z));
		server.runCommand("execute as @p at @p run echoes showcase " + entry);
		server.runCommand(String.format("tp @a %d %d %d 180 %d", x + camX, y + 1 + camY, z + camZ, pitch));
		context.waitTicks(wait);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}
}
