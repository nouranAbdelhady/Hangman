import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class is used to handle client connection and messages.
 * It handles all the messages sent from client to server (It reads the messages).
 * It will also send messages to all clients connected to server, as well as to a specific client.
 */
public class ServerHandler implements Runnable{ // Thread to handle client connection and messages

    private Socket currentSocket;    // Socket of client
    private ArrayList<Socket> clients;      // List of clients connected to server
    private HashMap<Socket, String> clientNameList;     // List of clients' name connected to server (to help with output message)

    private HashMap<String, User> users = new HashMap<>();      // List of users loaded from credentials file

    // loaded from files
    private Lookup lookup;
    private Credentials credentials;
    private GameConfig gameConfig;

    public ServerHandler(Socket socket, ArrayList<Socket> clients, HashMap<Socket, String> clientNameList) throws IOException {
        this.currentSocket = socket;
        this.clients = clients;
        this.clientNameList = clientNameList;

        // Load files
        this.lookup = new Lookup();
        this.credentials = new Credentials();
        this.users = (HashMap<String, User>) credentials.getUserMap();      // assign users that are loaded from credentials file
        this.gameConfig = new GameConfig();

        // Load score file and update scores for each user
        this.loadScore();
    }

    @Override
    public void run() {
        try {
            String welcome="Welcome to Hangman! \n";
            sendMessageToClient(currentSocket, welcome);
            // When first connected, menu is previewed to client
            // Handle client messages
            while (true){
                String menu = "1- Register \n" +
                        "2- Login";
                sendMessageToClient(currentSocket, menu);
                String option = getClientMessage(currentSocket);     // Read option sent from client
                if (option.equals("-")) {     //'-' means that client chose to disconnect
                    throw new SocketException();        // Throw exception to handle client disconnection
                }
                switch (option){
                    case "1":
                        // Register
                        String registerMenu = "Registration: \n" +
                                "Enter your name: ";
                        sendMessageToClient(currentSocket, registerMenu);
                        String name = getClientMessage(currentSocket);
                        sendMessageToClient(currentSocket, "Enter username");
                        String username = getClientMessage(currentSocket);
                        while (!credentials.isUniqueUsername(username)) {       // username must be unique
                            sendMessageToClient(currentSocket, "Username already taken. Please try again.");
                            username = getClientMessage(currentSocket);
                        }
                        sendMessageToClient(currentSocket, "Enter password");
                        String password = getClientMessage(currentSocket);
                        credentials.addUser(name,username, password);
                        sendMessageToClient(currentSocket, "Registration successful!");
                        break;
                    case "2":
                        // Login
                        String loginMenu = "Login: \n" +
                                "Enter your username: ";
                        sendMessageToClient(currentSocket, loginMenu);
                        String usernameLogin = getClientMessage(currentSocket);
                        sendMessageToClient(currentSocket, "Enter your password: ");
                        String passwordLogin = getClientMessage(currentSocket);
                        int loginResponse=credentials.checkCredentials(usernameLogin, passwordLogin);
                        if (loginResponse==200) {
                            sendMessageToClient(currentSocket, "Login successful!");
                            String printMessage = usernameLogin + " has connected to server";
                            System.out.println(printMessage);       // Print message to server
                            clientNameList.put(currentSocket, usernameLogin);
                        } else {
                            sendMessageToClient(currentSocket, loginResponse+" - Login Failed!");
                        }
                        break;
                    default:
                        sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                        break;
                }
            }
        } catch (SocketException e) {
            String printMessage = clientNameList.get(currentSocket) + " got disconnected";
            System.out.println(printMessage);       // Print message to server
            try {
                // Should only send to team members
                sendMessageToAllClients(currentSocket, printMessage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            clients.remove(currentSocket);
            clientNameList.remove(currentSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadScore() throws FileNotFoundException {
        FileInputStream file=new FileInputStream("./src/Files/score.txt");
        Scanner sc=new Scanner(file);    //file to be scanned
        //returns true if there is another line to read
        while(sc.hasNextLine())
        {
            String line=sc.nextLine();
            // separate username and score
            String[] parts = line.split("-");
            this.users.get(parts[0]).setScore(Integer.parseInt(parts[1]));     // Update score
        }
        sc.close();     //closes the scanner
    }

    private void sendMessageToAllClients(Socket sender, String message) throws IOException {
        BufferedWriter writer;

        // Loop on all *active* clients and send message
        for(Socket client : clients){
            if (client != sender) {     // Don't send message to sender
                writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        }
    }

    private void sendMessageToClient(Socket client, String message) throws IOException {
        BufferedWriter writer;
        writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    private String getClientMessage(Socket client) throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        return reader.readLine();
    }

    public void previewUsers() {
        System.out.println("Users: ");
        for (String key : users.keySet()) {
            System.out.println(key + " " + users.get(key).getScore());
        }
    }
}