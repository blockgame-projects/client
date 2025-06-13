package com.james090500.renderer;

import com.james090500.renderer.shaders.BasicBlockShader;
import com.james090500.renderer.shaders.BasicShader;
import com.james090500.renderer.shaders.ChunkShader;
import com.james090500.renderer.shaders.Shader;

public class ShaderManager {

    public static final Shader chunk = new ChunkShader();
    public static final Shader basicShader = new BasicShader();
    public static final Shader basicBlockShader = new BasicBlockShader();

}
