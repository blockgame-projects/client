package com.james090500.renderer;

import lombok.Setter;
import org.joml.Vector3f;

public interface Renderer {

    Vector3f getPosition();

    Vector3f getBoundingBox();

    void render();
}
