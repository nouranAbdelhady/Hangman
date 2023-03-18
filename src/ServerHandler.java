import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to handle client connection and messages.
 * It handles all the messages sent from client to server (It reads the messages).
 * It will also send messages to all clients connected to server, as well as to a specific client.
 */
public class ServerHandler implements Runnable{ // Thread to handle client connection and messages

    private Socket currentSocket;    // Socket of client
    private ArrayList<Socket> clients;      // List of clients connected to server
    private HashMap<Socket, String> clientNameList;     // List of clients' name connected to server (to help with output message)


    //load files
    Lookup lookup = new Lookup();
    Credentials credentials = new Credentials();

    public ServerHandler(Socket socket, ArrayList<Socket> clients, HashMap<Socket, String> clientNameList) throws IOException {
        this.currentSocket = socket;
        this.clients = clients;
        this.clientNameList = clientNameList;
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
                        // TODO: Check if username is already taken
                        String username = getClientMessage(currentSocket);
                        while (!credentials.isUniqueUsername(username)) {
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
                sendMessageToAllClients(currentSocket, printMessage);      // Send message to all clients that client disconnected
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            clients.remove(currentSocket);
            clientNameList.remove(currentSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}