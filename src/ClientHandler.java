import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class will handle all the messages sent from server to client (It reads the messages).
 * It will always be running in the background to receive messages from server.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BufferedReader input;
    private final BufferedWriter output;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = getServerMessage();
                System.out.println(message);
            }
        } catch (SocketException e) {
            System.out.println("You got disconnected :(");
            System.exit(0);
        } catch (IOException exception) {
            System.out.println(exception);
        } finally {
            try {
                socket.close();
                input.close();
                output.close();
            } catch (Exception exception) {
                System.out.println(exception);
            }
        }
    }

    public void sendToServer(String message) throws IOException {
        output.write(message);
        output.newLine();
        output.flush();
    }

    public String getServerMessage() throws IOException {
        String fullMessage = input.readLine();
        while (input.ready()){
            fullMessage+="\n";
            fullMessage+=input.readLine();
        }
        return fullMessage;
    }
}