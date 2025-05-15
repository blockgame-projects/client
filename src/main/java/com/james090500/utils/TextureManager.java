package com.james090500.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TextureManager {

    public static int terrainTexture;

    static {
        terrainTexture = loadTexture("terrain.png"); // adjust path as needed
    }

    public static int loadTexture(String resourceName) {
        // Load from classpath
        InputStream stream = TextureManager.class.getResourceAsStream("/" + resourceName);
        if (stream == null) {
            throw new RuntimeException("Resource not found: " + resourceName);
        }

        try {
            // Copy to temp file so STBImage can load it from a path
            Path tempFile = Files.createTempFile("texture", ".png");
            Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            int textureId = loadTextureFromFile(tempFile.toString());
            Files.delete(tempFile);
            return textureId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resourceName, e);
        }
    }

    public static int loadTextureFromFile(String filepath) {
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
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

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