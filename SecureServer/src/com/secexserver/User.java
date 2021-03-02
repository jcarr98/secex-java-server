package com.secexserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

/**
 * Manages user information
 */
public class User {
    private String name;
    private PublicKey key;
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    public User() {

    }

    /**
     * Send message out of user's socket
     * @param message The message to send
     */
    public void send(String message) {
        out.println(message);
    }

    /**
     * Send message out of user's socket
     * @param message The message to send
     */
    public void send(byte[] message) {
        // First, encode bytes into string
        String strMessage = Base64.getEncoder().encodeToString(message);

        // Send to socket
        out.println(strMessage);
    }

    /**
     * Read the next message from the user's socket
     * @return The message coming to the user
     */
    public String readString() {
        return in.nextLine();
    }

    public boolean stillReceiving() {
        return in.hasNextLine();
    }

    /**
     * Read the next message from user's socket and convert to bytes
     * @return The bytes coming into user's socket
     */
    public byte[] readBytes() {
        // First, read message
        String reply = in.nextLine();

        // Convert to bytes
        byte[] bytes = Base64.getDecoder().decode(reply);

        // Return these bytes
        return bytes;
    }

    /**
     * Set the display name for this user
     * @param name The name for the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the public RSA key for this user
     * @param key The public RSA for the user
     */
    public void setKey(PublicKey key) {
        this.key = key;
    }

    /**
     * Set the socket connected to the user
     * @param socket The socket connected to the user
     */
    public void setSocket(Socket socket) {
        this.socket = socket;

        // Create reader/writer for socket
        try {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the socket connected to the user
     * @return The socket connected to the user
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Get the display name associated with the user
     * @return The user's display name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the public RSA key associated with the user
     * @return User's public RSA key
     */
    public PublicKey getKey() {
        return key;
    }

    /**
     * Return the encoded public RSA key of the user
     * @return User's encoded RSA key
     */
    public byte[] getKeyBytes() {
        return key.getEncoded();
    }

    public void end() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
