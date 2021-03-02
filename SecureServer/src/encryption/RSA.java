package encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class RSA {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Cipher cipher;

    public RSA() {
        // Create public/private keys
        KeyPair pair = generateKeys();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();

        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        }
        catch(GeneralSecurityException e) {
            System.out.println("Error initiating cipher");
        }
    }

    /**
     * Generates the KeyPair for encryption/decryption
     * @return KeyPair to get public/private keys from
     */
    private KeyPair generateKeys() {
        // Create key generator
        KeyPairGenerator gen;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
        }
        catch(GeneralSecurityException e) {
            System.out.println("Error creating key generator");
            return null;
        }

        // Initialize key generator
        gen.initialize(2048, new SecureRandom());

        // Create keypair
        return gen.generateKeyPair();
    }

    /**
     * Get the public key for this RSA object
     * @return bytes of RSA public key
     */
    public byte[] getPublicKey() {
        return publicKey.getEncoded();
    }

    /**
     * Encrypt the provided plaintext using the provided PublicKey
     * @param plaintext Plaintext to encrypt
     * @param key Key to encrypt plaintext with
     * @return An array of bytes representing the encrypted plaintext
     */
    public byte[] encrypt(String plaintext, PublicKey key) {
        try {
            return encrypt(plaintext.getBytes("UTF-8"), key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] encrypt(byte[] plaintext, PublicKey key) {
        // Initialize cipher in encrypt mode and encrypt with cipher
        byte[] ciphertext;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ciphertext = cipher.doFinal(plaintext);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }

        return ciphertext;
    }

    /**
     * Decrypt the provided ciphertext using public key belonging to this RSA object
     * @param ciphertext Ciphertext to decrypt
     * @return A plaintext string of the provided ciphertext
     */
    public String decrypt(byte[] ciphertext) {
        // Initialize cipher in decrypt mode and encrypt with cipher
        String plaintext;
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            plaintext = new String(plaintextBytes, "UTF-8");
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        return plaintext;
    }
}
