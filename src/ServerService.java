import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public interface ServerService {
    public void sendMessageToAllClients(Socket sender, String message, ArrayList<Socket> clients) throws IOException;
    public void sendMessageToClient(Socket client, String message) throws IOException;
    public String getClientMessage(Socket client) throws IOException;
    public Files loadFiles() throws FileNotFoundException;
    public GameConfig loadGameConfig() throws FileNotFoundException;
    public Lookup loadLookupPhrases() throws FileNotFoundException;
    public void loadScore() throws FileNotFoundException;

}
