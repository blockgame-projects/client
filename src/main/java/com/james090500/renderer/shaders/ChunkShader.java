package com.james090500.renderer.shaders;

public class ChunkShader extends Shader {

    public ChunkShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 aPos;
                layout(location = 1) in vec2 aUv;
                layout(location = 2) in uint aLayer;
                layout(location = 3) in float aAo;
                layout(location = 4) in uint aLight;

                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
               
                out vec2 vUv;
                flat out uint vLayer;
                out vec3 mvPos;
                out float vAo;
                flat out uint vLight;
        
                void main() {
                    vUv = aUv;
                    vLayer = aLayer;
                    vAo = aAo;
                    vLight = aLight;
        
                    vec4 modelViewMatrix = view * model * vec4(aPos, 1.0);
                    gl_Position = projection * modelViewMatrix;
                    mvPos = modelViewMatrix.xyz;
                }
               """;

        String fragmentSrc =
                """
                #version 330 core
                
                in vec3 mvPos;
                in vec2 vUv;
                flat in uint vLayer;
                in float vAo;
                flat in uint vLight;
                
                uniform sampler2DArray texArray;
                
                out vec4 FragColor;
                
                """ + GlobalShader.FOG_METHOD + """

                void main() {
                    float faceLight = (1.0 / 16) * vLight;

                    vec4 texel = texture(texArray, vec3(vUv, float(vLayer)));
                
                    float finalAO = mix(0.15, 1.0, vAo / 3.0);

                    vec4 finalColor = vec4(texel.rgb * finalAO * faceLight, texel.a);
                    FragColor = calcFog(mvPos, finalColor);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
