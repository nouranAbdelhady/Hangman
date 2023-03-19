public class SinglePlayer {
    private User user;
    private Lookup lookup;
    private GameConfig gameConfig;

    public SinglePlayer(User user, Lookup lookup, GameConfig gameConfig) {
        this.user = user;
        this.lookup = lookup;
        this.gameConfig = gameConfig;
    }
    public void startGame(){
        // Start game

    }
    public void endGame(){
        // End game

    }
    public void updateScore(int score){
        // Update score
        user.updateScore(score);
    }
    public void updateState(){

    }

}
