package com.james090500.network.packets;

import com.james090500.BlockGame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class ConnectPacket implements BlockGamePacket {

    @Override
    public void write(Channel channel) {
        BlockGame.getLogger().info("Sending Connect Packet");
        ByteBuf buf = channel.alloc().buffer(8); // 4 bytes for length, 4 bytes for packet id,
        buf.writeInt(4); //Length
        buf.writeInt(1); //ID
        channel.writeAndFlush(buf);
    }

    @Override
    public void read(Channel channel, ByteBuf msg) {

    }
}
