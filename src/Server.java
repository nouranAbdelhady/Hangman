import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    // Server operations
    ServerService serverService;
    private static final int PORT = 5000;
    private final ArrayList<Socket> clients = new ArrayList<>();
    private final HashMap<Socket, String> clientNameList = new HashMap<>();


    // Loaded from files
    // list of clients and client names [in Server (Parent)]
    private HashMap<String, User> users = new HashMap<>();      // List of users loaded from credentials file

    // loaded from files
    private Lookup lookup;
    private Credentials credentials;
    private GameConfig gameConfig;

    public HashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void setLookup(Lookup lookup) {
        this.lookup = lookup;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public ArrayList<Socket> getClients(){
        return clients;
    }
    public HashMap<Socket, String> getClientNameList(){
        return clientNameList;
    }

    public Server() throws FileNotFoundException {
        this.serverService=new ServerServiceImplementation();
        Files files = this.serverService.loadFiles();
        // Set attributes
    }
    public static void main(String[] args) throws FileNotFoundException {
        new Server().start();
    }

    private void start() {
        try{
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("[SERVER] Started...");

            while (true) {
                Socket socket = serverSocket.accept(); // Accept new client
                clients.add(socket); // Add client to list

                MultiThreadedServer ThreadServer = new MultiThreadedServer(socket);    // Accept clients and handle messages (Sending/Receiving)
                new Thread(ThreadServer).start(); // start thread to handle

                if (clients.size() > 3) {
                    System.out.println(clients.size() + " players are connected to server");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}