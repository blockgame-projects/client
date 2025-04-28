package com.james090500.client;

import com.james090500.BlockGame;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f();
        view.translate(new Vector3f(0.0f, 0.0f, -3.0f));
        return view;
    }

    public Matrix4f getProjectionMatrix() {
        float width = BlockGame.getInstance().getClientWindow().getWidth();
        float height = BlockGame.getInstance().getClientWindow().getHeight();
        float aspect = width / height;

        return new Matrix4f()
                .perspective((float) Math.toRadians(45.0f), aspect, 0.1f, 100.0f);
    }
}
