package com.james090500.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public final class GameLogger {
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    // ANSI colors (kept simple)
    private static final String RESET = "\u001B[0m";
    private static final String DIM = "\u001B[2m";
    private static final String BOLD = "\u001B[1m";
    private static final String GRAY = "\u001B[90m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";

    private static final boolean colorsEnabled = isAnsiLikelySupported();

    private GameLogger() {}

    /** Get a configured logger. Safe to call multiple times. */
    public static Logger get(String name) {
        Logger log = Logger.getLogger(name);
        for (Handler h : log.getHandlers()) log.removeHandler(h);
        configureLogger(log);
        return log;
    }

    private static void configureLogger(Logger logger) {
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        // Console
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        ch.setFormatter(new ColorFormatter());
        logger.addHandler(ch);
    }

    private static final class ColorFormatter extends Formatter {
        @Override
        public String format(LogRecord r) {
            String time = TIME.format(Instant.ofEpochMilli(r.getMillis()));
            String level = r.getLevel().getName();
            String logger = r.getLoggerName();
            String msg = formatMessage(r);
            String thrown = "";

            if (r.getThrown() != null) {
                StringBuilder sb = new StringBuilder();
                Throwable t = r.getThrown();
                sb.append(System.lineSeparator()).append(stackToString(t));
                thrown = sb.toString();
            }

            String prefix = String.format("[%s] [%s] [%s] ", time, level, logger);
            if (!colorsEnabled) {
                return prefix + msg + thrown + System.lineSeparator();
            }

            String color = colorForLevel(r.getLevel());
            return color + prefix + RESET + msg + thrown + System.lineSeparator();
        }
    }

    private static String colorForLevel(Level lvl) {
        int v = lvl.intValue();
        if (v >= Level.SEVERE.intValue())  return BOLD + RED;
        if (v >= Level.WARNING.intValue()) return YELLOW;
        if (v >= Level.INFO.intValue())    return GREEN;
        if (v >= Level.CONFIG.intValue())  return BLUE;
        // FINE / FINER / FINEST
        return GRAY + DIM;
    }

    private static boolean isAnsiLikelySupported() {
        // Pretty safe defaults: most modern terminals (Win10+ Windows Terminal, macOS, Linux) support ANSI.
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // Try to detect CI or old consoles; you can force enable via setColorsEnabled(true)
            String term = System.getenv("TERM");
            return term != null && !term.equalsIgnoreCase("dumb");
        }
        return true;
    }

    private static String stackToString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        while (t != null) {
            sb.append(t).append(System.lineSeparator());
            for (StackTraceElement e : t.getStackTrace()) {
                sb.append("\tat ").append(e).append(System.lineSeparator());
            }
            t = t.getCause();
            if (t != null) sb.append("Caused by: ");
        }
        return sb.toString();
    }
}
