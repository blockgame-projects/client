package com.james090500.client;

import com.james090500.BlockGame;
import lombok.Getter;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    public float x, y, z;
    public float pitch, yaw;
    private float fov = 70f;
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

        this.setAspectRatio((float) BlockGame.getInstance().getClientWindow().getFramebufferWidth() / BlockGame.getInstance().getClientWindow().getFramebufferHeight());
        this.updateProjectionMatrix();
    }

    public void updateProjectionMatrix() {
        this.projectionMatrix = new Matrix4f().perspective(
                (float) Math.toRadians(fov),
                aspect,
                near,
                far);
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

        public float[] getDirection() {
        float radYaw = (float) Math.toRadians(yaw);
        float radPitch = (float) Math.toRadians(pitch);

        float dx = (float) (Math.cos(radPitch) * Math.cos(radYaw));
        float dy = (float) Math.sin(radPitch);
        float dz = (float) (Math.cos(radPitch) * Math.sin(radYaw));

        return new float[]{dx, dy, dz};
    }

    public Matrix4f getViewMatrix() {
        float[] dir = getDirection();
        Vector3f position = new Vector3f(x, y, z);
        Vector3f target = new Vector3f(x + dir[0], y + dir[1], z + dir[2]);
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
        projViewMatrix.set(getProjectionMatrix());
        projViewMatrix.mul(getViewMatrix());
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

    public void setAspectRatio(float aspect) {
        this.aspect = aspect;
    }
}
