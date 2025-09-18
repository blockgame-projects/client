package com.james090500.renderer.shaders;

public class ChunkShader extends Shader {

    public ChunkShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                layout(location = 1) in vec2 texCord;
                layout(location = 2) in float ao;
               
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
               
                out vec3 mvPos;
                out vec2 vTexCord;
                out float vAo;
        
                void main() {
                    vTexCord = texCord;
                    vAo = ao;
        
                    vec4 modelViewMatrix = view * model * vec4(position, 1.0);
                    gl_Position = projection * modelViewMatrix;
                    mvPos = modelViewMatrix.xyz;
                }
               """;

        String fragmentSrc =
                """
                #version 330 core
                
                uniform sampler2D baseTexture;

                in vec3 mvPos;
                in vec2 vTexCord;
                in float vAo;
                
                out vec4 FragColor;
                
                """ + GlobalShader.FOG_METHOD + """

                void main() {
                    float faceLight = 1.0;

                    vec4 texel = texture(baseTexture, vTexCord);
                
                    float finalAO = mix(0.15, 1.0, vAo / 3.0);

                    vec4 finalColor = vec4(texel.rgb * finalAO * faceLight, texel.a);
                    FragColor = calcFog(mvPos, finalColor);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
