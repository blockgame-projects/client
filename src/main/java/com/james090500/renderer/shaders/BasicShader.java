package com.james090500.renderer.shaders;

public class BasicShader extends Shader {

    public BasicShader() {
        String vertexSrc =
                """
                #version 330 core
                layout(location = 0) in vec3 position;
                
                uniform vec3 color;
                
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
                
                out vec3 vColor;
                
                void main() {
                    vColor = color;
                    gl_Position = projection * view * model * vec4(position, 1.0);
                }
                """;

        String fragmentSrc =
                """
                #version 330 core
                
                in vec3 vColor;
                
                out vec4 FragColor;
                
                void main() {
                    FragColor = vec4(vColor, 1.0);
                }
                """;

        this.create(vertexSrc, fragmentSrc);
    }
}
