import java.io.IOException;
import java.util.ArrayList;

public class Team {
    private int id;
    private String name;
    private ArrayList<User> players;
    private int score;

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();
        this.score = 0;
    }
    private void addPlayer(User player){
        players.add(player);
    }
    private void removePlayer(User player){
        players.remove(player);
    }
    private void setTeamName(String name){
        this.name=name;
    }
    private String getTeamName(){
        return name;
    }
    private int getTeamId(){
        return id;
    }
    private void setTeamId(int id){
        this.id=id;
    }
    private int getTeamScore(){
        return score;
    }
    private void setTeamScore(int score){
        this.score=score;
    }
    private void updateTeamScore(int score){
        this.score+=score;
    }

    private void updateScoreForAllPlayers() throws IOException {
        for (User player : players) {
            player.updateScore(this.score);
        }
    }
}
