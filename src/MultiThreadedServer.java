import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class is used to handle multiple client connections, and
 * opens a new thread for EACH socket connection
 */
public class MultiThreadedServer extends Server implements Runnable{

    private Socket currentSocket;    // Socket of client

    public MultiThreadedServer(Socket socket) throws IOException {
        this.currentSocket = socket;
    }

    @Override
    public void run() {
        try {
            String welcome="Welcome to Hangman! \n";
            super.serverService.sendMessageToClient(currentSocket, welcome);
            // When first connected, menu is previewed to client
            // Handle client messages
            while (true){
                String menu = "1- Register \n" +
                        "2- Login";
                super.serverService.sendMessageToClient(currentSocket, menu);
                String option = super.serverService.getClientMessage(currentSocket);     // Read option sent from client
                if (option.equals("-")) {     //'-' means that client chose to disconnect
                    throw new SocketException();        // Throw exception to handle client disconnection
                }
                switch (option){
                    case "1":
                        // Register
                        String registerMenu = "Registration: \n" +
                                "Enter your name: ";
                        super.serverService.sendMessageToClient(currentSocket, registerMenu);
                        String name = super.serverService.getClientMessage(currentSocket);
                        super.serverService.sendMessageToClient(currentSocket, "Enter username");
                        String username = super.serverService.getClientMessage(currentSocket);
                        while (!super.serverService.isUniqueUsername(username)) {       // username must be unique
                            super.serverService.sendMessageToClient(currentSocket, "Username already taken. Please try again.");
                            username = super.serverService.getClientMessage(currentSocket);
                        }
                        super.serverService.sendMessageToClient(currentSocket, "Enter password");
                        String password = super.serverService.getClientMessage(currentSocket);
                        super.serverService.addUser(name,username, password);
                        super.serverService.sendMessageToClient(currentSocket, "Registration successful!");
                        break;
                    case "2":
                        // Login
                        String loginMenu = "Login: \n" +
                                "Enter your username: ";
                        super.serverService.sendMessageToClient(currentSocket, loginMenu);
                        String usernameLogin = super.serverService.getClientMessage(currentSocket);
                        super.serverService.sendMessageToClient(currentSocket, "Enter your password: ");
                        String passwordLogin = super.serverService.getClientMessage(currentSocket);
                        int loginResponse=super.serverService.checkCredentials(usernameLogin, passwordLogin);
                        if (loginResponse==200) {
                            super.serverService.sendMessageToClient(currentSocket, "Login successful!");
                            String printMessage = usernameLogin + " has connected to server";
                            System.out.println(printMessage);       // Print message to server
                            super.getClientNameList().put(currentSocket, usernameLogin);

                            while (true){
                                // Start game
                                String gameMenu = "\n1- Single Player \n" +
                                        "2- Multiplayer";
                                super.serverService.sendMessageToClient(currentSocket, gameMenu);
                                String gameOption = super.serverService.getClientMessage(currentSocket);

                                if (gameOption.equals("-")) {     //'-' means that client chose to disconnect
                                    throw new SocketException();        // Throw exception to handle client disconnection
                                }
                                switch (gameOption){
                                    case "1":
                                        // Single Player
                                        super.serverService.sendMessageToClient(currentSocket, "Single Player");
                                        // Get current user
                                        User currentUser = super.getUsers().get(usernameLogin);

                                        //super.getLookup(),super.getGameConfig() are from Server class
                                        SinglePlayer game = new SinglePlayer(super.getLookup(),super.getGameConfig(), currentUser);
                                        // Print at server
                                        System.out.println("Phrase to guess: "+game.getOriginalPhrase());

                                        // Random phrase is selected from lookup
                                        // This is done inside the constructor of Game

                                        // Score = number of characters in phrase
                                        int scoreToUpdate=game.getOriginalPhrase().length();
                                        // Print at server
                                        System.out.println("Score: "+scoreToUpdate);

                                        int leftAttempts = game.getGameConfig().getIncorrectAttempts();
                                        // Initialize an array to keep track of guesses
                                        char[] guesses = new char[50];
                                        int numUniqueGuesses=0;

                                        while(!game.isGameOver() && leftAttempts>0)
                                        {
                                            // Preview number of attempts left
                                            super.serverService.sendMessageToClient(currentSocket, "Number of attempts left: "+leftAttempts);

                                            // Preview dashed word to client
                                            super.serverService.sendMessageToClient(currentSocket, game.getCurrentDashed());

                                            // Get guess from client
                                            super.serverService.sendMessageToClient(currentSocket, "Enter a character: ");
                                            String guess = super.serverService.getClientMessage(currentSocket);

                                            // If more than 1 character is entered, preview error message
                                            while (guess.length()>1)
                                            {
                                                super.serverService.sendMessageToClient(currentSocket, "Please enter only 1 character");
                                                guess = super.serverService.getClientMessage(currentSocket);
                                            }
                                            // Print at server
                                            System.out.println("User guessed: "+guess);

                                            // single character: user guess input to compare
                                            // with the characters in the array guesses.
                                            // char guessChar=guess.charAt(0);
                                            char guessChar = Character.toLowerCase(guess.charAt(0));

                                            boolean isDuplicateGuess = false;
                                            for (int i = 0; i < numUniqueGuesses; i++)
                                            {
                                                // check if the user guess is a duplicate guess
                                                if (guesses[i] == guessChar)
                                                {
                                                    isDuplicateGuess = true;
                                                    break;
                                                }
                                            }

                                            if (isDuplicateGuess)
                                            {
                                                // If the guess has already been made
                                                super.serverService.sendMessageToClient(currentSocket, "\nYou already guessed " + guessChar + "! Please enter another character.\n");
                                            }
                                            else
                                            {
                                                // Add guess to array
                                                guesses[numUniqueGuesses] = guessChar;
                                                numUniqueGuesses++;
                                                if (game.isCorrectGuess(guessChar))
                                                {
                                                    // If guess is correct
                                                    game.updateDashed(guessChar);
                                                }
                                                else
                                                {
                                                    // update left attempts
                                                    // note that If the user enters a wrong character that has
                                                    // already been guessed before, we don't decrement the number
                                                    // of remaining attempts again.
                                                    leftAttempts--;
                                                }
                                            }
                                        }

                                        // Game is over
                                        if (game.didWin())
                                        {
                                            super.serverService.sendMessageToClient(currentSocket, "You won!");
                                        }
                                        else
                                        {
                                            super.serverService.sendMessageToClient(currentSocket, "You lost!");
                                            super.serverService.sendMessageToClient(currentSocket, "The phrase was: "+game.getOriginalPhrase());
                                            // Update score (number of characters guessed correctly)
                                            scoreToUpdate-=game.getNumberOfDashed();
                                        }

                                        // Update score
                                        currentUser.updateScore(scoreToUpdate);
                                        super.serverService.sendMessageToClient(currentSocket, "Your new score is: "+currentUser.getScore());
                                        // Update in file
                                        // TODO: Update in file (function in serverService)
                                        break;

                                    case "2":
                                        // Multiplayer
                                        super.serverService.sendMessageToClient(currentSocket, "Multiplayer");
                                        break;

                                    default:
                                        super.serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                                        break;
                                }
                            }
                        } else {
                            super.serverService.sendMessageToClient(currentSocket, loginResponse+" - Login Failed!");
                        }
                        break;
                    default:
                        super.serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                        break;
                }
            }
        } catch (SocketException e) {
            String printMessage = super.getClientNameList().get(currentSocket) + " got disconnected";
            System.out.println(printMessage);       // Print message to server

            super.getClients().remove(currentSocket);
            super.getClientNameList().remove(currentSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}