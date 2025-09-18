package com.james090500.client;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.blocks.ShortGrassBlock;
import com.james090500.renderer.gui.ArmOverlay;
import com.james090500.renderer.gui.BlockOverlay;
import com.james090500.renderer.gui.CrosshairOverlay;
import com.james090500.utils.AABB;
import com.james090500.utils.Clock;
import com.james090500.utils.Raycast;
import com.james090500.utils.SoundManager;
import com.james090500.world.ChunkStatus;
import lombok.Getter;
import lombok.Setter;
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
    private final Camera camera;
    private final AABB aabb;
    private final float playerWidth = 0.4f;
    private final float playerHeight = 1.75f;
    private final Vector3f velocity = new Vector3f();
    private final Vector3f fallVelocity = new Vector3f();
    private double jumpStartTime = 0;
    private float stepCooldown;
    private String worldName;
    @Getter @Setter
    private int lastChunkX;
    @Getter @Setter
    private int lastChunkZ;

    BlockOverlay blockOverlay = new BlockOverlay();
    ArmOverlay armOverlay = new ArmOverlay();
    CrosshairOverlay crosshairOverlay = new CrosshairOverlay();

    public LocalPlayer() {
        this.aabb = new AABB(playerWidth, playerHeight);
        this.worldName = BlockGame.getInstance().getWorld().getWorldName();
        camera = BlockGame.getInstance().getCamera();

        if(BlockGame.getInstance().getWorld().isRemote()) {
            setPosition(0, 100, 0);
        } else {
            File playerPath = new File("worlds/" + worldName + "/players");
            File playerData = new File(playerPath + "/player.dat");

            if (!playerPath.exists()) {
                playerPath.mkdirs();

                savePlayer();
            } else {
                try (RandomAccessFile raf = new RandomAccessFile(playerData, "rw")) {
                    float x = raf.readFloat();
                    float y = raf.readFloat();
                    float z = raf.readFloat();
                    float pitch = raf.readFloat();
                    float yaw = raf.readFloat();

                    setPosition(x, y, z);
                    camera.setRotation(pitch, yaw);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void savePlayer() {
        if(!BlockGame.getInstance().getWorld().isRemote()) {
            File playerPath = new File("worlds/" + worldName + "/players");
            File playerData = new File(playerPath + "/player.dat");

            // Write to file
            try (RandomAccessFile raf = new RandomAccessFile(playerData, "rw")) {
                raf.setLength(0);
                raf.writeFloat(getPosition().x);
                raf.writeFloat(getPosition().y);
                raf.writeFloat(getPosition().z);
                raf.writeFloat(camera.pitch);
                raf.writeFloat(camera.yaw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Vector3f getPosition() {
        return camera.getPosition();
    }

    public void setPosition(float x, float y, float z) {
        camera.setPosition(x, y, z);
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
        if(!BlockGame.getInstance().getWorld().isChunkStatus(lastChunkX, lastChunkZ, ChunkStatus.FINISHED)) return;

        float moveSpeed = 0.9f;
        if (this.noclip) {
            moveSpeed = 10f;
        } else if(this.swimming) {
            moveSpeed = 0.4f;
        }

        // Get necessary references
        Vector3f playerPos = getPosition();
        HashMap<Integer, Boolean> keys = BlockGame.getInstance().getClientWindow().getClientInput().getKeys();

        // Current Block the player is in
        playerPos.y -= 1;
        Block currentBlock = BlockGame.getInstance().getWorld().getBlock(playerPos);

        Vector3f dir = new Vector3f(camera.getDirection());
        dir.y = 0;
        dir.normalize();

        Vector3f right = new Vector3f();
        dir.cross(new Vector3f(0, 1, 0), right);

        // Stop playing falling through the world
        if (getPosition().y < -30) {
            getPosition().y = 100;
        }

        // Dampen movement
        this.velocity.mul(0.8f);

        // Apply input movement and apply friction
        Vector3f acceleration = new Vector3f();

        // Lets not try any movement until the chunk is loaded
        int playerPosX = (int) Math.floor(playerPos.x / 16);
        int playerPosZ = (int) Math.floor(playerPos.z / 16);
        if(!BlockGame.getInstance().getWorld().isChunkStatus(playerPosX, playerPosZ, ChunkStatus.FINISHED)) {
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
        boolean newSwimming = currentBlock != null && currentBlock.isLiquid();
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

        if (!this.tryMove(new Vector3f(0, yVelocity, 0), 1)) {
            this.fallVelocity.y = 0;
            this.falling = false;
        } else if (!this.noclip) {
            this.falling = true;
        }

        // Try horizontal movement along X and Z axes
        boolean canMove1 = this.tryMove(this.velocity, 0);
        boolean canMove2 = this.tryMove(this.velocity, 2);

        if(canMove1 && canMove2) {
            // Play the movement sound
            if (acceleration.lengthSquared() > 0f) {
                stepCooldown -= delta; // deltaTime is the time since last frame
                if (stepCooldown <= 0f) {
                    Block blockAtFeet = BlockGame.getInstance().getWorld().getBlock(getPosition().sub(new Vector3f(0, 2, 0)));
                    if(blockAtFeet != null && blockAtFeet.getSound() != null) {
                        SoundManager.play("assets/sound/block/" + blockAtFeet.getSound(), 4);
                        stepCooldown = 0.5f; // play every half second while moving
                    }
                }
            }
        }

        camera.updateFrustum();
    }

    /**
     * Update interaction via mouse picking
     */
    private void updateInteraction() {
        Vector3f origin = new Vector3f(getPosition());
        Vector3f dir = new Vector3f(camera.getDirection()).normalize();

        Vector3i[] raycast = Raycast.block(origin, dir, 5f);
        if(raycast != null && raycast.length == 2) {
            Block hitBlock = BlockGame.getInstance().getWorld().getBlock(raycast[1].x, raycast[1].y, raycast[1].z);
            if (hitBlock != null && !hitBlock.isLiquid()) {
                blockOverlay.setPosition(new Vector3f(raycast[1]));
                blockOverlay.render();
            }

            HashMap<Integer, Boolean> mouse = BlockGame.getInstance().getClientWindow().getClientInput().getMouse();
            if(mouse.getOrDefault(GLFW_MOUSE_BUTTON_LEFT, false)) {
                BlockGame.getInstance().getWorld().setBlock(raycast[1].x, raycast[1].y, raycast[1].z, (byte) 0);
                mouse.put(GLFW_MOUSE_BUTTON_LEFT, false);
            }

            if(mouse.getOrDefault(GLFW_MOUSE_BUTTON_RIGHT, false)) {
                Block currentBlock = Blocks.get(this.currentBlock);
                Block lookingAt = BlockGame.getInstance().getWorld().getBlock(raycast[1].x, raycast[1].y, raycast[1].z);
                if(lookingAt instanceof ShortGrassBlock && !aabb.isColliding(origin, raycast[1])) {
                    BlockGame.getInstance().getWorld().setBlock(raycast[1].x, raycast[1].y, raycast[1].z, currentBlock.getId());
                } else if(!aabb.isColliding(origin, raycast[0])) {
                    BlockGame.getInstance().getWorld().setBlock(raycast[0].x, raycast[0].y, raycast[0].z, currentBlock.getId());
                }
                mouse.put(GLFW_MOUSE_BUTTON_RIGHT, false);
            }

            if(hitBlock != null && !hitBlock.isLiquid()) {
                if(mouse.getOrDefault(GLFW_MOUSE_BUTTON_MIDDLE, false)) {
                    this.changeHand(hitBlock.getId());
                    mouse.put(GLFW_MOUSE_BUTTON_MIDDLE, false);
                }
            }
        }
    }

    public void render() {
        armOverlay.render();
        crosshairOverlay.render();
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
    public boolean tryMove(Vector3f velocity, int axis) {
        Vector3f pos = new Vector3f(getPosition());
        pos.setComponent(axis,pos.get(axis) + velocity.get(axis));

        if (!aabb.isColliding(pos) || this.noclip) {
            camera.setPosition(axis, pos.get(axis));
            return true;
        }
        return false;
    }

    public void update(double delta) {
        this.updateControls();
        this.updateInteraction();
        this.updateMovement(delta);
    }

    public void loadGui() {
        this.armOverlay.create();
    }

    /**
     * What block to put in the hand
     * @param blockId The block id
     */
    public void changeHand(int blockId) {
        this.currentBlock = blockId;
        armOverlay.create();
    }

    /**
     * Scroll the hotbar
     * @param increase Whether to increase the hotbar or decrease
     */
    public void scrollHotbar(boolean increase) {
        int newBlock = increase ? this.currentBlock + 1 : this.currentBlock - 1;

        if(newBlock > Blocks.getTotalBlocks()) {
            this.changeHand(1);
        } else if(newBlock <= 0) {
            this.changeHand(Blocks.getTotalBlocks());
        } else {
            this.changeHand(newBlock);
        }
    }
}
