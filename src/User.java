import java.io.*;
import java.util.Scanner;

public class User {
    private String name;
    private String username;
    private String password;
    private int score;
    private boolean isOnline;
    private int teamId;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.score = 0;
        this.isOnline = false;
        this.teamId = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public void updateScore(int newScore) throws IOException {
        String usernameToUpdate = this.username; // The username whose score needs to be updated
        this.score+=newScore; // The new score to be updated

        // Create a temporary file
        File inputFile = new File("./src/Files/score.txt");
        File tempFile = new File("./src/Files/score_temp.txt");

        // Remove old score and write updated scores to temporary file
        Scanner sc = new Scanner(inputFile);
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("-");
            String username = parts[0];
            if (username.equals(usernameToUpdate)) {
                // Do not write the old score to the temporary file
                continue;
            }
            writer.println(line);       // Write the old score to the temporary file
        }
        writer.println(usernameToUpdate + "-" + this.score); // Write the new score to the temporary file

        // Close the input and output streams
        writer.flush();
        writer.close();
        sc.close();

        // Replace the original file with the temporary file
        inputFile.delete();
        tempFile.renameTo(inputFile);
    }
}
