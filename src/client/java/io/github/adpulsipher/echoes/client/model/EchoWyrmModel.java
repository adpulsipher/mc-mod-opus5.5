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
 * A serpentine dragon: long neck and tail, two-jointed wings with membranes, a hinged jaw. Used both for the
 * dragons seen in replays and for the Echo Wyrm itself.
 */
public class EchoWyrmModel extends EntityModel<GhostRenderState> {
	private final ModelPart body;
	private final ModelPart neck1;
	private final ModelPart neck2;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart leftWing;
	private final ModelPart leftWingTip;
	private final ModelPart rightWing;
	private final ModelPart rightWingTip;
	private final ModelPart[] tail = new ModelPart[4];
	private final ModelPart[] legs = new ModelPart[4];

	public EchoWyrmModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.neck1 = body.getChild("neck1");
		this.neck2 = neck1.getChild("neck2");
		this.head = neck2.getChild("head");
		this.jaw = head.getChild("jaw");
		this.leftWing = body.getChild("left_wing");
		this.leftWingTip = leftWing.getChild("left_wing_tip");
		this.rightWing = body.getChild("right_wing");
		this.rightWingTip = rightWing.getChild("right_wing_tip");
		ModelPart segment = body;
		for (int i = 0; i < tail.length; i++) {
			segment = segment.getChild("tail" + (i + 1));
			tail[i] = segment;
		}
		legs[0] = body.getChild("front_left_leg");
		legs[1] = body.getChild("front_right_leg");
		legs[2] = body.getChild("back_left_leg");
		legs[3] = body.getChild("back_right_leg");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-6, -5, -10, 12, 10, 20)
						.texOffs(64, 0).addBox(-1, -8, -8, 2, 3, 16),
				PartPose.offset(0, 10, 0));

		PartDefinition neck1 = body.addOrReplaceChild("neck1",
				CubeListBuilder.create().texOffs(0, 30).addBox(-3.5f, -3.5f, -7, 7, 7, 7),
				PartPose.offsetAndRotation(0, -1, -10, -0.25f, 0, 0));
		PartDefinition neck2 = neck1.addOrReplaceChild("neck2",
				CubeListBuilder.create().texOffs(28, 30).addBox(-3, -3, -6, 6, 6, 6),
				PartPose.offsetAndRotation(0, 0, -7, -0.15f, 0, 0));
		PartDefinition head = neck2.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 44).addBox(-4, -4, -8, 8, 6, 8)
						.texOffs(32, 44).addBox(-2.5f, -2, -14, 5, 3, 6)
						.texOffs(54, 44).addBox(-3, -7, -3, 1, 4, 1)
						.texOffs(54, 44).mirror().addBox(2, -7, -3, 1, 4, 1),
				PartPose.offsetAndRotation(0, 0, -6, 0.35f, 0, 0));
		head.addOrReplaceChild("jaw",
				CubeListBuilder.create().texOffs(0, 58).addBox(-2.5f, 0, -10, 5, 2, 10),
				PartPose.offset(0, 1.5f, -4));

		PartDefinition leftWing = body.addOrReplaceChild("left_wing",
				CubeListBuilder.create().texOffs(64, 20).addBox(0, -1, -1, 18, 2, 2)
						.texOffs(0, 72).addBox(0, 0, 1, 18, 0, 14),
				PartPose.offset(6, -3, -4));
		leftWing.addOrReplaceChild("left_wing_tip",
				CubeListBuilder.create().texOffs(64, 24).addBox(0, -0.5f, -0.5f, 16, 1, 1)
						.texOffs(0, 86).addBox(0, 0, 0.5f, 16, 0, 12),
				PartPose.offset(18, 0, 0));
		PartDefinition rightWing = body.addOrReplaceChild("right_wing",
				CubeListBuilder.create().mirror().texOffs(64, 20).addBox(-18, -1, -1, 18, 2, 2)
						.texOffs(0, 72).addBox(-18, 0, 1, 18, 0, 14),
				PartPose.offset(-6, -3, -4));
		rightWing.addOrReplaceChild("right_wing_tip",
				CubeListBuilder.create().mirror().texOffs(64, 24).addBox(-16, -0.5f, -0.5f, 16, 1, 1)
						.texOffs(0, 86).addBox(-16, 0, 0.5f, 16, 0, 12),
				PartPose.offset(-18, 0, 0));

		PartDefinition tail1 = body.addOrReplaceChild("tail1",
				CubeListBuilder.create().texOffs(64, 30).addBox(-3, -3, 0, 6, 6, 8),
				PartPose.offset(0, -1, 10));
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2",
				CubeListBuilder.create().texOffs(92, 30).addBox(-2.5f, -2.5f, 0, 5, 5, 8),
				PartPose.offset(0, 0, 8));
		PartDefinition tail3 = tail2.addOrReplaceChild("tail3",
				CubeListBuilder.create().texOffs(64, 44).addBox(-2, -2, 0, 4, 4, 8),
				PartPose.offset(0, 0, 8));
		tail3.addOrReplaceChild("tail4",
				CubeListBuilder.create().texOffs(88, 44).addBox(-1.5f, -1.5f, 0, 3, 3, 8)
						.texOffs(110, 44).addBox(-0.5f, -4.5f, 2, 1, 3, 6),
				PartPose.offset(0, 0, 8));

		body.addOrReplaceChild("front_left_leg", CubeListBuilder.create().texOffs(64, 56).addBox(-1.5f, 0, -1.5f, 3, 7, 3), PartPose.offset(5, 4, -6));
		body.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(64, 56).mirror().addBox(-1.5f, 0, -1.5f, 3, 7, 3), PartPose.offset(-5, 4, -6));
		body.addOrReplaceChild("back_left_leg", CubeListBuilder.create().texOffs(76, 56).addBox(-2, 0, -2, 4, 8, 4), PartPose.offset(5, 4, 6));
		body.addOrReplaceChild("back_right_leg", CubeListBuilder.create().texOffs(76, 56).mirror().addBox(-2, 0, -2, 4, 8, 4), PartPose.offset(-5, 4, 6));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		boolean grounded = state.pose == io.github.adpulsipher.echoes.replay.Pose.IDLE || state.dying;

		float flap = grounded ? Mth.sin(t * 0.05f) * 0.1f : Mth.sin(t * 0.25f);
		float tipFlap = grounded ? 0.0f : Mth.sin(t * 0.25f - 0.8f);
		float fold = grounded ? 1.1f : 0.0f;
		leftWing.zRot = -flap * 0.75f + fold;
		leftWingTip.zRot = -tipFlap * 0.55f + fold * 1.3f;
		rightWing.zRot = flap * 0.75f - fold;
		rightWingTip.zRot = tipFlap * 0.55f - fold * 1.3f;
		leftWing.yRot = grounded ? -0.5f : 0.0f;
		rightWing.yRot = grounded ? 0.5f : 0.0f;

		// Serpentine sway through neck and tail, bobbing with each wingbeat.
		body.y = 10.0f + (grounded ? 0.0f : flap * 1.2f);
		neck1.yRot = Mth.sin(t * 0.08f) * 0.12f;
		neck2.yRot = Mth.sin(t * 0.08f - 0.5f) * 0.12f;
		neck1.xRot = -0.25f + Mth.sin(t * 0.25f - 1.0f) * 0.06f;
		head.xRot = 0.35f + state.xRot * Mth.DEG_TO_RAD * 0.5f;
		for (int i = 0; i < tail.length; i++) {
			tail[i].yRot = Mth.sin(t * 0.12f - i * 0.7f) * (0.15f + i * 0.04f);
			tail[i].xRot = Mth.sin(t * 0.1f - i * 0.5f) * 0.06f + (grounded ? 0.08f : -0.02f);
		}

		float open = state.jawOpen ? 0.55f + Mth.sin(t * 0.6f) * 0.08f : 0.05f + Mth.sin(t * 0.1f) * 0.03f;
		jaw.xRot = open;

		float legTuck = grounded ? 0.0f : 0.9f;
		for (int i = 0; i < legs.length; i++) {
			legs[i].xRot = legTuck + (grounded ? Mth.sin(t * 0.3f + i * Mth.PI) * 0.05f : Mth.sin(t * 0.1f + i) * 0.08f);
		}
	}
}
