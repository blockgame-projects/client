package com.james090500.network.packets;

import com.james090500.BlockGame;
import com.james090500.entity.Entity;
import com.james090500.entity.PlayerEntity;
import com.james090500.utils.ThreadUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.joml.Vector3f;

public class EntityUpdatePacket implements BlockGamePacket {

    @Override
    public void write(Channel channel) {

    }

    @Override
    public void read(Channel channel, ByteBuf msg) {
        BlockGame.getLogger().info("Received Entity Update Packet");
        int entityId = msg.readInt();
        int action = msg.readInt();
        float x = msg.readFloat();
        float y = msg.readFloat();
        float z = msg.readFloat();

        ThreadUtil.getMainQueue().add(() -> {
            if(action == 1) { //Add
                Entity entity = new PlayerEntity();
                entity.setPosition(new Vector3f(x, y, z));
                BlockGame.getInstance().getWorld().entities.put(entityId, entity);
            } else if(action == 2) { //Update
                Entity entity = BlockGame.getInstance().getWorld().entities.get(entityId);
                entity.setPosition(new Vector3f(x, y, z));
            } else if(action == 3) { //Remove
                BlockGame.getInstance().getWorld().entities.remove(entityId);
            }
        });
    }
}
