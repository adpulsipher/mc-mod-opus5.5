package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Draws a creature with its own model in the shared hologram style. Ghostly creatures glow at full brightness;
 * solid ones (like the Shard Crawler) take the light of the world around them.
 */
public class ModelGhostRenderer<T extends Mob> extends MobRenderer<T, GhostRenderState, EntityModel<GhostRenderState>> {
	private final Identifier texture;
	private final float scale;
	private final float baseAlpha;
	private final boolean glowing;
	private final HumanoidGhostRenderer.Styler<T> styler;

	public ModelGhostRenderer(EntityRendererProvider.Context context, EntityModel<GhostRenderState> model, float shadow, Identifier texture, float scale,
			float baseAlpha, boolean glowing, HumanoidGhostRenderer.Styler<T> styler) {
		super(context, model, shadow);
		this.texture = texture;
		this.scale = scale;
		this.baseAlpha = baseAlpha;
		this.glowing = glowing;
		this.styler = styler;
	}

	@Override
	public GhostRenderState createRenderState() {
		return new GhostRenderState();
	}

	@Override
	public void extractRenderState(T entity, GhostRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.stride = entity.walkAnimation.position(partialTick);
		state.strideAmount = Math.min(1.0f, entity.walkAnimation.speed(partialTick));
		state.tint = 0xFFFFFF;
		float age = entity.tickCount + partialTick;
		state.alpha = glowing ? baseAlpha * Hologram.flicker(age, entity.getId()) : baseAlpha;
		styler.style(entity, state, partialTick);
	}

	@Override
	public Identifier getTextureLocation(GhostRenderState state) {
		return texture;
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		float death = 0.0f;
		if (state.deathTime > 0) {
			state.alpha *= Mth.clamp(1.0f - state.deathTime / 20.0f, 0.0f, 1.0f);
			death = glowing ? 0.0f : Mth.clamp(state.deathTime / 10.0f, 0.0f, 1.0f);
		}
		Hologram.submit(this.model, state, texture, poseStack, collector, scale, death, glowing ? Hologram.FULL_BRIGHT : state.lightCoords);
	}
}
