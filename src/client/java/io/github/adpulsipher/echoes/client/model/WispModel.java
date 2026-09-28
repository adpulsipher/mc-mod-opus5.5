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
 * A Dawn Wisp: a bright core inside two slowly turning rings of light.
 */
public class WispModel extends EntityModel<GhostRenderState> {
	private final ModelPart core;
	private final ModelPart inner;
	private final ModelPart ringA;
	private final ModelPart ringB;

	public WispModel(ModelPart root) {
		super(root);
		this.core = root.getChild("core");
		this.inner = core.getChild("inner");
		this.ringA = core.getChild("ring_a");
		this.ringB = core.getChild("ring_b");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition core = root.addOrReplaceChild("core",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2, -2, -2, 4, 4, 4),
				PartPose.offset(0, 19, 0));
		core.addOrReplaceChild("inner",
				CubeListBuilder.create().texOffs(16, 0).addBox(-3, -3, -3, 6, 6, 6),
				PartPose.ZERO);
		core.addOrReplaceChild("ring_a",
				CubeListBuilder.create().texOffs(0, 12).addBox(-5, 0, -5, 10, 0, 10),
				PartPose.ZERO);
		core.addOrReplaceChild("ring_b",
				CubeListBuilder.create().texOffs(0, 22).addBox(-6, 0, -6, 12, 0, 12),
				PartPose.rotation(Mth.HALF_PI, 0, 0));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		core.y = 19.0f + Mth.sin(t * 0.12f) * 1.2f;
		core.yRot = t * 0.05f;
		inner.yRot = -t * 0.11f;
		inner.xRot = t * 0.07f;
		ringA.yRot = t * 0.09f;
		ringA.zRot = Mth.sin(t * 0.05f) * 0.4f;
		ringB.zRot = t * 0.06f;
	}
}
