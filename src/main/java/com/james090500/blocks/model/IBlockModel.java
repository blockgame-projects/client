package com.james090500.blocks.model;

import org.joml.Vector3i;

public interface IBlockModel {

    void create();

    void render(Vector3i position);
}
