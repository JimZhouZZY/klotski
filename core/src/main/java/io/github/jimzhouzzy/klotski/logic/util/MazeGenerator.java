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
 * MazeGenerator.java
 * 
 * This class generates a random maze using a depth-first search algorithm.
 * The maze is represented as a 1D array where 0 = wall and 1 = path.
 * 
 * @author JimZhouZZY
 * @version 1.7
 * @since 2025-5-26
 * @see {@link https://en.wikipedia.org/wiki/Maze_generation_algorithm#Randomized_depth-first_search}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: Enhance GameScreen block color
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: Klotzki game
 */

package io.github.jimzhouzzy.klotski.logic.util;

import java.util.Random;
import java.util.Stack;

public class MazeGenerator {
    /**
     * Generates a randomized maze of the specified dimensions using a maze-generation algorithm.
     * The maze is represented as a2D array encoded in a flattened1D integer array, where each
     * cell contains bitmask values indicating the presence of walls (e.g., NORTH_WALL, EAST_WALL, etc.).
     * The maze is guaranteed to have a valid path between all reachable cells and is randomized using
     * the current system time as the seed for variation across calls.
     *
     * @param rows The number of rows in the maze. Must be a positive integer.
     * @param cols The number of columns in the maze. Must be a positive integer.
     * @return A flattened integer array representing the maze grid, where each element corresponds
     * to a cell's wall configuration. The array length is {@code rows * cols}.
     */
    public static int[] generateMaze(int rows, int cols) {
            return generateMaze(rows, cols, System.currentTimeMillis());
        }

    /**
     * Generates a perfect maze of specified dimensions using a randomized depth-first search algorithm.
     * The maze is represented as a1D array where0 indicates walls and1 indicates traversable paths.
     * The maze guarantees a single unique path between any two cells and includes an entrance in the
     * first row and an exit in the last row, both placed randomly on odd-indexed columns where possible.
     * The algorithm uses a provided seed for deterministic maze generation.
     *
     * @param rows The number of rows in the maze grid (must be a positive integer).
     * @param cols The number of columns in the maze grid (must be a positive integer).
     * @param seed The seed value used to initialize the random number generator for reproducible mazes.
     * @return A1D array of size {@code rows * cols} containing0s (walls) and1s (paths),
     * with the entrance at index {@code startRow * cols + startCol} and the exit
     * at index {@code (rows -1) * cols + exitCol}.
     */
    public static int[] generateMaze(int rows, int cols, long seed) {
            // 0 = wall, 1 = path
            int[] map = new int[rows * cols];
            boolean[][] visited = new boolean[rows][cols];
            Random rand = new Random(seed);
    
            // Initialize all cells as walls
            for (int i = 0; i < map.length; i++) {
                map[i] = 0;
            }
    
            // Directions: up, right, down, left (dx, dy pairs)
            int[] dx = {0, 1, 0, -1};
            int[] dy = {-1, 0, 1, 0};
    
            // Start from a random cell in the first row (ensure it's odd for wall placement)
            int startCol = rand.nextInt(cols / 2) * 2 + 1;
            if (startCol >= cols) startCol = cols - 1;
            int startRow = 0;
            
            Stack<int[]> stack = new Stack<>();
            stack.push(new int[]{startRow, startCol});
            visited[startRow][startCol] = true;
            map[startRow * cols + startCol] = 1;
    
            while (!stack.isEmpty()) {
                int[] curr = stack.peek();
                int y = curr[0], x = curr[1];
    
                // Randomize directions
                int[] dirs = {0, 1, 2, 3};
                for (int i = 3; i > 0; i--) {
                    int j = rand.nextInt(i + 1);
                    int tmp = dirs[i];
                    dirs[i] = dirs[j];
                    dirs[j] = tmp;
                }
    
                boolean moved = false;
                for (int d = 0; d < 4; d++) {
                    // Move 2 cells in each direction
                    int nx = x + 2 * dx[dirs[d]];
                    int ny = y + 2 * dy[dirs[d]];
    
                    // Check if the next cell is within bounds and not visited
                    if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && !visited[ny][nx]) {
                        // Carve path through the wall between current and next cell
                        int wallX = x + dx[dirs[d]];
                        int wallY = y + dy[dirs[d]];
                        map[wallY * cols + wallX] = 1;
                        visited[wallY][wallX] = true;
    
                        // Mark the new cell as path
                        map[ny * cols + nx] = 1;
                        visited[ny][nx] = true;
                        stack.push(new int[]{ny, nx});
                        moved = true;
                        break;
                    }
                }
                if (!moved) stack.pop();
            }
    
            // Ensure entrance and exit exist
            map[startRow * cols + startCol] = 1; // Entrance
            int exitCol = rand.nextInt(cols / 2) * 2 + 1;
            if (exitCol >= cols) exitCol = cols - 1;
            map[(rows - 1) * cols + exitCol] = 1; // Exit
    
            return map;
        }

    // Helper method to print the maze (for testing)
    /**
     * Prints a2D maze representation based on a1D integer array. The maze is formatted such that
     * each cell is represented by two characters: "##" for walls (array value not equal to1) and
     * two spaces for open paths (array value equal to1). The maze is printed row by row, with each
     * row starting on a new line.
     *
     * @param map The1D array representing the maze, where each element corresponds to a cell in
     * row-major order (row-by-row). A value of1 indicates an open path; any other value
     * indicates a wall.
     * @param rows The number of rows in the maze grid.
     * @param cols The number of columns in the maze grid.
     */
    public static void printMaze(int[] map, int rows, int cols) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    System.out.print(map[r * cols + c] == 1 ? "  " : "##");
                }
                System.out.println();
            }
        }

    /** * Entry point for generating and printing a maze. This method initializes the dimensions
     * of the maze (rows and columns) with default values, ensuring they are odd numbers to
     * facilitate proper wall placement. It generates the maze structure using the {@code generateMaze}
     * method and prints the resulting maze to the console via the {@code printMaze} method.
     *
     * @param args Command-line arguments (not used in this implementation).
     *
     * @see #generateMaze(int, int)
     * @see #printMaze(int[], int, int) */
    public static void main(String[] args) {
            int rows = 21; // Should be odd for proper wall placement
            int cols = 21; // Should be odd for proper wall placement
            int[] maze = generateMaze(rows, cols);
            printMaze(maze, rows, cols);
        }
}
