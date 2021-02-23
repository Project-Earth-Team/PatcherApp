package dev.projectearth.patcher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.KeyStoreFileManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ProjectEarth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context ctx = this;

        // Fix for OSDetection in apktool
        System.setProperty("sun.arch.data.model", (System.getProperty("os.arch").contains("64") ? "64" : "32"));

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages =  pm.getInstalledApplications(PackageManager.GET_META_DATA);

        String earthApk = "";
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.mojang.minecraftearth")) {
                earthApk = packageInfo.sourceDir;
                break;
            }
        }

        final Path outDir = getApplicationContext().getExternalFilesDir("").toPath().resolve("com.mojang.minecraftearth");
        final File outFile = getApplicationContext().getExternalFilesDir("").toPath().resolve("dev.projectearth.prod.unsigned.apk").toFile();
        final File outFileSigned = getApplicationContext().getExternalFilesDir("").toPath().resolve("dev.projectearth.prod.apk").toFile();
        final String frameworkDir = getApplicationContext().getExternalFilesDir("").toPath().resolve("framework").toString();

        // Extract aapt
        // aapt from https://github.com/JonForShort/android-tools
        final File aaptExec = getApplicationContext().getFilesDir().toPath().resolve("aapt").toFile();
        if(!aaptExec.exists()) {
            try (InputStream in = getApplicationContext().getResources().openRawResource(R.raw.aapt);
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
        final File earthKeystore = getApplicationContext().getFilesDir().toPath().resolve("earth_test.jks").toFile();
        if(!earthKeystore.exists()) {
            try (InputStream in = getApplicationContext().getResources().openRawResource(R.raw.earth_test);
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

        Button btnDecompile = (Button) findViewById(R.id.btnDecompile);
        final String finalEarthApk = earthApk;
        btnDecompile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ApkDecoder decoder = new ApkDecoder();
                        try {
                            decoder.setApkFile(new File(finalEarthApk));
                            decoder.setOutDir(outDir.toFile());
                            decoder.setForceDelete(true);
                            decoder.setFrameworkDir(frameworkDir);
                            decoder.decode();
                            Log.d(TAG, "DONE!");
                        } catch (AndrolibException | DirectoryException | IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                decoder.close();
                            } catch (IOException ignored) {}
                        }
                    }
                });
            }
        });

        Button btnPatch = (Button) findViewById(R.id.btnPatch);
        btnPatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int urlMax = 27;

                // Setup the server address input
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Server address");
                final EditText input = new EditText(ctx);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(urlMax)});
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        // Make sure we have http or https
                        if (!url.matches("^(http|https)://.*$")) {
                            url = "https://" + url;
                        }

                        // Remove trailing slash
                        if (url.endsWith("/")) {
                            url = url.substring(0, url.length() - 1);
                        }

                        // Check the url length
                        if (url.length() > urlMax) {
                            AndroidUtils.showToast(ctx, "String too long (" + url.length() + ">" + urlMax + ")");
                            return;
                        }

                        final String finalUrl = String.format("%1$-" + urlMax + "s", url).replace(' ', '\0');

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try (RandomAccessFile raf = new RandomAccessFile(outDir.resolve("lib/arm64-v8a/libgenoa.so").toString(), "rw")) {
                                    raf.seek(0x0514D05D);
                                    raf.write(finalUrl.getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        Button btnRecompile = (Button) findViewById(R.id.btnRecompile);
        btnRecompile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ApkOptions apkOptions = new ApkOptions();
                        apkOptions.frameworkFolderLocation = frameworkDir;
                        apkOptions.aaptPath = aaptExec.getAbsolutePath();
                        try {
                            new Androlib(apkOptions).build(outDir.toFile(), outFile);
                            Log.d(TAG, "DONE!");
                        } catch (BrutException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Button btnSign = (Button) findViewById(R.id.btnSign);
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            KeyStore keystore = KeyStoreFileManager.loadKeyStore(earthKeystore.getPath(), "earth_test".toCharArray());

                            ZipSigner zipSigner = new ZipSigner();
                            Certificate cert = keystore.getCertificate("earth_test");
                            X509Certificate publicKey = (X509Certificate)cert;
                            Key key = keystore.getKey("earth_test", "earth_test".toCharArray());
                            PrivateKey privateKey = (PrivateKey)key;

                            zipSigner.setKeys( "earth_test", publicKey, privateKey, "SHA1withRSA", null);
                            zipSigner.signZip(outFile.toString(), outFileSigned.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Button btnInstall = (Button) findViewById(R.id.btnInstall);
        btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(FileProvider.getUriForFile(ctx, getApplicationContext().getPackageName() + ".provider", outFileSigned), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        });
    }
}