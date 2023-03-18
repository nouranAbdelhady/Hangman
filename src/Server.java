import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 10;

    // This allows us to handle multiple clients at the same time
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private final ArrayList<Socket> clients = new ArrayList<>();
    private final HashMap<Socket, String> clientNameList = new HashMap<>();


    public static void main(String[] args) {
        new Server().run();
    }

    private void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("[SERVER] Started...");

            while (true) {
                Socket socket = serverSocket.accept(); // Accept new client
                clients.add(socket); // Add client to list

                // Create new thread for each client
                ServerHandler serverHandler = new ServerHandler(socket, clients, clientNameList);
                executorService.execute(serverHandler); // Use thread pool to handle client connection

                if (clients.size() > 3) {
                    System.out.println(clients.size() + " players are connected to server");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }
}