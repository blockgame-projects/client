package com.james090500.renderer.shaders;

public class ChunkShader extends Shader {

    public ChunkShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                layout(location = 1) in vec2 uv;
                layout(location = 2) in vec2 texOffset;
                layout(location = 3) in float ao;
               
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
               
                out vec2 vUv;
                out vec2 vTexOffset;
                out float vAo;
        
                void main() {
                    vUv = uv;
                    vTexOffset = texOffset;
                    vAo = ao;
        
                    gl_Position = projection * view * model * vec4(position, 1.0);
                }
               """;

        String fragmentSrc =
                """
                #version 330 core
                
                uniform sampler2D baseTexture;

                in vec2 vUv;
                in vec2 vTexOffset;
                in float vAo;
                
                out vec4 FragColor;

                void main() {
                    vec2 tileSize = vec2(0.0625);
                    float faceLight = 1.0;

                    vec2 texCoord = fract(vUv) * tileSize + vTexOffset;
                    vec4 texel = texture(baseTexture, texCoord);
                
                    float finalAO = mix(0.15, 1.0, vAo / 3.0);

                    FragColor = vec4(texel.rgb * finalAO * faceLight, texel.a);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
