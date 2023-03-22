import java.io.IOException;
import java.util.ArrayList;

public class Team {
    private String name;
    private ArrayList<User> players;
    private int score;
    private Boolean canStartGame;
    private Boolean isGameOver;
    private Team opponentTeam;
    private int numberOfAttemptsLeft;
    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        this.score = 0;
        this.canStartGame = false;
        this.opponentTeam = null;
        this.isGameOver = false;
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
    public Boolean getCanStartGame() {
        return canStartGame;
    }

    public void setCanStartGame(Boolean canStartGame) {
        this.canStartGame = canStartGame;
    }

    public Team getOpponentTeam() {
        return opponentTeam;
    }

    public void setOpponentTeam(Team opponentName) {
        this.opponentTeam = opponentName;
    }

    public void updateScoreForAllPlayers() throws IOException {
        for (User player : players) {
            player.updateScore(this.score);
        }
    }

    public int getNumberOfAttemptsLeft() {
        return numberOfAttemptsLeft;
    }

    public void setNumberOfAttemptsLeft(int numberOfAttemptsLeft) {
        this.numberOfAttemptsLeft = numberOfAttemptsLeft;
    }
    public void updateNumberOfAttemptsLeft() {
        this.numberOfAttemptsLeft--;
    }

    public Boolean getGameOver() {
        return isGameOver;
    }

    public void setGameOver(Boolean gameOver) {
        isGameOver = gameOver;
    }
    public String previewTeam(){
        String teamPreview = "";
        for (User player : players) {
            teamPreview += ("\t"+player.getName()+"\n");
        }
        return teamPreview;
    }
}
