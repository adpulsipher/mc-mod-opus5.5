package io.github.adpulsipher.echoes.client.model;

import io.github.adpulsipher.echoes.EchoesOfThePast;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ModModelLayers {
	public static final ModelLayerLocation GHOST_HUMANOID = new ModelLayerLocation(EchoesOfThePast.id("ghost_humanoid"), "main");
	public static final ModelLayerLocation ECHO_WYRM = new ModelLayerLocation(EchoesOfThePast.id("echo_wyrm"), "main");
	public static final ModelLayerLocation MEMORY_MOTH = new ModelLayerLocation(EchoesOfThePast.id("memory_moth"), "main");
	public static final ModelLayerLocation SIEGE_COLOSSUS = new ModelLayerLocation(EchoesOfThePast.id("siege_colossus"), "main");
	public static final ModelLayerLocation DAWN_WISP = new ModelLayerLocation(EchoesOfThePast.id("dawn_wisp"), "main");
	public static final ModelLayerLocation SHARD_CRAWLER = new ModelLayerLocation(EchoesOfThePast.id("shard_crawler"), "main");
	public static final ModelLayerLocation PROJECTOR_HOLOGRAM = new ModelLayerLocation(EchoesOfThePast.id("projector_hologram"), "main");

	private ModModelLayers() {
	}

	public static void init() {
		ModelLayerRegistry.registerModelLayer(GHOST_HUMANOID, GhostHumanoidModel::createLayer);
		ModelLayerRegistry.registerModelLayer(ECHO_WYRM, EchoWyrmModel::createLayer);
		ModelLayerRegistry.registerModelLayer(MEMORY_MOTH, MemoryMothModel::createLayer);
		ModelLayerRegistry.registerModelLayer(SIEGE_COLOSSUS, ColossusModel::createLayer);
		ModelLayerRegistry.registerModelLayer(DAWN_WISP, WispModel::createLayer);
		ModelLayerRegistry.registerModelLayer(SHARD_CRAWLER, CrawlerModel::createLayer);
		ModelLayerRegistry.registerModelLayer(PROJECTOR_HOLOGRAM, ProjectorHologramModel::createLayer);
	}
}
