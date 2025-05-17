package com.james090500.renderer.shaders;

public class ChunkShader extends Shader {

    public ChunkShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                layout(location = 1) in vec2 textureOffset;
                layout(location = 2) in float ao;
               
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
               
                out vec3 vPosition;
                out vec2 vTexOffset;
                out float vAo;
        
                void main() {
                    vPosition = position;
                    vTexOffset = textureOffset;
                    vAo = ao;
        
                    gl_Position = projection * view * model * vec4(position, 1.0);
                }
               """;

        String fragmentSrc =
                """
                #version 330 core
                
                uniform sampler2D baseTexture;

                in vec2 vUv;
                in vec3 vPosition;
                in vec2 vTexOffset;
                in float vAo;
                
                out vec4 FragColor;

                void main() {
                    vec2 tileSize = vec2(0.0625);
                    float faceLight = 1.0;

                    vec4 texel = texture(baseTexture, vTexOffset);
                    //float finalAO = mix(0.15, 1.0, vAo / 3.0);
                    float finalAO = 1.0;

                    FragColor = vec4(texel.rgb * finalAO * faceLight, texel.a);
                    //FragColor = vec4(1.0, 0.0, 0.0, 1.0);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
