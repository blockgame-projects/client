package com.james090500.renderer.shaders;

public class BasicBlockShader extends Shader {

    public BasicBlockShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                layout(location = 1) in vec2 texCord;
                
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
                
                out vec2 vTexCord;
                
                void main() {
                    gl_Position = projection * view * model * vec4(position, 1.0);
                    vTexCord = texCord;
                }
                """;

        String fragmentSrc =
                """
                #version 330 core
                
                out vec4 FragColor;
                
                in vec2 vTexCord;
                
                uniform sampler2D tex;
                
                void main() {
                    FragColor = texture(tex, vTexCord);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
