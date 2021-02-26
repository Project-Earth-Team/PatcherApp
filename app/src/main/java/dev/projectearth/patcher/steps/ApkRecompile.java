package dev.projectearth.patcher.steps;

import android.util.Log;

import java.io.IOException;

import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.androlib.res.AndrolibResources;
import brut.common.BrutException;
import dev.projectearth.patcher.utils.AndroidUtils;
import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import dev.projectearth.patcher.utils.UILogger;
import lombok.SneakyThrows;

public class ApkRecompile extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        // TODO: Close the logger or somehow cleanup the object
        UILogger libLogger = new UILogger(Androlib.class.getName(),null);
        libLogger.setLogEventListener(logEventListener);
        AndroidUtils.setFinalStatic(Androlib.class.getDeclaredField("LOGGER"), libLogger);

        ApkOptions apkOptions = new ApkOptions();
        apkOptions.frameworkFolderLocation = StorageLocations.getFrameworkDir();
        apkOptions.aaptPath = StorageLocations.getAaptExec().getAbsolutePath();

        new Androlib(apkOptions).build(StorageLocations.getOutDir().toFile(), StorageLocations.getOutFile());
        logEventListener.onLogLine("Done!");
    }
}
