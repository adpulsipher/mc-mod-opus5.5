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

public class MemoryMothModel extends EntityModel<GhostRenderState> {
	private final ModelPart body;
	private final ModelPart leftUpper;
	private final ModelPart leftLower;
	private final ModelPart rightUpper;
	private final ModelPart rightLower;
	private final ModelPart leftAntenna;
	private final ModelPart rightAntenna;

	public MemoryMothModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leftUpper = body.getChild("left_upper_wing");
		this.leftLower = body.getChild("left_lower_wing");
		this.rightUpper = body.getChild("right_upper_wing");
		this.rightLower = body.getChild("right_lower_wing");
		ModelPart head = body.getChild("head");
		this.leftAntenna = head.getChild("left_antenna");
		this.rightAntenna = head.getChild("right_antenna");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1, -1, -3, 2, 2, 6),
				PartPose.offset(0, 20, 0));
		PartDefinition head = body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(16, 0).addBox(-1, -1, -2, 2, 2, 2),
				PartPose.offset(0, -0.2f, -3));
		head.addOrReplaceChild("left_antenna",
				CubeListBuilder.create().texOffs(24, 0).addBox(0, -3, -2, 0, 3, 2),
				PartPose.offsetAndRotation(0.6f, -0.8f, -1, 0, 0, 0.3f));
		head.addOrReplaceChild("right_antenna",
				CubeListBuilder.create().texOffs(24, 0).mirror().addBox(0, -3, -2, 0, 3, 2),
				PartPose.offsetAndRotation(-0.6f, -0.8f, -1, 0, 0, -0.3f));
		body.addOrReplaceChild("left_upper_wing",
				CubeListBuilder.create().texOffs(0, 8).addBox(0, 0, -3, 7, 0, 6),
				PartPose.offset(1, -1, -1));
		body.addOrReplaceChild("left_lower_wing",
				CubeListBuilder.create().texOffs(0, 14).addBox(0, 0, -1, 5, 0, 5),
				PartPose.offset(1, -0.5f, 1));
		body.addOrReplaceChild("right_upper_wing",
				CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-7, 0, -3, 7, 0, 6),
				PartPose.offset(-1, -1, -1));
		body.addOrReplaceChild("right_lower_wing",
				CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-5, 0, -1, 5, 0, 5),
				PartPose.offset(-1, -0.5f, 1));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float flap = Mth.sin(t * 1.7f) * 0.95f;
		leftUpper.zRot = -0.25f - flap;
		leftLower.zRot = -0.15f - flap * 0.8f;
		rightUpper.zRot = 0.25f + flap;
		rightLower.zRot = 0.15f + flap * 0.8f;
		body.y = 20.0f + Mth.sin(t * 0.25f) * 1.2f;
		body.xRot = -0.15f + Mth.sin(t * 0.25f + 1.0f) * 0.08f;
		leftAntenna.xRot = Mth.sin(t * 0.3f) * 0.15f;
		rightAntenna.xRot = Mth.sin(t * 0.3f + 1.0f) * 0.15f;
	}
}
