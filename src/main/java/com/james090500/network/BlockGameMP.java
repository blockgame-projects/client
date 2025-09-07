package com.james090500.network;

import com.james090500.BlockGame;
import com.james090500.gui.ScreenManager;
import com.james090500.utils.ThreadUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BlockGameMP extends SimpleChannelInboundHandler<ByteBuf> {



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int chunkX = msg.readInt();
        int chunkZ = msg.readInt();
        byte[] data = new byte[msg.readableBytes()];
        msg.readBytes(data);

        ThreadUtil.getMainQueue().add(() -> {
            BlockGame.getInstance().getWorld().loadRemoteChunk(chunkX, chunkZ, data);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
