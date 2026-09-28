package io.github.adpulsipher.echoes.client.render;

import io.github.adpulsipher.echoes.replay.Pose;
import io.github.adpulsipher.echoes.replay.Role;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state shared by every ghostly figure: replay holograms, lingerers and the wyrm.
 */
public class GhostRenderState extends LivingEntityRenderState {
	public Role role = Role.SOLDIER;
	public Pose pose = Pose.IDLE;
	/** Ticks (with partial tick) since the current pose began. */
	public float poseTime;
	/** RGB tint of the ghost. */
	public int tint = 0xBFEFFF;
	/** Overall opacity, 0..1, including materializing, fading and flicker. */
	public float alpha = 0.7f;
	/** Walk cycle driver in radians-ish units. */
	public float stride;
	public float strideAmount;
	/** Lingerer: currently phased out of the present. */
	public boolean phased;
	/** Wyrm: jaw open (breathing fire or roaring). */
	public boolean jawOpen;
	/** Moth: attuned to a player. */
	public boolean attuned;
	public boolean dying;
	public float dyingTime;
	/** Projector hologram: which parts of the light-work are lit. */
	public boolean showCrystal;
	public boolean showBeam;
	/** Humanoid props to show, as a bitmask of GhostHumanoidModel.PROP_*, or -1 for the role's defaults. */
	public int props = -1;
	/** Bosses: the current attack and how long it has been going. */
	public int attackState;
	public float attackTime;
	public boolean enraged;
	/** Hierophant: surviving wards. */
	public int count;
}
