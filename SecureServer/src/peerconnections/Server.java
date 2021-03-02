package peerconnections;
import com.secexserver.User;
import encryption.AES;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PublicKey;

public class Server implements Runnable {
    User u1, u2;
    AES aes;
    int numUsers;

    public Server() {
        numUsers = 0;

        // Initiate aes
        aes = new AES();
    }

    public int userCount() {
        return numUsers;
    }

    public byte[] getKey() {
        return aes.getKey();
    }

    /**
     * Adds a peer to connect to this server. Only two peers may be added.
     * @param user The socket connected to the user to add
     *
     * @return True if peer was added successfully, false if peer was not added successfully
     */
    public void addUser(User user) {
        if(numUsers == 0) {
            u1 = user;
        }
        else {
            u2 = user;
        }

        numUsers++;
    }

    /**
     * Run the server
     */
    public void run() {
        // Send ok to clients

        // Send connection ok and usernames
        u1.send(encryptRSA("CONNECTED " + u2.getName(), u1.getKey()));
        u2.send(encryptRSA("CONNECTED " + u1.getName(), u2.getKey()));

        // Initiate connection between users
        communicate();
    }

    /**
     * Manage communication between clients. Creates a Sender/Receiver thread
     * for each peer
     */
    private void communicate() {
        // Create sending/receiving threads for each peer
        SenderThread S1 = new SenderThread(u1, u2);
        SenderThread S2 = new SenderThread(u2, u1);
        ReceiverThread R1 = new ReceiverThread(u1, u2);
        ReceiverThread R2 = new ReceiverThread(u2, u1);

        S1.start();
        S2.start();
        R1.start();
        R2.start();

        try{
            S1.join();
            S2.join();
            R1.join();
            R2.join();
            System.out.println("All threads closed");
        }
        catch(InterruptedException e) {
            System.out.println("Error joining threads");
        }

        return;
    }

    private byte[] encryptRSA(String plaintext, PublicKey key) {
        // Create RSA cipher
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        } catch (GeneralSecurityException e) {
            System.out.println("Error creating RSA cipher");
            return null;
        }

        // Initialize cipher in encrypt mode and encrypt with cipher
        byte[] ciphertext;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }

        return ciphertext;
    }
}
