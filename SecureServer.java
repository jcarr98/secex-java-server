public class SecureServer {
    public static void main(String[] args) {
        System.out.println("Running the Secure Server...");

        Server serv = new Server();

        System.out.println("Starting server...");
        serv.start();
    }
}