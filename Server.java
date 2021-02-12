// Packet imports
import java.net.ServerSocket;
import java.net.Socket;

// Reading imports
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Server {
    private ServerSocket serv;
    final private int port = 8008;

    public Server() {
        // Create and bind socket to port
        try {
            this.serv = new ServerSocket(this.port);
        }
        catch(IOException e) {
            System.out.println("Port is already in use");
        }
    }

    public void start() {
        System.out.println("Server started on " + this.port);
        // Constantly run server
        while(true) {
            run();
        }
    }

    private void run() {
        System.out.println("Waiting for connections...");
        // Wait for connection
        Socket connection;
        try {
            connection = this.serv.accept();
        }
        catch(IOException e) {
            System.out.println("Failed accepting connection");
            return;
        }

        System.out.println("Connection received!");
        System.out.println("Client info: " + connection.getInetAddress());

        // Create readers for in and out
        Scanner in;
        PrintWriter out;
        try {
            in = new Scanner(connection.getInputStream());
            out = new PrintWriter(connection.getOutputStream(), true);
        }
        catch(Exception e) {
            System.out.println("Error with reader or writer");

            try {
                connection.close();
                System.out.println("Connection closed");
            }
            catch(IOException f) {
                System.out.println("Error closing connection");
            }

            return;
        }

        while(in.hasNextLine()) {
            String line = in.nextLine();
            System.out.println("Client sent " + line);
            System.out.println("Sending " + line.toUpperCase());
            out.println(line.toUpperCase());
        }

        // Done, close socket
        try {
            connection.close();
            System.out.println("Connection closed");
        }
        catch(IOException e) {
            System.out.println("Error closing connection");
        }

        return;
    }
}