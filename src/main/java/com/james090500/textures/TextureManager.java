package com.james090500.textures;

import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL42.glTexStorage3D;

public class TextureManager {

    @Getter
    private int chunkTexture;

    public TextureManager() {
        List<String> assets = loadAssets();

        this.chunkTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, this.chunkTexture);

        // every layer must be same WxH/format (e.g., 16x16 RGBA8)
        int tileSize = 16;
        int layers = assets.size();
        int levels = 1 + (int)Math.floor(Math.log(tileSize) / Math.log(2));

        //Loop all images to add to OpenGL
        glTexStorage3D(GL_TEXTURE_2D_ARRAY, levels, GL_RGBA8, tileSize, tileSize, layers); // use SRGB if you want gamma-correct
        for (int i = 0; i < layers; i++) {
            String filePath = assets.get(i);
            ByteBuffer pixels = loadTextureFromFile(filePath);

            if(filePath.contains("assets/blocks") || filePath.contains("assets/foliage")) {
                glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, tileSize, tileSize, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
                TextureLocation.get(filePath).setId(i);
            }

            //Free up
            STBImage.stbi_image_free(pixels);
        }

        // Mip mapping!
        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR); // crisp voxel look
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);

    }

    /**
     * Gets a list of all assets to process with OpenGL
     * @return
     */
    private List<String> loadAssets() {
        List<String> assetPaths;
        try (Stream<Path> stream = Files.walk(Path.of("assets"))) {
            assetPaths = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                    .map(p -> {
                        // convert Path -> "assets/block/dirt"
                        String rel = Path.of("assets").relativize(p).toString();
                        rel = rel.replace(File.separatorChar, '/'); // use forward slashes
                        if (rel.endsWith(".png")) {
                            rel = rel.substring(0, rel.length() - 4);
                        }
                        return "assets/" + rel;
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load assets", e);
        }

        return assetPaths;
    }


    /**
     * Loads a texture from file into GL memory
     * @param filepath
     * @return
     */
    private ByteBuffer loadTextureFromFile(String filepath) {
        // Prepare buffers for image data
        IntBuffer width  = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Flip image vertically (OpenGL's origin is bottom-left)
        STBImage.stbi_set_flip_vertically_on_load(!filepath.contains("gui"));

        // Load image data
        ByteBuffer image = STBImage.stbi_load(filepath + ".png", width, height, channels, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load image: " + filepath + "\n" + STBImage.stbi_failure_reason());
        }

        // Register with TextureLocation
        TextureLocation textureLocation = TextureLocation.get(filepath);
        if(filepath.contains("gui")) {
            textureLocation.loadVGTexture(image, width.get(), height.get());
        } else {
            textureLocation.loadGLTexture(image, width.get(), height.get());
        }

        return image;
    }
}
