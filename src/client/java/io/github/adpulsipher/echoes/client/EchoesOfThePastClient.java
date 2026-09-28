package io.github.adpulsipher.echoes.client;

import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.client.render.EchoFigureRenderer;
import io.github.adpulsipher.echoes.client.render.EchoProjectorRenderer;
import io.github.adpulsipher.echoes.client.render.EchoWyrmRenderer;
import io.github.adpulsipher.echoes.client.render.LingererRenderer;
import io.github.adpulsipher.echoes.client.render.MemoryMothRenderer;
import io.github.adpulsipher.echoes.registry.ModBlockEntities;
import io.github.adpulsipher.echoes.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class EchoesOfThePastClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModModelLayers.init();
		EntityRenderers.register(ModEntities.ECHO_FIGURE, EchoFigureRenderer::new);
		EntityRenderers.register(ModEntities.LINGERER, LingererRenderer::new);
		EntityRenderers.register(ModEntities.MEMORY_MOTH, MemoryMothRenderer::new);
		EntityRenderers.register(ModEntities.ECHO_WYRM, EchoWyrmRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.ECHO_PROJECTOR, EchoProjectorRenderer::new);
	}
}
