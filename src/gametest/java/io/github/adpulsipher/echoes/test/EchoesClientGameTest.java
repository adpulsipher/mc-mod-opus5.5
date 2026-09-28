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
		}
	}
}
