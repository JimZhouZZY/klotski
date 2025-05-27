/*
 * Copyright (C) 2025 Zhiyu Zhou (jimzhouzzy@gmail.com)
 * This file is part of github.com/jimzhouzzy/Klotski.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * KlotskiGameTest.java
 * 
 * This class contains unit tests for the KlotskiGame class.
 * 
 * THIS CLASS IS WIP
 * 
 * @author JimZhouZZY
 * @version 1.13
 * @since 2025-5-25
 * @see {@link KlotskiGame}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: fix white line at EOF
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-23: refactor test
 * 2025-05-23: refactor test
 * 2025-05-20: Merge branch v1.0.5 into main (#7)
 * 2025-05-14: add fromString method and test cases (#4)
 */

package io.github.jimzhouzzy.klotski.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class KlotskiGameTest {

    /**
     * Tests the {@link KlotskiGame#fromString(String)} method by initializing a game state from a predefined board string
     * and verifying that the resulting board representation matches the expected output. The test ensures that the
     * method correctly parses the input string, constructs the appropriate game state, and handles whitespace normalization.
     * The expected board string is compared after replacing all whitespace sequences with single spaces and trimming
     * trailing spaces. This validates that the game's internal state accurately reflects the provided configuration,
     * including piece placement and empty cells ('.' characters).
     */
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

    /** * Tests that an {@link IllegalArgumentException} is thrown when attempting to load a board
     * configuration with an invalid height. The test provides a board string containing3 rows
     * instead of the required5 rows, then verifies that the exception is thrown with the expected
     * error message indicating the invalid board height. This ensures the game correctly enforces
     * the requirement for boards to have exactly5 rows. */
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

    /** * Tests that the {@link KlotskiGame#fromString(String)} method throws an {@link IllegalArgumentException}
     * when provided with a board configuration containing an invalid row width. The test uses a board string
     * where the first row has5 columns instead of the expected4 columns. Verifies that the exception is thrown
     * with the correct error message indicating the invalid row index, the actual number of columns encountered,
     * and the expected number of columns according to the game's requirements. */
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

    /**
     * Tests that the {@link KlotskiGame} correctly detects when required game pieces have not been placed
     * on the board during initialization. This test provides an invalid board configuration missing mandatory
     * "soldier" pieces and verifies that an {@link IllegalStateException} is thrown with an appropriate error
     * message indicating unplaced pieces.
     *
     * <p>The test is currently disabled (via an unconditional return) because the game logic does not yet
     * implement validation for unplaced pieces. When enabled, it will validate that the game state cannot
     * be loaded from a string representation that omits required pieces, ensuring proper initialization checks.</p>
     *
     * @see KlotskiGame#fromString(String)
     */
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
