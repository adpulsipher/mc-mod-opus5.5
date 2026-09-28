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
 * The light-work above an Echo Projector: the floating echo crystal, two counter-rotating rings and the beam.
 * Rendered by the projector's block entity renderer, which toggles parts depending on its state.
 */
public class ProjectorHologramModel extends EntityModel<GhostRenderState> {
	public final ModelPart crystal;
	public final ModelPart innerRing;
	public final ModelPart outerRing;
	public final ModelPart beam;

	public ProjectorHologramModel(ModelPart root) {
		super(root);
		this.crystal = root.getChild("crystal");
		this.innerRing = root.getChild("inner_ring");
		this.outerRing = root.getChild("outer_ring");
		this.beam = root.getChild("beam");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("crystal",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2, -3, -2, 4, 6, 4)
						.texOffs(16, 0).addBox(-1, -4.5f, -1, 2, 9, 2),
				PartPose.ZERO);
		root.addOrReplaceChild("inner_ring",
				CubeListBuilder.create().texOffs(0, 16).addBox(-6, 0, -6, 12, 1, 1)
						.texOffs(0, 16).addBox(-6, 0, 5, 12, 1, 1)
						.texOffs(0, 18).addBox(-6, 0, -5, 1, 1, 10)
						.texOffs(0, 18).addBox(5, 0, -5, 1, 1, 10),
				PartPose.ZERO);
		root.addOrReplaceChild("outer_ring",
				CubeListBuilder.create().texOffs(0, 30).addBox(-9, 0, -9, 18, 1, 1)
						.texOffs(0, 30).addBox(-9, 0, 8, 18, 1, 1)
						.texOffs(0, 32).addBox(-9, 0, -8, 1, 1, 16)
						.texOffs(0, 32).addBox(8, 0, -8, 1, 1, 16),
				PartPose.ZERO);
		root.addOrReplaceChild("beam",
				CubeListBuilder.create().texOffs(48, 0).addBox(-1.5f, -48, -1.5f, 3, 48, 3),
				PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		crystal.visible = state.showCrystal;
		innerRing.visible = state.showBeam;
		outerRing.visible = state.showBeam;
		beam.visible = state.showBeam;
		crystal.yRot = t * 0.05f;
		crystal.xRot = Mth.sin(t * 0.03f) * 0.2f;
		crystal.y = Mth.sin(t * 0.08f) * 1.0f;
		innerRing.yRot = t * 0.08f;
		innerRing.xRot = 0.35f + Mth.sin(t * 0.04f) * 0.15f;
		outerRing.yRot = -t * 0.05f;
		outerRing.zRot = 0.25f + Mth.cos(t * 0.05f) * 0.15f;
		beam.yRot = t * 0.1f;
	}
}
