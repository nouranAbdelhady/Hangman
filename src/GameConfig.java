import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class GameConfig {
    private int incorrectAttempts;
    private int minPlayers;
    private int maxPlayers;

    public GameConfig() throws FileNotFoundException {
        // Load game config from file
        FileInputStream file=new FileInputStream("./src/Files/configurations.txt");
        Scanner sc=new Scanner(file);    //file to be scanned

        //attemps
        String line=sc.nextLine();
        //split on '='
        String[] parts = line.split("=");
        this.incorrectAttempts= Integer.parseInt(parts[1]);

        //min players
        line=sc.nextLine();
        //split on '='
        parts = line.split("=");
        this.minPlayers= Integer.parseInt(parts[1]);

        //max players
        line=sc.nextLine();
        //split on '='
        parts = line.split("=");
        this.maxPlayers= Integer.parseInt(parts[1]);

        sc.close();     //closes the scanner
    }

    public void previewConfig() {
        System.out.println("Game Config: ");
        System.out.println("Incorrect Attempts: " + this.incorrectAttempts);
        System.out.println("Min Players: " + this.minPlayers);
        System.out.println("Max Players: " + this.maxPlayers);
    }
}
