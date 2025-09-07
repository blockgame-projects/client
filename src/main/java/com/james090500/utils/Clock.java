package com.james090500.utils;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Clock {
    private double startTime;

    public Clock() {
        reset();
    }

    public void reset() {
        startTime = glfwGetTime();
    }

    public double getElapsedTime() {
        return glfwGetTime() - startTime;
    }

}
