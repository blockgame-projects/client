package com.james090500.renderer.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
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

public class ChunkRenderer implements Renderer {

    private Chunk chunk;

    private int vao;
    private int vertexCount;

    public void mesh(Chunk chunk) {
        this.chunk = chunk;

        VoxelResult result = makeVoxels(new int[] { 0, 0, 0 }, new int[] { chunk.chunkSize, chunk.chunkHeight, chunk.chunkSize }, (x, y, z) -> {
            Block block = chunk.getBlock(x, y, z);
            if(block != null && !block.isTransparent()) {
                return block.getId();
            } else {
                return 0;
            }
        });

        ChunkMesh chunkMesh = this.generateMesh(result.dims, result.voxels);

        int numVertices = chunkMesh.vertices.size();
        int numFaces = chunkMesh.faces.size();

        // Allocate buffers with exact size
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(numVertices * 3 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        IntBuffer indexBuffer = ByteBuffer.allocateDirect(numFaces * 6 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer texOffsetBuffer = ByteBuffer.allocateDirect(numFaces * 2 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        IntBuffer aoBuffer = ByteBuffer.allocateDirect(numFaces * 4 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();


        for (int i = 0; i < chunkMesh.vertices.size(); i++) {
            int[] v = chunkMesh.vertices.get(i);
            vertexBuffer.put((float) v[0]);
            vertexBuffer.put((float) v[1]);
            vertexBuffer.put((float) v[2]);
        }

        for (int i = 0; i < chunkMesh.faces.size(); i++) {
            MeshFace face = chunkMesh.faces.get(i);

            int[] fi = face.faceIndicies;
            indexBuffer.put(fi[0]);
            indexBuffer.put(fi[1]);
            indexBuffer.put(fi[2]);
            indexBuffer.put(fi[0]);
            indexBuffer.put(fi[2]);
            indexBuffer.put(fi[3]);

            int[] to = face.texOffset;
            texOffsetBuffer.put(to[0]);
            texOffsetBuffer.put(to[1]);

            int[] ao = face.faceAO;
            aoBuffer.put(ao[0]);
            aoBuffer.put(ao[1]);
            aoBuffer.put(ao[2]);
            aoBuffer.put(ao[3]);
        }

        // Flip for OpenGL
        vertexBuffer.flip();
        indexBuffer.flip();
        texOffsetBuffer.flip();
        aoBuffer.flip();

        vertexCount = indexBuffer.limit();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        int ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);

        glBindVertexArray(0); // unbind for safety
    }

    @Override
    public void render() {
        Matrix4f model = new Matrix4f();

        ShaderManager.chunk.use();
        ShaderManager.chunk.setUniformMat4("model", model);
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);
        ShaderManager.chunk.stop();
    }

    private static class ChunkMesh {
        ArrayList<int[]> vertices;
        ArrayList<MeshFace> faces;

        public ChunkMesh(ArrayList<int[]> vertices, ArrayList<MeshFace> faces) {
            this.vertices = vertices;
            this.faces = faces;
        }
    }

    private static class MeshFace {
        int[] faceIndicies;
        int[] texOffset;
        int[] faceAO;

        public MeshFace(int[] faceIndices, int[] texOffset, int[] faceAO) {
            this.faceIndicies = faceIndices;
            this.texOffset = texOffset;
            this.faceAO = faceAO;
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
        Block blockA = Blocks.ids[id];
        Block neighbor = BlockGame.getInstance().getWorld().getChunkBlock(
                chunk.chunkX,
                chunk.chunkZ,
                pos[0] + step[0],
                pos[1] + step[1],
                pos[2] + step[2]
        );

        return neighbor != null && (!neighbor.isTransparent() || blockA.isTransparent()) ? 0 : id;
    }

    private ChunkMesh generateMesh(int[] dims, int[] voxels) {
        int[] mask = new int[0];
        int[] aoMask = new int[0];

        ArrayList<int[]> vertices = new ArrayList<>();
        ArrayList<MeshFace> faces = new ArrayList<>();

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

                            if (blockId > 0) {
                                du[u] = width;
                                dv[v] = height;
                            } else {
                                blockId = -blockId;
                                aoVal = -aoVal;
                                dv[u] = width;
                                du[v] = height;
                            }

                            // Determine face orientation
                            boolean isPositiveFace = mask[n] > 0;

                            // Get block and texture offset
                            Block block = Blocks.ids[blockId];
                            int[] texOffset = block.getTexture();
                            if (axis == 1) {
                                texOffset = block.getTexture(isPositiveFace ? "top" : "bottom");
                            }

                            // Prepare base vertex count and corners
                            int vCount = vertices.size();
                            int[] v0 = new int[]{pos[0], pos[1], pos[2]};
                            int[] v1 = new int[]{pos[0] + du[0], pos[1] + du[1], pos[2] + du[2]};
                            int[] v2 = new int[]{v1[0] + dv[0], v1[1] + dv[1], v1[2] + dv[2]};
                            int[] v3 = new int[]{v0[0] + dv[0], v0[1] + dv[1], v0[2] + dv[2]};

                            // Extract AO values
                            int[] ao = new int[]{
                                    (aoVal >> 0) & 0b11,
                                    (aoVal >> 2) & 0b11,
                                    (aoVal >> 4) & 0b11,
                                    (aoVal >> 6) & 0b11,
                            };

                            // Rotate AO per axis and face direction
                            int[] faceAO = isPositiveFace ? new int[]{ao[2], ao[3], ao[1], ao[0]} : new int[]{ao[2], ao[0], ao[1], ao[3]};
                            if (axis == 1) {
                                faceAO = isPositiveFace ? new int[]{ao[2], ao[0], ao[1], ao[3]} : new int[]{ao[2], ao[3], ao[1], ao[0]};
                            }

                            // Choose diagonal based on AO
                            int[] faceIndices;
                            boolean flipDiagonal = ao[0] + ao[3] > ao[1] + ao[2];

                            if (flipDiagonal) {
                                vertices.add(v3);
                                vertices.add(v2);
                                vertices.add(v1);
                                vertices.add(v0);

                                faceIndices = new int[]{
                                        vCount + 2,
                                        vCount + 1,
                                        vCount,
                                        vCount + 3,
                                };

                                // Reverse AO
                                faceAO = new int[]{faceAO[3], faceAO[2], faceAO[1], faceAO[0]};
                            } else {
                                vertices.add(v0);
                                vertices.add(v1);
                                vertices.add(v2);
                                vertices.add(v3);

                                faceIndices = new int[]{
                                        vCount,
                                        vCount + 1,
                                        vCount + 2,
                                        vCount + 3,
                                };
                            }

                            // Push face data
                            faces.add(new MeshFace(faceIndices, texOffset, faceAO));

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
        return new ChunkMesh(vertices, faces);
    }
}