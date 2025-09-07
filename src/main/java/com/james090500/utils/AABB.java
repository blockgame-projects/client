package com.james090500.utils;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class AABB {

    private float width;
    private float height;

    public AABB(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public boolean isColliding(Vector3f pos) {
        return isColliding(pos, null);
    }

    /**
     * Checks if the player's AABB collides with any solid blocks in the world.
     */
    public boolean isColliding(Vector3f pos, Vector3i futureBlock) {
        float halfWidth = this.width / 2;
        Vector3f min = new Vector3f(
                pos.x - halfWidth,
                pos.y - height,
                pos.z - halfWidth
        );
        Vector3f max = new Vector3f(pos.x + halfWidth, pos.y, pos.z + halfWidth);

        for (int x = (int) Math.floor(min.x); x <= Math.floor(max.x); x++) {
            for (int y = (int) Math.floor(min.y); y <= Math.floor(max.y); y++) {
                for (int z = (int) Math.floor(min.z); z <= Math.floor(max.z); z++) {
                    if (futureBlock != null) {
                        if (futureBlock.x == x && futureBlock.y == y && futureBlock.z == z) {
                            return true;
                        }
                    }

                    Block block = BlockGame.getInstance().getWorld().getBlock(x, y, z);
                    if(block != null && block.isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
