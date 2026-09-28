package io.github.adpulsipher.echoes.client.model;

import io.github.adpulsipher.echoes.client.render.GhostRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Shard Crawler: a squat six-legged beetle with echo crystal growing through its shell.
 */
public class CrawlerModel extends EntityModel<GhostRenderState> {
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart[] legs = new ModelPart[6];

	public CrawlerModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = body.getChild("head");
		for (int i = 0; i < 6; i++) {
			legs[i] = root.getChild("leg_" + i);
		}
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4, -2, -6, 8, 4, 12)
						.texOffs(40, 8).addBox(-2.5f, -6, -3, 2, 4, 2)
						.texOffs(48, 8).addBox(0.5f, -5, 0, 2, 3, 2)
						.texOffs(56, 8).addBox(-1, -7.5f, 2, 2, 6, 2),
				PartPose.offset(0, 19.5f, 0));
		PartDefinition head = body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(40, 0).addBox(-3, -1.5f, -4, 6, 4, 4),
				PartPose.offset(0, 0, -6));
		head.addOrReplaceChild("mandibles",
				CubeListBuilder.create()
						.texOffs(0, 24).addBox(-2.5f, 1.5f, -6, 1, 1, 3)
						.texOffs(0, 24).addBox(1.5f, 1.5f, -6, 1, 1, 3),
				PartPose.ZERO);
		for (int i = 0; i < 6; i++) {
			boolean right = i < 3;
			float z = -4 + (i % 3) * 4;
			root.addOrReplaceChild("leg_" + i,
					CubeListBuilder.create().texOffs(0, 16).addBox(-0.5f, 0, -0.5f, 1, 6, 1),
					PartPose.offsetAndRotation(right ? -4 : 4, 20, z, 0, 0, right ? 0.85f : -0.85f));
		}
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float walk = state.stride * 1.3f;
		float amount = Math.min(1.0f, state.strideAmount * 1.5f);
		for (int i = 0; i < 6; i++) {
			float phase = (i % 2 == 0) ? 0.0f : Mth.PI;
			legs[i].yRot = Mth.cos(walk + phase + (i % 3)) * 0.5f * amount;
			float base = i < 3 ? 0.85f : -0.85f;
			legs[i].zRot = base + (i < 3 ? 1 : -1) * Mth.abs(Mth.sin(walk + phase)) * 0.3f * amount;
		}
		head.yRot = Mth.clamp(state.yRot, -30.0f, 30.0f) * Mth.DEG_TO_RAD;
		head.xRot = Mth.sin(state.ageInTicks * 0.1f) * 0.05f;
		body.y = 19.5f + Mth.abs(Mth.sin(walk)) * 0.4f * amount;
	}
}
