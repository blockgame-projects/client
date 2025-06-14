package com.james090500;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    private String runDir;
    private String name = "BlockGame Client";
    private String version = "0.0.1";

    private boolean paused = true;
    private long FPS;
}
