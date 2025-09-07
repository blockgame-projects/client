package com.james090500.network.packets;

import com.james090500.BlockGame;
import com.james090500.utils.ThreadUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class ChunkPacket implements BlockGamePacket {

    @Override
    public void write(Channel channel) {

    }

    @Override
    public void read(Channel channel, ByteBuf msg) {
        int chunkX = msg.readInt();
        int chunkZ = msg.readInt();
        byte[] data = new byte[msg.readableBytes()];
        msg.readBytes(data);

        ThreadUtil.getMainQueue().add(() -> {
            BlockGame.getInstance().getWorld().loadRemoteChunk(chunkX, chunkZ, data);
        });
    }
}
