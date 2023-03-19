import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Credentials {
    // hashmap of usernames and passwords
    private Map<String, User> user_map = new HashMap<String, User>();

    public Credentials(Map<String, User> users)  {
        this.user_map = users;
    }

    public void printContent(){
        // print the contents of the hashmap
        for (Map.Entry<String, User> entry : user_map.entrySet()) {
            // print username and password
            System.out.println(entry.getKey() + " -> " + entry.getValue().getPassword()+" -> " + entry.getValue().getScore());
        }
    }

    public Map<String, User> getUserMap(){
        return user_map;
    }

    public int checkCredentials(String username, String password){
        // check if username and password are in the hashmap
        if (user_map.containsKey(username)){
            if (user_map.get(username).getPassword().equals(password)){
                // password correct
                // update user status
                user_map.get(username).setOnline(true);
                return 200;     // 200 = ok
            }else{
                // password incorrect
                return 401;       // 401 = unauthorized
            }
        }else{
            // username not found
            return 404;     // 404 = not found
        }
    }

    public void logout(String username){
        // update user status
        user_map.get(username).setOnline(false);
    }

    public boolean isUniqueUsername(String username){
        // check if username is unique
        return !user_map.containsKey(username);     // return true if username is *NOT* in hashmap
    }

    public void addUser(String name, String username, String password) throws IOException {
        // add new user to hashmap
        user_map.put(username, new User(name, username, password));
        // add to file (login and score)
        FileWriter myWriterLogin = new FileWriter("./src/Files/login.txt", true);
        myWriterLogin.write(name + "-" + username + "-" + password+"\n");
        myWriterLogin.close();
        FileWriter myWriterScore = new FileWriter("./src/Files/score.txt", true);
        myWriterScore.write(username + "-" + 0+"\n");
        myWriterScore.close();
    }
}
