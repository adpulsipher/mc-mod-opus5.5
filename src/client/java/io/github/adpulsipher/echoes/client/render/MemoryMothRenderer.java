package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.client.model.MemoryMothModel;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.entity.MemoryMothEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class MemoryMothRenderer extends MobRenderer<MemoryMothEntity, GhostRenderState, MemoryMothModel> {
	private static final Identifier TEXTURE = EchoesOfThePast.id("textures/entity/memory_moth.png");

	public MemoryMothRenderer(EntityRendererProvider.Context context) {
		super(context, new MemoryMothModel(context.bakeLayer(ModModelLayers.MEMORY_MOTH)), 0.15f);
	}

	@Override
	public GhostRenderState createRenderState() {
		return new GhostRenderState();
	}

	@Override
	public void extractRenderState(MemoryMothEntity entity, GhostRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.attuned = entity.isAttuned();
		state.tint = state.attuned ? 0xFFFFFF : 0xE6F6FF;
		float age = entity.tickCount + partialTick;
		state.alpha = state.attuned ? 0.95f : 0.8f + 0.1f * Mth.sin(age * 0.2f);
	}

	@Override
	public Identifier getTextureLocation(GhostRenderState state) {
		return TEXTURE;
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		float death = state.deathTime > 0 ? Mth.clamp(state.deathTime / 10.0f, 0.0f, 1.0f) : 0.0f;
		Hologram.submit(this.model, state, TEXTURE, poseStack, collector, 1.0f, death, Hologram.FULL_BRIGHT);
	}
}
