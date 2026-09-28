package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Shared helpers for drawing translucent, self-lit ghosts.
 */
public final class Hologram {
	/** Packed light coordinates for "fully lit", so ghosts glow even in darkness. */
	public static final int FULL_BRIGHT = 0xF000F0;

	private Hologram() {
	}

	public static int argb(float alpha, int rgb) {
		int a = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f);
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	/**
	 * A projector-style flicker: mostly steady, with brief dips like a failing lantern. Deterministic per entity so
	 * neighbouring ghosts don't flicker in unison.
	 */
	public static float flicker(float time, int seed) {
		float base = 0.92f + 0.08f * Mth.sin(time * 0.9f + seed);
		float glitch = Mth.sin(time * 0.37f + seed * 1.7f) * Mth.sin(time * 0.11f + seed * 0.3f);
		if (glitch > 0.93f) {
			base *= 0.45f;
		}
		return base;
	}

	/**
	 * Submits a ghost model with the standard living-entity transform, optionally toppled over by {@code deathProgress}.
	 */
	public static <S extends GhostRenderState> void submit(EntityModel<? super S> model, S state, Identifier texture, PoseStack poseStack,
			SubmitNodeCollector collector, float scale, float deathProgress, int light) {
		if (state.alpha <= 0.02f) {
			return;
		}
		poseStack.pushPose();
		poseStack.mulPose(new Matrix4f().rotationY((180.0f - state.bodyRot) * Mth.DEG_TO_RAD));
		if (deathProgress > 0.0f) {
			poseStack.mulPose(new Matrix4f().rotationZ(deathProgress * Mth.HALF_PI));
		}
		poseStack.scale(-scale, -scale, scale);
		poseStack.translate(0.0f, -1.501f, 0.0f);
		collector.submitModel(model, state, poseStack, RenderTypes.entityTranslucent(texture), light, OverlayTexture.NO_OVERLAY,
				argb(state.alpha, state.tint), (TextureAtlasSprite) null, 0);
		poseStack.popPose();
	}
}
