import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) {
        ArrayList<Socket> clients = new ArrayList<>();
        HashMap<Socket, String> clientNameList = new HashMap<Socket, String>();

        try {
            ServerSocket serversocket = new ServerSocket(5000);  // Create server socket

            // Load files
            Lookup lookup = new Lookup();
            System.out.println("[SERVER] Started...");

            while (true) {
                Socket socket = serversocket.accept();  // Accept new client
                clients.add(socket);   // Add client to list

                // Create new thread for *each* client
                ServerHandler ThreadServer = new ServerHandler(socket, clients, clientNameList);    // Accept clients and handle messages (Sending/Receiving)
                new Thread(ThreadServer).start(); // start thread to handle client connection

                if (clients.size()>3) {
                    System.out.println(clients.size()+" players are connected to server");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}