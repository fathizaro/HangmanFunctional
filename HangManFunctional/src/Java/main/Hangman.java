package Java.main;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Hangman {
    private String word;
    private int attempts; // Available guesses remaining; decremented until loss condition
    private HashSet<Character> guessedSet;
    private String playerName;
    private int score;

    // CONSTRUCTORS
    public Hangman() {
        // Non-custom instantiation
        word = selectWord(1, 30);
        attempts = 6;
        guessedSet = new HashSet<>();
        playerName = userInput("What is your name?\n", Pattern.compile("[a-zA-Z0-9]+"), "No special characters or spaces allowed.\n");
    }

    public Hangman(int score) {
        // Initialization with previous score record
        word = selectWord(1, 30);
        attempts = 6;
        guessedSet = new HashSet<>();
        playerName = userInput("What is your name?\n", Pattern.compile("[a-zA-Z0-9]+"), "No special characters or spaces allowed.\n");
        score = score;
    }

    public Hangman(int minLength, int maxLength) {
        // Custom instantiation; allows for parameterization of word length
        word = selectWord(minLength, maxLength);
        attempts = 6;
        guessedSet = new HashSet<>();
        playerName = userInput("What is your name?\n", Pattern.compile("[a-zA-Z0-9]+"), "No special characters or spaces allowed.\n");
    }

    // GETTERS AND SETTERS
    public void setWord(String w) {
        this.word = w;
    }
    public String getWord() {
        return word;
    }
    public void setAttempts(int a) {
        this.attempts = a;
    }
    public int getAttempts() {
        return attempts;
    }
    public boolean addToGuessedSet(char c) {
        // Case-insensitive; converts capital to lower-case
        // Returns true if character is not already in guessedSet, else false
        if (guessedSet.contains(c)) {
            return false;
        }
        this.guessedSet.add(("" + c).toLowerCase().charAt(0));
        return true;
    }
    public HashSet<Character> getGuessedSet() {
        return new HashSet<Character>(guessedSet);
    }
    public void setPlayerName(String name) {
        this.playerName = name;
    }
    public String getPlayerName() {
        return playerName;
    }
    public void setScore(int s) {
        this.score = s;
    }
    public int getScore() {
        return score;
    }

    public static String selectWord(int minLength, int maxLength) {
        if (maxLength < minLength) return "";

        try {
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/wordrepo.txt"));
            Collections.shuffle(lines); // Shuffles order of word repository to introduce randomness

            // Filters out word that don't match the desired word based on word length and content
            Pattern wordPat = Pattern.compile("[0-9- ]"); // For excluding words with numbers, hyphen, or spaces
            return lines.stream().filter(
                            line -> line.length() >= minLength && line.length() <= maxLength && !wordPat.matcher(line).find())
                    .findFirst().orElse("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String generateGallows() {
        // Reads gallows ascii art from 1 of 7 'gallow' text files, returning properly formatted string
        String gallows = "";
        try {
            gallows = Files.readAllLines(Paths.get("src/main/resources/gallow%d.txt".formatted(attempts))).stream()
                    .reduce("", (acc, line) -> acc.concat(line + "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gallows;
    }

    public String generateMissed() {
        String output = "Guessed letters: ";
        for (char c: guessedSet.stream().sorted().collect(Collectors.toList())) {
            output = output.length() == 17 ? output.concat("" + c) : output.concat(", " + c);
        }
        return output;
    }

    public String generateHiddenWord() {
        String hiddenWord = "";
        for (char c: word.toCharArray()) {
            if (guessedSet.contains(c)) {
                hiddenWord = hiddenWord.concat("" + c);
            } else {
                hiddenWord = hiddenWord.concat("_");
            }
        }
        return hiddenWord;
    }

    public String generateGameScreen() {
        return generateGallows() + "\n" + generateMissed() + "\n" + generateHiddenWord() + "\n";
    }

    public static boolean userInputYesOrNo(String question) {
        // Asks the user a yes or no question, returning boolean depending on answer, using recursion for input validation
        Scanner sc = new Scanner(System.in);

        String suffix = "(y or n)\n";
        System.out.println(question + suffix);
        try {
            String response = sc.nextLine().toLowerCase();
            if (response.equals("y")) return true;
            else if (response.equals("n")) return false;
            else {
                System.out.println("Invalid input. Please enter " + suffix);
                return userInputYesOrNo(question);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // This statement should never be reached
        return false;
    }

    public static String userInput(String question, Pattern validInputs, String invalidInputSuffix) {
        // Asks user a question using recursion for input validation
        Scanner sc = new Scanner(System.in);

        System.out.println(question);
        try {
            String response = sc.nextLine().toLowerCase().strip();
            if (validInputs.matcher(response).matches()) return response;
            else {
                System.out.println("Invalid input. Please enter " + invalidInputSuffix);
                userInput(question, validInputs, invalidInputSuffix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // This statement should never be reached
        return "";
    }

    public void guess() {
        try {
            char g = Hangman.userInput(
                    "Guess a letter\n",
                    Pattern.compile("[A-Za-z]"),
                    "a single letter.\n"
            ).charAt(0);
            if (!guessedSet.contains(g) && word.contains("" + g)) { // Guess was not already guessed and is part of word
                guessedSet.add(g);
            } else if (!guessedSet.contains(g) && !word.contains("" + g)) { // Guess was not already guessed and is not part of word
                guessedSet.add(g);
                attempts--;
            } else { // Guess was already guessed
                System.out.println("You already made that guess.");
                guess();
            }
        } catch (Exception e) {
            guess();
        }
    }

    public boolean hasWon() {
        Set<Character> wordSet = word.chars().mapToObj(e -> (char) e).collect(Collectors.toSet());
        return guessedSet.containsAll(wordSet);
    }

    public int determineScore() {
        // For a single word, not consecutive runs
        // Score is the number of unique characters correctly guessed
        // If the whole word is not guessed, the score is zero
        int score = 0;
        if (hasWon()) {
            score = word.chars().mapToObj(c -> (char) c).collect(Collectors.toSet()).size();
        }
        return score;
    }

    public void recordScore() {
        // Writes the new score to highScores.txt in the proper score order (highest scores at the top)
        List<String> titleList = Arrays.asList("Score : Player");
        try {
            List<String> currentScores = Files.readAllLines(Paths.get("src/main/resources/highScores.txt"));
            if (currentScores.size() == 1) {
                currentScores.add("Score : Player");
                currentScores.add("%d : %s".formatted(score, playerName));
            } else {
                currentScores.add("%d : %s".formatted(score, playerName));
                currentScores.removeAll(titleList);
                currentScores.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return Integer.valueOf(o1.split(" : ")[0]).compareTo(Integer.valueOf(o2.split(" : ")[0])) * -1;
                    }
                });
                currentScores.add(0, "Score : Player");
            }
            Files.write(
                    Paths.get("src/main/resources/highScores.txt"),
                    currentScores,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isHighScore(int score) {
        // Returns true of the input score is higher than all previous scores, otherwise returns false
        if (score == 0) return false;
        List<String> titleList = Arrays.asList("Score : Player");
        List<Integer> scores = new ArrayList<>();
        try {
            List<String> scoresStr = Files.readAllLines(Paths.get("src/main/resources/highScores.txt"));
            if (scoresStr.size() == 1) return true;
            scoresStr.removeAll(titleList);
            scores = scoresStr.stream().map(line -> Integer.parseInt(line.split(" : ")[0])).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (scores.isEmpty()) return true;
        return scores.stream().anyMatch(s -> score > s);
    }

    public static void main(String[] args) {
        try {
            boolean newGame = false;
            int score = 0;
            Hangman game = new Hangman();

            while (true) { // Game Loop
                // Game Initialization
                if (newGame && score != 0) {
                    game = new Hangman(score);
                    newGame = false;
                }
                else if (newGame) {
                    game = new Hangman();
                }
                System.out.println(game.generateGameScreen());
                System.out.println(game.getWord());

                game.guess();

                if (game.hasWon()) {
                    score += game.determineScore();
                    System.out.println(game.generateGameScreen());
                    if (isHighScore(score)) {
                        System.out.printf("Congratulations! You won! The word was %s. Your current score is %d.\nThat's a new high score!\n", game.getWord(), score);
                    } else {
                        System.out.printf("Congratulations! You won! The word was %s. Your current score is %d.\n", game.getWord(), score);
                    }
                    game.setScore(game.getScore() + score);
                    if (Hangman.userInputYesOrNo("Would you like to play again?")) newGame = true;
                    else {
                        game.recordScore();
                        break;
                    }
                } else if (game.getAttempts() == 0) {
                    System.out.println(game.generateGameScreen());
                    if (isHighScore(game.getScore())) {
                        System.out.printf("Game over...\nThe word was %s. Your final score was %d.\nThat's a new high score!\n", game.getWord(), score);
                    } else {
                        System.out.printf("Game over...\nThe word was %s. Your final score was %d.\n", game.getWord(), score);
                    }
                    game.setScore(game.getScore() + score);
                    if (game.getScore() != 0) game.recordScore();
                    if (Hangman.userInputYesOrNo("Would you like to play again?")) newGame = true;
                    else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("An exception was caught; you're safe now.");
            e.printStackTrace();
        }
    }}
