package io.github.adpulsipher.echoes.client.model;

import io.github.adpulsipher.echoes.client.render.GhostRenderState;
import io.github.adpulsipher.echoes.entity.SiegeColossusEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Siege Colossus: a hulking war engine with a furnace in its chest, fists like battering rams and a ruined
 * battlement still standing on its back.
 */
public class ColossusModel extends EntityModel<GhostRenderState> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public ColossusModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = body.getChild("head");
		this.rightArm = body.getChild("right_arm");
		this.leftArm = body.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-12, -22, -7, 24, 18, 14)
						.texOffs(0, 32).addBox(-10, -4, -6, 20, 6, 12)
						.texOffs(76, 0).addBox(-5, -17, -8, 10, 8, 1)
						.texOffs(0, 50).addBox(-8, -30, 4, 16, 10, 8)
						.texOffs(48, 50).addBox(-8, -33, 4, 4, 3, 8)
						.texOffs(48, 50).addBox(-2, -33, 4, 4, 3, 8)
						.texOffs(48, 50).addBox(4, -33, 4, 4, 3, 8),
				PartPose.offset(0, 6, 0));
		body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(64, 32).addBox(-5, -8, -5, 10, 9, 10),
				PartPose.offset(0, -22, -3));
		body.addOrReplaceChild("right_arm",
				CubeListBuilder.create()
						.texOffs(0, 68).addBox(-6, -2, -5, 10, 24, 10)
						.texOffs(40, 68).addBox(-7, 22, -6, 12, 8, 12),
				PartPose.offset(-16, -18, 0));
		body.addOrReplaceChild("left_arm",
				CubeListBuilder.create()
						.texOffs(0, 68).mirror().addBox(-4, -2, -5, 10, 24, 10)
						.texOffs(40, 68).mirror().addBox(-5, 22, -6, 12, 8, 12),
				PartPose.offset(16, -18, 0));
		root.addOrReplaceChild("right_leg",
				CubeListBuilder.create().texOffs(88, 68).addBox(-4, 0, -4, 8, 18, 8),
				PartPose.offset(-6, 6, 0));
		root.addOrReplaceChild("left_leg",
				CubeListBuilder.create().texOffs(88, 68).mirror().addBox(-4, 0, -4, 8, 18, 8),
				PartPose.offset(6, 6, 0));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float a = state.attackTime;

		head.yRot = Mth.clamp(state.yRot, -40.0f, 40.0f) * Mth.DEG_TO_RAD;
		head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5f;

		float swing = Mth.cos(state.stride * 0.35f) * 0.55f * state.strideAmount;
		rightLeg.xRot = swing;
		leftLeg.xRot = -swing;
		rightArm.xRot = -swing * 0.5f;
		leftArm.xRot = swing * 0.5f;
		rightArm.zRot = 0.08f + Mth.sin(t * 0.05f) * 0.03f;
		leftArm.zRot = -0.08f - Mth.sin(t * 0.05f) * 0.03f;
		body.y = 6.0f + Mth.sin(t * 0.08f) * 0.4f;
		body.zRot = Mth.sin(state.stride * 0.35f) * 0.04f * state.strideAmount;

		switch (state.attackState) {
			case SiegeColossusEntity.SLAM -> {
				float raise;
				if (a < 24) {
					raise = Mth.clamp(a / 20.0f, 0.0f, 1.0f);
				} else {
					raise = Mth.clamp(1.0f - (a - 24) / 3.0f, -0.15f, 1.0f);
				}
				rightArm.xRot = -2.9f * raise + (raise < 0.1f ? -0.5f : 0.0f);
				leftArm.xRot = -2.9f * raise + (raise < 0.1f ? -0.5f : 0.0f);
				rightArm.zRot = -0.2f * raise;
				leftArm.zRot = 0.2f * raise;
				body.xRot = a >= 24 ? 0.35f * Mth.clamp(1.0f - (a - 28) / 12.0f, 0.0f, 1.0f) : -0.15f * raise;
			}
			case SiegeColossusEntity.BARRAGE -> {
				float raise = Mth.clamp(a / 12.0f, 0.0f, 1.0f);
				rightArm.xRot = -2.5f * raise;
				leftArm.xRot = -2.5f * raise;
				rightArm.zRot = 0.4f * raise;
				leftArm.zRot = -0.4f * raise;
				head.xRot = -0.4f * raise;
				body.xRot = -0.1f * raise;
			}
			case SiegeColossusEntity.STOMP -> {
				float lift = a < 10 ? a / 10.0f : Mth.clamp(1.0f - (a - 10) / 2.0f, 0.0f, 1.0f);
				rightLeg.xRot = -0.9f * lift;
				body.xRot = -0.1f * lift;
			}
			default -> {
			}
		}
	}
}
