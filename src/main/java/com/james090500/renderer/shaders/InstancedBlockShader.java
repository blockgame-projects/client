package com.james090500.renderer.shaders;

public class InstancedBlockShader extends Shader {

    public InstancedBlockShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 aPos;       // 0..1 quad space
                layout(location = 1) in vec3 iWorldPos;  // instance world position (block coords)
                layout(location = 2) in vec2 texCord;
                
                uniform mat4 view;
                uniform mat4 projection;
                
                out vec3 mvPos;
                out vec2 vTexCord;
                
                void main() {
                    vTexCord = texCord;
  
                    vec3 world = aPos + iWorldPos;
                    vec4 modelViewMatrix = projection * view * vec4(world, 1.0);
                    gl_Position = modelViewMatrix;
                    mvPos = modelViewMatrix.xyz;
                }
                """;

        String fragmentSrc =
                """
                #version 330 core
                
                in vec3 mvPos;
                in vec2 vTexCord;
                
                out vec4 FragColor;
                
                uniform sampler2D tex;
                
                """ + GlobalShader.FOG_METHOD + """
                
                void main() {
                    vec4 color = texture(tex, vTexCord);
                    if (color.a < 0.5) discard;
                
                    FragColor = calcFog(mvPos, color);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
