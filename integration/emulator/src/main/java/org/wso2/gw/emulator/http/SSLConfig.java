package org.wso2.gw.emulator.http;

import java.io.File;

/**
 * Created by dilshank on 1/12/16.
 */
public class SSLConfig {

    private File keyStore;
    private String keyStorePass;
    private String certPass;
    private File trustStore;
    private String trustStorePass;

    public SSLConfig(File keyStore, String keyStorePass, String certPass, File trustStore, String trustStorePass) {
        this.keyStore = keyStore;
        this.keyStorePass = keyStorePass;
        this.certPass = certPass;
        this.trustStore = trustStore;
        this.trustStorePass = trustStorePass;
    }

    public String getCertPass() {
        return certPass;
    }


    public File getTrustStore() {
        return trustStore;
    }


    public String getTrustStorePass() {
        return trustStorePass;
    }


    public File getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }
}

