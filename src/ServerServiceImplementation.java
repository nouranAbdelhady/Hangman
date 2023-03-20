import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerServiceImplementation implements ServerService {
    @Override
    public void sendMessageToAllClients(Socket sender, String message, ArrayList<Socket> clients) throws IOException {
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

    @Override
    public void sendMessageToClient(Socket client, String message) throws IOException{
        BufferedWriter writer;
        writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    @Override
    public String getClientMessage(Socket client) throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        return reader.readLine();
    }

    @Override
    public Files loadFiles() throws FileNotFoundException {
        //System.out.println("Starting to load files");
        // Logic
        Files readFiles = new Files();

        // Game Configurations
        GameConfig readGameConfigFile = this.loadGameConfig();
        readFiles.setGameConfigFile(readGameConfigFile);

        // Lookup Phrases
        Lookup readLookupFile = this.loadLookupPhrases();
        readFiles.setLookupFile(readLookupFile);

        //System.out.println("Game Config:");
        //readGameConfigFile.previewConfig();
        //System.out.println("Lookup:");
        //readLookupFile.printContent();

        Map<String, User> loadedUsers = this.loadUsers();
        readFiles.setAllUsers(loadedUsers);
        //System.out.println("Users:");

        System.out.println("Files loaded");
        return readFiles;
    }

    @Override
    public GameConfig loadGameConfig() throws FileNotFoundException {
        // Load game config from file
        FileInputStream file=new FileInputStream("./src/Files/configurations.txt");
        Scanner sc=new Scanner(file);    //file to be scanned

        //attemps
        String line=sc.nextLine();
        //split on '='
        String[] parts = line.split("=");
        int incorrectAttempts = Integer.parseInt(parts[1]);

        //min players
        line=sc.nextLine();
        //split on '='
        parts = line.split("=");
        int minPlayers= Integer.parseInt(parts[1]);

        //max players
        line=sc.nextLine();
        //split on '='
        parts = line.split("=");
        int maxPlayers= Integer.parseInt(parts[1]);

        sc.close();     //closes the scanner
        return new GameConfig(incorrectAttempts, minPlayers, maxPlayers);
    }

    public Lookup loadLookupPhrases() throws FileNotFoundException {
        // Load phrases from file
        ArrayList<String> phrases = new ArrayList<>();

        FileInputStream file=new FileInputStream("./src/Files/lookup.txt");
        Scanner sc=new Scanner(file);    //file to be scanned
        //returns true if there is another line to read
        while(sc.hasNextLine())
        {
            phrases.add(sc.nextLine());      //adds the line to the arraylist
        }
        sc.close();     //closes the scanner
        return new Lookup(phrases);
    }

    public Map<String, User> loadUsers() throws FileNotFoundException {
        // Load usernames and passwords from file
        Map<String, User> user_map = new HashMap<String, User>();

        FileInputStream loginn=new FileInputStream("./src/Files/login.txt");
        Scanner login=new Scanner(loginn);    //file to be scanned
        //returns true if there is another line to read
        while(login.hasNextLine())
        {
            String readLine = login.nextLine();
            //System.out.println(readLine);

            String[] line = readLine.split("-"); // split line into username and password
            user_map.put(line[1], new User(line[0], line[1], line[2])); // add username and password to hashmap
        }
        login.close();     //closes the scanner

        // Update scores
        FileInputStream file=new FileInputStream("./src/Files/score.txt");
        Scanner sc=new Scanner(file);    //file to be scanned
        //returns true if there is another line to read
        while(sc.hasNextLine())
        {
            String line=sc.nextLine();
            // separate username and score
            String[] parts = line.split("-");
            user_map.get(parts[0]).setScore(Integer.parseInt(parts[1]));     // Update score
        }
        sc.close();     //closes the scanner

        return user_map;
    }

    @Override
    public int checkCredentials(String username, String password) throws IOException {
        // hashmap of usernames and passwords
        Map<String, User> loadedUsers = this.loadUsers();
        // check if username and password are in the hashmap
        if (loadedUsers.containsKey(username)){
            if (loadedUsers.get(username).getPassword().equals(password)){
                // password correct
                // update user status
                loadedUsers.get(username).setOnline(true);
                return 200;     // 200 = ok
            }else{
                // password incorrect
                return 401;       // 401 = unauthorized
            }
        }else{
            // username not found
            return 404;     // 404 = not found
        }
    }

    @Override
    public boolean isUniqueUsername(String username) throws IOException {
        Map<String, User> loadedUsers = this.loadUsers();
        return !loadedUsers.containsKey(username);     // return true if username is unique
    }

    @Override
    public void printUserContent() throws IOException {
        Map<String, User> loadedUsers = this.loadUsers();
        // print the contents of the hashmap
        for (Map.Entry<String, User> entry : loadedUsers.entrySet()) {
            // print username and password
            System.out.println(entry.getKey() + " -> " + entry.getValue().getPassword()+" -> " + entry.getValue().getScore());
        }
    }

    @Override
    public void addUser(String name, String username, String password) throws IOException {
        Map<String, User> loadedUsers = this.loadUsers();
        // add new user to hashmap
        loadedUsers.put(username, new User(name, username, password));

        // add to file (login and score)
        FileWriter myWriterLogin = new FileWriter("./src/Files/login.txt", true);
        myWriterLogin.write(name + "-" + username + "-" + password+"\n");
        myWriterLogin.close();
        FileWriter myWriterScore = new FileWriter("./src/Files/score.txt", true);
        myWriterScore.write(username + "-" + 0+"\n");
        myWriterScore.close();
    }

    @Override
    public void login(String username) throws IOException {
        Map<String, User> loadedUsers = this.loadUsers();
        // update user status
        loadedUsers.get(username).setOnline(true);
    }

    @Override
    public void logout(String username) throws IOException {
        Map<String, User> loadedUsers = this.loadUsers();
        // update user status
        loadedUsers.get(username).setOnline(false);
    }

    @Override
    public void printTeams(Map<String, Team> teams) {
        // Loop on teams
        for (Map.Entry<String, Team> entry : teams.entrySet()) {
            // print team name
            System.out.println("Team: "+entry.getKey());
            // print team members
            for (User singleUser : entry.getValue().getPlayers()) {
                System.out.println("\t"+singleUser.getUsername());
            }
            System.out.println();
        }
    }
}
