import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

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
                                currentUser.setOnline(true);
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

                                            while (game.hasBeenGuessed(guessChar))
                                            {
                                                // If the guess has already been made
                                                serverService.sendMessageToClient(currentSocket, "\nYou already guessed " + guessChar + "! Please enter another character.\n");
                                                guess = serverService.getClientMessage(currentSocket);
                                                while (guess.length()>1)
                                                {
                                                    serverService.sendMessageToClient(currentSocket, "Please enter only 1 character");
                                                    guess = serverService.getClientMessage(currentSocket);
                                                }
                                                guessChar = Character.toLowerCase(guess.charAt(0));
                                            }
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
                                                break;      // Go back to main menu
                                            }

                                            MultiPlayer multiPlayer = new MultiPlayer(super.getLookup(), super.getGameConfig());

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
                                                    newTeam.addPlayer(currentUser); // First player to join is the creator
                                                    currentUser.setCurrentTeam(newTeam);
                                                    // Add team to teams list
                                                    super.getTeams().put(teamName, newTeam);

                                                    // Send message to client that team was created, and
                                                    // he is waiting for others to join by sending them the team name
                                                    serverService.sendMessageToClient(currentSocket, "Team created!");

                                                    while (!currentUser.getCurrentTeam().getCanStartGame())
                                                    {
                                                        String message = serverService.getClientMessage(currentSocket);
                                                        if(message.equals("P") || message.equals("p"))
                                                        {
                                                            // Check team validation
                                                            // Check if team has at least 2 players
                                                            if(newTeam.getPlayers().size() < super.getGameConfig().getMinPlayers())
                                                            {
                                                                serverService.sendMessageToClient(currentSocket, "You must have at least "+super.getGameConfig().getMinPlayers() +" players to start the game");
                                                                continue;
                                                            }

                                                            // Check if already assigned to opponent team
                                                            if(newTeam.getOpponentTeam()!=null)
                                                            {
                                                                // Validate equal number of players
                                                                Team opponentTeam = newTeam.getOpponentTeam();
                                                                // Validate equal number of players
                                                                // Must have equal number of players
                                                                if (opponentTeam.getPlayers().size() != newTeam.getPlayers().size())
                                                                {
                                                                    // Send to the less populated team
                                                                    int difference = opponentTeam.getPlayers().size() - newTeam.getPlayers().size();
                                                                    if (difference > 0)
                                                                    {
                                                                        serverService.sendMessageToAllTeamMembers(newTeam, "You are down "+difference+" player(s)", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(opponentTeam, "Waiting for other team...", super.getClientNameList());
                                                                        opponentTeam.setCanStartGame(true);
                                                                    } else
                                                                    {
                                                                        serverService.sendMessageToAllTeamMembers(opponentTeam, "You are down "+(-1*difference)+" player(s)", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(newTeam, "Waiting for other team....", super.getClientNameList());
                                                                        newTeam.setCanStartGame(true);
                                                                    }
                                                                    continue;
                                                                }
                                                                // Validation complete
                                                                // Set can start game to true
                                                                newTeam.setCanStartGame(true);
                                                                opponentTeam.setCanStartGame(true);

                                                                // Start game
                                                                Team teamA = currentUser.getCurrentTeam();
                                                                Team teamB = teamA.getOpponentTeam();
                                                                if(opponentTeam.getCanStartGame() && teamA.getCanStartGame()) {     // Start Game
                                                                    multiPlayer.setTeamA(teamA);
                                                                    multiPlayer.setTeamB(teamB);
                                                                    multiPlayer.initialAttempts(getGameConfig().getIncorrectAttempts());
                                                                    serverService.sendMessageToAllTeamMembers(teamA, "\nStarting Game!\nOpponent Team: " + teamB.getTeamName(), super.getClientNameList());
                                                                    serverService.sendMessageToAllTeamMembers(teamB, "\nStarting Game!\nOpponent Team: " + teamA.getTeamName(), super.getClientNameList());
                                                                    Boolean teamATurn = true;

                                                                    ArrayList<User> teamAUsers = teamA.getPlayers();
                                                                    ArrayList<User> teamBUsers = teamB.getPlayers();
                                                                    int index=0;

                                                                    System.out.println("Word: "+multiPlayer.getOriginalPhrase());   // DEBUG
                                                                    while (true)
                                                                    {
                                                                        int AAttempts = teamA.getNumberOfAttemptsLeft();
                                                                        int BAttempts = teamB.getNumberOfAttemptsLeft();

                                                                        if(AAttempts==0 || BAttempts==0){
                                                                            // Game over
                                                                            multiPlayer.setGameOver(true);
                                                                            break;
                                                                        }
                                                                        if(multiPlayer.getNumberOfDashed()==0){
                                                                            // Game over
                                                                            multiPlayer.setGameOver(true);
                                                                            break;
                                                                        }

                                                                        // Preview for all teams number of attempts left
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "Number of attempts left: "+AAttempts, super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "Number of attempts left: "+BAttempts, super.getClientNameList());

                                                                        // Preview dashed word to client
                                                                        String dashedWord = multiPlayer.getCurrentDashed();
                                                                        serverService.sendMessageToAllTeamMembers(teamA, dashedWord, super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, dashedWord, super.getClientNameList());

                                                                        // Switch turns
                                                                        User targetedUser;
                                                                        if (teamATurn){
                                                                            targetedUser= teamAUsers.get(index);
                                                                            teamATurn= false;
                                                                        }else{
                                                                            targetedUser = teamBUsers.get(index);
                                                                            teamATurn= true;
                                                                            index++;
                                                                            if(index==teamAUsers.size()){
                                                                                index=0;        // Reset index
                                                                            }
                                                                        }
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "\n"+targetedUser.getUsername()+"'s turn to guess!\n", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "\n"+targetedUser.getUsername()+"'s turn to guess!\n", super.getClientNameList());

                                                                        // Get targeted user socket
                                                                        Socket targetedSocket= null;
                                                                        for (Map.Entry<Socket, String> entry : super.getClientNameList().entrySet()) {
                                                                            if (entry.getValue().equals(targetedUser.getUsername())) {
                                                                                targetedSocket = entry.getKey();
                                                                            }
                                                                        }
                                                                        // Get guess from client
                                                                        serverService.sendMessageToClient(targetedSocket, "Enter a character: ");
                                                                        String guess = serverService.getClientMessage(targetedSocket);

                                                                        // If more than 1 character is entered, preview error message
                                                                        while (guess.length()>1)
                                                                        {
                                                                            serverService.sendMessageToClient(targetedSocket, "Please enter only 1 character");
                                                                            guess = serverService.getClientMessage(targetedSocket);
                                                                        }

                                                                        // Handle guess
                                                                        // converted to lower to handle case sensitivity
                                                                        Character guessChar = Character.toLowerCase(guess.charAt(0));
                                                                        while (multiPlayer.hasBeenGuessed(guessChar))
                                                                        {
                                                                            // If the guess has already been made
                                                                            serverService.sendMessageToClient(targetedSocket, guessChar+" was already guessed! Please enter another character.\n");
                                                                            guess = serverService.getClientMessage(targetedSocket);
                                                                            while (guess.length()>1)
                                                                            {
                                                                                serverService.sendMessageToClient(currentSocket, "Please enter only 1 character");
                                                                                guess = serverService.getClientMessage(currentSocket);
                                                                            }
                                                                            guessChar = Character.toLowerCase(guess.charAt(0));
                                                                        }
                                                                        {
                                                                            // Unique guess
                                                                            // Add guess to array
                                                                            multiPlayer.addGuess(guessChar);
                                                                            if (multiPlayer.isCorrectGuess(guessChar))
                                                                            {
                                                                                // If guess is correct
                                                                                // update dashed word
                                                                                multiPlayer.updateDashed(guessChar);
                                                                                // update team score
                                                                                targetedUser.getCurrentTeam().updateTeamScore(1);
                                                                            }else
                                                                            {
                                                                                // update left attempts
                                                                                // note that If the user enters a wrong character that has
                                                                                // already been guessed before, we don't decrement the number
                                                                                // of remaining attempts again.
                                                                                targetedUser.getCurrentTeam().updateNumberOfAttemptsLeft();
                                                                            }
                                                                        }
                                                                    }
                                                                    // Game over
                                                                    // Display winner
                                                                    if(teamA.getTeamScore()>teamB.getTeamScore()) {
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "You won!", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "You lost!", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "The word was: "+multiPlayer.getOriginalPhrase(), super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "All team members got extra "+teamA.getTeamScore()+" point", super.getClientNameList());
                                                                        // Update team score
                                                                        teamA.updateScoreForAllPlayers();
                                                                    } else {
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "You won!", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "You lost!", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "The word was: "+multiPlayer.getOriginalPhrase(), super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "All team members got extra "+teamB.getTeamScore()+" point", super.getClientNameList());
                                                                        // Update team score
                                                                        teamB.updateScoreForAllPlayers();
                                                                    }
                                                                }
                                                                break;
                                                            }
                                                            else{
                                                                // Get the opponent team by name
                                                                serverService.sendMessageToClient(currentSocket, "Enter opponent's team name: ");
                                                                String opponentTeamName = serverService.getClientMessage(currentSocket);

                                                                while (!super.getTeams().containsKey(opponentTeamName))
                                                                {
                                                                    if(!super.getTeams().containsKey(opponentTeamName)){
                                                                        serverService.sendMessageToClient(currentSocket, "Team doesn't exist, please enter another name: ");
                                                                    }
                                                                    opponentTeamName = serverService.getClientMessage(currentSocket);
                                                                }

                                                                Team opponentTeam = super.getTeams().get(opponentTeamName);
                                                                // Set opponent team
                                                                newTeam.setOpponentTeam(opponentTeam);
                                                                opponentTeam.setOpponentTeam(newTeam);
                                                                serverService.sendMessageToClient(currentSocket, "Opponent team set!");
                                                                serverService.sendMessageToClient(currentSocket, "Press 'P' to start the game");
                                                                continue;
                                                            }
                                                        }
                                                        else {
                                                            serverService.sendMessageToClient(currentSocket, "You must send 'P' to start the game");
                                                        }
                                                    }
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
                                                        // Make sure that team capacity is not full
                                                        if(super.getTeams().get(teamNameToJoin).getPlayers().size() >= super.getGameConfig().getMaxPlayers())
                                                        {
                                                            serverService.sendMessageToClient(currentSocket, "Team is full, please enter another name: ");
                                                            teamNameToJoin = serverService.getClientMessage(currentSocket);
                                                            continue;
                                                        }
                                                        break;
                                                    }
                                                    // If team exists AND not full, add user to team
                                                    // valid team names
                                                    // Send message to all team members that a new player joined
                                                    Team targetedTeam = super.getTeams().get(teamNameToJoin);
                                                    serverService.sendMessageToAllTeamMembers(targetedTeam, currentUser.getUsername() + " joined the team!", super.getClientNameList());

                                                    // Add user to team
                                                    super.getTeams().get(teamNameToJoin).addPlayer(currentUser);
                                                    currentUser.setCurrentTeam(super.getTeams().get(teamNameToJoin));
                                                    // Send message to client that he joined the team
                                                    serverService.sendMessageToClient(currentSocket, "You joined the team!");
                                                    break;
                                                case "3":
                                                    // Join game room
                                                    serverService.sendMessageToClient(currentSocket, "Game Room");
                                                    break;
                                                default:
                                                    serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                                                    break;
                                            }

                                            // Loop on current's user teams to check if game started
                                            if(!currentUser.getCurrentTeam().getCanStartGame()){
                                                serverService.sendMessageToClient(currentSocket, "Waiting for other players to join...");
                                                while (!currentUser.getCurrentTeam().getCanStartGame()){
                                                    // wait;
                                                    if(multiPlayer.getGameOver()){
                                                        break;
                                                    }
                                                }
                                            }

                                            if(!currentUser.getCurrentTeam().getCanStartGame()){
                                                if (currentUser.getCurrentTeam().getOpponentTeam()!=null){
                                                    if (!currentUser.getCurrentTeam().getOpponentTeam().getCanStartGame()){
                                                        while (!currentUser.getCurrentTeam().getOpponentTeam().getCanStartGame()){
                                                            // wait;
                                                            if(multiPlayer.getGameOver()){
                                                                break;
                                                            }
                                                            continue;
                                                        }
                                                    }
                                                }
                                            }
                                            if(multiPlayer.getGameOver()){
                                                break;
                                            }
                                            else{
                                                while (!multiPlayer.getGameOver()){
                                                    // trap
                                                }
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
                        if(team.getPlayers().get(0).getUsername().equals(player.getUsername())){    // Team leader = first player in team
                            // If user is team leader, remove team --> disconnect all team members
                            super.getTeams().remove(team.getTeamName());
                            // Send message to all team members that the team was removed
                            try {
                                serverService.sendMessageToAllTeamMembers(team, ("Connection Issue!"), super.getClientNameList());
                                // TODO: Disconnect all team members
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }else{  // User is not team leader
                            // Remove user from team
                            team.removePlayer(player);
                            // Send message to all team members that a player left
                            try {
                                serverService.sendMessageToAllTeamMembers(team, (player.getUsername()+" left the team!"), super.getClientNameList());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
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