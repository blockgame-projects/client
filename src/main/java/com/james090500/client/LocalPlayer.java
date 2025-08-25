package com.james090500.client;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.renderer.gui.ArmOverlay;
import com.james090500.renderer.gui.BlockOverlay;
import com.james090500.utils.Clock;
import com.james090500.utils.SoundManager;
import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class LocalPlayer {

    // Player state
    @Getter private int currentBlock = 1;
    private boolean noclip = false;
    private boolean jumping = false;
    private boolean falling = false;
    private boolean swimming = false;

    Clock clock = new Clock();
    private final float playerWidth = 0.4f;
    private final float playerHeight = 1.5f;
    private final Vector3f velocity = new Vector3f();
    private final Vector3f fallVelocity = new Vector3f();
    private double jumpStartTime = 0;
    private float stepCooldown;
    private String worldName;

    BlockOverlay blockOverlay = new BlockOverlay();
    ArmOverlay armOverlay = new ArmOverlay();

    public LocalPlayer(String worldName) {
        this.worldName = worldName;

        File playerPath = new File("worlds/" + worldName + "/players");
        File playerData = new File(playerPath + "/player.dat");
        Camera camera = BlockGame.getInstance().getCamera();

        if(!playerPath.exists()) {
            playerPath.mkdirs();

            savePlayer();
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(playerData, "rw")) {
                float x = raf.readFloat();
                float y = raf.readFloat();
                float z = raf.readFloat();
                float pitch = raf.readFloat();
                float yaw = raf.readFloat();

                camera.setPosition(x, y, z);
                camera.setRotation(pitch, yaw);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePlayer() {
        File playerPath = new File("worlds/" + worldName + "/players");
        File playerData = new File(playerPath + "/player.dat");
        Camera camera = BlockGame.getInstance().getCamera();

        // Write to file
        try (RandomAccessFile raf = new RandomAccessFile(playerData, "rw")) {
            raf.setLength(0);
            raf.writeFloat(camera.getPosition().x);
            raf.writeFloat(camera.getPosition().y);
            raf.writeFloat(camera.getPosition().z);
            raf.writeFloat(camera.pitch);
            raf.writeFloat(camera.yaw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        } else if(this.swimming) {
            moveSpeed = 0.4f;
        }

        // Get necessary references
        Camera camera = BlockGame.getInstance().getCamera();
        HashMap<Integer, Boolean> keys = BlockGame.getInstance().getClientWindow().getClientInput().getKeys();

        // Current Block the player is in
        Vector3f playerPos = new Vector3f(camera.getPosition());
        playerPos.y -= 1;
        Block currentBlock = BlockGame.getInstance().getWorld().getBlock(playerPos);

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
        int playerPosX = (int) Math.floor(playerPos.x / 16);
        int playerPosZ = (int) Math.floor(playerPos.z / 16);
        if(!BlockGame.getInstance().getWorld().isChunkLoaded(playerPosX, playerPosZ)) {
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

        // No Clip Logic, else Swimming Logic, else Jumping Logic
        boolean newSwimming = currentBlock != null && !currentBlock.isSolid();
        if(this.swimming && !newSwimming) {
            this.fallVelocity.y = 10;
        }
        this.swimming = newSwimming;

        if(this.noclip) {
            if (keys.getOrDefault(GLFW_KEY_SPACE, false)) {
                this.fallVelocity.y = 15;
            } else if (keys.getOrDefault(GLFW_KEY_LEFT_SHIFT, false)) {
                this.fallVelocity.y = -15;
            } else {
                this.fallVelocity.y = 0;
            }
        } else if (this.swimming) {
            if (keys.getOrDefault(GLFW_KEY_SPACE, false)) {
                this.fallVelocity.y = 5;
            } else {
                float gravity = 20f;
                float terminalVelocity = -20;
                this.fallVelocity.y -= (float) (gravity * delta);
                if (this.fallVelocity.y < terminalVelocity) {
                    this.fallVelocity.y = terminalVelocity;
                }
            }
        } else {
            // Start jump if grounded and Space is pressed
            if (!this.jumping && !this.falling && keys.getOrDefault(GLFW_KEY_SPACE, false)) {
                keys.put(GLFW_KEY_SPACE, false);
                this.jumping = true;
                this.jumpStartTime = this.clock.getElapsedTime();
            }

            double timeSinceJump = this.clock.getElapsedTime() - this.jumpStartTime;
            float maxJumpTime = 0.2f; // Adjust for smooth 1.2 block rise

            // Ascend phase
            if (this.jumping && timeSinceJump < maxJumpTime && !this.swimming) {
                this.fallVelocity.y = 5; // Initial jump velocity upward
            } else {
                this.jumping = false;
            }

            // Apply gravity if not jumping
            if (!this.jumping) {
                float gravity = 45f;
                float terminalVelocity = -90;
                this.fallVelocity.y -= (float) (gravity * delta);
                if (this.fallVelocity.y < terminalVelocity) {
                    this.fallVelocity.y = terminalVelocity;
                }
            }
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
        boolean canMove1 = this.tryMove(this.velocity, 0, halfWidth, this.playerHeight);
        boolean canMove2 = this.tryMove(this.velocity, 2, halfWidth, this.playerHeight);

        if(canMove1 && canMove2) {
            // Play the movement sound
            if (velocity.lengthSquared() > 0.0004f && !this.falling && !this.jumping) {
                stepCooldown -= delta; // deltaTime is the time since last frame
                if (stepCooldown <= 0f) {
                    Block blockAtFeet = BlockGame.getInstance().getWorld().getBlock(camera.getPosition().sub(new Vector3f(0, 2, 0)));
                    if(blockAtFeet.getSound() != null) {
                        int sound = 1 + (int) (Math.random() * 4);
                        SoundManager.play("assets/sound/block/" + blockAtFeet.getSound() + sound + ".ogg");
                        stepCooldown = 0.5f; // play every half second while moving
                    }
                }
            }
        }

        camera.updateFrustum();
    }

    /**
     * Update interaction via mouse picking
     * @param delta
     */
    private void updateInteraction(double delta) {
        Camera camera = BlockGame.getInstance().getCamera();
        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f dir = new Vector3f(camera.getDirection()).normalize();

        Vector3i[] raycast = raycastBlock(origin, dir, 5f);
        if(raycast != null && raycast.length == 2) {
            Block hitBlock = BlockGame.getInstance().getWorld().getBlock(raycast[1].x, raycast[1].y, raycast[1].z);
            if (hitBlock != null && hitBlock.isSolid()) {
                blockOverlay.setPosition(new Vector3f(raycast[1]));
                blockOverlay.render();
            }

            HashMap<Integer, Boolean> mouse = BlockGame.getInstance().getClientWindow().getClientInput().getMouse();
            if(mouse.getOrDefault(GLFW_MOUSE_BUTTON_LEFT, false)) {
                BlockGame.getInstance().getWorld().setBlock(raycast[1].x, raycast[1].y, raycast[1].z, (byte) 0);
                mouse.put(GLFW_MOUSE_BUTTON_LEFT, false);
            }

            if(mouse.getOrDefault(GLFW_MOUSE_BUTTON_RIGHT, false)) {
                BlockGame.getInstance().getWorld().setBlock(raycast[0].x, raycast[0].y, raycast[0].z, (byte) this.currentBlock);
                mouse.put(GLFW_MOUSE_BUTTON_RIGHT, false);
            }

//            if(mouse.getOrDefault(GLFW_MOUSE_))
        }
    }

    public void render() {
        armOverlay.render();
    }

    /**
     * Checks if the player's AABB collides with any solid blocks in the world.
     * @param {Vector3} min - Minimum bounds of the AABB.
     * @param {Vector3} max - Maximum bounds of the AABB.
     * @param {Object} futureBlock - Are we attempting to place a block?
     * @returns {boolean} If the player is colliding
     */
    private boolean isAABBColliding(Vector3f min, Vector3f max) {
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
                    if(block != null && block.isSolid()) {
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

        if (!this.isAABBColliding(min, max) || this.noclip) {
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
     * @return
     */
    public Vector3i[] raycastBlock(Vector3f origin, Vector3f direction, float maxDistance) {
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

    public void loadGui() {
        this.armOverlay.create();
    }

    public void changeHand(int i) {
        this.currentBlock += i;
        if(this.currentBlock > Blocks.ids.length - 1) {
            this.currentBlock = 1;
        } else if(this.currentBlock <= 0) {
            this.currentBlock = Blocks.ids.length - 1;
        }

        armOverlay.create();
    }
}
