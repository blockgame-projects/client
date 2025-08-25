package com.james090500.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldManager {

    /**
     * List all worlds
     * @return
     */
    public static List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();

        File path = new File("worlds");
        File[] folders = path.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                worlds.add(folder.getName());
            }
        }

        return worlds;
    }

    /**
     * Delete a world
     * @param worldname
     * @return
     */
    public static boolean deleteWorld(String worldname) {
        File path = new File("worlds/" + worldname);
        for (File subfile : path.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }

            subfile.delete();
        }
        return path.delete();
    }

    /**
     * Recursively delete a directory
     * @param directoryToBeDeleted
     * @return
     */
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
