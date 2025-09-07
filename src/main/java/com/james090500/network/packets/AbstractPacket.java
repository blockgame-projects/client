package com.james090500.network.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public abstract class AbstractPacket implements BlockGamePacket {

    // default write behaviour: allocate, write id, then payload, then flush
    @Override
    public void write(Channel channel) {
        // If you use LengthFieldPrepender in pipeline, the length will be added for you,
        // so here we only write the id + payload.
        ByteBuf buf = channel.alloc().buffer();
        writePayload(buf);              // subclass writes its fields
        channel.writeAndFlush(buf);     // ownership passed to Netty
    }

    // subclass should write only payload (no id/length)
    protected abstract void writePayload(ByteBuf out);

    // default read can be left abstract or provided here
    @Override
    public abstract void read(Channel channel, ByteBuf msg);
}