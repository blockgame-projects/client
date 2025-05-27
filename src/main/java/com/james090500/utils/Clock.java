package com.james090500.utils;

public class Clock {
    private double startTime;

    public Clock() {
        reset();
    }

    public void reset() {
        startTime = org.lwjgl.glfw.GLFW.glfwGetTime();
    }

    public double getElapsedTime() {
        return org.lwjgl.glfw.GLFW.glfwGetTime() - startTime;
    }

    public boolean hasElapsed(double seconds) {
        return getElapsedTime() >= seconds;
    }
}
