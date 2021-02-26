package dev.projectearth.patcher.steps;

import java.io.IOException;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.res.AndrolibResources;
import brut.directory.DirectoryException;
import dev.projectearth.patcher.utils.AndroidUtils;
import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import dev.projectearth.patcher.utils.UILogger;
import lombok.SneakyThrows;

public class ApkDecompile extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        // TODO: Close the loggers or somehow cleanup the objects
        UILogger libLogger = new UILogger(Androlib.class.getName(),null);
        libLogger.setLogEventListener(logEventListener);
        AndroidUtils.setFinalStatic(Androlib.class.getDeclaredField("LOGGER"), libLogger);

        UILogger resLogger = new UILogger(AndrolibResources.class.getName(),null);
        resLogger.setLogEventListener(logEventListener);
        AndroidUtils.setFinalStatic(AndrolibResources.class.getDeclaredField("LOGGER"), resLogger);

        ApkDecoder decoder = new ApkDecoder();
        try {
            decoder.setApkFile(StorageLocations.getEarthApk());
            decoder.setOutDir(StorageLocations.getOutDir().toFile());
            decoder.setForceDelete(true);
            decoder.setFrameworkDir(StorageLocations.getFrameworkDir());
            decoder.decode();
            logEventListener.onLogLine("Done!");
        } finally {
            try {
                decoder.close();
            } catch (IOException ignored) {}
        }
    }
}
