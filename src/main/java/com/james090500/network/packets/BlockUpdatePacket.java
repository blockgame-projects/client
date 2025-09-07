package com.james090500.network.packets;

import com.james090500.BlockGame;
import com.james090500.utils.ThreadUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class BlockUpdatePacket extends AbstractPacket {
    private int x;
    private int y;
    private int z;
    private byte block;

    public BlockUpdatePacket() {}

    public BlockUpdatePacket(int x, int y, int z, byte block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    @Override
    protected void writePayload(ByteBuf out) {
        out.writeInt(17); //id, x, y, z, block
        out.writeInt(4);
        out.writeInt(x); //X
        out.writeInt(y); //Y
        out.writeInt(z); //Z
        out.writeByte(block); //Data
    }

    @Override
    public void read(Channel channel, ByteBuf msg) {
        this.x = msg.readInt();
        this.y = msg.readInt();
        this.z = msg.readInt();
        this.block = msg.readByte();

        ThreadUtil.getMainQueue().add(() -> BlockGame.getInstance().getWorld().setChunkBlock(0, 0, x, y, z, block));
    }
}