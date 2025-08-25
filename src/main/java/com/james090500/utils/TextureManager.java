package com.james090500.utils;

import com.james090500.BlockGame;
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

import static org.lwjgl.nanovg.NanoVG.*;

public class TextureManager {

    public static int logo;
    public static int pack;
    public static int background;
    public static int button;
    public static int button_active;
    public static int button_disabled;
    public static int terrainTexture;

    static {
        logo = loadVGTexture("assets/gui/logo.png");
        pack = loadVGTexture("assets/gui/pack.png");
        background = loadVGTexture("assets/gui/background.png");
        button = loadVGTexture("assets/gui/button.png");
        button_active = loadVGTexture("assets/gui/button_active.png");
        button_disabled = loadVGTexture("assets/gui/button_disabled.png");
        terrainTexture = loadGLTexture("assets/terrain.png");
    }

    /**
     * Load a GL texture to memory
     * @param resourceName The name of the file
     * @return
     */
    public static int loadGLTexture(String resourceName) {
        try {
            Path tempFile = Path.of(resourceName);
            int textureId = loadTextureFromFile(tempFile.toString());
            Files.delete(tempFile);
            return textureId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resourceName, e);
        }
    }

    /**
     * Load a VG texture to memory
     * @param resourceName The name of the file
     * @return
     */
    private static int loadVGTexture(String resourceName) {
        try {
            Path tempFile = Path.of(resourceName);
            int textureId = nvgCreateImage(BlockGame.getInstance().getClientWindow().getVg(), tempFile.toString(), NVG_IMAGE_REPEATX | NVG_IMAGE_REPEATY);
            Files.delete(tempFile);
            return textureId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resourceName, e);
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