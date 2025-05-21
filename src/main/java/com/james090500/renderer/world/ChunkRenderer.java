package com.james090500.renderer.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.renderer.LayeredRenderer;
import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import com.james090500.utils.ThreadUtil;
import com.james090500.world.Chunk;
import com.james090500.world.World;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class ChunkRenderer implements LayeredRenderer {

    private Chunk chunk;

    private int solidVAO;
    private int solidVertexCount;

    private int transVAO;
    private int transVertexCount;

    public ChunkRenderer(Chunk chunk) {
        this.chunk = chunk;
    }

    public void mesh() {
        ThreadUtil.getQueue().submit(() -> {
            VoxelResult result = makeVoxels(new int[]{0, 0, 0}, new int[]{chunk.chunkSize, chunk.chunkHeight, chunk.chunkSize}, (x, y, z) -> {
                Block block = chunk.getBlock(x, y, z);
                if (block != null && !block.isTransparent()) {
                    return block.getId();
                } else {
                    return 0;
                }
            });

            ChunkMesh chunkMesh = this.generateMesh(result.dims, result.voxels);

            ThreadUtil.getMainQueue().add(() -> {
                this.createMesh(chunkMesh, false);
            });
        });
    }

    public void meshTransparent() {
        ThreadUtil.getQueue().submit(() -> {
            VoxelResult result = makeVoxels(new int[]{0, 0, 0}, new int[]{chunk.chunkSize, chunk.chunkHeight, chunk.chunkSize}, (x, y, z) -> {
                Block block = chunk.getBlock(x, y, z);
                if (block != null && block.isTransparent()) {
                    return block.getId();
                } else {
                    return 0;
                }
            });

            ChunkMesh chunkMesh = this.generateMesh(result.dims, result.voxels);

            ThreadUtil.getMainQueue().add(() -> {
                this.createMesh(chunkMesh, true);
            });
        });
    }

    private void createMesh(ChunkMesh chunkMesh, boolean transparent) {
        int numVertices = chunkMesh.vertices.size();
        int numFaces = chunkMesh.indices.size();

        // Allocate buffers with exact size
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(numVertices * 3 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        IntBuffer indexBuffer = ByteBuffer.allocateDirect(numFaces * 6 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        FloatBuffer uvBuffer = ByteBuffer.allocateDirect(numVertices * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer texOffsetBuffer = ByteBuffer.allocateDirect(numVertices * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer aoBuffer = ByteBuffer.allocateDirect(numVertices * 1 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < chunkMesh.vertices.size(); i++) {
            float[] v = chunkMesh.vertices.get(i);
            vertexBuffer.put(v[0]);
            vertexBuffer.put(v[1]);
            vertexBuffer.put(v[2]);
        }

        for(int i = 0; i < chunkMesh.indices.size(); i++) {
            int[] in = chunkMesh.indices.get(i);
            indexBuffer.put(in[0]);
            indexBuffer.put(in[1]);
            indexBuffer.put(in[2]);
            indexBuffer.put(in[0]);
            indexBuffer.put(in[2]);
            indexBuffer.put(in[3]);
        }

        for (int i = 0; i < chunkMesh.uvs.size(); i++) {
            float[] t = chunkMesh.uvs.get(i);
            for (int j = 0; j < 8; j++) {
                uvBuffer.put(t[j]);
            }
        }


        for (int i = 0; i < chunkMesh.texOffset.size(); i++) {
            float[] t = chunkMesh.texOffset.get(i);
            for (int j = 0; j < 4; j++) {
                texOffsetBuffer.put(t[0]);
                texOffsetBuffer.put(t[1]);
            }
        }

        for (int i = 0; i < chunkMesh.aos.size(); i++) {
            float[] a = chunkMesh.aos.get(i);
            aoBuffer.put(a[0]);
            aoBuffer.put(a[1]);
            aoBuffer.put(a[2]);
            aoBuffer.put(a[3]);
        }

        // Flip for OpenGL
        vertexBuffer.flip();
        indexBuffer.flip();
        uvBuffer.flip();
        texOffsetBuffer.flip();
        aoBuffer.flip();

        if(transparent) {
            transVertexCount = indexBuffer.limit();
        } else {
            solidVertexCount = indexBuffer.limit();
        }

        if(transparent) {
            transVAO = glGenVertexArrays();
            glBindVertexArray(transVAO);
        } else {
            solidVAO = glGenVertexArrays();
            glBindVertexArray(solidVAO);
        }

        // --- Vertex Positions (Attribute 0) ---
        int vertexVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // --- UV (Attribute 1) ---
        int uvVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvVBO);
        glBufferData(GL_ARRAY_BUFFER, uvBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        // --- Texture Offset (Attribute 2) ---
        int texOffsetVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texOffsetVBO);
        glBufferData(GL_ARRAY_BUFFER, texOffsetBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);

        // --- Ambient Occlusion (Attribute 3) ---
        int aoVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, aoVBO);
        glBufferData(GL_ARRAY_BUFFER, aoBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 0, 0);

        // --- Index Buffer ---
        int ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Unbind VAO (good practice)
        glBindVertexArray(0);
    }

    @Override
    public void render() {
        Matrix4f model = new Matrix4f().translate(chunk.chunkX * chunk.chunkSize, 0, chunk.chunkZ * chunk.chunkSize);

        ShaderManager.chunk.use();
        ShaderManager.chunk.setUniformMat4("model", model);

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(transVAO);
        glDrawElements(GL_TRIANGLES, transVertexCount, GL_UNSIGNED_INT, 0L);

        glBindVertexArray(solidVAO);
        glDrawElements(GL_TRIANGLES, solidVertexCount, GL_UNSIGNED_INT, 0L);

        glBindVertexArray(0);
        ShaderManager.chunk.stop();
    }

    @Override
    public void renderTransparent() {
        Matrix4f model = new Matrix4f().translate(chunk.chunkX * chunk.chunkSize, 0, chunk.chunkZ * chunk.chunkSize);

        ShaderManager.chunk.use();
        ShaderManager.chunk.setUniformMat4("model", model);

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(transVAO);
        glDrawElements(GL_TRIANGLES, transVertexCount, GL_UNSIGNED_INT, 0L);

        glBindVertexArray(0);
        ShaderManager.chunk.stop();
    }

    private static class ChunkMesh {
        ArrayList<float[]> vertices;
        ArrayList<int[]> indices;
        ArrayList<float[]> uvs;
        ArrayList<float[]> texOffset;
        ArrayList<float[]> aos;

        public ChunkMesh(
                ArrayList<float[]> vertices,
                ArrayList<int[]> indices,
                ArrayList<float[]> uvs,
                ArrayList<float[]> texOffset,
                ArrayList<float[]> aos
        ) {
            this.vertices = vertices;
            this.indices = indices;
            this.uvs = uvs;
            this.texOffset = texOffset;
            this.aos = aos;
        }
    }

    /**
     * A private callback class for makeVoxels
     */
    private static class VoxelResult {
        public int[] voxels;
        public int[] dims;

        public VoxelResult(int[] voxels, int[] dims) {
            this.voxels = voxels;
            this.dims = dims;
        }
    }

    @FunctionalInterface
    interface VoxelFunction {
        int apply(int x, int y, int z);
    }

    /**
     * Create the voxels which should be renderer
     *
     * @param l
     * @param h
     * @param f
     */
    private static VoxelResult makeVoxels(int[] l, int[] h, VoxelFunction f) {
        int[] dims = new int[]{
                h[0] - l[0],
                h[1] - l[1],
                h[2] - l[2]
        };

        int totalSize = dims[0] * dims[1] * dims[2];
        int[] voxels = new int[totalSize];
        int n = 0;

        for (int k = l[2]; k < h[2]; ++k) {
            for (int j = l[1]; j < h[1]; ++j) {
                for (int i = l[0]; i < h[0]; ++i, ++n) {
                    voxels[n] = f.apply(i, j, k);
                }
            }
        }

        return new VoxelResult(voxels, dims);
    }


    // Helper to get block ID at a voxel coordinate
    private int getVoxel(int[] dims, int [] voxels, int x, int y, int z) {
        return voxels[x + dims[0] * (y + dims[1] * z)];
    }

    private int getAo(int[] currentPos, int[] step, int axis, boolean neg) {
        int[] aoLevels = new int[4];
        int x = currentPos[0];
        int y = currentPos[1];
        int z = currentPos[2];

        // Determine other axes (axes not equal to the given axis)
        int a1 = (axis + 1) % 3;
        int a2 = (axis + 2) % 3;

        int[][] cornerOffsets = {
                {-1, 1}, // TL
                {1, 1}, // TR
                {-1, -1}, // BL
                {1, -1}  // BR
        };

        for (int i = 0; i < 4; i++) {
            int s1 = cornerOffsets[i][0];
            int s2 = cornerOffsets[i][1];

            int[] side1 = neg ? new int[]{-step[0], -step[1], -step[2]} : new int[]{0, 0, 0};
            int[] side2 = neg ? new int[]{-step[0], -step[1], -step[2]} : new int[]{0, 0, 0};
            int[] corner = neg ? new int[]{-step[0], -step[1], -step[2]} : new int[]{0, 0, 0};

            side1[a1] += s1;
            side2[a2] += s2;
            corner[a1] += s1;
            corner[a2] += s2;

            World world = BlockGame.getInstance().getWorld();
            Block b1 = world.getChunkBlock(chunk.chunkX, chunk.chunkZ, x + side1[0], y + side1[1], z + side1[2]);
            Block b2 = world.getChunkBlock(chunk.chunkX, chunk.chunkZ, x + side2[0], y + side2[1], z + side2[2]);
            Block b3 = world.getChunkBlock(chunk.chunkX, chunk.chunkZ, x + corner[0], y + corner[1], z + corner[2]);

            int hasSide1 = (b1 != null && !b1.isTransparent()) ? 1 : 0;
            int hasSide2 = (b2 != null && !b2.isTransparent()) ? 1 : 0;
            int hasCorner = (b3 != null && !b3.isTransparent()) ? 1 : 0;

            int ao = (hasSide1 == 1 && hasSide2 == 1) ? 0 : 3 - (hasSide1 + hasSide2 + hasCorner);
            aoLevels[i] = ao;
        }

        return ((aoLevels[0] & 0b11) << 0) |  // TL
                ((aoLevels[1] & 0b11) << 2) |  // TR
                ((aoLevels[2] & 0b11) << 4) |  // BL
                ((aoLevels[3] & 0b11) << 6);   // BR
    }

    private int getMaskValue(int id, int[] pos, int[] step) {
        Block block = Blocks.ids[id];
        Block neighbor = BlockGame.getInstance().getWorld().getChunkBlock(
                chunk.chunkX,
                chunk.chunkZ,
                pos[0] + step[0],
                pos[1] + step[1],
                pos[2] + step[2]
        );

        // neighbor is missing, so render the face
        if (neighbor == null) {
            return id;
        }

        // Hide face if both blocks are transparent (internal face)
        if (block.isTransparent() && neighbor.isTransparent()) {
            return 0;
        }

        // Hide face if neighbor is opaque
        if (!neighbor.isTransparent()) {
            return 0;
        }

        // Otherwise, show the face
        return id;
    }

    private ChunkMesh generateMesh(int[] dims, int[] voxels) {
        int[] mask = new int[0];
        int[] aoMask = new int[0];

        ArrayList<float[]> vertices = new ArrayList<>();
        ArrayList<int[]> indices = new ArrayList<>();
        ArrayList<float[]> uvs = new ArrayList<>();
        ArrayList<float[]> textures = new ArrayList<>();
        ArrayList<float[]> aos = new ArrayList<>();

        // Sweep across 3 dimensions: X, Y, Z (0, 1, 2)
        for (int axis = 0; axis < 3; ++axis) {
            int u = (axis + 1) % 3;
            int v = (axis + 2) % 3;
            int[] pos = new int[]{0, 0, 0};
            int[] step = new int[]{0, 0, 0};
            step[axis] = 1;

            // Resize the masks
            if (mask.length < dims[u] * dims[v]) {
                mask = new int[dims[u] * dims[v]];
                aoMask = new int[dims[u] * dims[v]];
            }

            for (pos[axis] = -1; pos[axis] < dims[axis]; ) {
                int n = 0;

                // Build the mask
                for (pos[v] = 0; pos[v] < dims[v]; ++pos[v]) {
                    for (pos[u] = 0; pos[u] < dims[u]; ++pos[u], ++n) {
                        int currID = pos[axis] >= 0 ? this.getVoxel(dims, voxels, pos[0], pos[1], pos[2]) : 0;
                        int[] nextPos = new int[]{
                                pos[0] + step[0],
                                pos[1] + step[1],
                                pos[2] + step[2],
                        };
                        int nextID = pos[axis] < dims[axis] - 1 ? this.getVoxel(dims, voxels, nextPos[0], nextPos[1], nextPos[2]) : 0;

                        if ((currID != 0) == (nextID != 0)) {
                            mask[n] = 0;
                            aoMask[n] = 3;
                        } else {
                            // Generate an AO for the block, the value will be a bitwise total unique to the AO pattern
                            if (currID != 0) {
                                mask[n] = getMaskValue(currID, pos, step);
                                aoMask[n] = this.getAo(
                                        nextPos,
                                        step,
                                        axis,
                                        false
                                );
                            } else {
                                mask[n] = -getMaskValue(nextID, pos, new int[] { 0, 0, 0 });
                                aoMask[n] = -this.getAo(
                                        nextPos,
                                        step,
                                        axis,
                                        true
                                );
                            }
                        }
                    }
                }

                ++pos[axis];

                // Generate quads
                n = 0;
                for (int j = 0; j < dims[v]; ++j) {
                    for (int i = 0; i < dims[u]; ) {
                        int blockId = mask[n];
                        int aoVal = aoMask[n];
                        if (blockId != 0) {
                            // Calculate quad width
                            int width = 1;
                            while (i + width < dims[u] && blockId == mask[n + width] && aoVal == aoMask[n + width]) {
                                ++width;
                            }

                            // Calculate quad height
                            int height = 1;
                            boolean stop = false;
                            while (j + height < dims[v]) {
                                for (int k = 0; k < width; ++k) {
                                    if (blockId != mask[n + k + height * dims[u]] || aoVal != aoMask[n + k + height * dims[u]]) {
                                        stop = true;
                                        break;
                                    }
                                }
                                if (stop) break;
                                ++height;
                            }

                            // Construct quad
                            pos[u] = i;
                            pos[v] = j;
                            int[] du = new int[]{0, 0, 0};
                            int[] dv = new int[]{0, 0, 0};

                            // Determine face orientation
                            boolean isPositiveFace;

                            if (blockId > 0) {
                                isPositiveFace = true;
                                du[u] = width;
                                dv[v] = height;
                            } else {
                                isPositiveFace = false;
                                blockId = -blockId;
                                aoVal = -aoVal;
                                dv[u] = width;
                                du[v] = height;
                            }

                            // Get block and texture offset
                            Block block = Blocks.ids[blockId];
                            float[] texOffset = block.getTexture();
                            if (axis == 1) {
                                texOffset = block.getTexture(isPositiveFace ? "top" : "bottom");
                            }

                            // Prepare base vertex count and corners
                            int vCount = vertices.size();
                            float[] v0 = new float[]{pos[0], pos[1], pos[2]};
                            float[] v1 = new float[]{pos[0] + du[0], pos[1] + du[1], pos[2] + du[2]};
                            float[] v2 = new float[]{v1[0] + dv[0], v1[1] + dv[1], v1[2] + dv[2]};
                            float[] v3 = new float[]{v0[0] + dv[0], v0[1] + dv[1], v0[2] + dv[2]};

                            // Extract AO values
                            float[] ao = new float[]{
                                    (aoVal >> 0) & 0b11,
                                    (aoVal >> 2) & 0b11,
                                    (aoVal >> 4) & 0b11,
                                    (aoVal >> 6) & 0b11,
                            };

                            // Rotate AO per axis and face direction
                            float[] faceAO = isPositiveFace ? new float[] { ao[2], ao[3], ao[1], ao[0] } : new float[] { ao[2], ao[0], ao[1], ao[3] };

                            // Choose diagonal based on AO
                            boolean flipDiagonal = ao[0] + ao[3] > ao[1] + ao[2];

                            //UV
                            float[] uv = new float[8];

                            switch (axis) {
                                case 0 -> uv = isPositiveFace
                                    ? new float[]{0, 0, 0, width, height, width, height, 0}
                                    : new float[]{height, 0, 0, 0, 0, width, height, width};
                                case 1 -> uv = isPositiveFace
                                    ? new float[]{width, 0, 0, 0, 0, height, width, height}
                                    : new float[]{height, 0, 0, 0, 0, width, height, width};
                                case 2 -> uv = isPositiveFace
                                    ? new float[]{width, 0, 0, 0, 0, height, width, height}
                                    : new float[]{0, 0, 0, height, width, height, width, 0};
                            }

                            if (flipDiagonal) {
                                // Add vertices
                                vertices.add(v3);
                                vertices.add(v2);
                                vertices.add(v1);
                                vertices.add(v0);
                                
                                // Add Indices
                                indices.add(new int[]{
                                        vCount + 2,
                                        vCount + 1,
                                        vCount,
                                        vCount + 3,
                                });

                                // Reverse AO
                                faceAO = new float[] { faceAO[3], faceAO[2], faceAO[1], faceAO[0] };

                                // Reverse UV
                                uv = new float[] { uv[6], uv[7], uv[4], uv[5], uv[2], uv[3], uv[0], uv[1] };
                            } else {
                                // Add Vertices
                                vertices.add(v0);
                                vertices.add(v1);
                                vertices.add(v2);
                                vertices.add(v3);

                                // Add indices
                                indices.add(new int[]{
                                        vCount,
                                        vCount + 1,
                                        vCount + 2,
                                        vCount + 3,
                                });
                            }

                            // Tex Offset
                            textures.add(texOffset);

                            // AO Values
                            aos.add(faceAO);

                            // UV
                            uvs.add(uv);

                            // Zero the mask
                            for (int dy = 0; dy < height; ++dy) {
                                for (int dx = 0; dx < width; ++dx) {
                                    mask[n + dx + dy * dims[u]] = 0;
                                    aoMask[n + dx + dy * dims[u]] = 0;
                                }
                            }

                            i += width;
                            n += width;
                        } else {
                            ++i;
                            ++n;
                        }
                    }
                }
            }
        }
        return new ChunkMesh(vertices, indices, uvs, textures, aos);
    }
}