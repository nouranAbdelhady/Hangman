import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
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
        System.out.println("Starting to load files");
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

    public void loadScore() throws FileNotFoundException {
        FileInputStream file=new FileInputStream("./src/Files/score.txt");
        Scanner sc=new Scanner(file);    //file to be scanned
        //returns true if there is another line to read
        while(sc.hasNextLine())
        {
            String line=sc.nextLine();
            // separate username and score
            String[] parts = line.split("-");
            //this.users.get(parts[0]).setScore(Integer.parseInt(parts[1]));     // Update score
        }
        sc.close();     //closes the scanner
    }
}
