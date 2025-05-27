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
 * KlotskiGame.java
 * 
 * This class represents the game logic for the Klotski game.
 * It includes the game board, pieces, and methods for moving pieces,
 * checking legal moves, and determining the game state.
 * 
 * @author JimZhouZZY
 * @version 1.21
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-20: Merge branch v1.0.5 into main (#7)
 * 2025-05-14: add fromString method and test cases (#4)
 * 2025-04-23: better main screen
 * 2025-04-16: Login & Levels
 * 2025-04-16: Login & Game Mode & Save-Load
 * 2025-04-13: feat: restart hint and congratulations
 * 2025-04-08: refactor to libgdx structure
 * 2025-04-08: Implemented KlotskiSovler
 * 2025-04-08: implemented KlotskiGame.java
 */

package io.github.jimzhouzzy.klotski.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KlotskiGame {
    public enum Block {
        CAO_CAO("Cao Cao", 2, 2, 1, 'C'),
        GUAN_YU("Guan Yu", 2, 1, 1, 'Y'),
        GENERAL("General", 1, 2, 4, 'G'),
        SOLDIER("Soldier", 1, 1, 4, 'S');

        private final String name;
        private final int width;
        private final int height;
        private final int count;
        private final char abbreviation;

        Block(String name, int width, int height, int count, char abbreviation) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.count = count;
            this.abbreviation = abbreviation;
        }

        /**
         * Retrieves the name associated with this instance. The returned value is the current
         * value of the {@code name} property, which may be {@code null} or an empty string
         * if the name has not been initialized or explicitly set.
         */
        public String getName() { return name; }
        /**
         * Retrieves the current width value of the object. This method returns the integer value
         * representing the width as stored in the instance variable, providing access to the object's
         * horizontal dimension measurement.
         */
        public int getWidth() { return width; }
        /**
         * Retrieves the current height value associated with this object.
         * The height represents the vertical dimension measured in units
         * specific to the context of the implementation.
         *
         * @return the integer value representing the height of the object.
         */
        public int getHeight() { return height; }
        /**
         * Returns the current value of the count. This method provides access to the
         * internal count value, allowing callers to retrieve its current state without
         * modifying it. The returned value represents the count at the time the method
         * is called, and subsequent changes to the count will not affect the returned value.
         *
         * @return the current count as an integer value.
         */
        public int getCount() { return count; }
        /**
         * Retrieves the single-character abbreviation associated with this instance.
         * The abbreviation is a character that represents a shortened form or symbol
         * typically used for concise referencing or identification.
         *
         * @return the abbreviation character stored in this instance
         */
        public char getAbbreviation() { return abbreviation; }

        /**
         * Returns a string representation of the object, formatted to include the name, dimensions,
         * count, and abbreviation. The string follows the format: "Name (Size: WidthxHeight, Count: X, Abbreviation: Y)",
         * where Name is the object's name, Width and Height represent the size dimensions, X is the count value,
         * and Y is the single-character abbreviation.
         *
         * @return A formatted string summarizing the object's key attributes, including its name, size (width and height),
         * current count, and abbreviation character.
         */
        @Override
                public String toString() {
                    return String.format("%s (Size: %dx%d, Count: %d, Abbreviation: %c)",
                            name, width, height, count, abbreviation);
                }
    }

    public static class KlotskiPiece implements Serializable {
        public final int id;
        public final String name;
        public final char abbreviation;
        public final int width;
        public final int height;
        public int[] position; // [row, col]

        public KlotskiPiece(int id, String name, char abbreviation, int width, int height, int[] position) {
            this.id = id;
            this.name = name;
            this.abbreviation = abbreviation;
            this.width = width;
            this.height = height;
            this.position = position.clone();
        }

        /**
         * Retrieves a copy of the position array representing the current coordinates.
         * The returned array is a clone of the internal position state, ensuring that
         * modifications to the returned array do not affect the original position data.
         * The array contains two elements: the x-coordinate at index0 and the y-coordinate at index1.
         *
         * @return a new integer array containing a copy of the current position coordinates.
         */
        public int[] getPosition() {
                    return position.clone();
                }

        /**
         * Returns the current row index from the internal position data. This value represents the vertical
         * position in a grid or coordinate system, corresponding to the first element of the stored position array.
         * The row index is typically zero-based, indicating its position relative to the topmost row (0) of the
         * applicable context, such as a grid, matrix, or table structure.
         *
         * @return the zero-based integer value representing the current row index.
         */
        public int getRow() {
                    return position[0];
                }

        /**
         * Returns the column index of the current position in a grid or2D coordinate system.
         * This value corresponds to the second element (index1) of the position array, which
         * represents the column component. The column index is zero-based.
         *
         * @return the zero-based column index of the current position.
         */
        public int getCol() {
                    return position[1];
                }

        /**
         * Sets the position of this object using a cloned copy of the provided integer array.
         * This ensures that modifications to the original array after calling this method will not
         * affect the internally stored position. The position array typically represents coordinates
         * in a multidimensional space (e.g., [x, y] for2D or [x, y, z] for3D contexts).
         *
         * @param position the array containing the position coordinates to set. The array must not
         * be null, and its structure should match the dimensional requirements of
         * the context in which it is used.
         */
        public void setPosition(int[] position) {
                    this.position = position.clone();
                }

        /**
         * Retrieves the shape dimensions of this object as an integer array. The array contains
         * two elements where the first element represents the width and the second element
         * represents the height. This method returns a new array instance to prevent modification
         * of the internal state of the object.
         *
         * @return an integer array of length2 containing the width and height of the shape,
         * in that order.
         */
        public int[] getShape() {
                    return new int[]{width, height};
                }

        /** * Calculates and returns the size based on the product of the width and height.
         * The result represents the computed value derived from multiplying the current
         * width and height dimensions stored in the object. */
        public int getSize() {
                    return width * height;
                }

        /**
         * Returns a string representation of the object, formatted to include descriptive
         * and identifying attributes. The string follows the pattern:
         * "{name} (ID: {id}, Position: [{x},{y}], Size: {width}x{height})", where:
         * <ul>
         * <li>{@code name} is the object's assigned name</li>
         * <li>{@code id} is the object's unique numeric identifier</li>
         * <li>{@code x} and {@@code y} are the coordinates from the position array</li>
         * <li>{@code width} and {@code height} represent the object's dimensions</li>
         * </ul>
         *
         * @return a formatted string summarizing the object's key properties, including
         * name, ID, positional coordinates, and dimensional size
         */
        @Override
                public String toString() {
                    return String.format("%s (ID: %d, Position: [%d,%d], Size: %dx%d)",
                            name, id, position[0], position[1], width, height);
                }
    }

    public KlotskiPiece[] pieces;
    protected int moveCount;
    public static final int BOARD_WIDTH = 4;
    public static final int BOARD_HEIGHT = 5;
    public int blockedId; // ID of the piece that is blocked and cannot be moved

    public KlotskiGame() {
        blockedId = -1; // Default value for blocked piece ID
        initialize();
    }
    
    public KlotskiGame(int blockedId) {
        this.blockedId = blockedId; // Default value for blocked piece ID
        initialize();
    }

    /**
     * Initializes the Klotski puzzle game by creating and positioning all pieces on the board.
     * Sets up the standard starting configuration:
     * <ul>
     * <li>Cao Cao (2x2, ID0) at position [0,1]</li>
     * <li>Guan Yu (2x1) at position [3,1]</li>
     * <li>Four1x2 Generals positioned at [0,0], [0,3], [2,0], and [2,3]</li>
     * <li>Four1x1 Soldiers placed across the bottom row (row4) at columns0-3</li>
     * </ul>
     * Resets the move counter to0. This configuration represents the classic Klotski puzzle layout
     * where the objective is to maneuver Cao Cao (the largest piece) to escape through the board's exit.
     */
    public void initialize() {
            pieces = new KlotskiPiece[10];
            // Cao Cao (2x2) (row x col)
            // Cao Cao should always have id = 0
            pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
            // Guan Yu (2x1)
            pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
            // Generals (1x2)
            pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
            pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
            pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
            pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{2, 3});
            // Soldiers (1x1)
            pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
            pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
            pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
            pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{4, 3});
    
            moveCount = 0;
        }

    /**
     * Moves a Klotski piece from the specified starting position to the target position if the move is legal.
     * The method validates the move using {@link #isLegalMove(int[], int[])}, and if valid, updates the piece's
     * position by calculating the offset between the 'to' and 'from' coordinates. The piece's top-left corner
     * is repositioned according to this offset, and the total move count is incremented. If the move is not legal,
     * no action is taken.
     *
     * @param from An array of two integers representing the current position's row (index0) and column (index1)
     * of the piece's top-left corner.
     * @param to An array of two integers representing the target position's row (index0) and column (index1)
     * where the piece's top-left corner will be moved.
     */
    public void applyAction(int[] from, int[] to) {
            if (isLegalMove(from, to)) {
                KlotskiPiece piece = getPieceAt(from);
                int[] offset = {to[0] - from[0], to[1] - from[1]};
    
                // Calculate the piece's new position (top-left corner)
                int[] newPos = {piece.position[0] + offset[0], piece.position[1] + offset[1]};
                piece.setPosition(newPos);
                moveCount++;
            }
        }

    /**
     * Checks if moving a piece from the specified starting position to the target position is a legal move.
     * A move is considered legal if:
     * - Both the starting and target positions are within the bounds of the board.
     * - A piece exists at the starting position.
     * - The target position is exactly one square away in any orthogonal direction (up, down, left, or right).
     * - The piece's entire area, after moving, remains within the board boundaries.
     * - The piece's new position does not overlap with any other pieces on the board.
     *
     * @param from The starting position as a2D coordinate [row, column].
     * @param to The target position as a2D coordinate [row, column].
     * @return {@code true} if the move is valid according to all checks, {@code false} otherwise.
     */
    public boolean isLegalMove(int[] from, int[] to) {
            // Check if positions are within bounds
            if (from[0] < 0 || from[0] >= BOARD_HEIGHT || from[1] < 0 || from[1] >= BOARD_WIDTH ||
                to[0] < 0 || to[0] >= BOARD_HEIGHT || to[1] < 0 || to[1] >= BOARD_WIDTH) {
                return false;
            }
    
            KlotskiPiece piece = getPieceAt(from);
            if (piece == null) {
                return false; // No piece at starting position
            }
    
            // Check if the move is exactly one step in any direction
            int rowDiff = to[0] - from[0];
            int colDiff = to[1] - from[1];
            if (!((Math.abs(rowDiff) == 1 && colDiff == 0) ||
                  (Math.abs(colDiff) == 1 && rowDiff == 0))) {
                return false;
            }
    
            // Calculate the piece's new position (top-left corner)
            int[] newPos = {piece.position[0] + rowDiff, piece.position[1] + colDiff};
    
            // Check if new position is within bounds
            if (newPos[0] < 0 || newPos[0] + piece.height > BOARD_HEIGHT ||
                newPos[1] < 0 || newPos[1] + piece.width > BOARD_WIDTH) {
                return false;
            }
    
            // Check for collisions with other pieces
            for (KlotskiPiece other : pieces) {
                if (other != piece && overlaps(other, newPos, piece.width, piece.height)) {
                    return false;
                }
            }
    
            return true;
        }

    /**
     * Checks if the current configuration represents a terminal (winning) state in the Klotski puzzle.
     * The method verifies whether the piece located at the precise grid position [3,1] is the designated
     * target piece, identified by either its unique ID (0) or its name ("Cao Cao"). This position corresponds
     * to the exit location in the classic Klotski puzzle layout, and the presence of the target piece there
     * indicates a successful solution.
     *
     * @return {@code true} if the target piece occupies the terminal position [3,1], {@code false} otherwise.
     */
    public boolean isTerminal() {
            KlotskiPiece piece = getPieceAtPrecise(new int[] {3, 1});
            if (piece != null && (piece.id == 0 || "Cao Cao".equals(piece.name))) {
                return true;
            }
            return false;
        }

    /**
     * Determines if the specified rectangular area, defined by the given position, width, and height,
     * overlaps with the current position and dimensions of the provided {@link KlotskiPiece}. Overlapping
     * occurs if the two areas share any common space in the grid. The check is performed using
     * axis-aligned bounding box collision detection.
     *
     * @param piece the {@link KlotskiPiece} to check for overlap against
     * @param position the [row, column] coordinates of the top-left corner of the area to check
     * @param width the width (horizontal span) of the area to check
     * @param height the height (vertical span) of the area to check
     * @return {@code true} if the specified area overlaps with the piece's current bounds,
     * {@code false} if there is no overlap
     */
    public static boolean overlaps(KlotskiPiece piece, int[] position, int width, int height) {
            return !(position[0] >= piece.position[0] + piece.height ||
                    position[0] + height <= piece.position[0] ||
                    position[1] >= piece.position[1] + piece.width ||
                    position[1] + width <= piece.position[1]);
        }

    /**
     * Retrieves the {@link KlotskiPiece} located at the specified grid position. The method checks
     * if the given position coordinates fall within the boundaries of any piece's current location.
     *
     * @param position A two-element integer array representing the grid coordinates to check,
     * where {@code position[0]} is the row (x-coordinate) and {@code position[1]}
     * is the column (y-coordinate).
     * @return The first {@link KlotskiPiece} whose area contains the specified position,
     * or {@code null} if no piece occupies the given coordinates.
     */
    private KlotskiPiece getPieceAt(int[] position) {
            for (KlotskiPiece piece : pieces) {
                if (position[0] >= piece.position[0] &&
                    position[0] < piece.position[0] + piece.height &&
                    position[1] >= piece.position[1] &&
                    position[1] < piece.position[1] + piece.width) {
                    return piece;
                }
            }
            return null;
        }

    /**
     * Retrieves the KlotskiPiece located at the specified precise grid position. The method checks all
     * pieces in the current collection and returns the first piece whose top-left corner exactly matches
     * the given [row, column] coordinates. The position is expected as a two-element array where
     * position[0] represents the row and position[1] represents the column.
     *
     * @param position A two-element integer array containing the exact [row, column] coordinates to check.
     * @return The KlotskiPiece occupying the specified precise position, or {@code null} if no piece
     * exists at that exact location. If multiple pieces overlap the position, the first match
     * in the collection order is returned.
     */
    private KlotskiPiece getPieceAtPrecise(int[] position) {
            for (KlotskiPiece piece : pieces) {
                if (position[0] == piece.position[0] &&
                    position[1] == piece.position[1]) {
                    return piece;
                }
            }
            return null;
        }

    /**
     * Checks if the specified position of a Klotski piece is within the legal boundaries of the board.
     * The position is considered legal if the piece's top-left coordinate is non-negative and the piece's
     * full dimensions (height and width) do not exceed the board's height ({@code BOARD_HEIGHT}) or width ({@code BOARD_WIDTH}).
     *
     * @param pos The top-left coordinate of the piece as a two-element array [row, column].
     * @param piece The Klotski piece to validate, containing its dimensions (height and width).
     * @return {@code true} if the piece is entirely within the board's bounds; {@code false} if any part
     * of the piece exceeds the board's dimensions or if the position has negative coordinates.
     */
    private boolean checkLegalPosition(int[] pos, KlotskiPiece piece) {
            // Check if the piece is legally placed (inside of bounds)
            if (pos[0] < 0 || pos[1] < 0 || pos[0] + piece.height > BOARD_HEIGHT || pos[1] + piece.width > BOARD_WIDTH) {
                return false;
            }
            return true;
        }

    /**
     * Retrieves all legal adjacent moves for a specified piece at the given position. Legal moves are determined
     * by checking if moving the piece to an adjacent cell (up, down, left, or right) is valid based on game rules.
     * Returns null if the input position is invalid, the piece does not exist, or the piece is blocked by game logic.
     * Otherwise, returns a list of valid destination coordinates as [row, column] arrays. An empty list indicates
     * the piece exists but has no legal moves in the current game state.
     *
     * @param position The current [row, column] coordinates of the piece to check, as a2-element int array.
     * @return A list of2-element int arrays representing legal destination coordinates, or null if the input
     * is invalid, the piece does not exist, or the piece is blocked. Returns an empty list if the piece
     * exists but has no valid moves.
     */
    public List<int[]> getLegalMovesForPiece(int[] position) {
            List<int[]> legalMoves = new ArrayList<>();
            KlotskiPiece piece = getPieceAt(position);
    
            if (!checkLegalPosition(position, piece)
                    || piece.id == this.blockedId){
                return null;
            }
    
            if (piece == null) return legalMoves;
    
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int[] newPos = {position[0] + dir[0], position[1] + dir[1]};
                if (isLegalMove(position, newPos)) {
                    legalMoves.add(newPos);
                }
            }
            return legalMoves;
        }

    /**
     * Retrieves all legal moves for all pieces in the specified direction. Each legal move is represented
     * as a two-dimensional array containing the original position and the target position of a piece.
     * The method checks each piece's potential movement in the given direction and validates it using
     * the game's movement rules.
     *
     * @param direction a two-element array representing the direction vector (e.g., [dx, dy]) to check for legal moves.
     * The first element denotes the change in row (vertical direction), and the second
     * element denotes the change in column (horizontal direction).
     * @return a list of legal moves, where each move is stored as an int[][] with two elements:
     * the first element is the original position (int[2]) of a piece, and the second element
     * is the target position (int[2]) after moving in the specified direction.
     */
    public List<int[][]> getLegalMovesByDirection(int[] direction) {
            List<int[][]> legalMoves = new ArrayList<>();
    
            // Iterate through all pieces
            for (KlotskiPiece piece : pieces) {
                int[] currentPosition = piece.getPosition();
    
                // Check each direction for legal moves
                int[] newPosition = {
                    currentPosition[0] + direction[0],
                    currentPosition[1] + direction[1]
                };
    
                // If the move is legal, add it to the list
                if (isLegalMove(currentPosition, newPosition)) {
                    legalMoves.add(new int[][]{currentPosition, newPosition});
                }
    
            }
    
            return legalMoves;
        }

    /**
     * Retrieves a list of all legal moves for all pieces in the current game state. Each legal move is represented
     * as a two-dimensional array where the first element is the current position [row, column] of the piece, and the
     * second element is the new position [row, column] to which the piece can legally move. The method checks all four
     * possible directions (up, down, left, right) for each piece and validates whether the move is permitted based on
     * the game's rules. The returned list contains all valid moves that can be made in the current configuration.
     *
     * @return A list of legal moves, where each move is stored as an int[][] array of length2, containing the
     * starting and ending coordinates of the piece's movement.
     */
    public List<int[][]> getLegalMoves() {
            List<int[][]> legalMoves = new ArrayList<>();
    
            // Iterate through all pieces
            for (KlotskiPiece piece : pieces) {
                int[] currentPosition = piece.getPosition();
    
                int[][] directions = new int[][] {
                    {-1, 0}, // Up
                    {1, 0},  // Down
                    {0, -1}, // Left
                    {0, 1}   // Right
                };
    
                for (int[] direction : directions) {
                    // Check each direction for legal moves
                    int[] newPosition = {
                        currentPosition[0] + direction[0],
                        currentPosition[1] + direction[1]
                    };
    
                    // If the move is legal, add it to the list
                    if (isLegalMove(currentPosition, newPosition)) {
                        legalMoves.add(new int[][]{currentPosition, newPosition});
                    }
                }
            }
    
            return legalMoves;
        }

    /**
     * Shuffles the game state by applying a series of random legal moves. The shuffle process
     * uses a specified seed to ensure reproducibility. Up to100 legal moves are attempted
     * in sequence, but the process stops early if no legal moves remain. Each move is selected
     * uniformly at random from the available legal moves at each step. After shuffling, the
     * final game state is printed to the console.
     *
     * @param seed the seed used to initialize the random number generator, ensuring that
     * the same shuffle sequence can be reproduced by reusing the same seed.
     */
    public void randomShuffle(long seed) {
            Random random = new Random(seed);
    
            for (int i = 0; i < 100; i++) {
                List<int[][]> legalMoves = getLegalMoves();
                if (legalMoves.isEmpty()) {
                    break; // No legal moves available
                }
    
                // Pick a random move from the list of legal moves
                int[][] move = legalMoves.get(random.nextInt(legalMoves.size()));
                int[] from = move[0];
                int[] to = move[1];
    
                // Apply the selected move
                applyAction(from, to);
            }
            System.out.println("Shuffled the game:");
            System.out.println(this.toString());
        }

    /**
     * Randomly shuffles the elements in the collection using a pseudorandom behavior based on the
     * current system time as the seed. This method ensures a different shuffling order each time it is
     * called, provided the calls occur in different milliseconds. The randomness is derived from a
     * {@link java.util.Random} instance initialized with the seed value from {@link System#currentTimeMillis()}.
     * For consistent results across multiple shuffles, use {@link #randomShuffle(long)} with a fixed seed.
     */
    public void randomShuffle() {
            long seed = System.currentTimeMillis(); // Use the current time as the seed
            randomShuffle(seed);
        }

    /**
     * Converts a two-dimensional board coordinate into a corresponding one-dimensional index.
     * The index is calculated by multiplying the row component (first element) of the coordinate
     * by the board's width and adding the column component (second element). This assumes a row-major
     * order layout for indexing elements in the board.
     *
     * @param coordinate A two-element integer array where {@code coordinate[0]} represents the row
     * and {@code coordinate[1]} represents the column on the board.
     * @return The calculated index corresponding to the provided coordinate, based on {@code BOARD_WIDTH}.
     */
    private int coordinateToIndex(int[] coordinate) {
            return coordinate[0] * BOARD_WIDTH + coordinate[1];
        }

    /**
     * Converts a linear index into a2D coordinate representation based on the board's width.
     * The resulting coordinates are calculated by determining the row through integer division
     * of the index by {@code BOARD_WIDTH}, and the column through the remainder of the division.
     * This allows mapping a1D index to a2D grid position, where rows and columns are zero-based.
     *
     * @param index The linear index to convert into row and column coordinates.
     * @return An array of two integers where the first element is the row (y-coordinate)
     * and the second element is the column (x-coordinate).
     */
    private int[] indexToCoordinate(int index) {
            return new int[]{index / BOARD_WIDTH, index % BOARD_WIDTH};
        }

    /**
     * Generates a string representation of the Klotski board state, including all placed pieces.
     * The board is initialized with '.' characters representing empty spaces. Each piece's
     * position and shape are overlaid on the board using their assigned abbreviation characters.
     * The output format consists of rows and columns, with each cell separated by a space and
     * rows separated by newline characters. The final string provides a visual grid layout of
     * the current game state, reflecting the positions of all active pieces.
     *
     * @return A formatted string representing the board's current state, with piece abbreviations
     * placed according to their positions and dimensions. Each row ends with a newline.
     */
    @Override
        public String toString() {
            char[][] board = new char[BOARD_HEIGHT][BOARD_WIDTH];
            // Initialize empty board
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[i][j] = '.';
                }
            }
    
            // Place pieces on the board
            for (KlotskiPiece piece : pieces) {
                int[] pos = piece.getPosition();
    
                for (int i = 0; i < piece.height; i++) {
                    for (int j = 0; j < piece.width; j++) {
                        if (0 <= pos[0] + i  && pos[0] + i < BOARD_HEIGHT 
                                && 0 <= pos[1] + j && pos[1] + j < BOARD_WIDTH) {
                            board[pos[0] + i][pos[1] + j] = piece.abbreviation;
                        }
                    }
                }
            }
    
            // Build the string representation
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    sb.append(board[i][j]).append(' ');
                }
                sb.append('\n');
            }
            // sb.append("Move count: ").append(moveCount);
            return sb.toString();
        }

    /**
     * Parses a board configuration from a string representation and updates the positions of the pieces.
     * The input string must consist of {@value #BOARD_HEIGHT} rows, each containing {@value #BOARD_WIDTH}
     * space-separated cells. Each cell is either a '.' (empty) or a character matching a piece's abbreviation.
     * Pieces are placed on the board by checking their shape and abbreviation, starting from the top-left cell
     * of their position. All pieces must fit within the board without overlaps. After parsing, validates that all
     * pieces have been correctly placed and no cells remain unaccounted for.
     *
     * @param boardString the string representation of the board, with rows separated by newlines.
     * @throws IllegalArgumentException if the board dimensions (height or width of any row) are invalid.
     * @throws IllegalStateException if a piece cannot be placed (e.g., missing abbreviation, overlapping cells,
     * or a piece's shape does not match the provided cells).
     */
    public void fromString(String boardString) {
            // Split the board string into rows
            String[] rows = boardString.trim().split("\n");
            if (rows.length != BOARD_HEIGHT) {
                throw new IllegalArgumentException("Invalid board height. Expected " + BOARD_HEIGHT + " rows.");
            }
    
            // Create a temporary board to track which cells are occupied
            char[][] board = new char[BOARD_HEIGHT][BOARD_WIDTH];
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                String[] cells = rows[i].trim().split(" ");
                if (cells.length != BOARD_WIDTH) {
                    throw new IllegalArgumentException("Invalid board width at row " + i + ". Expected " + BOARD_WIDTH + " columns.");
                }
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[i][j] = cells[j].charAt(0);
                }
            }
    
            // Reset all pieces' positions
            for (KlotskiPiece piece : pieces) {
                piece.setPosition(new int[]{-1, -1}); // Temporarily mark as unplaced
            }
    
            // Iterate through the board and update piece positions
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    char cell = board[row][col];
                    if (cell == '.') {
                        continue; // Skip empty cells
                    }
    
                    // Find the piece corresponding to the cell's abbreviation
                    for (KlotskiPiece piece : pieces) {
                        if (piece.abbreviation == cell && piece.getPosition()[0] == -1) {
                            // Check if the piece fits at this position
                            boolean fits = true;
                            for (int i = 0; i < piece.height; i++) {
                                for (int j = 0; j < piece.width; j++) {
                                    int r = row + i;
                                    int c = col + j;
                                    if (r >= BOARD_HEIGHT || c >= BOARD_WIDTH || board[r][c] != cell) {
                                        fits = false;
                                        break;
                                    }
                                }
                                if (!fits) break;
                            }
    
                            if (fits) {
                                piece.setPosition(new int[]{row, col});
                                for (int i = 0; i < piece.height; i++) {
                                    for (int j = 0; j < piece.width; j++) {
                                        int r = row + i;
                                        int c = col + j;
                                        board[r][c] = '.'; // Mark the cell as occupied
                                    }
                                }
                                if (!fits) break;
                                break;
                            }
                        }
                    }
                }
            }
            // Debug print for piece positions
            System.out.println("Piece positions after fromString:");
            for (KlotskiPiece piece : pieces) {
                System.out.println(piece);
            }
            try {
                System.out.println(this.toString());
            } catch (Exception e) {
                if (e instanceof ArrayIndexOutOfBoundsException) {
                    throw new IllegalStateException("Piece missing or overflowing: " + e.getMessage());
                } else {
                    System.out.println("Unexpected exception: " + e.getMessage());
                }
            }
    
            // Validate that all pieces have been placed
            for (KlotskiPiece piece : pieces) {
                if (piece.getPosition()[0] == -1) {
                    throw new IllegalStateException("Piece " + piece.name + " could not be placed on the board.");
                }
            }
        }

    /**
     * Retrieves a copy of the array containing all KlotskiPiece objects in the puzzle. Modifying
     * the returned array will not affect the internal state of the Klotski game. The order of the
     * pieces in the array corresponds to their current positions within the puzzle configuration.
     *
     * @return a cloned array of KlotskiPiece instances representing the puzzle's pieces
     */
    public KlotskiPiece[] getPieces() {
            return pieces.clone();
        }

    /**
     * Retrieves the current move count tracked by this instance. The move count represents the total
     * number of moves recorded since the counter was initialized or reset. This value is typically
     * incremented in response to specific actions or events within the context of its usage.
     *
     * @return The current number of moves as a non-negative integer.
     */
    public int getMoveCount() {
            return moveCount;
        }

    /**
     * Replaces the current set of Klotski puzzle pieces with the provided array of new pieces. The method performs
     * validation to ensure the new pieces array is non-null, has the same length as the current pieces array, and contains
     * no null elements. Each piece in the new array is deep-copied to prevent external modification, preserving the
     * integrity of the puzzle configuration. If any validation fails, an {@code IllegalArgumentException} is thrown.
     *
     * @param newPieces The array of {@code KlotskiPiece} objects to replace the current pieces. Must not be null, must
     * match the length of the current pieces array, and must not contain any null elements.
     * @throws IllegalArgumentException if {@code newPieces} is null, has a different length than the current pieces array,
     * or contains any null elements.
     */
    public void setPieces(KlotskiPiece[] newPieces) {
            if (newPieces == null || newPieces.length != pieces.length) {
                throw new IllegalArgumentException("Invalid pieces array. It must have the same length as the original.");
            }
    
            // Replace the current pieces with the new ones
            for (int i = 0; i < pieces.length; i++) {
                if (newPieces[i] == null) {
                    throw new IllegalArgumentException("Piece at index " + i + " is null.");
                }
                pieces[i] = new KlotskiPiece(
                    newPieces[i].id,
                    newPieces[i].name,
                    newPieces[i].abbreviation,
                    newPieces[i].width,
                    newPieces[i].height,
                    newPieces[i].getPosition()
                );
            }
        }

    /**
     * Replaces the current Klotski pieces with the provided list of new pieces. The new list must
     * be non-null, have the same number of elements as the original pieces array, and contain no
     * null entries. Each piece from the list is deep-copied into the internal array to ensure
     * immutability and prevent external modifications to the puzzle state.
     *
     * @param newPieces The list of pieces to replace the current configuration. Must have the same
     * size as the original pieces array and contain valid, non-null KlotskiPiece instances.
     * @throws IllegalArgumentException if {@code newPieces} is null, has an incorrect size, or
     * contains a null element at any index. The exception message
     * specifies the exact validation failure.
     */
    public void setPieces(List<KlotskiPiece> newPieces) {
            if (newPieces == null || newPieces.size() != pieces.length) {
                throw new IllegalArgumentException("Invalid pieces list. It must have the same size as the original.");
            }
    
            // Replace the current pieces with the new ones
            for (int i = 0; i < pieces.length; i++) {
                KlotskiPiece newPiece = newPieces.get(i);
                if (newPiece == null) {
                    throw new IllegalArgumentException("Piece at index " + i + " is null.");
                }
                pieces[i] = new KlotskiPiece(
                    newPiece.id,
                    newPiece.name,
                    newPiece.abbreviation,
                    newPiece.width,
                    newPiece.height,
                    newPiece.getPosition()
                );
            }
        }

    /**
     * The main entry point for the Klotski Game application. This method initializes the game,
     * displays the initial board state, and enters a command loop to process user input. Supported
     * commands include:
     * <ul>
     * <li>{@code move}: Lists all pieces with available legal moves and their possible destinations.</li>
     * <li>{@code move [row] [col]}: Attempts to move the piece at the specified (row, column) position.
     * If the piece has only one legal move, it is executed automatically. Multiple moves display options.</li>
     * <li>{@code move [fromRow] [fromCol] [toRow] [toCol]}: Directly moves a piece from the starting
     * position to the target position if the move is valid.</li>
     * <li>{@code restart}: Resets the game to its initial state and displays the new board.</li>
     * <li>{@code exit}: Terminates the game session.</li>
     * </ul>
     * The game loop continuously updates the board state, checks for win conditions (when the Cao Cao
     * block reaches the designated exit position), and provides user feedback for invalid inputs.
     * Victory is declared when the win condition is met, displaying the total moves used. The loop
     * persists until the user exits or wins the game.
     */
    public static void main(String[] args) {
            KlotskiGame game = new KlotskiGame();
            java.util.Scanner scanner = new java.util.Scanner(System.in);
    
            System.out.println("Welcome to Klotski Game!");
            System.out.println("Commands:");
            System.out.println("  move - Show pieces with legal moves");
            System.out.println("  move [row] [col] - Move piece at (row,col)");
            System.out.println("  restart - Reset the game");
            System.out.println("  exit - Quit the game");
            System.out.println("Current board:");
            System.out.println(game);
    
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
    
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Thanks for playing!");
                    break;
                } else if (input.equalsIgnoreCase("restart")) {
                    game.initialize();
                    System.out.println("Game restarted. Current board:");
                    System.out.println(game);
                } else if (input.startsWith("move")) {
                    try {
                        String[] parts = input.split(" ");
    
                        if (parts.length == 1) {
                            // Case: "move" - show all pieces with legal moves
                            boolean anyLegalMoves = false;
                            for (KlotskiPiece piece : game.getPieces()) {
                                int[] pos = {piece.position[0], piece.position[1]};
                                List<int[]> moves = game.getLegalMovesForPiece(pos);
                                if (!moves.isEmpty()) {
                                    anyLegalMoves = true;
                                    System.out.printf("Piece at (%d,%d) can move to:\n", pos[0], pos[1]);
                                    for (int[] move : moves) {
                                        System.out.printf("  -> (%d,%d)\n", move[0], move[1]);
                                    }
                                }
                            }
                            if (!anyLegalMoves) {
                                System.out.println("No pieces have legal moves.");
                            }
                        } else if (parts.length == 3) {
                            // Case: "move x y" - try to move piece at (x,y)
                            int row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            int[] position = {row, col};
    
                            KlotskiPiece piece = game.getPieceAt(position);
                            if (piece == null) {
                                System.out.println("No piece at (" + row + "," + col + ")");
                                continue;
                            }
    
                            List<int[]> moves = game.getLegalMovesForPiece(position);
                            if (moves.isEmpty()) {
                                System.out.println("No legal moves for piece at (" + row + "," + col + ")");
                            } else if (moves.size() == 1) {
                                // Single legal move - execute it automatically
                                int[] target = moves.get(0);
                                game.applyAction(position, target);
                                System.out.printf("Moved piece from (%d,%d) to (%d,%d)\n",
                                        row, col, target[0], target[1]);
                                System.out.println(game);
    
                                // Check win condition
                                KlotskiPiece cao = game.getPieces()[0];
                                if (cao.position[0] == 3 && cao.position[1] == 1) {
                                    System.out.println("Congratulations! You won in " + game.getMoveCount() + " moves!");
                                    System.out.println("Type 'restart' to play again or 'exit' to quit.");
                                }
                            } else {
                                // Multiple legal moves - show options
                                System.out.printf("Multiple moves possible for piece at (%d,%d):\n", row, col);
                                for (int[] move : moves) {
                                    System.out.printf("  -> (%d,%d)\n", move[0], move[1]);
                                }
                                System.out.println("Specify target position with 'move [fromRow] [fromCol] [toRow] [toCol]'");
                            }
                        } else if (parts.length == 5) {
                            // Case: "move fromRow fromCol toRow toCol" - direct move
                            int fromRow = Integer.parseInt(parts[1]);
                            int fromCol = Integer.parseInt(parts[2]);
                            int toRow = Integer.parseInt(parts[3]);
                            int toCol = Integer.parseInt(parts[4]);
    
                            if (game.isLegalMove(new int[]{fromRow, fromCol}, new int[]{toRow, toCol})) {
                                game.applyAction(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
                                System.out.printf("Moved piece from (%d,%d) to (%d,%d)\n",
                                        fromRow, fromCol, toRow, toCol);
                                System.out.println(game);
    
                                // Check win condition
                                if (game.isTerminal()) {
                                    System.out.println("Congratulations! You won in " + game.getMoveCount() + " moves!");
                                    System.out.println("Type 'restart' to play again or 'exit' to quit.");
                                }
                            } else {
                                System.out.println("Invalid move.");
                            }
                        } else {
                            System.out.println("Invalid command. Usage:");
                            System.out.println("  move - Show pieces with legal moves");
                            System.out.println("  move [row] [col] - Move piece at (row,col)");
                            System.out.println("  move [fromRow] [fromCol] [toRow] [toCol] - Direct move");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid coordinates. Please enter numbers for row and column.");
                    }
                } else {
                    System.out.println("Unknown command. Available commands:");
                    System.out.println("  move - Show pieces with legal moves");
                    System.out.println("  move [row] [col] - Move piece at (row,col)");
                    System.out.println("  restart - Reset the game");
                    System.out.println("  exit - Quit the game");
                }
            }
            scanner.close();
        }

    /**
     * Retrieves the {@link KlotskiPiece} from the internal array at the specified index.
     * The index corresponds to the position of the piece in the collection maintained
     * by the current context (e.g., a board or puzzle configuration).
     *
     * @param index the zero-based index of the piece to retrieve. Must be a valid index
     * within the bounds of the pieces array.
     * @return the {@link KlotskiPiece} located at the given index in the pieces array.
     */
    public KlotskiPiece getPiece(int index) {
            return(this.pieces[index]);
        }
}
