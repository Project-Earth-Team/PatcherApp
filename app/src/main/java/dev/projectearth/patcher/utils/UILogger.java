package dev.projectearth.patcher.utils;

import androidx.annotation.Nullable;

import java.util.logging.Logger;

import lombok.Setter;

public class UILogger extends Logger {
    @Setter
    private EventListeners.LogEventListener logEventListener;

    public UILogger(@Nullable String name, @Nullable String resourceBundleName) {
        super(name, resourceBundleName);
    }

    @Override
    public void info(@Nullable String msg) {
        logEventListener.onLogLine(msg);
        super.info(msg);
    }
}
