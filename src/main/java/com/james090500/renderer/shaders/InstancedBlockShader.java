package com.james090500.renderer.shaders;

public class InstancedBlockShader extends Shader {

    public InstancedBlockShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 aPos;
                layout(location = 1) in vec3 iWorldPos;
                layout(location = 2) in vec2 aUv;
                layout(location = 3) in uint aLayer;
                
                uniform mat4 view;
                uniform mat4 projection;
                
                out vec2 vUv;
                flat out uint vLayer;
                out vec3 mvPos;
                
                void main() {
                    vUv = aUv;
                    vLayer = aLayer;
            
                    vec3 world = aPos + iWorldPos;
            
                    // view-space position (for fog)
                    vec4 viewPos = view * vec4(world, 1.0);
            
                    gl_Position = projection * viewPos;
                    mvPos = viewPos.xyz;
                }
                """;

        String fragmentSrc =
                """
                #version 330 core
                
                in vec3 mvPos;
                in vec2 vUv;
                flat in uint vLayer;
                
                out vec4 FragColor;
                
                uniform sampler2DArray texArray;
                
                """ + GlobalShader.FOG_METHOD + """
                
                void main() {
                    vec4 color = texture(texArray, vec3(vUv, float(vLayer)));
                    if (color.a < 0.5) discard;
                
                    FragColor = calcFog(mvPos, color);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
