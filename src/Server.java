import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {

    // serverService has all the Server operations
    public static ServerService serverService;
    private static final int PORT = 5000;
    private static final ArrayList<Socket> clients = new ArrayList<>();
    private static final HashMap<Socket, String> clientNameList = new HashMap<>();

    // Loaded from files
    private static Lookup lookup;
    private static GameConfig gameConfig;

    // list of clients and client names [in Server (Parent)]
    private static Map<String, User> users = new HashMap<>();      // List of users loaded from credentials file

    private static Map<String, Team> teams = new HashMap<>();      // List of teams formed
    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public ServerService getServerService() {
        return serverService;
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    public Map<String, Team> getTeams() {
        return teams;
    }

    public void addTeam(Team team){
        this.teams.put(team.getTeamName(), team);
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void setLookup(Lookup lookup) {
        this.lookup = lookup;
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

    public Server() {
    }

    public static void main(String[] args) throws FileNotFoundException {
        Server server = new Server();
        server.start();
    }

    private void start() {
        try{
            // Load files
            this.serverService=new ServerServiceImplementation();
            Files files = this.serverService.loadFiles();

            // Set attributes
            this.setLookup(files.getLookupFile());
            this.setGameConfig(files.getGameConfigFile());
            this.setUsers(files.getAllUsers());

            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("[SERVER] Started...");

            while (true) {
                Socket socket = serverSocket.accept(); // Accept new client
                clients.add(socket); // Add client to list

                MultiThreadedServer ThreadServer = new MultiThreadedServer(socket);    // Accept clients and handle messages (Sending/Receiving)
                new Thread(ThreadServer).start(); // start thread to handle
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}