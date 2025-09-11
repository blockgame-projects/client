package com.james090500;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Config {
    private UserOptions userOptions = new UserOptions();

    private String runDir;
    private String name = "BlockGame Client";
    private String version = Objects.requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "Dev");

    private boolean paused = true;
    private long FPS;

    @Getter @Setter
    public static class UserOptions {
        private SliderOption renderDistance = new SliderOption(1, 32, 16);
    }

    @Data
    @AllArgsConstructor
    public static class SliderOption {
        private int min;
        private int max;
        private int value;

        public void setValue(int value) {
            if(value >= min && value <= max) {
                this.value = value;
            }
        }
    }
}
