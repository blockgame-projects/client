package com.james090500.network;

import com.james090500.BlockGame;
import com.james090500.network.packets.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockGameMP extends SimpleChannelInboundHandler<ByteBuf> {

    private final Map<Integer, Supplier<BlockGamePacket>> parsers = new HashMap<>();
    public BlockGameMP() {
        parsers.put(1, ConnectPacket::new);
        parsers.put(2, DisconnectPacket::new);
        parsers.put(3, ChunkPacket::new);
        parsers.put(4, BlockUpdatePacket::new);
        parsers.put(5, EntityUpdatePacket::new);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        BlockGamePacket packet = parsers.get(1).get();
        packet.write(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        int packetId = msg.readInt();
        BlockGamePacket packet = parsers.get(packetId).get();
        packet.read(ctx.channel(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        BlockGame.getInstance().exit();
        cause.printStackTrace();
        ctx.close();
    }
}
