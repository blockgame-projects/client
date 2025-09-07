package com.james090500.io;

import com.james090500.BlockGame;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AssetManager {

    public static void extractAssets() {
        File outputDir = new File("assets");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (InputStream in = AssetManager.class.getResourceAsStream("/assets.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                InputStream asset = AssetManager.class.getResourceAsStream("/assets/" + line);
                if (asset == null) {
                    System.err.println("Missing asset: /assets/" + line);
                    continue;
                }

                Path out = Path.of("assets").resolve(line.replace("\\", "/"));
                Files.createDirectories(out.getParent());
                Files.copy(asset, out, StandardCopyOption.REPLACE_EXISTING);
                BlockGame.getLogger().info("Asset placed in " + out.toAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        BlockGame.getLogger().info("Assets successfully extracted");
    }
}
