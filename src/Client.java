import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This class is used send message to the server (using methods in ClientHandler class)
 */
public class Client {

    public static void main(String[] args) {

        try{
            Socket server = new Socket("localhost", 5000);  // Create socket to connect to server
            ClientHandler threadClient = new ClientHandler(server);
            new Thread(threadClient).start(); // start thread to receive messages from server

            Scanner scanner = new Scanner(System.in);
            String reply;
            do {
                reply = scanner.nextLine();
                if (reply.equals("-")) {    // Client can send message to server until he/she enters '-'
                    threadClient.sendToServer("-");
                    break;
                }
                threadClient.sendToServer(reply);
            } while (true);

            // Disconnect from server
            scanner.close();
            server.close();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}