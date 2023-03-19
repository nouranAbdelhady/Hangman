import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
public class Lookup {
    public ArrayList<String> phrases;

    public Lookup(ArrayList<String> phrase){
        this.phrases=phrase;
    }

    public void printContent(){
        // Print the content of the file which was loaded
        for (String phrase : phrases) {
            System.out.println(phrase);
        }
    }

}
