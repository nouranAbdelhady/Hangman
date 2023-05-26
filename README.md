# Hangman Game with Client-Server Architecture
This project is an implementation of the classic hangman game using client-server architecture. It utilizes socket programming and multithreading to provide a multiplayer gaming experience. The server allows both single-player and multiplayer modes, where multiple clients can play together. The game requires users to login or register before playing and provides various features and options.

## Features
1. **Logging into the application:**
Existing users can log in using their username and password, while new users need to register by providing their name, username, and password. Invalid login or registration scenarios are handled with appropriate error messages.
2. **Game Setup:** 
The server loads the following files on startup:
   - login.txt: Contains users' login credentials
   - score.txt: Contains score history for each user
   - configurations.txt: Contains game configuration (e.g., number of incorrect guesses needed to lose, maximum and minimum number of players per team)
   - lookup.txt: Contains phrases to be guessed

    After loading the files, the server waits for players to log in and starts the game based on the chosen options.
3. **Game Options:** 
Once logged in, users are presented with a menu where they can choose to play as a single player or with multiple players. If the multiplayer option is selected, users can form teams and play against each other.
4. **Teams:**
Users can choose to play in teams with other users. The teams should have unique names, and the server checks the number of players in both teams before starting the game. An error message is displayed if the number of players is unequal.
5. **The Game:** 
The game starts once all players are ready. The phrase to be guessed is displayed as underscores to represent hidden letters. The game proceeds in turns, and each user's guess is case-insensitive. When a user guesses a correct character, it is revealed to all users, and the user's score is updated and displayed.
6. **Number of Attempts:**
The game ends when the users are out of attempts, even if the phrase has missing characters to be guessed. The correct phrase is displayed, and the number of attempts is updated and displayed for each user after an incorrect guess.
7. **Scores:** 
Each user has a score history for their last single-player or multiplayer games. The score is calculated based on the number of correctly guessed characters in multiplayer games and equals the length of the word (including whitespaces) in single-player games.
8. **Quit the Game:** 
Users can choose to quit the game anytime by pressing the '-' character. This allows users to exit the game whenever they want.
9. **Disconnect in Multiplayer:**
In multiplayer mode, if one player disconnects from the game, all other team members and opponents' team are disconnected as well. This ensures a fair gameplay experience for all participants.
10. **Ready State in Multiplayer:**
In multiplayer mode, each team can indicate their readiness by pressing the 'P' key. Both teams must be ready for the game to start. This ensures that all participants are prepared for the gameplay.

## Contributors
This project was developed by *Nouran Abdelhady* and *Sarah Alsisi*.

Feel free to contribute to the project by submitting pull requests or suggesting improvements.

