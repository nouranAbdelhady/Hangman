import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface ServerService {
    public void sendMessageToAllClients(Socket sender, String message, ArrayList<Socket> clients) throws IOException;
    public void sendMessageToClient(Socket client, String message) throws IOException;
    public void sendMessageToAllTeamMembers(Team team, String message, HashMap<Socket, String> clients) throws IOException;
    public String getClientMessage(Socket client) throws IOException;
    public Files loadFiles() throws FileNotFoundException;
    public GameConfig loadGameConfig() throws FileNotFoundException;
    public Lookup loadLookupPhrases() throws FileNotFoundException;
    public Map<String, User> loadUsers() throws FileNotFoundException;
    public int checkCredentials(String username, String password) throws IOException;
    public boolean isUniqueUsername(String username) throws IOException;
    public void printUserContent() throws IOException;
    public void addUser(String name, String username, String password) throws IOException;
    public void login(String username) throws IOException;
    public void logout(String username) throws IOException;
    public void printTeams(Map<String, Team> teams);
}
