package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.client.model.GhostHumanoidModel;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.entity.LingererEntity;
import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.Role;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class LingererRenderer extends MobRenderer<LingererEntity, GhostRenderState, GhostHumanoidModel> {
	private static final Identifier TEXTURE = EchoesOfThePast.id("textures/entity/lingerer.png");

	public LingererRenderer(EntityRendererProvider.Context context) {
		super(context, new GhostHumanoidModel(context.bakeLayer(ModModelLayers.GHOST_HUMANOID)), 0.4f);
	}

	@Override
	public GhostRenderState createRenderState() {
		return new GhostRenderState();
	}

	@Override
	public void extractRenderState(LingererEntity entity, GhostRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.role = Role.SOLDIER;
		state.phased = entity.isPhased();
		state.tint = state.phased ? 0x9FB8FF : 0xCFF4FF;
		boolean attacking = entity.isAggressive();
		state.pose = attacking && entity.getTarget() != null && entity.distanceToSqr(entity.getTarget()) < 9.0 ? Pose.ATTACK : Pose.IDLE;
		state.stride = entity.walkAnimation.position(partialTick);
		state.strideAmount = Math.min(1.0f, entity.walkAnimation.speed(partialTick));
		float age = entity.tickCount + partialTick;
		float base = state.phased ? 0.22f + 0.1f * Mth.sin(age * 0.8f) : 0.78f;
		state.alpha = base * Hologram.flicker(age, entity.getId());
	}

	@Override
	public Identifier getTextureLocation(GhostRenderState state) {
		return TEXTURE;
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		float death = state.deathTime > 0 ? Mth.clamp(Mth.sqrt((state.deathTime - 1.0f) / 20.0f * 1.6f), 0.0f, 1.0f) : 0.0f;
		if (state.deathTime > 0) {
			state.alpha *= Mth.clamp(1.0f - state.deathTime / 20.0f, 0.0f, 1.0f);
		}
		Hologram.submit(this.model, state, TEXTURE, poseStack, collector, 1.0f, death, Hologram.FULL_BRIGHT);
	}
}
