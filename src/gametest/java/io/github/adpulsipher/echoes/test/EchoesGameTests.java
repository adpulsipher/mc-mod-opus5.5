package io.github.adpulsipher.echoes.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class EchoesGameTests {
	@GameTest
	public void modLoads(GameTestHelper helper) {
		helper.succeed();
	}
}
