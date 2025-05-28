package com.james090500.client;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.utils.Clock;
import org.joml.Vector3f;

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

        // Handle noclip toggle
        if (keys.getOrDefault(GLFW_KEY_V, false)) {
            keys.put(GLFW_KEY_V, false);
            this.noclip = !this.noclip;
        }

        // Dampen movement
        this.velocity.mul(0.75f);

        // Apply input movement and apply friction
        Vector3f acceleration = new Vector3f();

        if (keys.getOrDefault(GLFW_KEY_ESCAPE, false))
            BlockGame.getInstance().pause();

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

    public void update(double delta) {
        this.updateMovement(delta);
    }

}
