import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class is used to handle multiple client connections, and
 * opens a new thread for EACH socket connection
 */
public class MultiThreadedServer extends Server implements Runnable {

    private final Socket currentSocket;    // Socket of client
    private User currentUser;

    public MultiThreadedServer(Socket socket) throws IOException {
        this.currentSocket = socket;
    }

    @Override
    public void run() {
        try {
            String welcome = "Welcome to Hangman! \n";
            serverService.sendMessageToClient(currentSocket, welcome);
            // When first connected, menu is previewed to client
            // Handle client messages
            while (true) {
                String menu = "1- Register \n" +
                        "2- Login";
                serverService.sendMessageToClient(currentSocket, menu);
                String option = serverService.getClientMessage(currentSocket);     // Read option sent from client
                if (option.equals("-")) {     //'-' means that client chose to disconnect
                    throw new SocketException();
                }
                switch (option) {
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
                        // Check login response
                        // If login response is true, start game menu
                        // If login response is false, return to menu
                        String loginMenu = "Login: \n" +
                                "Enter your username: ";
                        serverService.sendMessageToClient(currentSocket, loginMenu);
                        String usernameLogin = serverService.getClientMessage(currentSocket);
                        serverService.sendMessageToClient(currentSocket, "Enter your password: ");
                        String passwordLogin = serverService.getClientMessage(currentSocket);
                        int loginResponse=serverService.checkCredentials(usernameLogin, passwordLogin);
                        // Logged in successfully
                        if (loginResponse==200) {
                            serverService.sendMessageToClient(currentSocket, "Login successful!");
                            currentUser = super.getUsers().get(usernameLogin);
                            currentUser.setOnline(true);
                            super.getClientNameList().put(currentSocket, usernameLogin);
                            String printMessage = usernameLogin + " has connected to server";
                            System.out.println(printMessage);       // Print message to server

                            while(true) {
                                String gameMenu = "\nMain Menu \n" +
                                        "1- Single Player \n" +
                                        "2- Multiplayer \n";
                                serverService.sendMessageToClient(currentSocket, gameMenu);
                                String gameOption = serverService.getClientMessage(currentSocket);     // Read option sent from client
                                if (gameOption.equals("-")) {     //'-' means that client chose to disconnect
                                    throw new SocketException();
                                }
                                switch (gameOption) {
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
                                                    System.out.println("New team created: "+ newTeam.getTeamName());
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
                                                                        newTeam.setCanStartGame(false);
                                                                        break;
                                                                    } else
                                                                    {
                                                                        serverService.sendMessageToAllTeamMembers(opponentTeam, "You are down "+(-1*difference)+" player(s)", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(newTeam, "Waiting for other team....", super.getClientNameList());
                                                                        newTeam.setCanStartGame(true);
                                                                        opponentTeam.setCanStartGame(false);
                                                                        break;
                                                                    }
                                                                }
                                                                else{
                                                                    // Equal number of players
                                                                    // Set can start game to true
                                                                    newTeam.setCanStartGame(true);
                                                                    serverService.sendMessageToAllTeamMembers(newTeam, "Waiting for other team to start game....", super.getClientNameList());
                                                                    serverService.sendMessageToAllTeamMembers(opponentTeam, "You have been selected!\nPress 'P' to start game", super.getClientNameList());
                                                                    //opponentTeam.setCanStartGame(true);
                                                                }
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
                                                                            teamA.setGameOver(true);
                                                                            teamB.setGameOver(true);
                                                                            break;
                                                                        }
                                                                        if(multiPlayer.getNumberOfDashed()==0){
                                                                            // Game over
                                                                            multiPlayer.setGameOver(true);
                                                                            teamA.setGameOver(true);
                                                                            teamB.setGameOver(true);
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

                                                                        if (guess.equals("-")) {     //'-' means that client chose to disconnect
                                                                            throw new SocketException();
                                                                        }

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
                                                                    // Check score
                                                                    // Check if there is a tie
                                                                    if (teamA.getTeamScore()==teamB.getTeamScore())
                                                                    {
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "It's a tie!", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "It's a tie!", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "The word was: "+multiPlayer.getOriginalPhrase(), super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "The word was: "+multiPlayer.getOriginalPhrase(), super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamA, "All team members got extra "+teamA.getTeamScore()+" point", super.getClientNameList());
                                                                        serverService.sendMessageToAllTeamMembers(teamB, "All team members got extra "+teamB.getTeamScore()+" point", super.getClientNameList());
                                                                        // Update team score
                                                                        teamA.updateScoreForAllPlayers();
                                                                        teamB.updateScoreForAllPlayers();
                                                                        multiPlayer.setGameOver(true);
                                                                        teamA.setGameOver(true);
                                                                        teamB.setGameOver(true);
                                                                        break;
                                                                    }
                                                                    else{
                                                                        // Display winner
                                                                        if(teamA.getTeamScore()>teamB.getTeamScore()) {
                                                                            serverService.sendMessageToAllTeamMembers(teamA, "You won!", super.getClientNameList());
                                                                            serverService.sendMessageToAllTeamMembers(teamB, "You lost!", super.getClientNameList());
                                                                            serverService.sendMessageToAllTeamMembers(teamB, "The word was: "+multiPlayer.getOriginalPhrase(), super.getClientNameList());
                                                                            serverService.sendMessageToAllTeamMembers(teamA, "All team members got extra "+teamA.getTeamScore()+" point", super.getClientNameList());
                                                                            // Update team score
                                                                            teamA.updateScoreForAllPlayers();
                                                                            multiPlayer.setGameOver(true);
                                                                            teamA.setGameOver(true);
                                                                            teamB.setGameOver(true);
                                                                            break;
                                                                        } else {
                                                                            serverService.sendMessageToAllTeamMembers(teamB, "You won!", super.getClientNameList());
                                                                            serverService.sendMessageToAllTeamMembers(teamA, "You lost!", super.getClientNameList());
                                                                            serverService.sendMessageToAllTeamMembers(teamA, "The word was: "+multiPlayer.getOriginalPhrase(), super.getClientNameList());
                                                                            serverService.sendMessageToAllTeamMembers(teamB, "All team members got extra "+teamB.getTeamScore()+" point", super.getClientNameList());
                                                                            // Update team score
                                                                            teamB.updateScoreForAllPlayers();
                                                                            multiPlayer.setGameOver(true);
                                                                            teamA.setGameOver(true);
                                                                            teamB.setGameOver(true);
                                                                            break;
                                                                        }
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
                                                    // Do not return to main menu unless game is over (teamleader1 pressed 'P' and teamleader2 didnt press 'P')
                                                    while(true){
                                                        String thisUsername = super.getClientNameList().get(currentSocket);
                                                        User thisUser = super.getUsers().get(thisUsername);
                                                        if(thisUser.getCurrentTeam().getGameOver()){
                                                            break;
                                                        }
                                                    }
                                                    // wait 3 seconds before returning to main menu (preview score)
                                                    try {
                                                        Thread.sleep(3000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
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
                                                    System.out.println(currentUser.getUsername()+" joined team "+teamNameToJoin);
                                                    //System.out.println("Update: "+super.getTeams().get(teamNameToJoin).previewTeam());
                                                    break;
                                                case "3":
                                                    // Join game room
                                                    serverService.sendMessageToClient(currentSocket, "Join game room");
                                                    break;
                                                default: serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                                                    break;
                                            }

                                            // Game finished
                                            /*
                                            if (multiplayerOption.equals("1") || multiplayerOption.equals("2")){
                                                break;      // Go back to main menu after game
                                            }
                                             */
                                            // Loop on current's user teams to check if game started
                                            if(currentUser.getCurrentTeam()!=null&&!currentUser.getCurrentTeam().getCanStartGame()){
                                                serverService.sendMessageToClient(currentSocket, "Waiting for other players to join...");
                                            }

                                            while(true){
                                                String thisUsername = super.getClientNameList().get(currentSocket);
                                                User thisUser = super.getUsers().get(thisUsername);
                                                if(thisUser.getCurrentTeam().getGameOver()){
                                                    break;
                                                }
                                            }
                                            // wait 3 seconds before returning to main menu (preview score)
                                            try {
                                                Thread.sleep(3000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        }
                                        break;
                                    default:
                                        serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                                        break;

                                }
                            }
                        }
                        else{
                            // Login failed
                            serverService.sendMessageToClient(currentSocket, loginResponse+" - Login Failed!");
                            // Return to menu
                        }
                        break;
                    default:
                        serverService.sendMessageToClient(currentSocket, "Invalid option. Please try again.");
                        break;
                }
            }


        } catch (SocketException e) {

            // If user is in a team, disconnect all team members
            Team userTeam = currentUser.getCurrentTeam();
            this.disconnectClient(currentSocket);
            ArrayList<Socket> socketsToRemove = new ArrayList<>();
            ArrayList<User> playersToRemove = new ArrayList<>();
            if (userTeam != null) {
                // Disconnect all team members
                for (User user : userTeam.getPlayers()) {
                    // Get socket of team member
                    Set<Map.Entry<Socket, String>> list = getClientNameList().entrySet();
                    for (Map.Entry<Socket, String> entry : list) {
                        if (entry.getValue().equals(user.getUsername())) {
                            if(entry.getKey()!=currentSocket){      // avoid disconnecting currentSocket again
                                socketsToRemove.add(entry.getKey());
                            }
                        }
                    }
                    playersToRemove.add(user);
                }
                // Remove team from list of teams
                super.getTeams().remove(userTeam.getTeamName());

                // Disconnect opponent team
                Team opponentTeam = userTeam.getOpponentTeam();
                if(opponentTeam!=null){
                    ArrayList<User> opponentTeamMembers = opponentTeam.getPlayers();
                    for (User user : opponentTeamMembers) {
                        // Get socket of team member
                        Set<Map.Entry<Socket, String>> list = getClientNameList().entrySet();
                        for (Map.Entry<Socket, String> entry : list) {
                            if (entry.getValue().equals(user.getUsername())) {
                                socketsToRemove.add(entry.getKey());
                            }
                        }
                        playersToRemove.add(user);
                    }
                    // Remove team from list of teams
                    super.getTeams().remove(opponentTeam.getTeamName());
                }
            }

            // disconnect all team members
            for(Socket socket : socketsToRemove){
                if(!socket.isClosed()){
                    this.disconnectClient(socket);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch(NullPointerException e)
        {
            //System.out.print("NullPointerException Caught");
        }
    }

    public void disconnectClient(Socket client) {
        // Disconnect socket
        // Print message to server (client disconnected
        String printMessage = super.getClientNameList().get(client) + " got disconnected";
        System.out.println(printMessage);       // Print message to server

        // Reset user's state and set online to false
        String username=super.getClientNameList().get(client) ;
        if(username!=null){
            User user = super.getUsers().get(username);
            user.setOnline(false);

            //System.out.println("Offline: "+user.previewUserContent());
            if (user.getCurrentTeam() != null) {
                user.getCurrentTeam().removePlayer(user);
                user.setCurrentTeam(null);
            }
        }
        if(!client.isClosed()){
            try {
                serverService.sendMessageToClient(client, "Connection Error!");
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.getClientNameList().remove(client);     // Remove client from list
        super.getClients().remove(client);     // Remove client from list
    }
}