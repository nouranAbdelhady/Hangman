import java.util.ArrayList;

public abstract class Game {
    private Lookup lookup;
    private GameConfig gameConfig;
    private String originalPhrase;
    private String currentDashed;
    private ArrayList<Character> guesses;
    private Boolean isGameOver = false;

    public Game(Lookup lookup, GameConfig gameConfig) {
        this.lookup = lookup;
        this.gameConfig = gameConfig;

        // Prepare the game
        // convert to lowercase for comparison
        this.originalPhrase = pickRandomPhrase().toLowerCase();
        this.currentDashed = convertToDashes(this.originalPhrase);
        this.guesses = new ArrayList<>();
    }

    public ArrayList<Character> getGuesses() {
        return guesses;
    }
    public void addGuess(char newGuess) {
        this.guesses.add(newGuess);
    }
    public boolean hasBeenGuessed(Character newGuess) {
        return this.guesses.contains(newGuess);
    }

    public Lookup getLookup() {
        return lookup;
    }
    public void setLookup(Lookup lookup) {
        this.lookup = lookup;
    }
    public GameConfig getGameConfig() {
        return gameConfig;
    }
    public void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }
    public String getOriginalPhrase() {
        return originalPhrase;
    }
    public void setOriginalPhrase(String originalPhrase) {
        this.originalPhrase = originalPhrase;
    }
    public String getCurrentDashed() {
        return currentDashed;
    }
    public void setCurrentDashed(String currentDashed) {
        this.currentDashed = currentDashed;
    }

    public String pickRandomPhrase(){
        int max = lookup.phrases.size()-1;
        int randomNumber = (int) ((Math.random() * (max)) + 0);

        // Pick a random phrase from the list
        return lookup.phrases.get(randomNumber);
    }
    public String convertToDashes(String phrase){
        // split the phrase into an array of words using one or more whitespace characters
        String[] words = phrase.split("\\s+");

        // create a StringBuilder object to store the dashed line
        StringBuilder dashLine = new StringBuilder();

        // iterate over each word in the array
        for (String word : words)
        {
            // append a dash for each character in the word
            for (int i = 0; i < word.length(); i++)
            {
                dashLine.append("-");
            }
            // append a space after each word
            dashLine.append(" ");
        }
        // return the resulting dashed line as a string with leading and trailing whitespace removed
        return dashLine.toString().trim();
    }
    public void updateDashed(Character guessedChar){
        // loop on the original phrase
        for (int i = 0; i < this.originalPhrase.length(); i++)
        {
            // check if the character at the current index is equal to the guessed character
            if (this.originalPhrase.charAt(i) == Character.toLowerCase(guessedChar))
            {
                // if so, replace the dash at the same index in the dashed line with the guessed character
                this.currentDashed = this.currentDashed.substring(0, i) + guessedChar + this.currentDashed.substring(i + 1);
            }
        }
    }
    public boolean isGameOver(){
        // Check if the game is over
        return this.currentDashed.equals(this.originalPhrase);
    }
    public int getNumberOfDashed(){
        // this will be used to update score
        int count = 0;
        for (int i = 0; i < this.currentDashed.length(); i++)
        {
            if (this.currentDashed.charAt(i) == '-')
            {
                count++;
            }
        }
        return count;
    }
    public boolean didWin(){
        // Check if the player won
        return this.currentDashed.equals(this.originalPhrase);
    }

    public Boolean getGameOver() {
        return isGameOver;
    }

    public void setGameOver(Boolean gameOver) {
        isGameOver = gameOver;
    }

    public boolean isCorrectGuess(Character guessedChar){
        // Check if the guessed character is in the original phrase
        // Handle if upper or lower case
        return this.originalPhrase.indexOf(guessedChar) != -1;
    }
}