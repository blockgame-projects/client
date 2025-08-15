package com.james090500;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Config {
    private String runDir;
    private String name = "BlockGame Client";
    private String version = Objects.requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "Dev");

    private boolean paused = true;
    private long FPS;
}
