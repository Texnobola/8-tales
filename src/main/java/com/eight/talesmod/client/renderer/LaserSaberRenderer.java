package com.eight.talesmod.client.renderer;

import com.eight.talesmod.client.model.LaserSaberModel;
import com.eight.talesmod.item.LaserSaberItem;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LaserSaberRenderer extends GeoItemRenderer<LaserSaberItem> {

    /** Name of the blade bone inside {@code laser_saber.geo.json}. */
    private static final String BLADE_BONE = "blade";

    public LaserSaberRenderer() {
        super(new LaserSaberModel());
    }

    /**
     * Called by GeckoLib for every bone in the model hierarchy during a
     * render pass. When the <em>blade</em> bone is processed we:
     * <ol>
     *   <li>Enable GeckoLib’s matrix-tracking so world-space coordinates
     *       are available on {@link GeoBone#getWorldSpaceMatrix()}.</li>
     *   <li>Extract the blade tip position and orientation.</li>
     *   <li>Forward them to every active Photon {@link EntityEffectExecutor}
     *       that is attached to the local player, so the saber trail
     *       accurately follows the physical 3-D blade during every swing
     *       animation frame.</li>
     * </ol>
     */
    @Override
    public void renderRecursively(
            PoseStack poseStack,
            LaserSaberItem animatable,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour) {

        if (BLADE_BONE.equals(bone.getName())) {
            // Ask GeckoLib to keep the world-space matrix up-to-date.
            bone.setTrackingMatrices(true);

            updatePhotonEffectTransform(bone, partialTick);
        }

        // Always delegate to super so normal rendering is unaffected.
        super.renderRecursively(
                poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Reads the blade bone’s world-space transform from GeckoLib and pushes
     * a matching offset + rotation to every Photon executor on the player.
     */
    private void updatePhotonEffectTransform(GeoBone bladeBone, float partialTick) {
        // We only want to update transforms for the ‘in-hand’ render, which
        // means the animatable stored in the renderer is the actual item.
        // Guard: bail out if there is no active Photon executor cache.
        if (EntityEffectExecutor.CACHE == null || EntityEffectExecutor.CACHE.isEmpty()) {
            return;
        }

        // Extract world-space position from the blade bone matrix.
        Matrix4f world = bladeBone.getWorldSpaceMatrix();
        // Column 3 of the model-space matrix holds the translation.
        Vector3f bonePos = new Vector3f(world.m30(), world.m31(), world.m32());

        // Build a quaternion from the bone’s rotation columns.
        Quaternionf boneRot = world.getUnnormalizedRotation(new Quaternionf());

        // Walk every entity that has an active Photon executor.
        EntityEffectExecutor.CACHE.forEach((entity, executors) -> {
            if (!(entity instanceof Player)) return;

            for (EntityEffectExecutor executor : executors) {
                if (executor == null) continue;
                var runtime = executor.getRuntime();
                if (runtime == null) return;

                // Shift the effect origin to sit at the blade tip.
                // The blade pivot is at Y=13 in local units; the model scale
                // in GeckoLib geo JSON uses 1/16 blocks, so divide by 16.
                executor.setOffset(new Vector3f(
                        bonePos.x / 16f,
                        bonePos.y / 16f,
                        bonePos.z / 16f));
                executor.setRotation(boneRot);
            }
        });
    }
}
