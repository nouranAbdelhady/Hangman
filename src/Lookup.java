import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
public class Lookup {
    private ArrayList<String> phrases;

    public Lookup(ArrayList<String> phrase){
        this.phrases=phrase;
    }

    public String pickRandomPhrase(){
        int max = phrases.size()-1;
        int randomNumber = (int) ((Math.random() * (max)) + 0);
        // Pick a random phrase from the list
        String randomPhrase = phrases.get(randomNumber);
        return randomPhrase;
    }
    public String convertToDashes(String phrase){
        // split the phrase into an array of words using one or more whitespace characters
        String[] words = phrase.split("\\s+");
        // create a StringBuilder object to store the dashed line
        StringBuilder dashLine = new StringBuilder();
        // iterate over each word in the array
        for (String word : words) {
            // append a dash for each character in the word
            for (int i = 0; i < word.length(); i++) {
                dashLine.append("-");
            }
            // append a space after each word
            dashLine.append(" ");
        }
        // return the resulting dashed line as a string with leading and trailing whitespace removed
        return dashLine.toString().trim();
    }
    public String updateDashed(Character guessedChar, String originalPhrase, String currentDashed){
        // split the original phrase into an array of words using one or more whitespace characters
        String[] words = originalPhrase.split("\\s+");
        // create a StringBuilder object to store the updated dashed line
        StringBuilder updatedDashLine = new StringBuilder(currentDashed);
        // iterate over each word in the array
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            // iterate over each character in the word
            for (int j = 0; j < word.length(); j++) {
                // if the character matches the guessed character
                if (word.charAt(j) == guessedChar) {
                    // find the index of the corresponding dash in the current dashed line
                    int index = currentDashed.indexOf('-', j + i);
                    // update the corresponding character in the updated dashed line
                    updatedDashLine.setCharAt(index, guessedChar);
                }
            }
        }
        // return the updated dashed line as a string
        return updatedDashLine.toString();
    }
    public void printContent(){
        // Print the content of the file which was loaded
        for (String phrase : phrases) {
            System.out.println(phrase);
        }
    }

}
