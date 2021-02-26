package dev.projectearth.patcher.utils;

import android.widget.TextView;

import lombok.Getter;
import lombok.Setter;

public abstract class LoggedRunnable implements Runnable {
    @Setter
    protected EventListeners.LogEventListener logEventListener;

    @Setter
    @Getter
    protected TextView logView;
}
