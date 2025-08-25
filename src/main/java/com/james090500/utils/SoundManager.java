package com.james090500.utils;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SoundManager {

    private static long device;
    private static long context;

    static {
        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);

        // init OpenAL once
        System.out.println(defaultDeviceName);
        device = ALC10.alcOpenDevice(defaultDeviceName);
        context = ALC10.alcCreateContext(device, (IntBuffer) null);
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(ALC.createCapabilities(device));
    }

    public static void play(String filename) {
        new Thread(() -> {
            int buffer = AL10.alGenBuffers();
            int source = AL10.alGenSources();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                Path path = Paths.get(filename);
                byte[] bytes = Files.readAllBytes(path);
                ByteBuffer vorbis = BufferUtils.createByteBuffer(bytes.length).put(bytes);
                vorbis.flip();

                IntBuffer error = stack.mallocInt(1);
                long decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
                if (decoder == MemoryUtil.NULL) {
                    System.err.println("Failed to open OGG file: " + error.get(0));
                    return;
                }

                STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                STBVorbis.stb_vorbis_get_info(decoder, info);
                int channels = info.channels();
                int sampleRate = info.sample_rate();

                int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
                ShortBuffer pcm = MemoryUtil.memAllocShort(samples * channels);
                STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
                STBVorbis.stb_vorbis_close(decoder);

                AL10.alBufferData(buffer,
                    channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16,
                    pcm,
                    sampleRate
                );

                MemoryUtil.memFree(pcm);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcePlay(source);

            // Optional: detach cleanup thread
            new Thread(() -> {
                try {
                    while (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {}
                AL10.alDeleteSources(source);
                AL10.alDeleteBuffers(buffer);
            }).start();
        }).start();
    }

    public static void destroy() {
        ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}