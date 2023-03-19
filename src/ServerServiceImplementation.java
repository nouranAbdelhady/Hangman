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
    public Files loadFiles() {
        System.out.println("Starting to load files");
        // Logic
        Files readFiles=null;



        System.out.println("Files loaded");
        return readFiles;
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
