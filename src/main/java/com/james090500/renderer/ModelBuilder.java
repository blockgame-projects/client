package com.james090500.renderer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ModelBuilder {

    public record Model(int vao, int indicies) {};

    protected record Cube(float x, float y, float z, float width, float height, float depth) {
        public float[] getVertices() {
            float x0 = x;
            float y0 = y;
            float z0 = z;
            float x1 = x + width;
            float y1 = y + height;
            float z1 = z + depth;

            return new float[]{
                    // Front face
                    x0, y0, z1,   x1, y0, z1,   x1, y1, z1,   x0, y1, z1,
                    // Back face
                    x1, y0, z0,   x0, y0, z0,   x0, y1, z0,   x1, y1, z0,
                    // Left face
                    x0, y0, z0,   x0, y0, z1,   x0, y1, z1,   x0, y1, z0,
                    // Right face
                    x1, y0, z1,   x1, y0, z0,   x1, y1, z0,   x1, y1, z1,
                    // Top face
                    x0, y1, z1,   x1, y1, z1,   x1, y1, z0,   x0, y1, z0,
                    // Bottom face
                    x0, y0, z0,   x1, y0, z0,   x1, y0, z1,   x0, y0, z1
            };
        }

        public int[] getIndices() {
            return new int[] {
                0, 1, 2, 2, 3, 0,       // Front
                4, 5, 6, 6, 7, 4,       // Back
                8, 9, 10, 10, 11, 8,    // Left
                12, 13, 14, 14, 15, 12, // Right
                16, 17, 18, 18, 19, 16, // Top
                20, 21, 22, 22, 23, 20  // Bottom
            };
        }
    }
    protected record UV(float x, float z, float texture) {};

    private final ObjectArrayList<Cube> cubes = new ObjectArrayList<>();
    private final ObjectArrayList<UV> uvs = new ObjectArrayList<>();

    public static ModelBuilder create() {
        return new ModelBuilder();
    }

    public ModelBuilder addCube(float x, float y, float z, float width, float height, float depth) {
        cubes.add(new Cube(x, y, z, width, height, depth));
        return this;
    }

    public ModelBuilder setTexture(float x, float z, float texture) {
        uvs.add(new UV(x, z, texture));
        return this;
    }
    
    public Model build(float[] texCoords) {
        // 1. Precompute total length
        int totalVertices = 0;
        int totalIndices = 0;
        for (Cube cube : cubes) {
            totalVertices += cube.getVertices().length;
            totalIndices += cube.getIndices().length;
        }

        // 2. Allocate once
        float[] vertices = new float[totalVertices];
        int[] indices = new int[totalIndices];

        // 3. Copy into it sequentially
        int vertPos = 0;
        int indiPos = 0;
        for (Cube cube : cubes) {
            float[] verts = cube.getVertices();
            int[] indis = cube.getIndices();

            System.arraycopy(verts, 0, vertices, vertPos, verts.length);
            System.arraycopy(indis, 0, indices, indiPos, indis.length);

            vertPos += verts.length;
            indiPos += indis.length;
        }

        
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Position VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, MemoryUtil.memAllocFloat(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // UV VBO
        int tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, MemoryUtil.memAllocFloat(texCoords.length).put(texCoords).flip(), GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);

        // Index Buffer (EBO)
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, MemoryUtil.memAllocInt(indices.length).put(indices).flip(), GL_STATIC_DRAW);

        return new Model(vao, indices.length);
    }
}
