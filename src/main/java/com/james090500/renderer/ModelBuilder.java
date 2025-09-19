package com.james090500.renderer;

import com.james090500.textures.TextureLocation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;

public class ModelBuilder extends InstancedBlockRenderer {

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
    public ModelBuilder setUV(float[] texture) {
        this.setUV(6, texture);
        return this;
    }

    public ModelBuilder setTexture(TextureLocation[] texture) {
        int[] textures = new int[6];
        for(int i = 0; i < 6; i++) {
            textures[i] = texture[i].getId();
        }
        super.setTexture(textures);
        return this;
    }

    public ModelBuilder setTexture(TextureLocation texture) {
        int[] textures = new int[6];
        Arrays.fill(textures, texture.getId());
        super.setTexture(textures);
        return this;
    }

    /**
     * Build the cubes into a vertices array
     * @return The result
     */
    public ModelBuilder build() {
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

        this.setVertices(vertices);
        this.setIndices(indices);
        this.create();
        
        return this;
    }
}
