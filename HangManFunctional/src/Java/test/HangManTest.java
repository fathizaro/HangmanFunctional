package Java.test;


import Java.main.Hangman;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HangmanTest {

    // Hangman.selectWord tests
    @ParameterizedTest
    @CsvSource({"7,6", "0,1", "40,60"})
    void selectWord_ReturnsEmptyStringsForInvalidSizes(int minValue, int maxValue) {
        // If no valid word is found, selectWord should return an empty string
        //assertEquals( Hangman.selectWord(minValue, maxValue));
    }
    @ParameterizedTest
    @CsvSource({"0,30", "5,5", "7,30"})
    void selectWord_ReturnsWordForValidSizes(int minValue, int maxValue) {
        // Assuming there is a valid word found, a string of length greater than 0 should be returned
        assertTrue(Hangman.selectWord(minValue, maxValue).length() > 0);
    }

    // Hangman instance.generateMissed tests
    @Test
    void generateMissedReturnsOutputSorted() {
        System.setIn(new ByteArrayInputStream("name".getBytes()));
        Hangman game = new Hangman();
        game.addToGuessedSet('d');
        game.addToGuessedSet('c');
        game.addToGuessedSet('b');
        game.addToGuessedSet('a');
        assert(game.generateMissed().equals("Guessed letters: a, b, c, d"));
    }
    @Test
    void generateMissedIsCaseInsensitive() {
        System.setIn(new ByteArrayInputStream("name".getBytes()));
        Hangman game = new Hangman();
        game.addToGuessedSet('A');
        game.addToGuessedSet('a');
        assert (game.generateMissed().equals("Guessed letters: a"));
    }

    // Hangman.useInput tests
    @Test
    void userInputForDigitsReturnsDigits() {
        System.setIn(new ByteArrayInputStream("3".getBytes()));
        String output = Hangman.userInput(
                "",
                Pattern.compile("[\\d]"),
                ""
        );
        assert(output.equals("3"));
    }
    @Test
    void userInputForCharsReturnsChars() {
        System.setIn(new ByteArrayInputStream("d".getBytes()));
        String output = Hangman.userInput(
                "",
                Pattern.compile("[A-Za-z]"),
                ""
        );
        assert(output.equals("d"));
    }

    // Hangman instance.generateHiddenWord tests
    @Test
    void generateHiddenWordAddsCorrectGuesses() {
        System.setIn(new ByteArrayInputStream("name".getBytes()));
        Hangman game = new Hangman();
        game.setWord("apple");
        game.addToGuessedSet('p');
        assert(game.generateHiddenWord().equals("_pp__"));
    }

    // Hangman instance.hasWon tests
    @Test
    void hasWonReturnsTrueIfAllCharsFound() {
        System.setIn(new ByteArrayInputStream("name".getBytes()));
        Hangman game = new Hangman();
        game.setWord("apple");
        game.addToGuessedSet('a');
        game.addToGuessedSet('p');
        game.addToGuessedSet('l');
        game.addToGuessedSet('e');
        game.addToGuessedSet('z');
        assert(game.hasWon());
    }
    @Test
    void hasWonReturnsFalseIfAllCharsNotFound() {
        System.setIn(new ByteArrayInputStream("name".getBytes()));
        Hangman game = new Hangman();
        game.setWord("apple");
        game.addToGuessedSet('p');
        game.addToGuessedSet('l');
        game.addToGuessedSet('e');
        game.addToGuessedSet('z');
        assert(!game.hasWon());
    }
}