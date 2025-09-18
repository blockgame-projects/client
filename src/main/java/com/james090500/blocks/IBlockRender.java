package com.james090500.blocks;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public interface IBlockRender {

    void render(ObjectList<Vector3i> position);

}
