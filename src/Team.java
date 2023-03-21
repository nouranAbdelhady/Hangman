import java.io.IOException;
import java.util.ArrayList;

public class Team {
    private String name;
    private ArrayList<User> players;
    private int score;
    // flag to track if the first player is the team leader
    private boolean isLeader;
    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        this.score = 0;
        this.isLeader=true;
    }
    public void addPlayer(User player){
        players.add(player);
    }
    public ArrayList<User> getPlayers(){
        return players;
    }
    public void removePlayer(User player){
        players.remove(player);
    }
    public void setTeamName(String name){
        this.name=name;
    }
    String getTeamName(){
        return name;
    }
    public int getTeamScore(){
        return score;
    }
    public void setTeamScore(int score){
        this.score=score;
    }
    public void updateTeamScore(int score){
        this.score+=score;
    }
    public boolean isLeader() {
        return isLeader;
    }
    public void setIsLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public void updateScoreForAllPlayers() throws IOException {
        for (User player : players) {
            player.updateScore(this.score);
        }
    }
}
