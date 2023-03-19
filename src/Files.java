public class Files {
    Lookup lookupFile;      // Phrases
    GameConfig gameConfigFile;
    User allUsers;      // credentials + score

    public Lookup getLookupFile() {
        return lookupFile;
    }

    public void setLookupFile(Lookup lookupFile) {
        this.lookupFile = lookupFile;
    }

    public GameConfig getGameConfigFile() {
        return gameConfigFile;
    }

    public void setGameConfigFile(GameConfig gameConfigFile) {
        this.gameConfigFile = gameConfigFile;
    }

    public User getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(User allUsers) {
        this.allUsers = allUsers;
    }
}
