package com.james090500.client;

import com.james090500.BlockGame;
import lombok.Getter;
import lombok.Setter;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    public float x, y, z;
    public float pitch, yaw;
    @Getter private float fov = 70f;
    @Getter @Setter
    private float aspect = 1f;
    private float near = 0.1f;
    private float far = 1000f;

    @Getter Matrix4f projectionMatrix;

    private final Matrix4f projViewMatrix = new Matrix4f();
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = 0;
        this.yaw = -90;

        this.setAspect((float) BlockGame.getInstance().getClientWindow().getFramebufferWidth() / BlockGame.getInstance().getClientWindow().getFramebufferHeight());
        this.updateProjectionMatrix();
    }

    public void updateProjectionMatrix() {
        this.projectionMatrix = new Matrix4f().perspective(
                (float) Math.toRadians(fov),
                aspect,
                near,
                far);
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPosition(int axis, float value) {
        switch(axis) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            case 2:
                z = value;
                break;
        }
    }

    public void setRotation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Vector3f getDirection() {
        float radYaw = (float) Math.toRadians(yaw);
        float radPitch = (float) Math.toRadians(pitch);

        float dx = (float) (Math.cos(radPitch) * Math.cos(radYaw));
        float dy = (float) Math.sin(radPitch);
        float dz = (float) (Math.cos(radPitch) * Math.sin(radYaw));

        return new Vector3f(dx, dy, dz);
    }

    public Matrix4f getViewMatrix() {
        Vector3f dir = getDirection();
        Vector3f position = new Vector3f(x, y, z);
        Vector3f target = new Vector3f(x + dir.x, y + dir.y, z + dir.z);
        Vector3f up = new Vector3f(0, 1, 0);

        return new Matrix4f().lookAt(position, target, up);
    }

    public Vector3f getPosition() {
        return new Vector3f(x, y, z);
    }

    /**
     * Update the current Frustum view
     */
    public void updateFrustum() {
        projViewMatrix.set(getProjectionMatrix()).mul(getViewMatrix());
        frustumIntersection.set(projViewMatrix);
    }

    /**
     * Check if inside the Fustrum
     * @param min
     * @param max
     * @return
     */
    public boolean insideFrustum(Vector3f min, Vector3f max) {
        return frustumIntersection.testAab(min, max);
    }
}
