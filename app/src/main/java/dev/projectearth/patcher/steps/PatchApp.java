package dev.projectearth.patcher.steps;

import android.util.Log;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import lombok.SneakyThrows;

public class PatchApp extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        String serverAddress = "http://192.168.1.115";

        logEventListener.onLogLine("Patching server address...");
        try (RandomAccessFile raf = new RandomAccessFile(StorageLocations.getOutDir().resolve("lib/arm64-v8a/libgenoa.so").toString(), "rw")) {
            raf.seek(0x0514D05D);
            raf.write(serverAddress.getBytes());
        }

        try (Git git = Git.init().setDirectory(StorageLocations.getOutDir().toFile()).call()) {
            for (final File file : StorageLocations.getPatchDir().toFile().listFiles()) {
                if (!file.getName().endsWith(".patch")) {
                    continue;
                }

                logEventListener.onLogLine("Installing: " + file.getName());

                git.apply().setPatch(new FileInputStream(file)).call();
            }
            logEventListener.onLogLine("Done!");
        }
    }
}
