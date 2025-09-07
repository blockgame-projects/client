package com.james090500.network.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface BlockGamePacket {

    void write(Channel channel);

    void read(Channel channel, ByteBuf msg);
}
