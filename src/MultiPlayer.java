public class MultiPlayer extends Game{
    Team teamA;
    Team teamB;
    public MultiPlayer(Lookup lookup, GameConfig gameConfig, Team teamA, Team teamB) {
        super(lookup, gameConfig);
        this.teamA=teamA;
        this.teamB=teamB;
    }

    public MultiPlayer(Lookup lookup, GameConfig gameConfig) {
        super(lookup, gameConfig);
        this.teamA=null;
        this.teamB=null;
    }

    public Team getTeamA() {
        return teamA;
    }

    public void setTeamA(Team teamA) {
        this.teamA = teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public void setTeamB(Team teamB) {
        this.teamB = teamB;
    }
    public void initialAttempts(int attempts){
        this.teamA.setNumberOfAttemptsLeft(attempts);
        this.teamB.setNumberOfAttemptsLeft(attempts);
    }
}
