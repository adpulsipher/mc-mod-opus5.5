package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.client.model.GhostHumanoidModel;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.replay.Pose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Draws any humanoid echo (knights, archers, revenants and the humanoid bosses) with the shared ghost model. Each
 * creature supplies its texture, its scale and a small function that chooses its tint, props and pose.
 */
public class HumanoidGhostRenderer<T extends Mob> extends MobRenderer<T, GhostRenderState, GhostHumanoidModel> {
	/** Fills in the creature-specific parts of the render state. */
	public interface Styler<T> {
		void style(T entity, GhostRenderState state, float partialTick);
	}

	private final Identifier texture;
	private final float scale;
	private final float baseAlpha;
	private final Styler<T> styler;

	public HumanoidGhostRenderer(EntityRendererProvider.Context context, Identifier texture, float scale, float baseAlpha, Styler<T> styler) {
		super(context, new GhostHumanoidModel(context.bakeLayer(ModModelLayers.GHOST_HUMANOID)), 0.4f * scale);
		this.texture = texture;
		this.scale = scale;
		this.baseAlpha = baseAlpha;
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
		state.pose = entity.isAggressive() && entity.getTarget() != null && entity.distanceToSqr(entity.getTarget()) < 9.0 ? Pose.ATTACK : Pose.IDLE;
		state.poseTime = entity.tickCount + partialTick;
		state.props = -1;
		float age = entity.tickCount + partialTick;
		state.alpha = baseAlpha * Hologram.flicker(age, entity.getId());
		styler.style(entity, state, partialTick);
	}

	@Override
	public Identifier getTextureLocation(GhostRenderState state) {
		return texture;
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		float death = state.deathTime > 0 ? Mth.clamp(Mth.sqrt((state.deathTime - 1.0f) / 20.0f * 1.6f), 0.0f, 1.0f) : 0.0f;
		if (state.deathTime > 0) {
			state.alpha *= Mth.clamp(1.0f - state.deathTime / 20.0f, 0.0f, 1.0f);
		}
		Hologram.submit(this.model, state, texture, poseStack, collector, scale, death, Hologram.FULL_BRIGHT);
	}
}
