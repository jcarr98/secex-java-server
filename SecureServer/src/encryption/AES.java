package encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

public class AES {
    private SecretKey key;

    public AES() {
        // Generate AES key
        key = genKey();
    }

    public byte[] getKey() {
        return key.getEncoded();
    }

    private SecretKey genKey() {
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        // Create random byte generate
        SecureRandom r = new SecureRandom();

        // Initialize key generator
        keyGen.init(256, r);

        SecretKey key = keyGen.generateKey();

        return key;
    }
}
