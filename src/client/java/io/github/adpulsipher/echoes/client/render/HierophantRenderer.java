package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.client.model.WispModel;
import io.github.adpulsipher.echoes.entity.HierophantEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The Hierophant is drawn as a tall robed figure; while its wards hold, small suns of dawn light circle it.
 */
public class HierophantRenderer extends HumanoidGhostRenderer<HierophantEntity> {
	private static final Identifier WARD_TEXTURE = EchoesOfThePast.id("textures/entity/dawn_wisp.png");
	private final WispModel ward;

	public HierophantRenderer(EntityRendererProvider.Context context, Identifier texture, float scale, Styler<HierophantEntity> styler) {
		super(context, texture, scale, 0.9f, styler);
		this.ward = new WispModel(context.bakeLayer(ModModelLayers.DAWN_WISP));
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.0f, 0.25f + Mth.sin(state.ageInTicks * 0.08f) * 0.15f, 0.0f);
		super.submit(state, poseStack, collector, camera);
		poseStack.popPose();

		for (int i = 0; i < state.count; i++) {
			double angle = state.ageInTicks * 0.06 + i * Math.PI * 2 / Math.max(1, state.count);
			poseStack.pushPose();
			poseStack.translate(Math.cos(angle) * 1.6, 2.2 + Math.sin(state.ageInTicks * 0.1 + i) * 0.2, Math.sin(angle) * 1.6);
			poseStack.scale(-0.45f, -0.45f, 0.45f);
			poseStack.translate(0.0f, -1.2f, 0.0f);
			collector.submitModel(ward, state, poseStack, RenderTypes.entityTranslucent(WARD_TEXTURE), Hologram.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
					Hologram.argb(0.6f, 0xFFF0C0), (TextureAtlasSprite) null, 0);
			poseStack.popPose();
		}
	}
}
