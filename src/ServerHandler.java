import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to handle client connection
 * Each client will have a thread to handle its connection
 * This class will handle all the messages sent from client to server.
 * It will also send messages to all clients connected to server, and
 * can send messages to a specific client.
 */
public class ServerHandler extends Thread{ // Thread to handle client connection and messages

    private Socket currentSocket;    // Socket of client
    private ArrayList<Socket> clients;      // List of clients connected to server
    private HashMap<Socket, String> clientNameList;     // List of clients' name connected to server (to help with output message)
    private BufferedReader input;     // Read input sent from client

    public ServerHandler(Socket socket, ArrayList<Socket> clients, HashMap<Socket, String> clientNameList) throws IOException {
        this.currentSocket = socket;
        this.clients = clients;
        this.clientNameList = clientNameList;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        // When first connected, menu is previewed to client
        try {
            sendMessageToClient(currentSocket, "Welcome to Hangman! ");
            sendMessageToClient(currentSocket, "Enter your name");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Handle client messages
        try {
            while (true) {
                String outputString = input.readLine();     // Read message sent from client
                if (outputString.equals("-")) {     //'-' means that client chose to disconnect
                    throw new SocketException();        // Throw exception to handle client disconnection
                }
                if (!clientNameList.containsKey(currentSocket)) {      // If client name is not in the list, add it
                    String[] messageString = outputString.split(":", 2);    // Split message to get client name. Message format: "name: message"
                    clientNameList.put(currentSocket, messageString[0]);      // Add client name to list
                    System.out.println(messageString[0]+messageString[1]);    // Print message to server
                    showMessageToAllClients(currentSocket, messageString[0] + messageString[1]);    // Send message to all clients
                } else {
                    System.out.println(outputString);  // Print message to server (if name was in the list
                    showMessageToAllClients(currentSocket, outputString);
                }
            }
        } catch (SocketException e) {
            String printMessage = clientNameList.get(currentSocket) + " got disconnected";
            System.out.println(printMessage);
            try {
                showMessageToAllClients(currentSocket, printMessage);      // Send message to all clients that client disconnected
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            clients.remove(currentSocket);
            clientNameList.remove(currentSocket);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    private void showMessageToAllClients(Socket sender, String outputString) throws IOException {
        PrintWriter printWriter;
        // Loop on all *active* clients and send message
        for(Socket client : clients){
            if (client != sender) {     // Don't send message to sender
                printWriter = new PrintWriter(client.getOutputStream(), true);
                printWriter.println(outputString);
            }
        }
    }

    private void sendMessageToClient(Socket client, String message) throws IOException {
        PrintWriter printWriter;
        printWriter = new PrintWriter(client.getOutputStream(), true);
        printWriter.println(message);
    }

}