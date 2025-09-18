package com.james090500.renderer;

import com.james090500.renderer.shaders.*;

public class ShaderManager {

    public static final Shader chunk = new ChunkShader();
    public static final Shader basicShader = new BasicShader();
    public static final Shader basicBlockShader = new BasicBlockShader();
    public static final Shader instancedBlockShader = new InstancedBlockShader();

}
