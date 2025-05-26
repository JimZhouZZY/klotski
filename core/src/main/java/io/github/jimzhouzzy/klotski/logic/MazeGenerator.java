package io.github.jimzhouzzy.klotski.logic;

import java.util.Random;
import java.util.Stack;

public class MazeGenerator {
    public static int[] generateMaze(int rows, int cols) {
        return generateMaze(rows, cols, System.currentTimeMillis());
    }

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
    public static void printMaze(int[] map, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                System.out.print(map[r * cols + c] == 1 ? "  " : "##");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int rows = 21; // Should be odd for proper wall placement
        int cols = 21; // Should be odd for proper wall placement
        int[] maze = generateMaze(rows, cols);
        printMaze(maze, rows, cols);
    }
}