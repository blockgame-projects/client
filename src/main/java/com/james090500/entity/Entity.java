package com.james090500.entity;

import com.james090500.renderer.Renderer;
import org.joml.Vector3f;

public interface Entity {

    void setPosition(Vector3f pos);

    Renderer getModel();
}
