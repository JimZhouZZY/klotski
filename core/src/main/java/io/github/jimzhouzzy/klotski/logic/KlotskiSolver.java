/**
 * KlotskiSolver.java
 * 
 * This class implements a solver for the Klotski game using a breadth-first search (BFS) algorithm.
 * It explores all possible moves from the initial game state and finds the shortest solution.
 * The solution is represented as a list of moves, and the solver also provides statistics about
 * the search process, including the number of states examined and the time taken to find a solution.
 * 
 * @author JimZhouZZY
 * @version 1.9
 * @since 2025-5-25
 * @see {@link KlotskiGame}
 * 
 * Change log:
 * 2025-05-25: generate change log
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

    public static List<String> solve(KlotskiGame initialGame) {
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
            for (BoardState nextState : generateNextStates(current)) {
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

    private static boolean isSolved(BoardState state) {
        // Check if Cao Cao (id=0) is at the winning position (row=3, col=1)
        for (KlotskiGame.KlotskiPiece piece : state.pieces) {
            if (piece.id == 0) {
                return piece.position[0] == 3 && piece.position[1] == 1;
            }
        }
        return false;
    }

    private static List<BoardState> generateNextStates(BoardState state) {
        List<BoardState> nextStates = new ArrayList<>();

        // Create a temporary game to check moves
        KlotskiGame tempGame = new KlotskiGame();
        setGameState(tempGame, state);

        // Try moving each piece in all possible directions
        //System.out.println(tempGame);
        for (KlotskiGame.KlotskiPiece piece : state.pieces) {
            //System.out.print(piece);
            int[] position = {piece.position[0], piece.position[1]};
            List<int[]> legalMoves = tempGame.getLegalMovesForPiece(position);
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

    private static void setGameState(KlotskiGame game, BoardState state) {
        KlotskiGame.KlotskiPiece[] currentPieces = game.getPieces();
        for (int i = 0; i < currentPieces.length; i++) {
            currentPieces[i].setPosition(state.pieces[i].getPosition());
        }
    }

    private static List<String> reconstructSolution(BoardState state) {
        List<String> solution = new ArrayList<>();
        while (state != null && state.move != null) {
            solution.add(0, state.move); // Add to beginning to reverse order
            state = state.parent;
        }
        return solution;
    }

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
