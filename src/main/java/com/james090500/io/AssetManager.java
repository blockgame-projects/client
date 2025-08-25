package com.james090500.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class AssetManager {

    public static void extractAssets() {
        File outputDir = new File("/assets");
        // Create output directory if missing
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Check if running from a JAR
        List<String> filenames = getFiles("/assets");
        try {
            for (String filename : filenames) {
                // filename is like "/assets/dir/file.ext"
                String relative = filename.replaceFirst("^/assets/?", "");
                Path out = Path.of("assets").resolve(relative);
                Files.createDirectories(out.getParent());
                try (InputStream in = AssetManager.class.getResourceAsStream(filename)) {
                    if (in != null) Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Assets successfully extracted");
    }

    /**
     * Recursive loop to get all files
     * @param path
     * @return
     */
    private static List<String> getFiles(String path) {
        List<String> filenames = new ArrayList<>();

        try (
            InputStream in = AssetManager.class.getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                if(resource.contains(".")) {
                    filenames.add(path + "/" + resource);
                } else {
                    List<String> moreFiles = getFiles(path + "/" + resource);
                    filenames.addAll(moreFiles);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filenames;
    }
}
