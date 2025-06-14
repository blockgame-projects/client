package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.blocks.Blocks;
import com.james090500.utils.FontManager;
import com.james090500.world.Chunk;
import org.joml.Vector3f;

public class DebugScreen extends Screen {

    public DebugScreen() {
        setCloseable(false);
        setInGame(true);
    }

    @Override
    public void render() {
        Vector3f position = BlockGame.getInstance().getCamera().getPosition();
        String playerPos = String.format("Position: %.2f, %.2f, %.2f", position.x, position.y, position.z);

        FontManager.create().text("FPS: " + BlockGame.getInstance().getConfig().getFPS(), 20f, 10f, 50f);
        FontManager.create().text(playerPos, 20f, 10f, 70f);
        FontManager.create().text("Direction: " + getDirection(), 20f, 10f, 90f);
        FontManager.create().text("Active Chunks: " + BlockGame.getInstance().getWorld().getChunkCount(), 20f, 10f, 110f);
        FontManager.create().text("Seed: " + BlockGame.getInstance().getWorld().getWorldSeed(), 20f, 10f, 130f);

        int playerPosX = (int) Math.floor(position.x / 16);
        int playerPosZ = (int) Math.floor(position.z / 16);
        Chunk chunk = BlockGame.getInstance().getWorld().getChunk(playerPosX, playerPosZ);

        FontManager.create().text("Current Chunk Info", 20f, 10f, 170f);
        FontManager.create().text("Chunk Coords: " + playerPosX + ", " + playerPosZ, 20f, 10f, 190f);
        FontManager.create().text("Chunk Generated: " + chunk.generated, 20f, 10f, 210f);
        FontManager.create().text("Chunk Loaded: " + chunk.loaded, 20f, 10f, 230f);
        FontManager.create().text("Chunk Needs Update: " + chunk.needsUpdate, 20f, 10f, 250f);
        FontManager.create().text("Chunk Needs Queued: " + chunk.queued, 20f, 10f, 270f);
        FontManager.create().text("Chunk Solid Vertices: " + chunk.getChunkRenderer().solidVertexCount, 20f, 10f, 290f);
        FontManager.create().text("Chunk Trans Vertices: " + chunk.getChunkRenderer().transVertexCount, 20f, 10f, 310f);


        FontManager.create().text("Player Info", 20f, 10f, 350f);
        FontManager.create().text("Player Hand: " + Blocks.ids[BlockGame.getInstance().getLocalPlayer().getCurrentBlock()].getName(), 20f, 10f, 370f);

        super.render();
    }

    private String getDirection() {
        Vector3f forward = BlockGame.getInstance().getCamera().getDirection();
        float yaw = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));

        // Normalize yaw to 0–360
        if (yaw < 0) {
            yaw += 360;
        }

        String direction;
        if (yaw >= 45 && yaw < 135) {
            direction = "East";
        } else if (yaw >= 135 && yaw < 225) {
            direction = "South";
        } else if (yaw >= 225 && yaw < 315) {
            direction = "West";
        } else {
            direction = "North";
        }

        return direction;
    }
}
