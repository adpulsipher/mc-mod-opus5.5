package io.github.adpulsipher.echoes.replay;

/**
 * An actor appearing in a replay.
 *
 * @param id index of the actor within its script
 * @param role what the figure looks like
 * @param tint RGB tint of the ghost
 * @param x starting position relative to the projector
 * @param y starting height above the stage floor (non-zero only for flyers)
 * @param z starting position relative to the projector
 * @param yaw starting facing, in degrees
 * @param pose starting pose
 * @param appearTick when the actor materializes
 */
public record ActorSpec(int id, Role role, int tint, double x, double y, double z, float yaw, Pose pose, int appearTick) {
}
