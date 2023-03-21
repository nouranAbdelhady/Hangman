public class MultiPlayer extends Game{
    Team teamA;
    Team teamB;
    public MultiPlayer(Lookup lookup, GameConfig gameConfig, Team teamA, Team teamB) {
        super(lookup, gameConfig);
        this.teamA=teamA;
        this.teamB=teamB;
    }
}
