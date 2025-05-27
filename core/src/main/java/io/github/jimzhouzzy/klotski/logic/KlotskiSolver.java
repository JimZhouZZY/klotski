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
 * KlotskiSolver.java
 * 
 * This class implements a solver for the Klotski game using a breadth-first search (BFS) algorithm.
 * It explores all possible moves from the initial game state and finds the shortest solution.
 * The solution is represented as a list of moves, and the solver also provides statistics about
 * the search process, including the number of states examined and the time taken to find a solution.
 * 
 * @author JimZhouZZY
 * @version 1.19
 * @since 2025-5-25
 * @see {@link KlotskiGame}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: implement blocked pieces
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: refactor spectate screen to extend GameScreen
 * 2025-05-24: refactor spectate screen to extend GameScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-04-16: Login & Game Mode & Save-Load
 * 2025-04-13: feat: restart hint and congratulations
 * 2025-04-08: refactor to libgdx structure
 * 2025-04-08: Implemented KlotskiSovler
 */

package io.github.jimzhouzzy.klotski.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KlotskiSolver {
    private static class BoardState {
        final KlotskiGame.KlotskiPiece[] pieces;
        final String move;
        final BoardState parent;
        final String state;

        BoardState(KlotskiGame.KlotskiPiece[] pieces, String move, BoardState parent, String state) {
            this.pieces = deepCopyPieces(pieces);
            this.move = move;
            this.parent = parent;
            this.state = state;
        }

        /**
         * Creates a deep copy of the provided array of {@link KlotskiGame.KlotskiPiece} objects. Each piece in the
         * new array is instantiated as a new object with identical properties to the original, including
         * the piece's ID, name, abbreviation, dimensions (width and height), and current position.
         * This ensures that modifications to the copied array or its elements do not affect the original array
         * or its constituent pieces.
         *
         * @param original The array of {@link KlotskiGame.KlotskiPiece} objects to be deep-copied.
         * @return A new array containing newly instantiated {@link KlotskiGame.KlotskiPiece} objects with
         * the same properties as those in the original array.
         */
        private KlotskiGame.KlotskiPiece[] deepCopyPieces(KlotskiGame.KlotskiPiece[] original) {
                    KlotskiGame.KlotskiPiece[] copy = new KlotskiGame.KlotskiPiece[original.length];
                    for (int i = 0; i < original.length; i++) {
                        copy[i] = new KlotskiGame.KlotskiPiece(
                                original[i].id,
                                original[i].name,
                                original[i].abbreviation,
                                original[i].width,
                                original[i].height,
                                original[i].getPosition()
                                );
                    }
                    return copy;
                }

        /**
         * Compares this {@code BoardState} to the specified object for equality. Returns {@code true} if
         * the object is the same instance as this board state, or if it is another {@code BoardState}
         * instance with an equivalent internal state. Equality of the internal state is determined by
         * the {@code equals} method of the underlying {@code state} object.
         *
         * @param obj the object to compare for equality
         * @return {@code true} if the provided object is a {@code BoardState} with the same internal
         * state as this instance; {@code false} otherwise
         */
        @Override
                public boolean equals(Object obj) {
                    if (this == obj) return true;
                    if (!(obj instanceof BoardState)) return false;
                    BoardState other = (BoardState) obj;
                    if (state.equals(other.state)) {
                        return true;
                    }
                    return false;
                }

        /**
         * Computes the hash code for this Klotski game state based on the positions of all game pieces.
         * The hash code is generated by iterating over each piece in the {@code pieces} collection and
         * incorporating the x and y coordinates of each piece's position into the hash calculation.
         * This method uses the prime number31 as a multiplier to reduce hash collisions, following
         * common Java hash code generation practices. The order of the pieces in the collection affects
         * the final hash value. Overrides the {@link Object#hashCode()} method to provide a custom
         * implementation tailored to the Klotski game's state.
         */
        @Override
                public int hashCode() {
                    int result = 1;
                    for (KlotskiGame.KlotskiPiece piece : pieces) {
                        result = 31 * result + piece.position[0];
                        result = 31 * result + piece.position[1];
                    }
                    return result;
                }
    }

    /**
     * Solves the Klotski puzzle starting from the specified initial game configuration and returns
     * a sequence of moves leading to a solution. This method delegates to an overloaded solve method
     * while specifying that no piece is blocked from movement by default. A blocked piece (if specified)
     * would remain stationary during the solving process, but this implementation allows all pieces
     * to be moved freely.
     *
     * @param initialGame The starting configuration of the Klotski puzzle, including board state
     * and piece positions.
     * @return A list of strings representing the moves required to solve the puzzle, where each string
     * corresponds to a piece identifier followed by a direction (e.g., "A UP"). Returns an
     * empty list if no solution exists or if the initial configuration is invalid.
     */
    public static List<String> solve(KlotskiGame initialGame) {
            return solve(initialGame, -1); // Default to no blocked piece
        }

    /**
     * Solves the Klotski puzzle using a breadth-first search (BFS) algorithm to find the shortest solution.
     * This method explores all possible moves from the initial game state, excluding moves involving the piece
     * specified by {@code blockedId}. It tracks visited board states to avoid cycles and evaluates each valid
     * configuration until a solution is found. Statistics about the search process, including execution time,
     * number of solutions, and states examined, are printed to the console.
     *
     * @param initialGame The initial configuration of the Klotski game to solve.
     * @param blockedId The identifier of the puzzle piece that should not be moved during the solution process.
     * @return A list of strings representing the sequence of moves in the shortest solution,
     * or {@code null} if no solution exists. Each string corresponds to a move direction
     * (e.g., "UP", "DOWN") for a specific piece. The solution is reconstructed from the
     * terminal state back to the initial state using parent-child relationships tracked during BFS.
     */
    public static List<String> solve(KlotskiGame initialGame, int blockedId) {
            long startTime = System.currentTimeMillis();
            int statesExamined = 0;
            int solutionsFound = 0;
            int minMoves = Integer.MAX_VALUE;
            List<String> bestSolution = null;
    
            // Initialize BFS queue with starting state
            Deque<BoardState> queue = new ArrayDeque<>();
            Set<String> visited = new HashSet<>();
            Map<BoardState, BoardState> parentMap = new HashMap<>();
    
            BoardState initialState = new BoardState(initialGame.getPieces(), null, null, initialGame.toString());
            queue.add(initialState);
            visited.add(initialState.state);
    
            while (!queue.isEmpty()) {
                BoardState current = queue.poll();
                statesExamined++;
    
                // Check if current state is a solution
                if (isSolved(current)) {
                    solutionsFound++;
                    List<String> solution = reconstructSolution(current);
                    if (solution.size() < minMoves) {
                        minMoves = solution.size();
                        bestSolution = solution;
                    }
                    continue; // Continue searching for shorter solutions
                }
    
                // Generate all possible next moves
                for (BoardState nextState : generateNextStates(current, blockedId)) {
                    if (!visited.contains(nextState.state)) {
                        visited.add(nextState.state);
                        //System.out.println(visited.size());
                        parentMap.put(nextState, current);
                        queue.add(nextState);
                    } else {
                        //System.out.println("duplicated");
                    }
                }
            }
    
            long endTime = System.currentTimeMillis();
            System.out.printf("Finished in %.3f seconds\n", (endTime - startTime) / 1000.0);
            System.out.printf("  %d unique solutions\n", solutionsFound);
            System.out.printf("  %d moves in shortest solution\n", minMoves);
            System.out.printf("  %d board configurations examined\n", statesExamined);
            System.out.printf("  %d unique board states visited\n", visited.size());
    
            return bestSolution;
        }

    /**
     * Determines if the puzzle is in a solved state by checking whether Cao Cao (identified by ID0)
     * is positioned at the designated winning location (row3, column1). The method searches through
     * all pieces in the provided board state to locate Cao Cao and verifies if his current coordinates
     * match the target position. Returns {@code false} if Cao Cao is not found in the board state.
     *
     * @param state The current board state to evaluate for a winning configuration.
     * @return {@code true} if Cao Cao is present in the board state and positioned at row3, column1;
     * {@code false} otherwise.
     */
    private static boolean isSolved(BoardState state) {
            // Check if Cao Cao (id=0) is at the winning position (row=3, col=1)
            for (KlotskiGame.KlotskiPiece piece : state.pieces) {
                if (piece.id == 0) {
                    return piece.position[0] == 3 && piece.position[1] == 1;
                }
            }
            return false;
        }

    /**
     * Generates all possible next valid board states from the current state by moving each movable piece (excluding the blocked piece)
     * in all legal directions. For each valid move, a new {@link BoardState} is created with updated piece positions, a description
     * of the move, a reference to the parent state, and a string representation of the board configuration.
     *
     * @param state The current {@link BoardState} from which to generate successor states.
     * @param blockedId The ID of the piece that should be excluded from movement (blocked) in the current step.
     * @return A list of {@link BoardState} objects representing all valid next states reachable from the input state. Each state
     * includes details of the move applied, the parent state, and the new board configuration. Moves are generated by
     * checking legal positions for each piece (except the blocked piece) using a temporary game instance to validate moves.
     */
    private static List<BoardState> generateNextStates(BoardState state, int blockedId) {
            // System.out.println("Generating next states for blocked:" + blockedId);
            List<BoardState> nextStates = new ArrayList<>();
    
            // Create a temporary game to check moves
            KlotskiGame tempGame = new KlotskiGame();
            setGameState(tempGame, state);
    
            // Try moving each piece in all possible directions
            //System.out.println(tempGame);
            for (KlotskiGame.KlotskiPiece piece : state.pieces) {
                if (piece.id == blockedId) continue; // skip blocked piece
                int[] position = {piece.position[0], piece.position[1]};
                List<int[]> legalMoves = tempGame.getLegalMovesForPiece(position);
                // skip if no legal moves
                if (legalMoves == null || legalMoves.isEmpty()) {
                    //System.out.printf("No legal moves for piece %s at (%d,%d)\n", piece.name, position[0], position[1]);
                    continue;
                }
                for (int i=0; i < legalMoves.size(); i ++) {
                    //System.out.print(" ");
                    for (int j = 0; j < legalMoves.get(i).length; j ++){
                        //System.out.printf("%d", legalMoves.get(i)[j]);
                    }
                }
                //System.out.print("\n");
    
                for (int[] move : legalMoves) {
                    // Create new state with this move
                    KlotskiGame.KlotskiPiece[] newPieces = state.deepCopyPieces(state.pieces);
                    for (KlotskiGame.KlotskiPiece p : newPieces) {
                        if (p.id == piece.id) {
                            p.setPosition(new int[]{move[0], move[1]});
                            break;
                        }
                    }
    
                    String moveDesc = String.format("Move %s from (%d,%d) to (%d,%d)",
                            piece.name, position[0], position[1], move[0], move[1]);
    
                    char[][] board = new char[KlotskiGame.BOARD_HEIGHT][KlotskiGame.BOARD_WIDTH];
                    // Initialize empty board
                    for (int i = 0; i < KlotskiGame.BOARD_HEIGHT; i++) {
                        for (int j = 0; j < KlotskiGame.BOARD_WIDTH; j++) {
                            board[i][j] = '.';
                        }
                    }
    
                    // Place pieces on the board
                    for (KlotskiGame.KlotskiPiece newPiece : newPieces) {
                        int[] pos = newPiece.getPosition();
                        // check if the piece is within bounds
                        if (pos[0] < 0 || pos[1] < 0 || pos[0] >= KlotskiGame.BOARD_HEIGHT || pos[1] >= KlotskiGame.BOARD_WIDTH) {
                            continue; // Skip pieces that are out of bounds
                        }
                        for (int i = 0; i < newPiece.height; i++) {
                            for (int j = 0; j < newPiece.width; j++) {
                                if (pos[0] + i < KlotskiGame.BOARD_HEIGHT && pos[1] + j < KlotskiGame.BOARD_WIDTH) {
                                    board[pos[0] + i][pos[1] + j] = newPiece.abbreviation;
                                }
                            }
                        }
                    }
    
                    // Build the string representation
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < KlotskiGame.BOARD_HEIGHT; i++) {
                        for (int j = 0; j < KlotskiGame.BOARD_WIDTH; j++) {
                            sb.append(board[i][j]).append(' ');
                        }
                        sb.append('\n');
                    }
                    nextStates.add(new BoardState(newPieces, moveDesc, state, sb.toString()));
                }
            }
    
            return nextStates;
        }

    /**
     * Sets the game state by updating the positions of all pieces in the provided {@link KlotskiGame}
     * to match the positions specified in the given {@link BoardState}. This method iterates through
     * each piece in the game and sets its position to the corresponding piece's position in the
     * {@link BoardState}. The order of the pieces in the game and the state must align for accurate
     * synchronization. This method directly modifies the positions of the game's pieces in place.
     *
     * @param game the {@link KlotskiGame} instance whose piece positions will be updated
     * @param state the {@link BoardState} containing the target positions for each piece in the game
     */
    private static void setGameState(KlotskiGame game, BoardState state) {
            KlotskiGame.KlotskiPiece[] currentPieces = game.getPieces();
            for (int i = 0; i < currentPieces.length; i++) {
                currentPieces[i].setPosition(state.pieces[i].getPosition());
            }
        }

    /**
     * Reconstructs a sequence of moves leading to the provided {@code BoardState} by traversing
     * backward through parent states. Moves are added in reverse chronological order to the solution
     * list, resulting in a list ordered from the earliest move to the latest. If the input state or
     * any ancestor's move is {@code null}, traversal stops and the accumulated moves are returned.
     *
     * @param state the terminal {@link BoardState} from which to reconstruct the solution path
     * @return a {@link List} of strings representing moves in chronological order (earliest to latest),
     * or an empty list if no valid moves are found
     */
    private static List<String> reconstructSolution(BoardState state) {
            List<String> solution = new ArrayList<>();
            while (state != null && state.move != null) {
                solution.add(0, state.move); // Add to beginning to reverse order
                state = state.parent;
            }
            return solution;
        }

    /**
     * Prints the provided solution steps in a formatted manner or indicates that no solution was found.
     * If the input list is {@code null}, a message indicating no solution is printed. Otherwise, the method
     * displays the total number of moves followed by each step of the solution, numbered sequentially starting
     * from1. Each step is aligned with its corresponding number in a right-justified three-digit format.
     *
     * @param solution The list of strings representing the solution steps, which may be {@code null}.
     * If non-null, each element in the list corresponds to a single step in the solution.
     */
    public static void printSolution(List<String> solution) {
            if (solution == null) {
                System.out.println("No solution found!");
                return;
            }
    
            System.out.println("\nSolution (in " + solution.size() + " moves):");
            for (int i = 0; i < solution.size(); i++) {
                System.out.printf("%3d. %s\n", i + 1, solution.get(i));
            }
        }

    /**
     * The main entry point for the Klotski game solver application. This method initializes a Klotski game,
     * prints the initial board state, attempts to find a solution path using the {@code solve} method, and
     * prints the solution steps. Optionally, it replays the solution step-by-step, applying each move from
     * the solution list to the game board, displaying the updated state after each move with a1-second delay
     * between steps. Moves are parsed from strings in the format "Move [pieceName] from (row,col) to (row,col)",
     * extracting the piece name and coordinates to execute the corresponding game actions.
     *
     * @param args Command-line arguments (not used in this implementation).
     */
    public static void main(String[] args) {
            KlotskiGame game = new KlotskiGame();
            System.out.println("Initial board:");
            System.out.println(game);
    
            System.out.println("\nSolving...");
            List<String> solution = solve(game);
            printSolution(solution);
    
            // Optional: Replay the solution
            if (solution != null) {
                System.out.println("\nReplaying solution:");
                game.initialize();
                System.out.println(game);
                for (int i = 0; i < solution.size(); i++) {
                    System.out.printf("\n%d. %s\n", i + 1, solution.get(i));
                    // Parse and execute the move
                    String move = solution.get(i);
                    String[] parts = move.split(" ");
                    // Extract piece name (dynamic length)
                    int fromIndex = move.indexOf(" from ");
                    String pieceName = move.substring(5, fromIndex); // "Move " is 5 chars
    
                    // Extract "from" and "to" parts
                    String fromPart = move.substring(fromIndex + 6, move.indexOf(" to ")); // " from " is 6 chars
                    String toPart = move.substring(move.indexOf(" to ") + 4); // " to " is 4 chars
    
                    // Parse coordinates: "(row,col)" -> row, col
                    int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
                    int fromCol = Integer.parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
                    int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
                    int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));
    
                    game.applyAction(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
                    System.out.println(game);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
}
