package com.james090500.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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
                System.out.println("Asset placed in " + out.toAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Assets successfully extracted");
    }
}
