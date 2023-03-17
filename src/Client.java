import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 5000)) {
            PrintWriter cout = new PrintWriter(socket.getOutputStream(), true);

            ClientHandler threadClient = new ClientHandler(socket);
            new Thread(threadClient).start(); // start thread to receive messages from server

            String name = "empty";
            String reply = "empty";
            Scanner scanner = new Scanner(System.in);
            //System.out.println("Enter your name (Please enter your name to join the chat): ");
            reply = scanner.nextLine();
            name = reply;

            cout.println(reply + ": has joined!"); // send message to server
            do {
                String message = (name + " : ");
                reply = scanner.nextLine();
                if (reply.equals("-")) {    // Client can send message to server until he/she enters '-'
                    cout.println("-");
                    break;
                }
                cout.println(message + reply);
            } while (true);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}