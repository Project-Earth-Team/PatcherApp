package dev.projectearth.patcher.steps;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import dev.projectearth.patcher.utils.LoggedRunnable;
import dev.projectearth.patcher.utils.StorageLocations;
import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.KeyStoreFileManager;
import lombok.SneakyThrows;

public class ApkSign extends LoggedRunnable {
    @SneakyThrows
    @Override
    public void run() {
        logEventListener.onLogLine("Setting up...");
        KeyStore keystore = KeyStoreFileManager.loadKeyStore(StorageLocations.getEarthKeystore().getPath(), "earth_test".toCharArray());

        // TODO: Setup a proper key
        ZipSigner zipSigner = new ZipSigner();
        Certificate cert = keystore.getCertificate("earth_test");
        X509Certificate publicKey = (X509Certificate)cert;
        Key key = keystore.getKey("earth_test", "earth_test".toCharArray());
        PrivateKey privateKey = (PrivateKey)key;

        zipSigner.setKeys( "earth_test", publicKey, privateKey, "SHA1withRSA", null);

        logEventListener.onLogLine("Signing...");
        zipSigner.signZip(StorageLocations.getOutFile().toString(), StorageLocations.getOutFileSigned().toString());
        logEventListener.onLogLine("Done!");
    }
}
