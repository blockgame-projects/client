package com.james090500.renderer.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.blocks.IBlockRender;
import com.james090500.renderer.LayeredRenderer;
import com.james090500.renderer.RenderManager;
import com.james090500.renderer.ShaderManager;
import com.james090500.textures.TextureLocation;
import com.james090500.utils.ThreadUtil;
import com.james090500.world.Chunk;
import com.james090500.world.ChunkStatus;
import com.james090500.world.World;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class ChunkRenderer implements LayeredRenderer {

    private final Chunk chunk;

    private int solidVAO;
    public int solidVertexCount;

    private int transVAO;
    public int transVertexCount;

    private final Int2IntArrayMap customVBO = new Int2IntArrayMap();
    private final Object2ObjectArrayMap<IBlockRender, ObjectList<Vector3i>> customBlockModels = new Object2ObjectArrayMap<>();

    public ChunkRenderer(Chunk chunk) {
        this.chunk = chunk;
    }

    public void mesh() {
        //Really we shouldn't mesh until we know the neighbours are generated (not meshed)
        if(!chunk.isNeighbors(ChunkStatus.FINISHED)) {
            this.chunk.needsMeshing = true;
            return;
        }

        this.chunk.needsMeshing = false;

        // Temp list
        Object2ObjectArrayMap<IBlockRender, ObjectList<Vector3i>> newCustomBlockModels = new Object2ObjectArrayMap<>();

        VoxelResult solidResult = makeVoxels(new int[]{0, 0, 0}, new int[]{chunk.chunkSize, chunk.chunkHeight, chunk.chunkSize}, (x, y, z) -> {
            Block block = chunk.getBlock(x, y, z);
            if (block != null && !block.isTransparent() && !(block instanceof IBlockRender)) {
                return block.getId();
            } else if(block instanceof IBlockRender) {
                Vector3i position = new Vector3i(x + this.chunk.chunkX * this.chunk.chunkSize, y, z + this.chunk.chunkZ * this.chunk.chunkSize);
                newCustomBlockModels.computeIfAbsent((IBlockRender) block, b -> new ObjectArrayList<>()).add(position);
                return 0;
            } else {
                return 0;
            }
        });

        VoxelResult transparentResult = makeVoxels(new int[]{0, 0, 0}, new int[]{chunk.chunkSize, chunk.chunkHeight, chunk.chunkSize}, (x, y, z) -> {
            Block block = chunk.getBlock(x, y, z);
            if (block != null && block.isTransparent() && !(block instanceof IBlockRender)) {
                return block.getId();
            } else {
                return 0;
            }
        });

        ChunkMesh solidChunkMesh = this.generateMesh(solidResult.dims, solidResult.voxels);
        ChunkMesh transparentChunkMesh = this.generateMesh(transparentResult.dims, transparentResult.voxels);

        ThreadUtil.getMainQueue().add(() -> {
            RenderManager.remove(this);

            customBlockModels.clear();
            customBlockModels.putAll(newCustomBlockModels);

            this.createMesh(solidChunkMesh, false);
            this.createMesh(transparentChunkMesh, true);

            RenderManager.add(this);
        });
    }

    private void createMesh(ChunkMesh chunkMesh, boolean transparent) {
        int numVertices = chunkMesh.vertices.size();
        int numQuads = chunkMesh.indices.size();

        // Allocate exact sizes
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(numVertices * 3 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        IntBuffer indexBuffer = ByteBuffer.allocateDirect(numQuads * 6 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        FloatBuffer uvBuffer = ByteBuffer.allocateDirect(numQuads * 8 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        IntBuffer texLayerBuffer = ByteBuffer.allocateDirect(numQuads * 4 * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        FloatBuffer aoBuffer = ByteBuffer.allocateDirect(numQuads * 4 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        // Positions (vec3 per vertex)
        for (float[] v : chunkMesh.vertices) {
            vertexBuffer.put(v, 0, 3);
        }

        // Indices (quad -> two triangles)
        for (int[] q : chunkMesh.indices) {
            indexBuffer
                    .put(q[0]).put(q[1]).put(q[2])
                    .put(q[0]).put(q[2]).put(q[3]);
        }

        // UVs (8 floats per quad)
        for (float[] uv : chunkMesh.uvs) {
            uvBuffer.put(uv, 0, 8);
        }

        // Texture layer (same layer repeated for the quad’s 4 vertices)
        for (int layer : chunkMesh.texOffset) {
            texLayerBuffer.put(layer).put(layer).put(layer).put(layer);
        }

        // Ambient occlusion (4 verts per quad)
        for (float[] a : chunkMesh.aos) {
            aoBuffer.put(a, 0, 4);
        }

        // Flip for OpenGL
        vertexBuffer.flip();
        indexBuffer.flip();
        uvBuffer.flip();
        texLayerBuffer.flip();
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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // --- UV (Attribute 1) ---
        int uvVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvVBO);
        glBufferData(GL_ARRAY_BUFFER, uvBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        // --- Texture Offset (Attribute 2) ---
        int texOffsetVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texOffsetVBO);
        glBufferData(GL_ARRAY_BUFFER, texLayerBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(2);
        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, 0, 0);

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
    public Vector3f getPosition() {
        return new Vector3f(this.chunk.chunkX * this.chunk.chunkSize, 0, this.chunk.chunkZ * this.chunk.chunkSize);
    }

    @Override
    public Vector3f getBoundingBox() {
        return new Vector3f((this.chunk.chunkX * this.chunk.chunkSize) + this.chunk.chunkSize, this.chunk.chunkHeight, (this.chunk.chunkZ * this.chunk.chunkSize) + this.chunk.chunkSize);
    }

    @Override
    public void render() {
        Matrix4f model = new Matrix4f().translate(chunk.chunkX * chunk.chunkSize, 0, chunk.chunkZ * chunk.chunkSize);

        ShaderManager.chunk.use();
        ShaderManager.chunk.setMat4("model", model);
        ShaderManager.chunk.useFog();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockGame.getInstance().getTextureManager().getChunkTexture());

        glBindVertexArray(solidVAO);
        glDrawElements(GL_TRIANGLES, solidVertexCount, GL_UNSIGNED_INT, 0L);

        glBindVertexArray(0);
        ShaderManager.chunk.stop();

        // Render foliage
        boolean cullFace = glIsEnabled(GL_CULL_FACE);
        glDisable(GL_CULL_FACE);
        for (Object2ObjectMap.Entry<IBlockRender, ObjectList<Vector3i>> e : customBlockModels.object2ObjectEntrySet()) {
            IBlockRender blockModel = e.getKey();
            ObjectList<Vector3i> instances = e.getValue();
            blockModel.render(instances);
        }
        if(cullFace) glEnable(GL_CULL_FACE);
    }

    @Override
    public void renderTransparent() {
        Matrix4f model = new Matrix4f().translate(this.getPosition());

        ShaderManager.chunk.use();
        ShaderManager.chunk.setMat4("model", model);
        ShaderManager.chunk.useFog();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockGame.getInstance().getTextureManager().getChunkTexture());

        glBindVertexArray(transVAO);
        glDrawElements(GL_TRIANGLES, transVertexCount, GL_UNSIGNED_INT, 0L);

        glBindVertexArray(0);
        ShaderManager.chunk.stop();
    }

    private static class ChunkMesh {
        ArrayList<float[]> vertices;
        ArrayList<int[]> indices;
        ArrayList<float[]> uvs;
        ArrayList<Integer> texOffset;
        ArrayList<float[]> aos;

        public ChunkMesh(
                ArrayList<float[]> vertices,
                ArrayList<int[]> indices,
                ArrayList<float[]> uvs,
                ArrayList<Integer> texOffset,
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

    private int getAo(int[] currentPos, int[] step, int axis, boolean positive) {
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

            int[] side1 = positive ? new int[]{0, 0, 0} : new int[]{-step[0], -step[1], -step[2]};
            int[] side2 = positive ? new int[]{0, 0, 0} : new int[]{-step[0], -step[1], -step[2]};
            int[] corner = positive ? new int[]{0, 0, 0} : new int[]{-step[0], -step[1], -step[2]};

            side1[a1] += s1;
            side2[a2] += s2;
            corner[a1] += s1;
            corner[a2] += s2;

            World world = BlockGame.getInstance().getWorld();
            Block b1 = world.getChunkBlock(chunk.chunkX, chunk.chunkZ, x + side1[0], y + side1[1], z + side1[2]);
            Block b2 = world.getChunkBlock(chunk.chunkX, chunk.chunkZ, x + side2[0], y + side2[1], z + side2[2]);
            Block b3 = world.getChunkBlock(chunk.chunkX, chunk.chunkZ, x + corner[0], y + corner[1], z + corner[2]);

            int hasSide1 = (b1 != null && !b1.isTransparent() && b1.getModel() == null) ? 1 : 0;
            int hasSide2 = (b2 != null && !b2.isTransparent() && b2.getModel() == null) ? 1 : 0;
            int hasCorner = (b3 != null && !b3.isTransparent() && b3.getModel() == null) ? 1 : 0;

            int ao = (hasSide1 == 1 && hasSide2 == 1) ? 0 : 3 - (hasSide1 + hasSide2 + hasCorner);
            aoLevels[i] = ao;
        }

        return ((aoLevels[0] & 0b11) << 0) |  // TL
                ((aoLevels[1] & 0b11) << 2) |  // TR
                ((aoLevels[2] & 0b11) << 4) |  // BL
                ((aoLevels[3] & 0b11) << 6);   // BR
    }

    private int getMaskValue(int id, int[] pos, int[] step) {
        Block block = Blocks.get(id);
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
        // Exclude solid blocks from this as it causes leaves/glass to die
        if (block.isTransparent() && neighbor.isTransparent()) {
            return 0;
        }

        // Hide face if neighbor is opaque
        if (!neighbor.isTransparent() && neighbor.getModel() == null) {
            return 0;
        }

        // Otherwise, show the face
        return id;
    }

    private int getTextureValue(int id, int axis, boolean positive) {
        if(id == 0) return 0; //0 can still be a valid texture so we must return texture + 1;

        Block block = Blocks.get(id);
        int texture;

        if (axis == 1) {
            texture = block.getTexture(positive ? "top" : "bottom").getId();
        } else {
            texture = block.getTexture().getId();
        }

        return texture + 1;
    }

    private ChunkMesh generateMesh(int[] dims, int[] voxels) {
        int[] mask = new int[0];
        int[] aoMask = new int[0];
        int[] texMask = new int[0];

        ArrayList<float[]> vertices = new ArrayList<>();
        ArrayList<int[]> indices = new ArrayList<>();
        ArrayList<float[]> uvs = new ArrayList<>();
        ArrayList<Integer> textures = new ArrayList<>();
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
                texMask = new int[dims[u] * dims[v]];
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
                            texMask[n] = 0;
                        } else {
                            // Generate an AO for the block, the value will be a bitwise total unique to the AO pattern
                            if (currID != 0) {
                                mask[n] = getMaskValue(currID, pos, step);
                                aoMask[n] = this.getAo(
                                        nextPos,
                                        step,
                                        axis,
                                        true
                                );
                                texMask[n] = getTextureValue(currID, axis, true);
                            } else {
                                mask[n] = -getMaskValue(nextID, pos, new int[] { 0, 0, 0 });
                                aoMask[n] = -this.getAo(
                                        nextPos,
                                        step,
                                        axis,
                                        false
                                );
                                texMask[n] = -getTextureValue(nextID, axis, false);
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
                        int texVal = texMask[n];
                        if (blockId != 0) {
                            // Calculate quad width
                            int width = 1;
                            while (i + width < dims[u] && blockId == mask[n + width] && aoVal == aoMask[n + width] && texVal == texMask[n + width]) {
                                ++width;
                            }

                            // Calculate quad height
                            int height = 1;
                            boolean stop = false;
                            while (j + height < dims[v]) {
                                for (int k = 0; k < width; ++k) {
                                    if (blockId != mask[n + k + height * dims[u]] || aoVal != aoMask[n + k + height * dims[u]] || texVal != texMask[n + k + height * dims[u]]) {
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
                            boolean isPositiveFace = blockId > 0;
                            texVal = Math.abs(texVal) - 1;

                            if (isPositiveFace) {
                                du[u] = width;
                                dv[v] = height;
                            } else {
                                aoVal = -aoVal;
                                dv[u] = width;
                                du[v] = height;
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
                            boolean flipDiagonal = ao[0] + ao[3] > ao[1] + ao[2];
                            float[] faceAO = isPositiveFace ? new float[]{ao[2], ao[3], ao[1], ao[0]} : new float[]{ao[2], ao[0], ao[1], ao[3]};
                            float[] uv = new float[8];

                            uv = switch (axis) {
                                case 0 -> isPositiveFace //X
                                        ? new float[]{0, 0, 0, width, height, width, height, 0}
                                        : new float[]{height, 0, 0, 0, 0, width, height, width};
                                case 1 -> isPositiveFace //Y
                                        ? new float[]{width, 0, 0, 0, 0, height, width, height}
                                        : new float[]{height, 0, 0, 0, 0, width, height, width};
                                case 2 -> isPositiveFace //Z
                                        ? new float[]{width, 0, 0, 0, 0, height, width, height}
                                        : new float[]{0, 0, 0, height, width, height, width, 0};
                                default -> uv;
                            };

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

                            // UV
                            uvs.add(uv);

                            // Tex Offset
                            textures.add(texVal);

                            // AO Values
                            aos.add(faceAO);

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