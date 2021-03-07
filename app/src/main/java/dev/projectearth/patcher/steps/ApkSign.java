package dev.projectearth.patcher.steps;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.projectearth.patcher.MainActivity;
import dev.projectearth.patcher.R;
import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.KeyStoreFileManager;
import lombok.SneakyThrows;

public class ApkSign extends LoggedRunnable {
    private static final Map<String, Integer> progressEvents = new HashMap<>();

    @SneakyThrows
    @Override
    public void run() {
        logEventListener.onLogLine("00% - " + MainActivity.getAppContext().getResources().getString(R.string.step_sign_setup));
        KeyStore keystore = KeyStoreFileManager.loadKeyStore(StorageLocations.getEarthKeystore().getPath(), "earth_test".toCharArray());

        // TODO: Setup a proper key
        ZipSigner zipSigner = new ZipSigner();

        progressEvents.clear();
        zipSigner.addProgressListener(event -> {
            if (event.getPercentDone() % 5 == 0) {
                String messageKey = Arrays.stream(event.getMessage().split(" ")).limit(3).collect(Collectors.joining(" "));
                Integer prev = progressEvents.get(messageKey);
                if (prev == null || prev != event.getPercentDone()) {
                    progressEvents.put(messageKey, event.getPercentDone());

                    logEventListener.onLogLine(String.format("%1$2s", event.getPercentDone()).replaceAll(" ", "0") + "% - " + event.getMessage() + "...");
                }
            }
        });

        zipSigner.issueLoadingCertAndKeysProgressEvent();
        Certificate cert = keystore.getCertificate("earth_test");
        X509Certificate publicKey = (X509Certificate)cert;
        Key key = keystore.getKey("earth_test", "earth_test".toCharArray());
        PrivateKey privateKey = (PrivateKey)key;

        zipSigner.setKeys( "earth_test", publicKey, privateKey, "SHA1withRSA", null);

        logEventListener.onLogLine("00% - " + MainActivity.getAppContext().getResources().getString(R.string.step_sign_signing));
        zipSigner.signZip(StorageLocations.getOutFile().toString(), StorageLocations.getOutFileSigned().toString());
        logEventListener.onLogLine(MainActivity.getAppContext().getResources().getString(R.string.step_done));
    }
}
