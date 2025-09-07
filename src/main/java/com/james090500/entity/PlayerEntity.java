package com.james090500.entity;

import com.james090500.entity.model.PlayerModel;
import com.james090500.renderer.Renderer;
import org.joml.Vector3f;

public class PlayerEntity implements Entity {

    PlayerModel playerModel = new PlayerModel();
    Vector3f position = new Vector3f();

    public void setPosition(Vector3f pos) {
        this.position = pos;
        playerModel.setPosition(pos);
    }

    @Override
    public Renderer getModel() {
        return this.playerModel;
    }
}
