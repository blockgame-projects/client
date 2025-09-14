package com.james090500.utils;

import com.james090500.BlockGame;
import com.james090500.world.Biomes;

public class NoiseManager {

    /**
     * Get chunk noise
     * @param x The X Coordinate
     * @param y The Y Coordinate
     * @param z The Z Coordinate
     * @return The resulting noise
     */
    public static double chunkNoise(Biomes biome, int x, int y, int z) {
        int octaves = 4;
        double persistence = 0.5;
        double lacunarity = 2.0;
        double frequency = 0.005;
        double amplitude = 5;

        return octaveNoise3D(0, x, y, z, octaves, persistence, lacunarity, frequency, amplitude);
    }

    /**
     * Get biome noise
     * @param x The X Coordinate
     * @param z The Z Coordinate
     * @return The resulting noise
     */
    public static double biomeNoise(int x, int z) {
        int octaves = 4;
        double persistence = 0.2;
        double lacunarity = 4.0;
        double frequency = 0.001;
        double amplitude = 1;

        return octaveNoise2D(0, x, z, octaves, persistence, lacunarity, frequency, amplitude);
    }

    /**
     * Get biome noise
     * @param x The X Coordinate
     * @param z The Z Coordinate
     * @return The resulting noise
     */
    public static double elevationNoise(int x, int z) {
        int octaves = 4;
        double persistence = 0.2;
        double lacunarity = 4.0;
        double frequency = 0.001;
        double amplitude = 1;

        return octaveNoise2D(4536, x, z, octaves, persistence, lacunarity, frequency, amplitude);
    }

    /**
     * Layer some noise on top of each other (fractal / octave noise) to produce a
     * smoother, natural-looking 3D noise value for terrain, caves, etc.
     *
     * This method samples the underlying OpenSimplex noise at several octaves
     * (frequencies) and sums them together, scaling amplitude and frequency each
     * octave by `persistence` and `lacunarity` respectively. The result is
     * normalized by the accumulated amplitude (maxValue) so the return value is
     * approximately in the range [-1.0, 1.0].
     *
     * @param x           X coordinate (world / block coordinate). Converted to double for noise sampling.
     * @param y           Y coordinate (vertical). Converted to double for noise sampling.
     * @param z           Z coordinate (world / block coordinate). Converted to double for noise sampling.
     * @param octaves     Number of noise layers to combine. More octaves = more detail and more CPU cost.
     *                    Should be >= 1 (typical values: 3–8).
     * @param persistence Controls how amplitude changes for each subsequent octave.
     *                    Each octave's amplitude = previous_amplitude * persistence.
     *                    Typical range: 0.2–0.8. Lower persistence -> high-frequency octaves contribute less.
     * @param lacunarity  Controls how frequency changes for each subsequent octave.
     *                    Each octave's frequency = previous_frequency * lacunarity.
     *                    Typical values: >1.0 (commonly 2.0).
     * @param frequency   Base spatial frequency (how quickly noise varies with distance).
     *                    Lower frequency = larger, smoother features. Example: frequency = 0.005 -> wavelength ≈ 200 units.
     * @param amplitude   Base amplitude (how strongly the first octave contributes).
     *                    This scales the vertical magnitude of the first octave (e.g. in blocks).
     * @return A normalized noise value approximately in the range [-1.0, 1.0]. Use
     *         this directly for e.g. blending or multiply by a height scale if you
     *         want a concrete block offset.
     */
    public static double octaveNoise3D(int seedConst, int x, int y, int z, int octaves, double persistence, double lacunarity, double frequency, double amplitude) {
        double total = 0;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += OpenSimplexNoise.noise3_ImproveXY(
                    BlockGame.getInstance().getWorld().getWorldSeed() + seedConst,
                    x * frequency,
                    y * frequency,
                    z * frequency
            ) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    /**
     * Layer some noise on top of each other (fractal / octave noise) to produce a
     * smoother, natural-looking 3D noise value for terrain, caves, etc.
     *
     * This method samples the underlying OpenSimplex noise at several octaves
     * (frequencies) and sums them together, scaling amplitude and frequency each
     * octave by `persistence` and `lacunarity` respectively. The result is
     * normalized by the accumulated amplitude (maxValue) so the return value is
     * approximately in the range [-1.0, 1.0].
     *
     * @param x           X coordinate (world / block coordinate). Converted to double for noise sampling.
     * @param z           Z coordinate (world / block coordinate). Converted to double for noise sampling.
     * @param octaves     Number of noise layers to combine. More octaves = more detail and more CPU cost.
     *                    Should be >= 1 (typical values: 3–8).
     * @param persistence Controls how amplitude changes for each subsequent octave.
     *                    Each octave's amplitude = previous_amplitude * persistence.
     *                    Typical range: 0.2–0.8. Lower persistence -> high-frequency octaves contribute less.
     * @param lacunarity  Controls how frequency changes for each subsequent octave.
     *                    Each octave's frequency = previous_frequency * lacunarity.
     *                    Typical values: >1.0 (commonly 2.0).
     * @param frequency   Base spatial frequency (how quickly noise varies with distance).
     *                    Lower frequency = larger, smoother features. Example: frequency = 0.005 -> wavelength ≈ 200 units.
     * @param amplitude   Base amplitude (how strongly the first octave contributes).
     *                    This scales the vertical magnitude of the first octave (e.g. in blocks).
     * @return A normalized noise value approximately in the range [-1.0, 1.0]. Use
     *         this directly for e.g. blending or multiply by a height scale if you
     *         want a concrete block offset.
     */
    public static double octaveNoise2D(int seedConst, int x, int z, int octaves, double persistence, double lacunarity, double frequency, double amplitude) {
        double total = 0;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += OpenSimplexNoise.noise2_ImproveX(
                    BlockGame.getInstance().getWorld().getWorldSeed() + seedConst,
                    x * frequency,
                    z * frequency
            ) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }
}
