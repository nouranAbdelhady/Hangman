import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public interface ServerService {
    public void sendMessageToAllClients(Socket sender, String message, ArrayList<Socket> clients) throws IOException;
    public void sendMessageToClient(Socket client, String message) throws IOException;
    public String getClientMessage(Socket client) throws IOException;
    public Files loadFiles() throws FileNotFoundException;
    public GameConfig loadGameConfig() throws FileNotFoundException;
    public Lookup loadLookupPhrases() throws FileNotFoundException;
    public Map<String, User> loadUsers() throws FileNotFoundException;

}
