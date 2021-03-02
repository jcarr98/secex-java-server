package peerconnections;

import com.secexserver.User;

import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;

public class ReceiverThread extends Thread {
    private User u1, u2;
    private Thread t;

    public ReceiverThread(User u1, User u2) {
        this.u1 = u1;
        this.u2 = u2;
    }

    public void start() {
        if(t == null) {
            t = new Thread(this, "receiver");
            t.start();
        }
    }

    public void run() {
        while(u2.stillReceiving()) {
            String line = u2.readString();
            u1.send(line);
        }

        u1.end();
        u2.end();

        return;
    }
}
