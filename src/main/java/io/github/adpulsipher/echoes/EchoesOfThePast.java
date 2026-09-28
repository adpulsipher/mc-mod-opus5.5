package io.github.adpulsipher.echoes;

import io.github.adpulsipher.echoes.command.EchoesCommands;
import io.github.adpulsipher.echoes.registry.ModAttachments;
import io.github.adpulsipher.echoes.registry.ModBlockEntities;
import io.github.adpulsipher.echoes.registry.ModBlocks;
import io.github.adpulsipher.echoes.registry.ModComponents;
import io.github.adpulsipher.echoes.registry.ModCreativeTab;
import io.github.adpulsipher.echoes.registry.ModEntities;
import io.github.adpulsipher.echoes.registry.ModItems;
import io.github.adpulsipher.echoes.registry.ModSounds;
import io.github.adpulsipher.echoes.world.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoesOfThePast implements ModInitializer {
	public static final String MOD_ID = "echoes_of_the_past";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModSounds.init();
		ModComponents.init();
		ModAttachments.init();
		ModEntities.init();
		ModBlocks.init();
		ModItems.init();
		ModBlockEntities.init();
		ModCreativeTab.init();
		ModWorldgen.init();
		EchoesCommands.init();
		EchoStrikes.init();
		LOGGER.info("The echoes stir.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
