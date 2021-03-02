package com.secexserver;

import peerconnections.Server;
import encryption.RSA;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.Executors;
import java.util.HashMap;

public class SecureServer {
    private HashMap<Integer, Server> connections;
    private ServerSocket listener;
    private RSA rsa;

    public SecureServer() {
        // Initialize connections hashmap
        connections = new HashMap<Integer, Server>();

        // Create socket
        try {
            listener = new ServerSocket(8008);
        }
        catch(IOException e) {
            System.out.println("Error creating server socket");
            System.exit(1);
        }

        // Initialize RSA
        rsa = new RSA();
    }

    /** Initiates the socket's connection to the server. It checks for a valid handshake and then checks that the ID is valid.
     * A valid ID is one that isn't in use or is waiting for a second connection.
     *
     * @param socket The socket trying to make a connection
     *
     * @return -1 if this is not a valid connection, returns connection ID if connection is complete, returns 0 if waiting for next connection
     */
    public int connect(Socket socket) {
        // Value to return
        int results = 0;

        // Create server for user management
        Server server;

        // Create user for communication
        User user = new User();
        user.setSocket(socket);

        // First message should be HELLO <id>
        String hello = user.readString();
        System.out.println("Received '" + hello + "' from " + socket.getInetAddress() + ", " + socket.getPort());

        String[] helloParsed = hello.split(" ");

        if(helloParsed.length != 2 || !helloParsed[0].equals("HELLO")) {
            System.out.println("Bad handshake");
            return -1;
        }

        // Check ID exists
        int id;
        try {
            id = Integer.parseInt(helloParsed[1]);
        }
        catch(NumberFormatException e) {
            System.out.println("Bad ID format");
            return -1;
        }

        // Check connection exists
        if(connections.containsKey(id)) {
            // Get current server
            server = connections.get(id);

            if(server.userCount() > 1) {
                System.out.println("Two users already connected to this server");
                return -1;
            }

            results = id;
        }
        else {
            // Create server
            server = new Server();

            // Add server to server list
            connections.put(id, server);
            results = 0;
        }

        // Exchange RSA keys with user
        PublicKey uKey = keyExchange(user);

        // Check if key exchange was successful
        if(uKey == null) {
            System.out.println("Bad key exchange");
            user.end();
            return -1;
        }
        else {
            user.setKey(uKey);
        }

        // Send confirmation to user
        String confirmation;
        if(results > 0) {
            confirmation = "OK CONNECTION";
        }
        else {
            confirmation = "OK WAITING";
        }

        // Encrypt and send confirmation
        byte[] safeConfirm = rsa.encrypt(confirmation, user.getKey());
        user.send(safeConfirm);

        // Encrypt key and send to user
        byte[] safeSecret = rsa.encrypt(server.getKey(), user.getKey());
        user.send(safeSecret);

        // Wait for received confirmation and username
        byte[] eRecvConfirm = user.readBytes();
        String recvConfirm = rsa.decrypt(eRecvConfirm);
        String[] pRecvConfirm = recvConfirm.split("\\s", 2);
        System.out.println(recvConfirm);

        if(!pRecvConfirm[0].equals("KEYRECV")) {
            System.out.println("AES Key not received");
            return -1;
        }
        else {
            String dName = checkUsername(pRecvConfirm[1]);
            if(dName != null) {
                user.setName(dName);
            }
            else {
                System.out.println("Bad display name from user");
                return -1;
            }
        }

        // Add user to server
        System.out.println(user.getName() + " successfully connected");
        server.addUser(user);

        // Return results
        return results;
    }

    /**
     * Check validity of username packet.
     * Structure should be DNAME {username}
     *
     * @param uString The packet string to check
     * @return The name if valid, null if not valid
     */
    private String checkUsername(String uString) {
        if(uString.replaceAll(" ", "").length() < 1) {
            System.out.println("Bad: " + uString);
            return null;
        }
        else {
            return uString;
        }
    }

    private PublicKey keyExchange(User user) {
        // Tell user to send public key
        user.send("OK KEY");

        // Get user's public key
        byte[] uKeyBytes = user.readBytes();
        PublicKey uKey;
        try {
            uKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(uKeyBytes));
            user.setKey(uKey);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        // Send server's public key to user, encrypt OK message
        byte[] safeOk = rsa.encrypt("OK", user.getKey());
        user.send(safeOk);
        user.send(rsa.getPublicKey());

        return uKey;
    }

    public Server getServer(int id) {
        return connections.get(id);
    }

    public static void main(String[] args) {
        System.out.println("Running Secure Server...");
        SecureServer serv = new SecureServer();

        Socket sock;
        while(true) {
            // Manage thread pool
            var pool = Executors.newFixedThreadPool(20);

            // Wait for connection
            try {
                sock = serv.listener.accept();
            }
            catch(IOException e) {
                System.out.println("Error connecting to client...");
                continue;
            }

            // Connect with user
            int result = serv.connect(sock);

            // If connection is complete, create new servers
            if(result > 0) {
                Server server = serv.getServer(result);
                // Now communication is handled by server, can remove from responsibilities
                serv.connections.remove(result);

                // Create new thread to run server
                pool.execute(server);
            }
        }
    }
}