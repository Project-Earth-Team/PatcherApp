package dev.projectearth.patcher.utils;

public class EventListeners {
    /**
     * This is used for adding a listener to log events
     */
    public interface LogEventListener {
        void onLogLine(String line);
    }
}
