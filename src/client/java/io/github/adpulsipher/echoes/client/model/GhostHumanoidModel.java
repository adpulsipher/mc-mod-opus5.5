package io.github.adpulsipher.echoes.client.model;

import io.github.adpulsipher.echoes.client.render.GhostRenderState;
import io.github.adpulsipher.echoes.replay.Role;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A humanoid ghost with the classic skin layout (left 64x64 of a 128x128 texture) plus a set of props that are
 * shown or hidden per role: sword, bow, staff, pickaxe, helmet and crown.
 */
public class GhostHumanoidModel extends EntityModel<GhostRenderState> {
	public static final int PROP_SWORD = 1;
	public static final int PROP_BOW = 2;
	public static final int PROP_STAFF = 4;
	public static final int PROP_PICKAXE = 8;
	public static final int PROP_HELMET = 16;
	public static final int PROP_CROWN = 32;

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart sword;
	private final ModelPart bow;
	private final ModelPart staff;
	private final ModelPart pickaxe;
	private final ModelPart helmet;
	private final ModelPart crown;

	public GhostHumanoidModel(ModelPart root) {
		super(root);
		this.root = root;
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
		this.sword = rightArm.getChild("sword");
		this.pickaxe = rightArm.getChild("pickaxe");
		this.staff = rightArm.getChild("staff");
		this.bow = leftArm.getChild("bow");
		this.helmet = head.getChild("helmet");
		this.crown = head.getChild("crown");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		CubeDeformation outer = new CubeDeformation(0.25f);

		PartDefinition head = root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
						.texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.5f)),
				PartPose.offset(0, 0, 0));
		head.addOrReplaceChild("helmet",
				CubeListBuilder.create().texOffs(64, 48).addBox(-4.5f, -9.2f, -4.5f, 9, 4, 9, new CubeDeformation(0.3f))
						.texOffs(64, 61).addBox(-0.5f, -12.2f, -3.5f, 1, 3, 7),
				PartPose.ZERO);
		head.addOrReplaceChild("crown",
				CubeListBuilder.create().texOffs(0, 64).addBox(-4.5f, -10.0f, -4.5f, 9, 2, 9, new CubeDeformation(0.2f))
						.texOffs(40, 64).addBox(-4.5f, -12.0f, -4.5f, 1, 2, 1)
						.texOffs(40, 64).addBox(3.5f, -12.0f, -4.5f, 1, 2, 1)
						.texOffs(40, 64).addBox(-4.5f, -12.0f, 3.5f, 1, 2, 1)
						.texOffs(40, 64).addBox(3.5f, -12.0f, 3.5f, 1, 2, 1)
						.texOffs(44, 64).addBox(-0.5f, -12.5f, -4.8f, 1, 3, 1),
				PartPose.ZERO);

		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4)
						.texOffs(16, 32).addBox(-4, 0, -2, 8, 12, 4, outer),
				PartPose.ZERO);

		PartDefinition rightArm = root.addOrReplaceChild("right_arm",
				CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4)
						.texOffs(40, 32).addBox(-3, -2, -2, 4, 12, 4, outer),
				PartPose.offset(-5, 2, 0));
		rightArm.addOrReplaceChild("sword",
				CubeListBuilder.create().texOffs(64, 0).addBox(-0.5f, 8.5f, -3.0f, 1, 1, 4)
						.texOffs(64, 6).addBox(-2.0f, 8.0f, -4.0f, 4, 2, 1)
						.texOffs(64, 10).addBox(-0.5f, 8.0f, -15.0f, 1, 2, 11),
				PartPose.offset(-1, 0, 0));
		rightArm.addOrReplaceChild("pickaxe",
				CubeListBuilder.create().texOffs(64, 26).addBox(-0.5f, 8.5f, -10.0f, 1, 1, 12)
						.texOffs(64, 40).addBox(-0.5f, 5.5f, -11.0f, 1, 7, 1)
						.texOffs(68, 40).addBox(-0.5f, 4.5f, -10.0f, 1, 1, 1)
						.texOffs(68, 40).addBox(-0.5f, 12.5f, -10.0f, 1, 1, 1),
				PartPose.offset(-1, 0, 0));
		rightArm.addOrReplaceChild("staff",
				CubeListBuilder.create().texOffs(100, 0).addBox(-0.5f, -12.0f, -0.5f, 1, 26, 1)
						.texOffs(106, 0).addBox(-1.5f, -15.0f, -1.5f, 3, 3, 3),
				PartPose.offsetAndRotation(-1, 9, -1, Mth.HALF_PI, 0, 0));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm",
				CubeListBuilder.create().texOffs(32, 48).addBox(-1, -2, -2, 4, 12, 4)
						.texOffs(48, 48).addBox(-1, -2, -2, 4, 12, 4, outer),
				PartPose.offset(5, 2, 0));
		leftArm.addOrReplaceChild("bow",
				CubeListBuilder.create().texOffs(120, 0).addBox(-0.5f, -9.0f, -0.5f, 1, 18, 1)
						.texOffs(124, 0).addBox(0.0f, -8.0f, 1.0f, 0, 16, 1),
				PartPose.offsetAndRotation(1, 10, -1, Mth.HALF_PI, 0, 0));

		root.addOrReplaceChild("right_leg",
				CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4)
						.texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4, outer),
				PartPose.offset(-1.9f, 12, 0));
		root.addOrReplaceChild("left_leg",
				CubeListBuilder.create().texOffs(16, 48).addBox(-2, 0, -2, 4, 12, 4)
						.texOffs(0, 48).addBox(-2, 0, -2, 4, 12, 4, outer),
				PartPose.offset(1.9f, 12, 0));

		return LayerDefinition.create(mesh, 128, 128);
	}

	public static int defaultProps(Role role) {
		return switch (role) {
			case SOLDIER, KNIGHT -> PROP_SWORD | PROP_HELMET;
			case ARCHER -> PROP_BOW;
			case MAGE -> PROP_STAFF;
			case MINER -> PROP_PICKAXE | PROP_HELMET;
			case NOBLE -> PROP_CROWN;
			default -> 0;
		};
	}

	@Override
	public void setupAnim(GhostRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float p = state.poseTime;

		head.yRot = state.yRot * Mth.DEG_TO_RAD;
		head.xRot = state.xRot * Mth.DEG_TO_RAD;

		// Props per role
		int props = state.props >= 0 ? state.props : defaultProps(state.role);
		sword.visible = (props & PROP_SWORD) != 0;
		bow.visible = (props & PROP_BOW) != 0;
		staff.visible = (props & PROP_STAFF) != 0;
		pickaxe.visible = (props & PROP_PICKAXE) != 0;
		helmet.visible = (props & PROP_HELMET) != 0;
		crown.visible = (props & PROP_CROWN) != 0;

		// Idle breathing
		rightArm.zRot = Mth.cos(t * 0.09f) * 0.05f + 0.05f;
		leftArm.zRot = -Mth.cos(t * 0.09f) * 0.05f - 0.05f;
		rightArm.xRot = Mth.sin(t * 0.067f) * 0.05f;
		leftArm.xRot = -Mth.sin(t * 0.067f) * 0.05f;

		// Walking driven by the stride of the real movement
		float swing = Mth.cos(state.stride * 0.6662f) * 1.2f * state.strideAmount;
		rightLeg.xRot = swing;
		leftLeg.xRot = -swing;
		rightArm.xRot += -swing * 0.8f;
		leftArm.xRot += swing * 0.8f;

		switch (state.pose) {
			case WALK, RUN -> {
				float speed = state.pose == io.github.adpulsipher.echoes.replay.Pose.RUN ? 0.75f : 0.45f;
				float amount = state.pose == io.github.adpulsipher.echoes.replay.Pose.RUN ? 1.1f : 0.7f;
				float s = Mth.cos(t * speed) * amount;
				rightLeg.xRot = s;
				leftLeg.xRot = -s;
				rightArm.xRot = -s * 0.9f;
				leftArm.xRot = s * 0.9f;
				if (state.pose == io.github.adpulsipher.echoes.replay.Pose.RUN) {
					body.xRot = 0.15f;
					head.xRot += -0.1f;
				}
			}
			case CARRY -> {
				float s = Mth.cos(t * 0.45f) * 0.6f;
				rightLeg.xRot = s;
				leftLeg.xRot = -s;
				rightArm.xRot = -1.1f;
				leftArm.xRot = -1.1f;
				rightArm.yRot = -0.2f;
				leftArm.yRot = 0.2f;
			}
			case ATTACK -> {
				float cycle = (t * 0.25f) % Mth.TWO_PI;
				rightArm.xRot = -1.9f + Mth.sin(cycle) * 1.3f;
				rightArm.yRot = -0.3f + Mth.sin(cycle) * 0.3f;
				leftArm.xRot = -0.6f;
				body.yRot = Mth.sin(cycle) * 0.2f;
				rightLeg.xRot = 0.35f;
				leftLeg.xRot = -0.35f;
			}
			case SHOOT -> {
				rightArm.xRot = -Mth.HALF_PI + head.xRot;
				rightArm.yRot = -0.1f + head.yRot - 0.4f;
				leftArm.xRot = -Mth.HALF_PI + head.xRot;
				leftArm.yRot = 0.1f + head.yRot + 0.4f;
				float draw = Mth.sin(t * 0.15f) * 0.15f;
				rightArm.yRot -= draw;
			}
			case CAST -> {
				rightArm.xRot = -2.7f + Mth.sin(t * 0.2f) * 0.15f;
				leftArm.xRot = -2.7f + Mth.cos(t * 0.2f) * 0.15f;
				rightArm.zRot = 0.35f;
				leftArm.zRot = -0.35f;
				head.xRot = -0.35f;
			}
			case KNEEL -> {
				rightLeg.xRot = -Mth.HALF_PI;
				leftLeg.xRot = 0.0f;
				leftLeg.y = 17.0f;
				rightLeg.y = 17.0f;
				body.y = 5.0f;
				head.y = 5.0f;
				rightArm.y = 7.0f;
				leftArm.y = 7.0f;
				head.xRot = 0.45f;
				rightArm.xRot = -0.4f;
				leftArm.xRot = -0.2f;
			}
			case CHEER -> {
				float bounce = Mth.abs(Mth.sin(t * 0.35f));
				rightArm.xRot = -2.9f;
				leftArm.xRot = -2.9f;
				rightArm.zRot = 0.3f + bounce * 0.2f;
				leftArm.zRot = -0.3f - bounce * 0.2f;
				root.y = -bounce * 2.0f;
				head.xRot = -0.3f;
			}
			case COWER -> {
				body.xRot = 0.5f;
				head.xRot = 0.7f;
				head.z = -2.0f;
				rightArm.xRot = -2.6f;
				leftArm.xRot = -2.6f;
				rightArm.zRot = -0.5f;
				leftArm.zRot = 0.5f;
				rightArm.z = -1.5f;
				leftArm.z = -1.5f;
				float shiver = Mth.sin(t * 1.5f) * 0.03f;
				root.yRot = shiver;
			}
			case DANCE -> {
				float phase = t * 0.3f;
				rightArm.xRot = -2.2f + Mth.sin(phase) * 0.8f;
				leftArm.xRot = -2.2f - Mth.sin(phase) * 0.8f;
				rightLeg.xRot = Mth.sin(phase) * 0.6f;
				leftLeg.xRot = -Mth.sin(phase) * 0.6f;
				body.yRot = Mth.sin(phase * 0.5f) * 0.3f;
				root.y = -Mth.abs(Mth.sin(phase)) * 1.5f;
			}
			case TRADE -> {
				rightArm.xRot = -0.9f + Mth.sin(t * 0.12f) * 0.25f;
				rightArm.yRot = -0.2f;
				leftArm.xRot = -0.3f;
				head.xRot = 0.15f;
			}
			case MINE -> {
				float cycle = t * 0.22f;
				rightArm.xRot = -2.2f + Mth.abs(Mth.sin(cycle)) * 1.9f;
				leftArm.xRot = -1.4f + Mth.abs(Mth.sin(cycle)) * 1.1f;
				body.xRot = 0.2f;
			}
			case BOW -> {
				float amount = Mth.clamp(p / 15.0f, 0.0f, 1.0f);
				body.xRot = 0.55f * amount;
				head.z = -3.0f * amount;
				head.y = 1.0f * amount;
				head.xRot = 0.5f * amount;
				rightArm.z = -3.0f * amount;
				leftArm.z = -3.0f * amount;
			}
			case DEAD, IDLE, FLY, BREATHE -> {
			}
		}
	}
}
