import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class GameConfig {
    private int incorrectAttempts;
    private int minPlayers;
    private int maxPlayers;

    public GameConfig(int incorrectAttempts, int minPlayers, int maxPlayers) {
        this.incorrectAttempts = incorrectAttempts;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public void previewConfig() {
        System.out.println("Incorrect Attempts: " + this.incorrectAttempts);
        System.out.println("Min Players: " + this.minPlayers);
        System.out.println("Max Players: " + this.maxPlayers);
    }
}
