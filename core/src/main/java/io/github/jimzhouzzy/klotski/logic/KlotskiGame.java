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
 * @version 1.17
 * @since 2025-5-25
 * 
 * Change log:
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

        public String getName() { return name; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getCount() { return count; }
        public char getAbbreviation() { return abbreviation; }

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

        public int[] getPosition() {
            return position.clone();
        }

        public int getRow() {
            return position[0];
        }

        public int getCol() {
            return position[1];
        }

        public void setPosition(int[] position) {
            this.position = position.clone();
        }

        public int[] getShape() {
            return new int[]{width, height};
        }

        public int getSize() {
            return width * height;
        }

        @Override
        public String toString() {
            return String.format("%s (ID: %d, Position: [%d,%d], Size: %dx%d)",
                    name, id, position[0], position[1], width, height);
        }
    }

    public KlotskiPiece[] pieces;
    private int moveCount;
    public static final int BOARD_WIDTH = 4;
    public static final int BOARD_HEIGHT = 5;

    public KlotskiGame() {
        initialize();
    }

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

    public boolean isTerminal() {
        KlotskiPiece piece = getPieceAtPrecise(new int[] {3, 1});
        if (piece != null && (piece.id == 0 || "Cao Cao".equals(piece.name))) {
            return true;
        }
        return false;
    }

    public static boolean overlaps(KlotskiPiece piece, int[] position, int width, int height) {
        return !(position[0] >= piece.position[0] + piece.height ||
                position[0] + height <= piece.position[0] ||
                position[1] >= piece.position[1] + piece.width ||
                position[1] + width <= piece.position[1]);
    }

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

    private KlotskiPiece getPieceAtPrecise(int[] position) {
        for (KlotskiPiece piece : pieces) {
            if (position[0] == piece.position[0] &&
                position[1] == piece.position[1]) {
                return piece;
            }
        }
        return null;
    }

    public List<int[]> getLegalMovesForPiece(int[] position) {
        List<int[]> legalMoves = new ArrayList<>();
        KlotskiPiece piece = getPieceAt(position);
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

    public void randomShuffle() {
        long seed = System.currentTimeMillis(); // Use the current time as the seed
        randomShuffle(seed);
    }

    private int coordinateToIndex(int[] coordinate) {
        return coordinate[0] * BOARD_WIDTH + coordinate[1];
    }

    private int[] indexToCoordinate(int index) {
        return new int[]{index / BOARD_WIDTH, index % BOARD_WIDTH};
    }

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
                    if (pos[0] + i < BOARD_HEIGHT && pos[1] + j < BOARD_WIDTH) {
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

    public KlotskiPiece[] getPieces() {
        return pieces.clone();
    }

    public int getMoveCount() {
        return moveCount;
    }

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

    public KlotskiPiece getPiece(int index) {
        return(this.pieces[index]);
    }
}
