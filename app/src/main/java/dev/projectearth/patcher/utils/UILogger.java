package dev.projectearth.patcher.utils;

import android.util.Log;

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
        Log.i(getName(), msg);
        super.info(msg);
    }

    @Override
    public void warning(@Nullable String msg) {
        logEventListener.onLogLine(msg);
        Log.w(getName(), msg);
        super.warning(msg);
    }

    @Override
    public void fine(@Nullable String msg) {
        logEventListener.onLogLine(msg);
        Log.i(getName(), msg);
        super.fine(msg);
    }
}
