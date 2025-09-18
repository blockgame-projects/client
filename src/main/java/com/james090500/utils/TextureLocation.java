package com.james090500.utils;

import com.james090500.BlockGame;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextureLocation {

    private static final Object2IntOpenHashMap<String> textures = new Object2IntOpenHashMap<>();

    /**
     * Get the texture by name
     * @param name
     * @return
     */
    public static int get(String name) {
        String texturePath = name.toLowerCase();
        if (textures.containsKey(texturePath)) {
            return textures.getInt(texturePath);
        } else {
            int texture;
            if(texturePath.contains("gui")) {
                texture = loadVGTexture(texturePath);
            } else {
                texture = loadGLTexture(texturePath);
            }
            textures.put(texturePath, texture);
            return texture;
        }
    }

    /**
     * Load a GL texture to memory
     * @param resourceName The name of the file
     * @return
     */
    private static int loadGLTexture(String resourceName) {
        Path filePath = resourceToFilePath(resourceName);
        return loadTextureFromFile(filePath.toString());
    }

    /**
     * Load a VG texture to memory
     * @param resourceName The name of the file
     * @return
     */
    private static int loadVGTexture(String resourceName) {
        Path filePath = resourceToFilePath(resourceName);
        return nvgCreateImage(BlockGame.getInstance().getClientWindow().getVg(), filePath.toString(), NVG_IMAGE_REPEATX | NVG_IMAGE_REPEATY);
    }

    /**
     * Converts a resource string to a file name
     * @param resource The resource
     * @return
     */
    private static Path resourceToFilePath(String resource) {
        File file = new File(resource + ".png");
        if(file.exists()) {
            BlockGame.getLogger().info("Texture - " + resource + " at " + file);
            return file.toPath();
        } else {
            BlockGame.getLogger().severe("Texture - " + resource + " at " + file);
            return new File("assets/error.png").toPath();
        }
    }

    /**
     * Loads a texture from file into GL memory
     * @param filepath
     * @return
     */
    private static int loadTextureFromFile(String filepath) {
        // Prepare buffers for image data
        IntBuffer width  = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Flip image vertically (OpenGL's origin is bottom-left)
        STBImage.stbi_set_flip_vertically_on_load(true);

        // Load image data
        ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load image: " + filepath + "\n" + STBImage.stbi_failure_reason());
        }

        // Generate OpenGL texture
        int textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // Upload texture to GPU
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                width.get(0), height.get(0), 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image
        );

        // Free the image memory
        STBImage.stbi_image_free(image);

        return textureID;
    }
}
