package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.utils.FontManager;
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
