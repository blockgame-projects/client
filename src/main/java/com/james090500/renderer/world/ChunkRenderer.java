package com.james090500.renderer.world;

import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import com.james090500.world.Chunk;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.lang.reflect.Array;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ChunkRenderer implements Renderer {

    private int vao;
    private int vertexCount;

    private int

    public void mesh(Chunk chunk) {
        float[] vertices = {
            // Front face
            1.0f,  1.0f,  1.0f, // 0 Top Right Front
            1.0f, 0f,  1.0f, // 1 Bottom Right Front
            0f, 0f,  1.0f, // 2 Bottom Left Front
            0f,  1.0f,  1.0f, // 3 Top Left Front

            // Back face
            1.0f,  1.0f, 0f, // 4 Top Right Back
            1.0f, 0f, 0f, // 5 Bottom Right Back
            0f, 0f, 0f, // 6 Bottom Left Back
            0f,  1.0f, 0f  // 7 Top Left Back
        };

        int[] indices = {
                // Front face
                0, 1, 3,
                1, 2, 3,

                // Right face
                4, 5, 0,
                5, 1, 0,

                // Back face
                7, 6, 4,
                6, 5, 4,

                // Left face
                3, 2, 7,
                2, 6, 7,

                // Top face
                4, 0, 7,
                0, 3, 7,

                // Bottom face
                1, 5, 2,
                5, 6, 2
        };

        vertexCount = indices.length;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);

        int ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, BufferUtils.createIntBuffer(indices.length).put(indices).flip(), GL_STATIC_DRAW);

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

    /**
     * Create the voxels which should be renderer
     * @param l
     * @param h
     * @param f
     */
    private void makeVoxels(int[] l, int[] h, Object f) {
        int[] dims = new int[] { h[0] - l[0], h[1] - l[1], h[2] - l[2] };
        int[] voxels = new int[(dims[0] * dims[1] * dims[2])];
        int n = 0;

        for (var k = l[2]; k < h[2]; ++k) {
            for (var j = l[1]; j < h[1]; ++j) {
                for (var i = l[0]; i < h[0]; ++i, ++n) {
                    let result = f(i, j, k)
                    voxels[n] = result
                }
            }
        }

        return [ voxels, dims ]
    }

    // Helper to get block ID at a voxel coordinate
    private void getVoxel(int x, int y, int z) {
        return this.voxels[x + this.dims[0] * (y + this.dims[1] * z)]
    }

    getAo(currentPos, step, axis, neg) {
        const aoLevels = []
        const [x, y, z] = currentPos
        const [a1, a2] = [0, 1, 2].filter((i) => i !== axis)
        const cornerOffsets = [
            [-1, 1], // TL
            [1, 1], // TR
            [-1, -1], // BL
            [1, -1], // BR
        ]

        const getBlock = (dx, dy, dz) => {
            const block = this.getChunkBlock(
                    this.chunkX,
                    this.chunkY,
                    dx,
                    dy,
                    dz
            )
            return block && !block.transparent ? 1 : 0
        }

        for (let i = 0; i < 4; i++) {
            const [s1, s2] = cornerOffsets[i]

            let side1, side2, corner
            if (!neg) {
                side1 = [0, 0, 0]
                side2 = [0, 0, 0]
                corner = [0, 0, 0]
            } else {
                side1 = [-step[0], -step[1], -step[2]]
                side2 = [-step[0], -step[1], -step[2]]
                corner = [-step[0], -step[1], -step[2]]
            }

            side1[a1] = s1
            side2[a2] = s2
            corner[a1] = s1
            corner[a2] = s2

            const posSide1 = [x + side1[0], y + side1[1], z + side1[2]]
            const posSide2 = [x + side2[0], y + side2[1], z + side2[2]]
            const posCorner = [x + corner[0], y + corner[1], z + corner[2]]

            const hasSide1 = getBlock(...posSide1)
            const hasSide2 = getBlock(...posSide2)
            const hasCorner = getBlock(...posCorner)

            let ao =
                    hasSide1 && hasSide2 ? 0 : 3 - (hasSide1 + hasSide2 + hasCorner)

            aoLevels.push(ao)
        }

        const encoded =
                ((aoLevels[0] & 0b11) << 0) | // top-left (bits 0-1)
                        ((aoLevels[1] & 0b11) << 2) | // top-right (bits 2-3)
                        ((aoLevels[2] & 0b11) << 4) | // bottom-left (bits 4-5)
                        ((aoLevels[3] & 0b11) << 6) // bottom-right (bits 6-7)

        return encoded
    }

    generateMesh() {
        let mask = new Int32Array(1)
        let aoMask = new Int32Array(1)

        const vertices = []
        const faces = []

        // Sweep across 3 dimensions: X, Y, Z (0, 1, 2)
        for (let axis = 0; axis < 3; ++axis) {
            const u = (axis + 1) % 3
            const v = (axis + 2) % 3
            const pos = [0, 0, 0]
            const step = [0, 0, 0]
            step[axis] = 1

            if (mask.length < this.dims[u] * this.dims[v]) {
                mask = new Int32Array(this.dims[u] * this.dims[v])
                aoMask = new Int32Array(this.dims[u] * this.dims[v])
            }

            for (pos[axis] = -1; pos[axis] < this.dims[axis]; ) {
                let n = 0

                // Build the mask
                for (pos[v] = 0; pos[v] < this.dims[v]; ++pos[v]) {
                    for (pos[u] = 0; pos[u] < this.dims[u]; ++pos[u], ++n) {
                        const currID =
                                pos[axis] >= 0 ? this.getVoxel(...pos) : 0
                        const nextPos = [
                        pos[0] + step[0],
                                pos[1] + step[1],
                                pos[2] + step[2],
                        ]
                        const nextID =
                                pos[axis] < this.dims[axis] - 1
                                        ? this.getVoxel(...nextPos)
                                : 0

                        if (!!currID === !!nextID) {
                            mask[n] = 0
                            aoMask[n] = 3
                        } else {
                            const getMaskValue = (
                                    id,
                                    dx = 0,
                                    dy = 0,
                                    dz = 0
                            ) => {
                                const blockA = Blocks.ids[id]
                                const neighbor = this.getChunkBlock(
                                        this.chunkX,
                                        this.chunkY,
                                        pos[0] + dx,
                                        pos[1] + dy,
                                        pos[2] + dz
                                )
                                return neighbor &&
                                        (!neighbor.transparent ||
                                                blockA.transparent)
                                        ? 0
                                        : id
                            }

                            // Generate an AO for the block, the value will be a bitwise total uniquie to the AO pattern
                            if (currID) {
                                mask[n] = getMaskValue(currID, ...step)
                                aoMask[n] = this.getAo(
                                        nextPos,
                                        step,
                                        axis,
                                        false
                                )
                            } else {
                                mask[n] = -getMaskValue(nextID)
                                aoMask[n] = -this.getAo(
                                        nextPos,
                                        step,
                                        axis,
                                        true
                                )
                            }
                        }
                    }
                }

                ++pos[axis]

                // Generate quads
                n = 0
                for (let j = 0; j < this.dims[v]; ++j) {
                    for (let i = 0; i < this.dims[u]; ) {
                        let blockId = mask[n]
                        let aoVal = aoMask[n]
                        if (blockId !== 0) {
                            // Calculate quad width
                            let width = 1
                            while (
                                    i + width < this.dims[u] &&
                                            blockId === mask[n + width] &&
                                            aoVal === aoMask[n + width]
                            ) {
                                ++width
                            }

                            // Calculate quad height
                            let height = 1
                            let stop = false
                            while (j + height < this.dims[v]) {
                                for (let k = 0; k < width; ++k) {
                                    if (
                                            blockId !==
                                                    mask[
                                                            n + k + height * this.dims[u]
                                                            ] ||
                                                            aoVal !==
                                                            aoMask[
                                                                    n + k + height * this.dims[u]
                                                                    ]
                                    ) {
                                        stop = true
                                        break
                                    }
                                }
                                if (stop) break
                                ++height
                            }

                            // Construct quad
                            pos[u] = i
                            pos[v] = j
                            const du = [0, 0, 0]
                            const dv = [0, 0, 0]

                            if (blockId > 0) {
                                du[u] = width
                                dv[v] = height
                            } else {
                                blockId = -blockId
                                aoVal = -aoVal
                                dv[u] = width
                                du[v] = height
                            }

                            // Determine face orientation
                            const isPositiveFace = mask[n] > 0

                            // Get block and texture offset
                            const block = Blocks.ids[blockId]
                            let texOffset = block.getTexture()
                            if (axis === 1) {
                                texOffset = block.getTexture(
                                        isPositiveFace ? 'top' : 'bottom'
                                )
                            }

                            // Prepare base vertex count and corners
                            const vCount = vertices.length
                            const v0 = [pos[0], pos[1], pos[2]]
                            const v1 = [
                            pos[0] + du[0],
                                    pos[1] + du[1],
                                    pos[2] + du[2],
                            ]
                            const v2 = [
                            v1[0] + dv[0],
                                    v1[1] + dv[1],
                                    v1[2] + dv[2],
                            ]
                            const v3 = [
                            v0[0] + dv[0],
                                    v0[1] + dv[1],
                                    v0[2] + dv[2],
                            ]

                            // Extract AO values
                            let ao = [
                            (aoVal >> 0) & 0b11,
                                    (aoVal >> 2) & 0b11,
                                    (aoVal >> 4) & 0b11,
                                    (aoVal >> 6) & 0b11,
                            ]

                            // Rotate AO per axis and face direction
                            let faceAO = []
                            if (axis === 0) {
                                faceAO = isPositiveFace
                                        ? [ao[2], ao[3], ao[1], ao[0]]
                                    : [ao[2], ao[0], ao[1], ao[3]]
                            } else if (axis === 1) {
                                faceAO = isPositiveFace
                                        ? [ao[2], ao[0], ao[1], ao[3]]
                                    : [ao[2], ao[3], ao[1], ao[0]]
                            } else if (axis === 2) {
                                faceAO = isPositiveFace
                                        ? [ao[2], ao[3], ao[1], ao[0]]
                                    : [ao[2], ao[0], ao[1], ao[3]]
                            }

                            // Choose diagonal based on AO
                            let faceVerts, faceIndices
                            const flipDiagonal = ao[0] + ao[3] > ao[1] + ao[2]

                            if (flipDiagonal) {
                                faceVerts = [v3, v2, v1, v0]
                                faceIndices = [
                                vCount + 2,
                                        vCount + 1,
                                        vCount + 0,
                                        vCount + 3,
                                ]

                                // Reverse AO
                                faceAO = faceAO.reverse()
                            } else {
                                faceVerts = [v0, v1, v2, v3]
                                faceIndices = [
                                vCount,
                                        vCount + 1,
                                        vCount + 2,
                                        vCount + 3,
                                ]
                            }

                            // Add vertices
                            vertices.push(...faceVerts)

                            // Push face data
                            faces.push([
                                    faceIndices[0],
                                    faceIndices[1],
                                    faceIndices[2],
                                    faceIndices[3],
                                    texOffset,
                                    faceAO,
                            ])

                            // Zero the mask
                            for (let dy = 0; dy < height; ++dy) {
                                for (let dx = 0; dx < width; ++dx) {
                                    mask[n + dx + dy * this.dims[u]] = 0
                                    aoMask[n + dx + dy * this.dims[u]] = 0
                                }
                            }

                            i += width
                            n += width
                        } else {
                            ++i
                                    ++n
                        }
                    }
                }