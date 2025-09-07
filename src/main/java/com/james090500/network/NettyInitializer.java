package com.james090500.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        int maxFrameLength = 131_072; // 128 KiB safe cap for 76,804 byte frames
        ch.pipeline().addLast(
                new LengthFieldBasedFrameDecoder(
                        maxFrameLength, // maxFrameLength
                        0,               // lengthFieldOffset (length is at start)
                        4,               // lengthFieldLength (writeInt)
                        0,               // lengthAdjustment
                        4                // initialBytesToStrip (strip the length field so handler sees only payload)
                )
        );
        ch.pipeline().addLast(new BlockGameMP());
    }
}
