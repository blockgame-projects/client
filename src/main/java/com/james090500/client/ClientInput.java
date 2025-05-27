package com.james090500.client;

import com.james090500.BlockGame;
import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class ClientInput {

    private boolean firstMouse = true;
    private double lastMouseX;
    private double lastMouseY;

    public void mouseMovement(long w, double xpos, double ypos) {
        //Skip if paused
        if(BlockGame.getInstance().getConfig().isPaused())
            return;

        if (firstMouse) {
            lastMouseX = xpos;
            lastMouseY = ypos;
            firstMouse = false;
        }
        float sensitivity = 0.1f;
        double dx = xpos - lastMouseX;
        double dy = lastMouseY - ypos;

        lastMouseX = xpos;
        lastMouseY = ypos;

        Camera camera = BlockGame.getInstance().getCamera();
        camera.yaw += (float) (dx * sensitivity);
        camera.pitch += (float) (dy * sensitivity);
        camera.pitch = Math.max(-89f, Math.min(89f, camera.pitch));
    }

    public void mouseClicked(long win, int button, int action, int mods) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
            this.leftClick();
        }
    }


    public void leftClick() {
        List<Screen> screens = new ArrayList<>(ScreenManager.active());
        for (Screen screen : screens) {
            screen.click();
        }
    }
}
