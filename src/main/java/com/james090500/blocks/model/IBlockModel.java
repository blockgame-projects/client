package com.james090500.blocks.model;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public interface IBlockModel {

    void create();

    void render(ObjectList<Vector3i> positions);
}
