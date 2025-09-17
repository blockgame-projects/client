package com.james090500.world;

import com.james090500.BlockGame;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class Fog {

    private final Vector3f color;
    private final float start;
    private final float density;

    public Fog(Vector3f color, float start, float density) {
        this.color = color;
        this.start = getFogStart(start);
        this.density = density;
    }

    private float getFogStart(float start) {
        int worldSize = BlockGame.getInstance().getConfig().getUserOptions().getRenderDistance().getValue();
        return ((worldSize - 2) * 16) * start;
    }
}
