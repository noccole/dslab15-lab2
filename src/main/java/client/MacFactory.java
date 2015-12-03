package client;

import util.Keys;

import javax.crypto.Mac;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

class MacFactory {
    private final static String MAC_TYPE = "HmacSHA256";

    private final String keyFileName;

    public MacFactory(String keyFileName) {
        this.keyFileName = keyFileName;
    }

    public Mac createMac() throws MacFactoryException {
        try {
            final File hmacKeyFile = new File(keyFileName);
            final Key hmacKey = Keys.readSecretKey(hmacKeyFile);

            final Mac hmac = Mac.getInstance(MAC_TYPE);
            hmac.init(hmacKey);
            return hmac;
        } catch (IOException e) {
            throw new MacFactoryException("Could not load key from file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new MacFactoryException("Algorithm doesn't exist", e);
        } catch (InvalidKeyException e) {
            throw new MacFactoryException("Key is invalid", e);
        }
    }
}
