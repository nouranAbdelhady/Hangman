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

    public void updateScore(int score){
        this.score+=score;
    }
}
