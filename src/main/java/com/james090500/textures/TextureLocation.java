package com.james090500.textures;

import com.james090500.BlockGame;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextureLocation {

    private static final Object2ObjectArrayMap<String, TextureLocation> resources = new Object2ObjectArrayMap<>();

    private boolean guiTexture;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected int id;
    private int glTexture;
    private int vgTexture;

    /**
     * Get a texture that has been developed
     * @param resource
     * @return
     */
    public static TextureLocation get(String resource) {
        if(resources.containsKey(resource.toLowerCase())) {
            return resources.get(resource.toLowerCase());
        } else {
            TextureLocation textureLocation = new TextureLocation();
            resources.put(resource.toLowerCase(), textureLocation);
            return textureLocation;
        }
    }

    /**
     * Gets the texture ID for the relevant texture
     * @return
     */
    public int getTexture() {
        return (guiTexture) ? vgTexture : glTexture;
    }

    /**
     * Load a GL texture to memory
     * @param image The buffer of image data
     * @param width The image width
     * @param height The image height
     * @return
     */
    protected void loadGLTexture(ByteBuffer image, int width, int height) {
        // Generate OpenGL texture
        this.glTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.glTexture);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // Upload texture to GPU
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image
        );
    }

    /**
     * Load a VG texture to memory
     * @param image The buffer of image data
     * @param width The image width
     * @param height The image height
     * @return
     */
    protected void loadVGTexture(ByteBuffer image, int width, int height) {
        this.guiTexture = true;
        this.vgTexture = nvgCreateImageRGBA(BlockGame.getInstance().getClientWindow().getVg(), width, height, NVG_IMAGE_REPEATX | NVG_IMAGE_REPEATY, image);
    }
}
