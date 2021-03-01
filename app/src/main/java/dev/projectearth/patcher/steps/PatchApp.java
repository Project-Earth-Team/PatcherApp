package dev.projectearth.patcher.steps;

import android.util.Log;

import androidx.preference.PreferenceManager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import dev.projectearth.patcher.MainActivity;
import dev.projectearth.patcher.R;
import dev.projectearth.patcher.utils.AndroidUtils;
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
