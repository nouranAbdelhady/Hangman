import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class is used to handle multiple client connections, and
 * opens a new thread for EACH socket connection
 */
public class MultiThreadedServer extends Server implements Runnable{

    private Socket currentSocket;    // Socket of client

    public MultiThreadedServer(Socket socket) throws IOException {
        this.currentSocket = socket;
    }

    @Override
    public void run() {
        try {
            String welcome="Welcome to Hangman! \n";
            super.serverService.sendMessageToClient(currentSocket, welcome);
            // When first connected, menu is previewed to client
            // Handle client messages
            while (true){
                String menu = "1- Register \n" +
                        "2- Login";
                super.serverService.sendMessageToClient(currentSocket, menu);
                String option = super.serverService.getClientMessage(currentSocket);     // Read option sent from client
                if (option.equals("-")) {     //'-' means that client chose to disconnect
                    throw new SocketException();        // Throw exception to handle client disconnection
                }
                switch (option){
                    case "1":
                        // Register
                        String registerMenu = "Registration: \n" +
                                "Enter your name: ";
                        super.serverService.sendMessageToClient(currentSocket, registerMenu);
                        String name = super.serverService.getClientMessage(currentSocket);
                        super.serverService.sendMessageToClient(currentSocket, "Enter username");
                        String username = super.serverService.getClientMessage(currentSocket);
                        while (!super.getCredentials().isUniqueUsername(username)) {       // username must be unique
                            super.serverService.sendMessageToClient(currentSocket, "Username already taken. Please try again.");
                            username = super.serverService.getClientMessage(currentSocket);
                        }
                        super.serverService.sendMessageToClient(currentSocket, "Enter password");
                        String password = super.serverService.getClientMessage(currentSocket);
                        super.getCredentials().addUser(name,username, password);
                        super.serverService.sendMessageToClient(currentSocket, "Registration successful!");
                        break;
                    case "2":
                        // Login
                        String loginMenu = "Login: \n" +
                                "Enter your username: ";
                        super.serverService.sendMessageToClient(currentSocket, loginMenu);
                        String usernameLogin = super.serverService.getClientMessage(currentSocket);
                        super.serverService.sendMessageToClient(currentSocket, "Enter your password: ");
                        String passwordLogin = super.serverService.getClientMessage(currentSocket);
                        int loginResponse=super.getCredentials().checkCredentials(usernameLogin, passwordLogin);
                        if (loginResponse==200) {
                            super.serverService.sendMessageToClient(currentSocket, "Login successful!");
                            String printMessage = usernameLogin + " has connected to server";
                            System.out.println(printMessage);       // Print message to server
                            super.getClientNameList().put(currentSocket, usernameLogin);
                        } else {
                            super.serverService.sendMessageToClient(currentSocket, loginResponse+" - Login Failed!");
                        }
                        break;
                    default:
                        super.serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                        break;
                }
            }
        } catch (SocketException e) {
            String printMessage = super.getClientNameList().get(currentSocket) + " got disconnected";
            System.out.println(printMessage);       // Print message to server
            try {
                // Should only send to team members
                super.serverService.sendMessageToAllClients(currentSocket, printMessage, super.getClients());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            super.getClients().remove(currentSocket);
            super.getClientNameList().remove(currentSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}