package com.james090500.client;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.gui.ScreenManager;
import com.james090500.renderer.BlockOverlay;
import com.james090500.utils.Clock;
import com.james090500.world.World;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class LocalPlayer {

    // Player state
    //currentBlock = 1
    private boolean noclip = false;
    private boolean jumping = false;
    private boolean falling = false;

    Clock clock = new Clock();
    private float playerWidth = 0.4f;
    private float playerHeight = 1.5f;
    private Vector3f velocity = new Vector3f();
    private Vector3f fallVelocity = new Vector3f();
    private double jumpStartTime = 0;

    BlockOverlay blockOverlay = new BlockOverlay();

    private void updateControls() {
        HashMap<Integer, Boolean> keys = BlockGame.getInstance().getClientWindow().getClientInput().getKeys();

        // Handle noclip toggle
        if (keys.getOrDefault(GLFW_KEY_V, false)) {
            keys.put(GLFW_KEY_V, false);
            this.noclip = !this.noclip;
        }
    }

    /**
     * Updates player movement based on input and applies gravity and collision.
     * @param {number} delta - Time since last frame.
     */
    private void updateMovement(double delta) {
        float moveSpeed = 0.9f;
        if (this.noclip) {
            moveSpeed = 10f;
        }

        // Get necessary references
        Camera camera = BlockGame.getInstance().getCamera();
        HashMap<Integer, Boolean> keys = BlockGame.getInstance().getClientWindow().getClientInput().getKeys();

        Vector3f dir = new Vector3f(camera.getDirection());
        dir.y = 0;
        dir.normalize();

        Vector3f right = new Vector3f();
        dir.cross(new Vector3f(0, 1, 0), right);

        // Stop playing falling through the world
        if (camera.getPosition().y < -30) {
            camera.getPosition().y = 100;
        }

        // Dampen movement
        this.velocity.mul(0.75f);

        // Apply input movement and apply friction
        Vector3f acceleration = new Vector3f();

        // Lets not try any movement until the chunk is loaded
        if(!BlockGame.getInstance().getWorld().isChunkLoaded()) {
            return;
        }

        if (keys.getOrDefault(GLFW_KEY_W, false)) {
            acceleration.add(dir);
        }
        if (keys.getOrDefault(GLFW_KEY_A, false)) {
            acceleration.sub(right);
        }
        if (keys.getOrDefault(GLFW_KEY_D, false)) {
            acceleration.add(right);
        }
        if (keys.getOrDefault(GLFW_KEY_S, false)) {
            acceleration.add(new Vector3f(dir).negate());
        }

        if (acceleration.lengthSquared() > 0) {
            acceleration.normalize().mul(moveSpeed); // units per second
            this.velocity.add(acceleration.mul((float) delta)); // scale per frame
        }

        // Start jump if grounded and Space is pressed
        if (!this.jumping && !this.falling && keys.getOrDefault(GLFW_KEY_SPACE, false)) {
            keys.put(GLFW_KEY_SPACE, false);
            this.jumping = true;
            this.jumpStartTime = this.clock.getElapsedTime();
        }

        double timeSinceJump = this.clock.getElapsedTime() - this.jumpStartTime;
        float maxJumpTime = 0.2f; // Adjust for smooth 1.2 block rise

        // Ascend phase
        if (this.jumping && timeSinceJump < maxJumpTime) {
            this.fallVelocity.y = 5; // Initial jump velocity upward
        } else {
            this.jumping = false;
        }

        // Apply gravity if not jumping
        if (!this.noclip && !this.jumping) {
            float gravity = 50f;
            float terminalVelocity = -90;
            this.fallVelocity.y -= (float) (gravity * delta);
            if (this.fallVelocity.y < terminalVelocity) {
                this.fallVelocity.y = terminalVelocity;
            }
        } else if (!this.jumping) {
            this.fallVelocity.y = 0;
        }
        float yVelocity = (float) (this.fallVelocity.y * delta);

        // Half the players width
        float halfWidth = this.playerWidth / 2;

        if (!this.tryMove(new Vector3f(0, yVelocity, 0), 1, halfWidth, this.playerHeight)) {
            this.fallVelocity.y = 0;
            this.falling = false;
        } else if (!this.noclip) {
            this.falling = true;
        }

        // Try horizontal movement along X and Z axes
        this.tryMove(this.velocity, 0, halfWidth, this.playerHeight);
        this.tryMove(this.velocity, 2, halfWidth, this.playerHeight);
    }

    /**
     * Update interaction via mouse picking
     * @param delta
     */
    private void updateInteraction(double delta) {
        Camera camera = BlockGame.getInstance().getCamera();
        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f dir = new Vector3f(camera.getDirection()).normalize();

        Vector3i raycast = raycastBlock(origin, dir, 5f, false);
        if(raycast != null) {
            Block hitBlock = BlockGame.getInstance().getWorld().getBlock(raycast.x, raycast.y, raycast.z);
            if (hitBlock != null && hitBlock.isSolid()) {
                blockOverlay.setPosition(new Vector3f(raycast));
                blockOverlay.render();
            }

            HashMap<Integer, Boolean> mouse = BlockGame.getInstance().getClientWindow().getClientInput().getMouse();
            if(mouse.getOrDefault(GLFW_MOUSE_BUTTON_LEFT, false)) {
                BlockGame.getInstance().getWorld().setBlock(raycast.x, raycast.y, raycast.z, (byte) 0);
                BlockGame.getInstance().getWorld().regenChunk(raycast.x, raycast.z);
                mouse.put(GLFW_MOUSE_BUTTON_LEFT, false);
            }
        }
    }

    /**
     * Checks if the player's AABB collides with any solid blocks in the world.
     * @param {Vector3} min - Minimum bounds of the AABB.
     * @param {Vector3} max - Maximum bounds of the AABB.
     * @param {Object} futureBlock - Are we attempting to place a block?
     * @returns {boolean} True if a collision is found.
     */
    private boolean isAABBColliding(Vector3f min, Vector3f max) {
        if (this.noclip) {
            return false;
        }

        for (int x = (int) Math.floor(min.x); x <= Math.floor(max.x); x++) {
            for (int y = (int) Math.floor(min.y); y <= Math.floor(max.y); y++) {
                for (int z = (int) Math.floor(min.z); z <= Math.floor(max.z); z++) {
//                    if (futureBlock) {
//                        if (
//                                futureBlock[0] === x &&
//                                        futureBlock[1] === y &&
//                                        futureBlock[2] === z
//                        ) {
//                            return true
//                        }
//                    }

                    Block block = BlockGame.getInstance().getWorld().getBlock(x, y, z);
                    if (block != null && block.isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Attempts to move the camera along a single axis while checking for AABB collisions.
     * @param {PerspectiveCamera} camera - The camera to move.
     * @param {Vector3} velocity - The velocity vector.
     * @param {string} axis - The axis to apply movement ('x', 'y', or 'z').
     * @param {number} halfWidth - Half the width of the player's bounding box.
     * @param {number} playerHeight - Height of the player.
     * @param {Object} world - The world object to query blocks from.
     * @returns {boolean} True if the move was successful.
     */
    public boolean tryMove(Vector3f velocity, int axis, float halfWidth, float playerHeight) {
        Vector3f pos = new Vector3f(BlockGame.getInstance().getCamera().getPosition());
        pos.setComponent(axis,pos.get(axis) + velocity.get(axis));

        Vector3f min = new Vector3f(
                pos.x - halfWidth,
                pos.y - playerHeight,
                pos.z - halfWidth
        );

        Vector3f max = new Vector3f(pos.x + halfWidth, pos.y, pos.z + halfWidth);

        if (!this.isAABBColliding(min, max)) {
            BlockGame.getInstance().getCamera().setPosition(axis, pos.get(axis));
            return true;
        }
        return false;
    }

    /**
     * Raycast to a block or placing location
     * @param origin The start of the ray
     * @param direction The direction of the ray
     * @param maxDistance The distance of the ray
     * @param placement If we are trying to place or destroy
     * @return
     */
    public Vector3i raycastBlock(Vector3f origin, Vector3f direction, float maxDistance, boolean placement) {
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
            if (block != null) {
                return placement ? previousBlock : current;
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
    private float intBound(float s, float ds) {
        if (ds == 0) return Float.POSITIVE_INFINITY;
        float sIsInt = (float) Math.floor(s);
        if (ds > 0) {
            return (sIsInt + 1.0f - s) / ds;
        } else {
            return (s - sIsInt) / -ds;
        }
    }



    public void update(double delta) {
        this.updateControls();
        this.updateInteraction(delta);
        this.updateMovement(delta);
    }

}
