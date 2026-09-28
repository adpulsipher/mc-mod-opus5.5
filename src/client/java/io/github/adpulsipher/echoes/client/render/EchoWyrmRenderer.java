package io.github.adpulsipher.echoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.client.model.EchoWyrmModel;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.entity.EchoWyrmEntity;
import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.Role;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class EchoWyrmRenderer extends MobRenderer<EchoWyrmEntity, GhostRenderState, EchoWyrmModel> {
	private static final Identifier TEXTURE = EchoesOfThePast.id("textures/entity/echo_wyrm.png");

	public EchoWyrmRenderer(EntityRendererProvider.Context context) {
		super(context, new EchoWyrmModel(context.bakeLayer(ModModelLayers.ECHO_WYRM)), 1.5f);
	}

	@Override
	public GhostRenderState createRenderState() {
		return new GhostRenderState();
	}

	@Override
	public void extractRenderState(EchoWyrmEntity entity, GhostRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.role = Role.DRAGON;
		EchoWyrmEntity.Phase phase = entity.getPhase();
		state.jawOpen = phase == EchoWyrmEntity.Phase.BREATH || phase == EchoWyrmEntity.Phase.ROAR;
		state.pose = Pose.FLY;
		state.tint = 0xE6C4FF;
		state.dying = state.deathTime > 0;
		float age = entity.tickCount + partialTick;
		state.alpha = 0.88f * Hologram.flicker(age, entity.getId());
	}

	@Override
	public Identifier getTextureLocation(GhostRenderState state) {
		return TEXTURE;
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.deathTime > 0) {
			state.alpha *= Mth.clamp(1.0f - state.deathTime / 20.0f, 0.0f, 1.0f);
		}
		poseStack.pushPose();
		poseStack.translate(0.0f, 0.4f, 0.0f);
		Hologram.submit(this.model, state, TEXTURE, poseStack, collector, 1.7f, 0.0f, Hologram.FULL_BRIGHT);
		poseStack.popPose();
	}
}
