package dev.projectearth.patcher.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import dev.projectearth.patcher.InstallerStepsActivity;
import dev.projectearth.patcher.R;
import lombok.Getter;

public class StorageLocations {
    @Getter
    private static final Path patchDir;
    @Getter
    private static final Path outDir;
    @Getter
    private static final File outFile;
    @Getter
    private static final File outFileSigned;
    @Getter
    private static final String frameworkDir;
    @Getter
    private static final File earthApk;
    @Getter
    private static final File aaptExec;
    @Getter
    private static final File earthKeystore;

    static {
        patchDir = InstallerStepsActivity.getAppContext().getExternalCacheDir().toPath().resolve("patches");
        outDir = InstallerStepsActivity.getAppContext().getExternalCacheDir().toPath().resolve("com.mojang.minecraftearth");
        outFile = InstallerStepsActivity.getAppContext().getExternalCacheDir().toPath().resolve("dev.projectearth.prod.unsigned.apk").toFile();
        outFileSigned = InstallerStepsActivity.getAppContext().getExternalFilesDir("").toPath().resolve("dev.projectearth.prod.apk").toFile();
        frameworkDir = InstallerStepsActivity.getAppContext().getExternalCacheDir().toPath().resolve("framework").toString();
        aaptExec = InstallerStepsActivity.getAppContext().getFilesDir().toPath().resolve("aapt").toFile();
        earthKeystore = InstallerStepsActivity.getAppContext().getFilesDir().toPath().resolve("earth_test.jks").toFile();


        // Get the earth apk
        final PackageManager pm = InstallerStepsActivity.getAppContext().getPackageManager();
        List<ApplicationInfo> packages =  pm.getInstalledApplications(PackageManager.GET_META_DATA);

        String foundEarthApk = null;
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.mojang.minecraftearth")) {
                foundEarthApk = packageInfo.sourceDir;
                break;
            }
        }
        earthApk = new File(foundEarthApk);


        // Extract aapt
        // aapt from https://github.com/JonForShort/android-tools
        if(!aaptExec.exists()) {
            try (InputStream in = InstallerStepsActivity.getAppContext().getResources().openRawResource(R.raw.aapt);
                 OutputStream out = new FileOutputStream(aaptExec)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        aaptExec.setExecutable(true);


        // Extract keystore
        if(!earthKeystore.exists()) {
            try (InputStream in = InstallerStepsActivity.getAppContext().getResources().openRawResource(R.raw.earth_test);
                 OutputStream out = new FileOutputStream(earthKeystore)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
