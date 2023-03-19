public class SinglePlayer extends Game{
    private User user;
    private int countOfIncorrectGuesses;

    public SinglePlayer(Lookup lookup, GameConfig gameConfig, User user) {
        super(lookup, gameConfig);
        this.user = user;
        this.countOfIncorrectGuesses = 0;

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getCountOfIncorrectGuesses() {
        return countOfIncorrectGuesses;
    }

    public void setCountOfIncorrectGuesses(int countOfIncorrectGuesses) {
        this.countOfIncorrectGuesses = countOfIncorrectGuesses;
    }

}
