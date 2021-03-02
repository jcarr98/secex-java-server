package peerconnections;

import com.secexserver.User;

import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;

public class SenderThread extends Thread {
    private User u1, u2;
    private Thread t;

    public SenderThread(User u1, User u2) {
        this.u1 = u1;
        this.u2 = u2;
    }

    public void start() {
        if(t == null) {
            t = new Thread(this, "sender");
            t.start();
        }
    }

    public void run() {
        while(u1.stillReceiving()) {
            String line = u1.readString();

            // Check if user is quitting
            if(line.equals("/quit")) {
                break;
            }
            System.out.println("Forwarding " + line + " from " + u1.getName() + " to " + u2.getName());

            u2.send(line);
        }

        // If user quits, close the socket
        u1.end();
        u2.end();
    }
}
