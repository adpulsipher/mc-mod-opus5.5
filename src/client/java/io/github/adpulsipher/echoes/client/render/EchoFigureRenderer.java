package io.github.adpulsipher.echoes.client.render;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.adpulsipher.echoes.EchoesOfThePast;
import io.github.adpulsipher.echoes.client.model.EchoWyrmModel;
import io.github.adpulsipher.echoes.client.model.GhostHumanoidModel;
import io.github.adpulsipher.echoes.client.model.ModModelLayers;
import io.github.adpulsipher.echoes.entity.EchoFigureEntity;
import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.Role;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Draws the ghostly actors of a replay: translucent, self-lit and tinted by the faction they fought for.
 */
public class EchoFigureRenderer extends MobRenderer<EchoFigureEntity, GhostRenderState, GhostHumanoidModel> {
	private static final Map<Role, Identifier> TEXTURES = new EnumMap<>(Role.class);
	private static final Identifier DRAGON_TEXTURE = EchoesOfThePast.id("textures/entity/echo_wyrm.png");

	static {
		for (Role role : Role.values()) {
			TEXTURES.put(role, EchoesOfThePast.id("textures/entity/figure/" + role.textureName() + ".png"));
		}
	}

	private final EchoWyrmModel dragonModel;

	public EchoFigureRenderer(EntityRendererProvider.Context context) {
		super(context, new GhostHumanoidModel(context.bakeLayer(ModModelLayers.GHOST_HUMANOID)), 0.0f);
		this.dragonModel = new EchoWyrmModel(context.bakeLayer(ModModelLayers.ECHO_WYRM));
	}

	@Override
	public GhostRenderState createRenderState() {
		return new GhostRenderState();
	}

	@Override
	public void extractRenderState(EchoFigureEntity entity, GhostRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.role = entity.getRole();
		state.pose = entity.getFigurePose();
		state.poseTime = entity.tickCount - entity.getPoseStartTick() + partialTick;
		state.tint = entity.getTint();
		state.jawOpen = state.pose == Pose.BREATHE;
		state.dying = state.pose == Pose.DEAD;

		float age = entity.tickCount + partialTick;
		float materialize = Mth.clamp(age / 20.0f, 0.0f, 1.0f);
		float fade = entity.isFading() ? Mth.clamp(1.0f - (entity.getFadeTicks() + partialTick) / 30.0f, 0.0f, 1.0f) : 1.0f;
		float dead = state.pose == Pose.DEAD ? 0.6f : 1.0f;
		state.alpha = 0.7f * materialize * fade * dead * Hologram.flicker(age, entity.getId());
		state.dyingTime = state.pose == Pose.DEAD ? state.poseTime : 0.0f;
	}

	@Override
	public Identifier getTextureLocation(GhostRenderState state) {
		return state.role == Role.DRAGON ? DRAGON_TEXTURE : TEXTURES.get(state.role);
	}

	@Override
	public void submit(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		float topple = state.pose == Pose.DEAD ? Mth.clamp(state.dyingTime / 12.0f, 0.0f, 1.0f) : 0.0f;
		if (state.role == Role.DRAGON) {
			Hologram.submit(dragonModel, state, DRAGON_TEXTURE, poseStack, collector, 1.5f, topple * 0.3f, Hologram.FULL_BRIGHT);
			return;
		}
		float scale = state.role == Role.CHILD ? 0.65f : 1.0f;
		Hologram.submit(this.model, state, getTextureLocation(state), poseStack, collector, scale, topple, Hologram.FULL_BRIGHT);
	}
}
