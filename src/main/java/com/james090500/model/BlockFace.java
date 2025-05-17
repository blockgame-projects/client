package com.james090500.model;

public class BlockFace {
    static float[] left_vertices = {
        // Position       // Texture  // Light
        0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f,
        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.8f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.8f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.8f,
        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.8f,
        0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f,
    };

    static float[] right_vertices = {
        // Position       // Texture  // Light
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.8f,
        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.8f,
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f,
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f,
        0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.8f,
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.8f,
    };

    static float[] back_vertices = {
        // Position       // Texture  // Light
        0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.8f,
        0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.8f,
        0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.8f,
        0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.8f,
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.8f,
        0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.8f,
    };

    static float[] front_vertices = {
        // Position       // Texture  // Light
        1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.8f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.8f,
        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.8f,
        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.8f,
        1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.8f,
        1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.8f,
    };

    static float[] bottom_vertices = {
        // Position       // Texture  // Light
        0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.5f,
        1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f,
        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.5f,
        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.5f,
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f,
        0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.5f,
    };

    static float[] top_vertices = {
        // Position       // Texture  // Light
        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
    };

    public enum Face
    {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    public float[] getFace(Face face)
    {
        return switch (face) {
            case Face.FRONT -> front_vertices;
            case Face.BACK -> back_vertices;
            case Face.LEFT -> left_vertices;
            case Face.RIGHT -> right_vertices;
            case Face.TOP -> top_vertices;
            case Face.BOTTOM -> bottom_vertices;
            default -> null;
        };
    }
}
