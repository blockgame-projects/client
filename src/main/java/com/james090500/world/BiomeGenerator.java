package com.james090500.world;

import com.james090500.utils.NoiseManager;

public class BiomeGenerator {

    // This is also generation order
    // Eg; Ocean will only be next to forests
    private static final Biomes[] worldBiomes = {
            Biomes.DESERT,
            Biomes.PLAINS,
            Biomes.FOREST,
            Biomes.TAIGA,
    };

    /**
     * Get the biome at a coordinate
     * @param x
     * @param z
     * @return
     */
    public static Biomes getBiome(int x, int z) {
        double noise = NoiseManager.biomeNoise(x, z); // [-1, 1]
        double n = (noise + 1.0) / 2.0;

        int idx = (int) Math.floor(n * worldBiomes.length); // possible values 0..values.length
        if (idx >= worldBiomes.length) idx = worldBiomes.length - 1; // clamp edge case when n == 1.0

        return worldBiomes[idx];
    }

}
