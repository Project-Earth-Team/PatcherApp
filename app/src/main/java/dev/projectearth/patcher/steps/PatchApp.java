package dev.projectearth.patcher.steps;

import androidx.preference.PreferenceManager;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import dev.projectearth.patcher.MainActivity;
import dev.projectearth.patcher.R;
import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import lombok.SneakyThrows;

public class PatchApp extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        String serverAddress = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext()).getString("locator_server", "https://p.projectearth.dev");

        logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_patch_server));
        try (RandomAccessFile raf = new RandomAccessFile(StorageLocations.getOutDir().resolve("lib/arm64-v8a/libgenoa.so").toString(), "rw")) {
            raf.seek(0x0514D05D);
            raf.write(serverAddress.getBytes());
        }

        try (Git git = Git.init().setDirectory(StorageLocations.getOutDir().toFile()).call()) {
            for (final File file : StorageLocations.getPatchDir().toFile().listFiles()) {
                if (!file.getName().endsWith(".patch")) {
                    continue;
                }

                logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_patch_install, file.getName()));

                git.apply().setPatch(new FileInputStream(file)).call();
            }
            logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_done));
        }
    }
}
