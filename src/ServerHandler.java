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

    public ServerHandler(Socket socket, ArrayList<Socket> clients, HashMap<Socket, String> clientNameList) throws IOException {
        this.currentSocket = socket;
        this.clients = clients;
        this.clientNameList = clientNameList;
    }

    @Override
    public void run() {
        // When first connected, menu is previewed to client
        try {
            String menu = "Welcome to Hangman! \n" +
                    "Enter your name: ";
            sendMessageToClient(currentSocket, menu);

            // Handle client messages
            try {
                while (true) {
                    String outputString = getClientMessage(currentSocket);     // Read message sent from client
                    if (outputString.equals("-")) {     //'-' means that client chose to disconnect
                        throw new SocketException();        // Throw exception to handle client disconnection
                    }
                    if (!clientNameList.containsKey(currentSocket)) {      // If client name is not in the list, add it
                        String[] messageString = outputString.split(":", 2);    // Split message to get client name. Message format: "name: message"
                        clientNameList.put(currentSocket, messageString[0]);      // Add client name to list
                        System.out.println(messageString[0]+messageString[1]);    // Print message to server
                        sendMessageToAllClients(currentSocket, messageString[0] + messageString[1]);    // Send message to all clients
                    } else {
                        System.out.println(outputString);  // Print message to server (if name was in the list)
                        sendMessageToAllClients(currentSocket, outputString);
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
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
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