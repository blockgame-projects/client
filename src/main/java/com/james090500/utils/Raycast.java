package com.james090500.utils;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Raycast {

    /**
     * Raycast to a block or placing location
     * @param origin The start of the ray
     * @param direction The direction of the ray
     * @param maxDistance The distance of the ray
     * @return
     */
    public static Vector3i[] block(Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3i current = new Vector3i(
                (int) Math.floor(origin.x),
                (int) Math.floor(origin.y),
                (int) Math.floor(origin.z)
        );

        Vector3f rayDir = new Vector3f(direction).normalize();

        int stepX = (int) Math.signum(rayDir.x);
        int stepY = (int) Math.signum(rayDir.y);
        int stepZ = (int) Math.signum(rayDir.z);

        float tMaxX = intBound(origin.x, rayDir.x);
        float tMaxY = intBound(origin.y, rayDir.y);
        float tMaxZ = intBound(origin.z, rayDir.z);

        float tDeltaX = (rayDir.x == 0) ? Float.POSITIVE_INFINITY : Math.abs(1.0f / rayDir.x);
        float tDeltaY = (rayDir.y == 0) ? Float.POSITIVE_INFINITY : Math.abs(1.0f / rayDir.y);
        float tDeltaZ = (rayDir.z == 0) ? Float.POSITIVE_INFINITY : Math.abs(1.0f / rayDir.z);

        float distance = 0.0f;

        Vector3i previousBlock = new Vector3i(current);
        while (distance <= maxDistance) {
            // Check the block
            Block block = BlockGame.getInstance().getWorld().getBlock(current.x, current.y, current.z);
            if (block != null && block.isSolid()) {
                return new Vector3i[] { previousBlock, current };
            }

            // Step to next voxel
            previousBlock.set(current);
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    current.x += stepX;
                    distance = tMaxX;
                    tMaxX += tDeltaX;
                } else {
                    current.z += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    current.y += stepY;
                    distance = tMaxY;
                    tMaxY += tDeltaY;
                } else {
                    current.z += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            }
        }

        return null; // No block hit
    }

    /**
     * A helper function for raycasting
     * @param s
     * @param ds
     * @return
     */
    private static float intBound(float s, float ds) {
        if (ds == 0) return Float.POSITIVE_INFINITY;
        float sIsInt = (float) Math.floor(s);
        if (ds > 0) {
            return (sIsInt + 1.0f - s) / ds;
        } else {
            return (s - sIsInt) / -ds;
        }
    }
}
