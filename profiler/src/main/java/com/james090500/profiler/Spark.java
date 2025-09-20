package com.james090500.profiler;

import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.command.CommandResponseHandler;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.util.SparkThreadFactory;
import me.lucko.spark.common.util.classfinder.ClassFinder;
import me.lucko.spark.common.util.classfinder.FallbackClassFinder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Spark implements SparkPlugin {
    private final Set<StandaloneCommandSender> senders;
    private final ScheduledExecutorService scheduler;
    private final SparkPlatform platform;

    public Spark(String argument) {
        this.senders = ConcurrentHashMap.newKeySet();
        this.senders.add(StandaloneCommandSender.SYSTEM_OUT);
        this.scheduler = Executors.newScheduledThreadPool(4, new SparkThreadFactory());
        this.platform = new SparkPlatform(this);
        this.platform.enable();

        if (argument.contains("start")) {
            execute(new String[]{"profiler", "start"}, StandaloneCommandSender.SYSTEM_OUT).join();

            if (argument.contains("open")) {
                execute(new String[]{"profiler", "open"}, StandaloneCommandSender.SYSTEM_OUT).join();
            }
        }
    }

    public void disable() {
        this.platform.disable();
        this.scheduler.shutdown();
    }

    public CompletableFuture<Void> execute(String[] args, StandaloneCommandSender sender) {
        return this.platform.executeCommand(sender, args);
    }

    public CommandResponseHandler createResponseHandler(StandaloneCommandSender sender) {
        return new CommandResponseHandler(this.platform, sender);
    }

    @Override
    public String getVersion() {
        return "@version@";
    }

    @Override
    public Path getPluginDirectory() {
        return Paths.get("spark");
    }

    @Override
    public String getCommandName() {
        return "spark";
    }

    @Override
    public Stream<StandaloneCommandSender> getCommandSenders() {
        return this.senders.stream();
    }

    @Override
    public void executeAsync(Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void log(Level level, String msg) {
        log(level, msg, null);
    }

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        CommandResponseHandler resp = createResponseHandler(StandaloneCommandSender.SYSTEM_OUT);
        if (level.intValue() >= 900 || throwable != null) { // severe/warning
            resp.replyPrefixed(Component.text(msg, NamedTextColor.RED));
            if (throwable != null) {
                StringWriter stringWriter = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stringWriter));
                resp.replyPrefixed(Component.text(stringWriter.toString(), NamedTextColor.YELLOW));
            }
        } else {
            resp.replyPrefixed(Component.text(msg));
        }
    }

    @Override
    public ClassFinder createClassFinder() {
        return FallbackClassFinder.INSTANCE;
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new PlatformInfo() {
            @Override
            public Type getType() {
                return Type.APPLICATION;
            }

            @Override
            public String getName() {
                return "BlockGame";
            }

            @Override
            public String getBrand() {
                return "";
            }

            @Override
            public String getVersion() {
                return "";
            }

            @Override
            public String getMinecraftVersion() {
                return "";
            }
        };
    }
}