package com.james090500.renderer.shaders;

public class ChunkShader extends Shader {

    public ChunkShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                layout(location = 1) in vec2 normal;
                layout(location = 2) in vec2 textureOffset;
                layout(location = 3) in float ao;
               
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
               
                out vec2 vNormal;
                out vec3 vPosition;
                out vec2 vTexOffset;
                out float vAo;
        
                void main() {
                    vNormal = normal;
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
                in vec2 vNormal;
                in vec3 vPosition;
                in vec2 vTexOffset;
                in float vAo;
                
                out vec4 FragColor;

                void main() {
                    vec2 tileSize = vec2(0.0625);
                    vec2 tileUV;
                    float faceLight = 1.0;

                    // Determine correct UV projection based on face normal
                    if (abs(vNormal.x) > 0.5) {  // Left/Right faces
                        faceLight = 0.8;
                        tileUV = vPosition.zy;
                    } else if (vNormal.y > 0.5) {  // Top Face
                        tileUV = vPosition.xz;
                    } else if (vNormal.y < -0.5) {  // Bottom Face
                        tileUV = vPosition.xz;
                        faceLight = 0.5;
                    } else {  // Front/Back faces
                        faceLight = 0.8;
                        tileUV = vPosition.xy;
                    }

                    vec2 texCoord = vTexOffset + tileSize * fract(tileUV);
                    vec4 texel = texture(baseTexture, texCoord);
                    float finalAO = mix(0.15, 1.0, vAo / 3.0);

                    FragColor = vec4(1.0, 1.0, 1.0, 1.0);
                    //FragColor = vec4(texel.rgb, texel.a);
                    //FragColor = vec4(texel.rgb * finalAO * faceLight, texel.a);
                    //FragColor = vec4(vec2(vAo / 3.0), 1.0, 1.0);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
