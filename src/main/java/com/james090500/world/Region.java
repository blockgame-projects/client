package com.james090500.world;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * BGR region file implementation (32×32 chunks per region).
 *
 * Layout on disk:
 *  - Header (32 bytes, little-endian):
 *      u32 magic "BGR" (0x32475242), u16 version=2, u16 regionSize=32,
 *      u32 dirEntrySize=16, u64 reserved=0, u64 reserved=0
 *  - Directory table (regionSize * regionSize * entrySize = 32*32*16 = 16384 bytes):
 *      Per-chunk 16-byte entry:
 *          u64 offset (0 = absent)
 *          u32 length (compressed payload length)
 *          u8  codec (0=RAW, 2=LZ4)
 *          u8  flags (bit0=hasChecksum [unused here])
 *          u8  reserved
 *          u8  reserved
 *  - Chunk record at 'offset':
 *      u32 uncompressedLength
 *      payload (compressed or raw as indicated by codec)
 *
 * Notes:
 *  - Append-only writes: we never overwrite old chunk data in-place. We only update the directory entry
 *    after the new data is fsynced. This improves crash safety and avoids partial-write corruption.
 *  - We use LZ4 for fast compression; if compression would grow the data, we store RAW instead.
 *  - All integers are little-endian.
 */
public class Region {
    // ---------- Constants ----------
    private static final int MAGIC_BGR = 0x32475242; // "BGR" little-endian codepoints
    private static final short VERSION = 2;
    private static final short REGION_SIZE = 32;       // 32×32 chunks per region
    private static final int DIR_ENTRY_SIZE = 16;      // bytes per directory entry

    private static final int HEADER_SIZE = 32;         // fixed header size
    private static final int DIRECTORY_SIZE = REGION_SIZE * REGION_SIZE * DIR_ENTRY_SIZE; // 16384
    private static final long MIN_FILE_SIZE = HEADER_SIZE + DIRECTORY_SIZE;               // 16416

    // Directory entry byte layout (offsets within a 16-byte entry)
    private static final int ENTRY_OFF_OFFSET = 0;     // u64
    private static final int ENTRY_LEN_OFFSET = 8;     // u32
    private static final int ENTRY_CODEC_OFFSET = 12;  // u8
    private static final int ENTRY_FLAGS_OFFSET = 13;  // u8
    // 14,15 reserved

    // Codec identifiers
    private static final byte CODEC_RAW = 0;
    private static final byte CODEC_LZ4 = 2;

    // Flags (not used yet, reserved for future)
    private static final byte FLAG_CHECKSUM = 1; // bit0 (unused in this implementation)

    // ---------- State ----------
    private final File regionFile;
    private final RandomAccessFile raf;
    private final FileChannel ch;

    private final LZ4Factory lz4 = LZ4Factory.fastestInstance();

    // A lightweight lock per region to serialize writes/reads and protect header/directory integrity.
    // (You can replace with a ReentrantReadWriteLock if you want more concurrency.)
    private final Object ioLock = new Object();

    public Region(String worldName, int regionX, int regionZ) throws IOException {
        File dir = new File("worlds/" + worldName + "/regions");
        // Ensure directory exists
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create region directory: " + dir.getAbsolutePath());
        }

        this.regionFile = new File(dir, "r." + regionX + "." + regionZ + ".bgr");
        this.raf = new RandomAccessFile(this.regionFile, "rw");
        this.ch = raf.getChannel();

        // Initialize file structure if new or truncated
        synchronized (ioLock) {
            if (raf.length() < MIN_FILE_SIZE) {
                initEmptyFile();
            } else {
                verifyHeader();
            }
        }
    }

    /**
     * Initialize a brand-new BGR file with zeroed directory.
     */
    private void initEmptyFile() throws IOException {
        // Header buffer
        ByteBuffer hdr = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        hdr.putInt(MAGIC_BGR);
        hdr.putShort(VERSION);
        hdr.putShort(REGION_SIZE);
        hdr.putInt(DIR_ENTRY_SIZE);
        hdr.putLong(0L); // reserved
        hdr.putLong(0L); // reserved
        hdr.flip();

        ch.position(0);
        while (hdr.hasRemaining()) ch.write(hdr);

        // Zero the directory table
        ch.position(HEADER_SIZE);
        // For efficiency, write in blocks
        ByteBuffer zero = ByteBuffer.allocateDirect(8192);
        long remaining = DIRECTORY_SIZE;
        while (remaining > 0) {
            zero.clear();
            int toWrite = (int) Math.min(zero.capacity(), remaining);
            zero.limit(toWrite);
            // zero buffer already filled with zeros
            ch.write(zero);
            remaining -= toWrite;
        }

        // Ensure file is at least MIN_FILE_SIZE
        if (raf.length() < MIN_FILE_SIZE) {
            raf.setLength(MIN_FILE_SIZE);
        }
        ch.force(true); // fsync header and directory
    }

    /**
     * Basic header sanity checks (throws if not a BGR file we expect).
     */
    private void verifyHeader() throws IOException {
        ByteBuffer hdr = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        ch.position(0);
        int read = ch.read(hdr);
        if (read != HEADER_SIZE) throw new EOFException("Header truncated");
        hdr.flip();
        int magic = hdr.getInt();
        short version = hdr.getShort();
        short regionSize = hdr.getShort();
        int entrySize = hdr.getInt();
        // skip reserved
        hdr.getLong();
        hdr.getLong();

        if (magic != MAGIC_BGR)
            throw new IOException("Bad magic: not a BGR file (found 0x" + Integer.toHexString(magic) + ")");
        if (version != VERSION)
            throw new IOException("Unsupported BGR version: " + version);
        if (regionSize != REGION_SIZE)
            throw new IOException("Unexpected region size: " + regionSize);
        if (entrySize != DIR_ENTRY_SIZE)
            throw new IOException("Unexpected directory entry size: " + entrySize);
    }

    /**
     * Save a chunk's bytes at (chunkX, chunkZ). This method chooses LZ4 or RAW per chunk.
     * @param chunkX world chunk X
     * @param chunkZ world chunk Z
     * @param chunkData uncompressed chunk payload (e.g., your block-id bytes)
     */
    public void saveChunk(int chunkX, int chunkZ, byte[] chunkData) throws IOException {
        if (chunkData == null) throw new IllegalArgumentException("chunkData == null");

        synchronized (ioLock) {
            // ---- 1) Compress with LZ4 (fast) and compare to RAW ----
            byte codec = CODEC_LZ4;
            byte[] payload;

            // LZ4 bound and compression
            LZ4Compressor compressor = lz4.fastCompressor();
            int maxCompressedLength = compressor.maxCompressedLength(chunkData.length);
            byte[] tmp = new byte[maxCompressedLength];
            int compLen = compressor.compress(chunkData, 0, chunkData.length, tmp, 0, maxCompressedLength);
            if (compLen <= 0 || compLen >= chunkData.length) {
                // Compression ineffective: store RAW
                codec = CODEC_RAW;
                payload = chunkData;
            } else {
                payload = Arrays.copyOf(tmp, compLen);
            }

            // ---- 2) Append chunk record at end of file ----
            long appendOffset = raf.length();
            ch.position(appendOffset);

            // Record = [u32 uncompressedLength][payload]
            ByteBuffer recHdr = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            recHdr.putInt(chunkData.length);
            recHdr.flip();
            while (recHdr.hasRemaining()) ch.write(recHdr);
            ch.write(ByteBuffer.wrap(payload));
            ch.force(true); // ensure payload is durable before directory update

            // ---- 3) Update directory entry atomically ----
            int localX = Math.floorMod(chunkX, REGION_SIZE);
            int localZ = Math.floorMod(chunkZ, REGION_SIZE);
            int index = localZ * REGION_SIZE + localX;

            long entryPos = HEADER_SIZE + (long) index * DIR_ENTRY_SIZE;
            ByteBuffer dir = ByteBuffer.allocate(DIR_ENTRY_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            dir.putLong(appendOffset);         // offset
            dir.putInt(payload.length);        // compressed length
            dir.put(codec);                    // codec
            dir.put((byte) 0);                 // flags (no checksum)
            dir.put((byte) 0);                 // reserved
            dir.put((byte) 0);                 // reserved
            dir.flip();

            ch.position(entryPos);
            while (dir.hasRemaining()) ch.write(dir);
            ch.force(true); // fsync directory update
        }
    }

    /**
     * Load a chunk at (chunkX, chunkZ). Returns null if the chunk isn't present.
     */
    public byte[] loadChunk(int chunkX, int chunkZ) throws IOException {
        synchronized (ioLock) {
            // ---- 1) Read directory entry ----
            int localX = Math.floorMod(chunkX, REGION_SIZE);
            int localZ = Math.floorMod(chunkZ, REGION_SIZE);
            int index = localZ * REGION_SIZE + localX;
            long entryPos = HEADER_SIZE + (long) index * DIR_ENTRY_SIZE;

            ByteBuffer dir = ByteBuffer.allocate(DIR_ENTRY_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            ch.position(entryPos);
            int read = ch.read(dir);
            if (read != DIR_ENTRY_SIZE) return null; // truncated directory (corrupt file)
            dir.flip();

            long offset = dir.getLong();
            int length = dir.getInt();
            byte codec = dir.get();
            byte flags = dir.get(); // currently unused
            dir.get(); // reserved
            dir.get(); // reserved

            if (offset == 0L || length == 0) return null; // not present

            // ---- 2) Read chunk record header (uncompressed length) ----
            ch.position(offset);
            ByteBuffer recHdr = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            int n = ch.read(recHdr);
            if (n != 4) throw new EOFException("Chunk header truncated at offset " + offset);
            recHdr.flip();
            int uncompressedLen = recHdr.getInt();

            // ---- 3) Read payload ----
            ByteBuffer payload = ByteBuffer.allocate(length);
            int total = 0;
            while (total < length) {
                int r = ch.read(payload);
                if (r < 0) throw new EOFException("Chunk payload truncated at offset " + (offset + 4));
                total += r;
            }
            byte[] payloadArr = payload.array();

            // ---- 4) Decompress according to codec ----
            switch (codec) {
                case CODEC_RAW:
                    if (payloadArr.length != uncompressedLen) {
                        // Defensive: RAW should match expected size
                        if (payloadArr.length < uncompressedLen) {
                            // pad with zeros if short (corrupt but survivable)
                            byte[] fixed = new byte[uncompressedLen];
                            System.arraycopy(payloadArr, 0, fixed, 0, payloadArr.length);
                            return fixed;
                        }
                        // If longer, trim to expected length
                        return Arrays.copyOf(payloadArr, uncompressedLen);
                    }
                    return payloadArr;
                case CODEC_LZ4:
                    LZ4SafeDecompressor decompressor = lz4.safeDecompressor();
                    return decompressor.decompress(payloadArr, uncompressedLen);
                default:
                    throw new IOException("Unknown codec id: " + codec);
            }
        }
    }

    /**
     * Cleanly close underlying file handles when you're done with the region.
     */
    public void close() {
        synchronized (ioLock) {
            try { ch.close(); } catch (IOException ignored) {}
            try { raf.close(); } catch (IOException ignored) {}
        }
    }
}
