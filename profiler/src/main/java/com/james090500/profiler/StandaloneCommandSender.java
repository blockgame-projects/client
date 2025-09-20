package com.james090500.profiler;

import me.lucko.spark.common.command.sender.AbstractCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.UUID;

public class StandaloneCommandSender extends AbstractCommandSender<StandaloneCommandSender.Output> {
    public static final StandaloneCommandSender SYSTEM_OUT = new StandaloneCommandSender(System.out::println);

    public StandaloneCommandSender(Output output) {
        super(output);
    }

    @Override
    public String getName() {
        return "BlockGame";
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public void sendMessage(Component message) {
        System.out.println(PlainTextComponentSerializer.plainText().serialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    public interface Output {
        void sendMessage(String message);
    }

}