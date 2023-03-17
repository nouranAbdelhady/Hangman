import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class will handle all the messages sent from server to client.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BufferedReader input;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = input.readLine();
                System.out.println(message);
            }
        } catch (SocketException e) {
            System.out.println("You got disconnected....");
        } catch (IOException exception) {
            System.out.println(exception);
        } finally {
            try {
                input.close();
            } catch (Exception exception) {
                System.out.println(exception);
            }
        }
    }
}