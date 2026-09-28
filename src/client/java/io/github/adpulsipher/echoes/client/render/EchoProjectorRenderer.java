package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.block.entity.EchoProjectorBlockEntity;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.client.model.ProjectorHologramModel;
import io.github.adpulsipher.echoes.history.Era;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Renders the floating echo crystal above the projector, and while projecting, the spinning rings and the
 * column of light that carries the memory out into the world.
 */
public class EchoProjectorRenderer implements BlockEntityRenderer<EchoProjectorBlockEntity, EchoProjectorRenderer.State> {
	private static final Identifier TEXTURE = EchoesOfThePast.id("textures/entity/projector_hologram.png");
	private final ProjectorHologramModel model;

	public EchoProjectorRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new ProjectorHologramModel(context.bakeLayer(ModModelLayers.PROJECTOR_HOLOGRAM));
	}

	public static class State extends BlockEntityRenderState {
		public final GhostRenderState hologram = new GhostRenderState();
		public boolean hasEcho;
		public boolean projecting;
		public float projectionTime;
		public int tint = 0x9FE8FF;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EchoProjectorBlockEntity projector, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(projector, state, partialTick, cameraPos, crumblingOverlay);
		state.hasEcho = projector.hasEcho();
		state.projecting = projector.getProjectionTicks() > 0;
		state.projectionTime = projector.getProjectionTicks() + partialTick;
		int era = projector.getEraIndex();
		state.tint = era >= 0 && era < Era.values().length ? Era.values()[era].tint() : 0x9FE8FF;
		long gameTime = projector.getLevel() != null ? projector.getLevel().getGameTime() : 0L;
		float time = (gameTime % 24000L) + partialTick;
		state.hologram.ageInTicks = state.projecting ? time * 3.0f : time;
		state.hologram.showCrystal = state.hasEcho;
		state.hologram.showBeam = state.projecting;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.hasEcho && !state.projecting) {
			return;
		}
		float rise = state.projecting ? Mth.clamp(state.projectionTime / 40.0f, 0.0f, 1.0f) : 0.0f;
		float alpha = state.projecting ? 0.55f + 0.2f * Mth.sin(state.projectionTime * 0.3f) : 0.75f;

		poseStack.pushPose();
		poseStack.translate(0.5f, 1.2f + rise * 0.6f, 0.5f);
		poseStack.scale(-1.0f, -1.0f, 1.0f);
		collector.submitModel(model, state.hologram, poseStack, RenderTypes.entityTranslucent(TEXTURE), Hologram.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, Hologram.argb(alpha, state.tint), (TextureAtlasSprite) null, 0);
		poseStack.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}
}
