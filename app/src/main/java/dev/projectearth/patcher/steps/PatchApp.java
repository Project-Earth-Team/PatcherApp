package dev.projectearth.patcher.steps;

import androidx.preference.PreferenceManager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import dev.projectearth.patcher.MainActivity;
import dev.projectearth.patcher.R;
import dev.projectearth.patcher.utils.AndroidUtils;
import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import lombok.SneakyThrows;

public class PatchApp extends LoggedRunnable {
    private static final int urlMax = 27;

    @SneakyThrows
    @Override
    public void run() {
        logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_patch_server));
        String serverAddress = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext()).getString("locator_server", "https://p.projectearth.dev");

        // Make sure we have http or https
        if (!serverAddress.matches("^(http|https)://.*$")) {
            serverAddress = "https://" + serverAddress;
        }

        // Remove trailing slash
        if (serverAddress.endsWith("/")) {
            serverAddress = serverAddress.substring(0, serverAddress.length() - 1);
        }

        // Check the url length
        if (serverAddress.length() > urlMax) {
            throw new IndexOutOfBoundsException("Server address too long (" + serverAddress.length() + ">" + urlMax + ")");
        }

        serverAddress = String.format("%1$-" + 27 + "s", serverAddress).replaceAll(" ", "\0");

        try (RandomAccessFile raf = new RandomAccessFile(StorageLocations.getOutDir().resolve("lib/arm64-v8a/libgenoa.so").toString(), "rw")) {
            // Write server address
            raf.seek(0x0514D05D);
            raf.write(serverAddress.getBytes());

            // Patch sunset check for 0.33.0
            raf.seek(0x22A6DC8);
            raf.write(0x540005CB); // asm: b.ge -> b.lt
        }

        try (Git git = Git.init().setDirectory(StorageLocations.getOutDir().toFile()).call()) {
            File[] files = StorageLocations.getPatchDir().toFile().listFiles();

            if (files == null) {
                return;
            }

            Arrays.sort(files); // Fix patch ordering on some devices

            for (final File file : files) {
                if (!file.getName().endsWith(".patch")) {
                    continue;
                }

                logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_patch_install, file.getName()));

                // Semi hacky fix for line endings being mixed in source files
                FileInputStream patchFile = new FileInputStream(file);
                Patch p = new Patch();
                p.parse(patchFile);
                for (FileHeader fileHeader : p.getFiles()) {
                    if (fileHeader.getPatchType() == FileHeader.PatchType.UNIFIED) {
                        // Replace the line endings
                        AndroidUtils.normalizeFile(StorageLocations.getOutDir().resolve(fileHeader.getOldPath()).toFile());
                    }
                }

                git.apply().setPatch(new FileInputStream(file)).call();
            }
            logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_done));
        }
    }
}
