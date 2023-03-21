import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

/**
 * This class is used to handle multiple client connections, and
 * opens a new thread for EACH socket connection
 */
public class MultiThreadedServer extends Server implements Runnable{

    private final Socket currentSocket;    // Socket of client

    public MultiThreadedServer(Socket socket) throws IOException {
        this.currentSocket = socket;
    }

    @Override
    public void run() {
        try {
            String welcome="Welcome to Hangman! \n";
            serverService.sendMessageToClient(currentSocket, welcome);
            // When first connected, menu is previewed to client
            // Handle client messages
            while (true){
                String menu = "1- Register \n" +
                        "2- Login";
                serverService.sendMessageToClient(currentSocket, menu);
                String option = serverService.getClientMessage(currentSocket);     // Read option sent from client
                if (option.equals("-")) {     //'-' means that client chose to disconnect
                    throw new SocketException();        // Throw exception to handle client disconnection
                }
                switch (option){
                    case "1":
                        // Register
                        String registerMenu = "Registration: \n" +
                                "Enter your name: ";
                        serverService.sendMessageToClient(currentSocket, registerMenu);
                        String name = serverService.getClientMessage(currentSocket);
                        serverService.sendMessageToClient(currentSocket, "Enter username");
                        String username = serverService.getClientMessage(currentSocket);
                        while (!serverService.isUniqueUsername(username)) {       // username must be unique
                            serverService.sendMessageToClient(currentSocket, "Username already taken. Please try again.");
                            username = serverService.getClientMessage(currentSocket);
                        }
                        serverService.sendMessageToClient(currentSocket, "Enter password");
                        String password = serverService.getClientMessage(currentSocket);
                        serverService.addUser(name,username, password);
                        serverService.sendMessageToClient(currentSocket, "Registration successful!");
                        break;
                    case "2":
                        // Login
                        String loginMenu = "Login: \n" +
                                "Enter your username: ";
                        serverService.sendMessageToClient(currentSocket, loginMenu);
                        String usernameLogin = serverService.getClientMessage(currentSocket);
                        serverService.sendMessageToClient(currentSocket, "Enter your password: ");
                        String passwordLogin = serverService.getClientMessage(currentSocket);
                        int loginResponse=serverService.checkCredentials(usernameLogin, passwordLogin);
                        if (loginResponse==200) {
                            serverService.sendMessageToClient(currentSocket, "Login successful!");
                            String printMessage = usernameLogin + " has connected to server";
                            System.out.println(printMessage);       // Print message to server
                            super.getClientNameList().put(currentSocket, usernameLogin);

                            while (true){
                                // Start game
                                String gameMenu = "\nMain Menu \n" +
                                        "1- Single Player \n" +
                                        "2- Multiplayer \n";
                                serverService.sendMessageToClient(currentSocket, gameMenu);
                                String gameOption = serverService.getClientMessage(currentSocket);

                                if (gameOption.equals("-")) {     //'-' means that client chose to disconnect
                                    throw new SocketException();        // Throw exception to handle client disconnection
                                }
                                // Get current user
                                User currentUser = super.getUsers().get(usernameLogin);
                                switch (gameOption){
                                    case "1":
                                        // Single Player
                                        serverService.sendMessageToClient(currentSocket, "Single Player");

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
                                        while(!game.isGameOver() && leftAttempts>0)
                                        {
                                            // Preview number of attempts left
                                            serverService.sendMessageToClient(currentSocket, "Number of attempts left: "+leftAttempts);
                                            // Preview dashed word to client
                                            serverService.sendMessageToClient(currentSocket, game.getCurrentDashed());
                                            // Get guess from client
                                            serverService.sendMessageToClient(currentSocket, "Enter a character: ");
                                            String guess = serverService.getClientMessage(currentSocket);

                                            // If more than 1 character is entered, preview error message
                                            while (guess.length()>1)
                                            {
                                                serverService.sendMessageToClient(currentSocket, "Please enter only 1 character");
                                                guess = serverService.getClientMessage(currentSocket);
                                            }

                                            // single character: user guess input to compare
                                            // with the characters in the array guesses (in Game class).
                                            // converted to lower to handle case sensitivity
                                            Character guessChar = Character.toLowerCase(guess.charAt(0));

                                            if (game.hasBeenGuessed(guessChar))
                                            {
                                                // If the guess has already been made
                                                serverService.sendMessageToClient(currentSocket, "\nYou already guessed " + guessChar + "! Please enter another character.\n");
                                            }
                                            else
                                            {
                                                // Unique guess
                                                // Add guess to array
                                                game.addGuess(guessChar);
                                                if (game.isCorrectGuess(guessChar))
                                                {
                                                    // If guess is correct
                                                    // update dashed word
                                                    game.updateDashed(guessChar);
                                                    // send to client
                                                    serverService.sendMessageToClient(currentSocket, "Correct guess!");
                                                }
                                                else
                                                {
                                                    // update left attempts
                                                    // note that If the user enters a wrong character that has
                                                    // already been guessed before, we don't decrement the number
                                                    // of remaining attempts again.
                                                    leftAttempts--;
                                                    // send to client
                                                    serverService.sendMessageToClient(currentSocket, "Wrong guess!");
                                                }
                                            }
                                        }

                                        // Game is over
                                        if (game.didWin())
                                        {
                                            serverService.sendMessageToClient(currentSocket, "You won!");
                                        }
                                        else
                                        {
                                            serverService.sendMessageToClient(currentSocket, "You lost!");
                                            serverService.sendMessageToClient(currentSocket, "The phrase was: "+game.getOriginalPhrase());
                                            // Update score (number of characters guessed correctly)
                                            scoreToUpdate-=game.getNumberOfDashed();
                                        }

                                        // Update score
                                        currentUser.updateScore(scoreToUpdate);
                                        serverService.sendMessageToClient(currentSocket, "Your new score is: "+currentUser.getScore());
                                        break;

                                    case "2":
                                        // Multiplayer
                                        serverService.sendMessageToClient(currentSocket, "Multiplayer");
                                        String multiplayerMenu = "1- Create new team \n" +
                                                "2- Join existing team \n" +
                                                "3- Join game room \n" +
                                                "4- Back";
                                        while(true){
                                            serverService.sendMessageToClient(currentSocket, multiplayerMenu);
                                            String multiplayerOption = serverService.getClientMessage(currentSocket);
                                            if(multiplayerOption.equals("-")){
                                                throw new SocketException();
                                            }
                                            if(multiplayerOption.equals("4")){
                                                break;
                                            }

                                            // Handle menu
                                            switch (multiplayerOption){
                                                case "1":
                                                    // Create new team
                                                    serverService.sendMessageToClient(currentSocket, "Enter team name: ");
                                                    String teamName = serverService.getClientMessage(currentSocket);

                                                    // team name must be unique
                                                    while(super.getTeams().containsKey(teamName))
                                                    {
                                                        serverService.sendMessageToClient(currentSocket, "Team name is taken, please enter another name: ");
                                                        teamName = serverService.getClientMessage(currentSocket);
                                                    }

                                                    // Create new team
                                                    Team newTeam = new Team(teamName);

                                                    // Add user to team
                                                    newTeam.addPlayer(currentUser);

                                                    // Add team to teams list
                                                    super.getTeams().put(teamName, newTeam);

                                                    // Send message to client that team was created, and
                                                    // he is waiting for others to join by sending them the team name
                                                    serverService.sendMessageToClient(currentSocket, "Team created! Waiting for others to join...");

                                                    // If user sent 'P' to start the game
                                                    while (true)
                                                    {
                                                        String message = serverService.getClientMessage(currentSocket);
                                                        if(message.equals("P") || message.equals("p"))
                                                        {
                                                            // check if teams have equal number of players before
                                                            // starting the game
                                                            boolean teamsAreEqual= true;
                                                            int numOfPlayersInTeam=newTeam.getPlayers().size();

                                                            // iterate through teams and check if number of players in
                                                            // each team is equal to numPlayersInTeam
                                                            for(Team team: super.getTeams().values())
                                                            {
                                                                if(team.getPlayers().size() != numOfPlayersInTeam)
                                                                {
                                                                    teamsAreEqual= false;
                                                                    break;
                                                                }
                                                            }

                                                            if(!teamsAreEqual)
                                                            {
                                                                serverService.sendMessageToClient(currentSocket, "Error: Teams have different number of players. Please wait for all teams to have equal number of players.");
                                                                continue;
                                                            }

                                                            // Start game
                                                            serverService.sendMessageToClient(currentSocket, "You pressed 'P'! GAME IS STARTING");
                                                            break;
                                                        }
                                                    }
                                                    // Validation for game before starting

                                                    break;
                                                case "2":
                                                    // Join existing team
                                                    serverService.sendMessageToClient(currentSocket, "Enter team name: ");
                                                    String teamNameToJoin = serverService.getClientMessage(currentSocket);

                                                    while (true)
                                                    {
                                                        // Check if team exists
                                                        // If team doesn't exist, send message to client
                                                        if (!super.getTeams().containsKey(teamNameToJoin))
                                                        {
                                                            serverService.sendMessageToClient(currentSocket, "Team doesn't exist, please enter another name: ");
                                                            teamNameToJoin = serverService.getClientMessage(currentSocket);
                                                            continue;
                                                        }

                                                        // Check Min team players
                                                        if (super.getTeams().get(teamNameToJoin).getPlayers().size() < getGameConfig().getMinPlayers())
                                                        {
                                                            serverService.sendMessageToClient(currentSocket, "Team must have at least two players: ");
                                                            teamNameToJoin = serverService.getClientMessage(currentSocket);
                                                            continue;
                                                        }

                                                        // Check Max team players
                                                        if (super.getTeams().get(teamNameToJoin).getPlayers().size() >= getGameConfig().getMaxPlayers())
                                                        {
                                                            serverService.sendMessageToClient(currentSocket, "Team members limit exceeded, please join another team: ");
                                                            teamNameToJoin = serverService.getClientMessage(currentSocket);
                                                            continue;
                                                        }

                                                        break;
                                                    }
                                                    // If team exists, add user to team
                                                    // valid team names

                                                    // TODO: Check if team config is valid
                                                    // If team config is valid, add user to team
                                                    // If team config is invalid, send message to client

                                                    // Send message to all team members that a new player joined
                                                    for(User player : super.getTeams().get(teamNameToJoin).getPlayers())
                                                    {
                                                        for (Map.Entry<Socket, String> entry : super.getClientNameList().entrySet())
                                                        {
                                                            if (entry.getValue().equals(player.getUsername()))
                                                            {
                                                                Socket targetedUser = entry.getKey();
                                                                serverService.sendMessageToClient(targetedUser, currentUser.getUsername()+" joined the team!");
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    // Add user to team
                                                    super.getTeams().get(teamNameToJoin).addPlayer(currentUser);
                                                    // Send message to client that he joined the team
                                                    serverService.sendMessageToClient(currentSocket, "You joined the team!");
                                                    // Wait for team leader to start the game
                                                    serverService.sendMessageToClient(currentSocket,"Waiting for team leader to start the game...");
                                                    // Check game state


                                                    break;
                                                case "3":
                                                    // Join game room
                                                    serverService.sendMessageToClient(currentSocket, "Game Room");
                                                    break;
                                                default:
                                                    serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                                                    break;
                                            }
                                            if(multiplayerOption.equals("1") || multiplayerOption.equals("2") || multiplayerOption.equals("3")){
                                                // Game for multiplayer finished
                                                break;  // Go back to main menu
                                            }
                                        }
                                        break;
                                    default:
                                        serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                                        break;
                                }
                            }
                        } else {
                            serverService.sendMessageToClient(currentSocket, loginResponse+" - Login Failed!");
                        }
                        break;
                    default:
                        serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                        break;
                }
            }
        } catch (SocketException e) {
            String printMessage = super.getClientNameList().get(currentSocket) + " got disconnected";
            System.out.println(printMessage);       // Print message to server

            // Change state of user to offline
            for(User user : super.getUsers().values()){
                if(user.getUsername().equals(super.getClientNameList().get(currentSocket))){
                    user.setOnline(false);
                }
            }

            // Remove user from team (if he is in one)
            for(Team team : super.getTeams().values()){
                for(User player : team.getPlayers()){
                    if(player.getUsername().equals(super.getClientNameList().get(currentSocket))){
                        team.removePlayer(player);

                        // Send message to all team members that a player left
                        for(User playerr : team.getPlayers()){
                            for (Map.Entry<Socket, String> entry : super.getClientNameList().entrySet()) {
                                if (entry.getValue().equals(playerr.getUsername())) {
                                    Socket targetedUser = entry.getKey();
                                    try {
                                        serverService.sendMessageToClient(targetedUser, player.getUsername()+" left the team!");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    break;
                                }
                            }

                        }
                    }
                }
            }

            // Remove user from clients list
            super.getClients().remove(currentSocket);
            super.getClientNameList().remove(currentSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}