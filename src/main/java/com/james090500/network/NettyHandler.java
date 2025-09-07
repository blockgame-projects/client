package com.james090500.network;

import com.james090500.BlockGame;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.multiplayer.ConnectingScreen;
import com.james090500.gui.multiplayer.ConnectionScreen;
import com.james090500.utils.ThreadUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyHandler {

    private final String host;
    private final int port;

    public NettyHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        ScreenManager.clear();
        ScreenManager.add(new ConnectingScreen(host + ":" + port));

        EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new NettyInitializer());

        ChannelFuture channelFuture = bootstrap.connect(host, port);
        channelFuture.addListener((ChannelFuture future) -> {
            if(future.isSuccess()) {
                ThreadUtil.getMainQueue().add(() -> {
                    ScreenManager.clear();
                    BlockGame.getInstance().startRemote();
                });
            } else {
                String error = future.cause().getLocalizedMessage();
                BlockGame.getLogger().warning(error);
                ScreenManager.clear();
                ScreenManager.add(new ConnectionScreen(error));
            }
        });
    }

}
