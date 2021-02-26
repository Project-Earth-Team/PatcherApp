package dev.projectearth.patcher.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dev.projectearth.patcher.utils.AndroidUtils;
import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import lombok.SneakyThrows;

public class DownloadPatches extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        File zipFile = StorageLocations.getPatchDir().resolve("patches.zip").toFile();

        // Empty the dir
        if (StorageLocations.getPatchDir().toFile().exists()) {
            logEventListener.onLogLine("Removing existing patches...");
            for (final String file :  StorageLocations.getPatchDir().toFile().list()) {
                StorageLocations.getPatchDir().resolve(file).toFile().delete();
            }
        } else {
            StorageLocations.getPatchDir().toFile().mkdir();
        }

        // Always download the latest patches
        logEventListener.onLogLine("Downloading latest...");
        AndroidUtils.downloadFile("https://github.com/Project-Earth-Team/Patches/archive/main.zip", zipFile);

        logEventListener.onLogLine("Extracting...");
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.getName().endsWith(".patch")) {
                    continue;
                }
                Path fileName = Paths.get(entry.getName()).getFileName();

                logEventListener.onLogLine("Found patch: " + fileName);

                FileOutputStream fOut = new FileOutputStream(StorageLocations.getPatchDir().resolve(fileName).toString());

                byte[] buffer = new byte[8192];
                int len;
                while ((len = zip.read(buffer)) != -1)
                {
                    fOut.write(buffer, 0, len);
                }
                fOut.close();

                zip.closeEntry();
            }
        } catch (IOException e) {
            logEventListener.onLogLine(AndroidUtils.getStackTrace(e));
        }
        logEventListener.onLogLine("Done!");
    }
}
