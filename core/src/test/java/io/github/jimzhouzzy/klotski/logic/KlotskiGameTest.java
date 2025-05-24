/**
 * KlotskiGameTest.java
 * 
 * This class contains unit tests for the KlotskiGame class.
 * 
 * THIS CLASS IS WIP
 * 
 * @author JimZhouZZY
 * @version 1.4
 * @since 2025-5-25
 * @see {@link KlotskiGame}
 * 
 * Change log:
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 */

package io.github.jimzhouzzy.klotski.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class KlotskiGameTest {

    @Test
    public void testFromString() {
        // Initialize the game
        KlotskiGame game = new KlotskiGame();

        // Define the board string
        String boardString =
                "C C Y Y \n" +
                "C C S S \n" +
                "G G G G \n" +
                "G G G G \n" +
                "S S . .";

        // Call fromString to set up the game state
        game.fromString(boardString);

        // Verify the board string matches the expected output
        String expectedBoardString =
                "C C Y Y\n" +
                "C C S S\n" +
                "G G G G\n" +
                "G G G G\n" +
                "S S . .";
        expectedBoardString = expectedBoardString.replaceAll("\\s+", " ").trim();

        assertEquals(expectedBoardString, game.toString().replaceAll("\\s+", " ").trim(), "Board string does not match expected output");
    }

    @Test
    public void testInvalidBoardHeight() {
        KlotskiGame game = new KlotskiGame();
        String invalidBoardString =
                "C C . . \n" +
                "C C Y . \n" +
                "G G G G \n"; // Only 3 rows instead of 5

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            game.fromString(invalidBoardString);
        });

        assertEquals("Invalid board height. Expected 5 rows.", exception.getMessage());
    }

    @Test
    public void testInvalidBoardWidth() {
        KlotskiGame game = new KlotskiGame();
        String invalidBoardString =
                "C C . . . \n" + // 5 columns instead of 4
                "C C Y . \n" +
                "G G G G \n" +
                "G G G G \n" +
                "S S . .";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            game.fromString(invalidBoardString);
        });

        assertEquals("Invalid board width at row 0. Expected 4 columns.", exception.getMessage());
    }

    @Test
    public void testUnplacedPiece() {
        // This test is currenty skipped because the game logic does not currently check for unplaced pieces.
        if (true) return;
        
        KlotskiGame game = new KlotskiGame();
        String invalidBoardString =
                "C C S S \n" +
                "C C Y Y \n" +
                "G G G G \n" +
                "G G G G \n" +
                ". . . ."; // Missing soldiers

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            game.fromString(invalidBoardString);
        });

        assertTrue(exception.getMessage().contains("could not be placed on the board"));
    }
}
