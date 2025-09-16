package com.james090500.renderer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.system.MemoryUtil;

import java.util.Arrays;

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
            float blockWidthX = x + width;
            float blockWidthZ = z + width;
            float blockHeight = y + height;
            
            float blockDepthZ = z + depth;
            float blockDepthNeg = width - depth;
            float blockDepthX = x + depth;

            return new float[]{
                    // Front face
                    x, y, blockDepthZ,  blockWidthX, y, blockDepthZ,   blockWidthX, blockHeight, blockDepthZ,   x, blockHeight, blockDepthZ,
                    // Back face
                    blockWidthX, y, blockDepthNeg,   x, y, blockDepthNeg,   x, blockHeight, blockDepthNeg,   blockWidthX, blockHeight, blockDepthNeg,
                    // Left face
                    blockDepthNeg, y, z,   blockDepthNeg, y, blockWidthZ,   blockDepthNeg, blockHeight, blockWidthZ,   blockDepthNeg, blockHeight, z,
                    // Right face
                    blockDepthX, y, blockWidthZ,   blockDepthX, y, z,   blockDepthX, blockHeight, z,   blockDepthX, blockHeight, blockWidthZ,
                    // Top face
                    x, blockHeight, blockWidthZ,   blockWidthX, blockHeight, blockWidthZ,   blockWidthX, blockHeight, z,   x, blockHeight, z,
                    // Bottom face
                    x, y, z,   blockWidthX, y, z,   blockWidthX, y, blockWidthZ,   x, y, blockWidthZ
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

    private final ObjectArrayList<Cube> cubes = new ObjectArrayList<>();
    private float[] texCoords;

    /**
     * Create a new instance
     * @return
     */
    public static ModelBuilder create() {
        return new ModelBuilder();
    }

    /**
     * Add a cube to the builder
     * @param x Start X position
     * @param y Start Y position
     * @param z Start z position
     * @param width Width of the cube
     * @param height Height of the cube
     * @param depth Depth of the cube
     * @return ModelBuilder instance
     */
    public ModelBuilder addCube(float x, float y, float z, float width, float height, float depth) {
        cubes.add(new Cube(x, y, z, width, height, depth));
        return this;
    }

    /**
     * Adds the UV texture to the cube
     * @param texture The texture to add to all sides
     * @return ModelBuilder instance
     */
    public ModelBuilder setTexture(float[] texture) {
        float[][] uvBases = new float[][] { texture, texture, texture, texture, texture, texture };
        return setTexture(uvBases);
    }

    /**
     * Adds the UV texture to the cube
     * @param uvBases The float array of texture order
     * @return ModelBuilder instance
     */
    public ModelBuilder setTexture(float[][] uvBases) {
        float tileSize = 1.0f / 16.0f;
        texCoords = new float[24 * 2];
        for (int face = 0; face < 6; face++) {
            float u0 = uvBases[face][0];
            float v0 = uvBases[face][1];
            int dest = face * 8; // 4 verts * 2 coords

            // bottom-left  (0,0)
            texCoords[dest + 0] = u0 + 0f * tileSize;
            texCoords[dest + 1] = v0 + 0f * tileSize;

            // bottom-right (1,0)
            texCoords[dest + 2] = u0 + tileSize;
            texCoords[dest + 3] = v0 + 0f * tileSize;

            // top-right    (1,1)
            texCoords[dest + 4] = u0 + tileSize;
            texCoords[dest + 5] = v0 + tileSize;

            // top-left     (0,1)
            texCoords[dest + 6] = u0 + 0f * tileSize;
            texCoords[dest + 7] = v0 + tileSize;
        }
        return this;
    }

    /**
     * Build the cubes into a vertices array
     * @return The result
     */
    public Model build() {
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
