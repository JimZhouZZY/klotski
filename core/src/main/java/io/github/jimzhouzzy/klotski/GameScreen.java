// KNOWN ISSUES:
// 1. The move count is incorrect when the user dragged a piece 
//    across multiple grid.
// 2. Restart in an leveled (seedly random shuffeled) game won't
//    reset the game to the shuffeled state.

package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.KlotskiGame;
import io.github.jimzhouzzy.klotski.KlotskiSolver;
import io.github.jimzhouzzy.klotski.RectangleBlockActor;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class GameScreen extends ApplicationAdapter implements Screen {
    private static final String SAVE_FILE = "game_save.dat";

    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private float cellSize;
    private final int rows = 5;
    private final int cols = 4;
    private List<RectangleBlockActor> blocks; // List of all blocks
    private Group congratulationsGroup;

    private int[][] autoMoves;
    private int autoStep;
    public boolean isAutoSolving;
    private boolean isTerminal = false;

    private List<int[][]> moveHistory; // Stores the history of moves
    private int currentMoveIndex; // Tracks the current move in the history

    private float elapsedTime; // Tracks the elapsed time in seconds
    private Label timerLabel; // Label to display the timer
    private Label timerLabelCongrats; // Label to display the timer
    private Label movesLabel; // Label to display the total moves

    private KlotskiGame game; // Reference to the game logic
    private Klotski klotski; // Reference to the main game class
    private List<String> solution; // Stores the current solution
    private int solutionIndex; // Tracks the current step in the solution

    private TextButton autoButton;

    private Timer.Task autoSaveTask;

    private boolean isAttackMode; // Flag to track if the game is in 3min-Attack mode
    private float attackModeTimeLimit = 3 * 60; // 3 minutes in seconds
    private Label congratsLabel;

    public GameScreen(final Klotski klotski) {
        this.klotski = klotski;
    }

    public void setGameMode(boolean isAttackMode) {
        this.isAttackMode = isAttackMode;
    }

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("skins/default/skin/uiskin.json"));
        shapeRenderer = new ShapeRenderer();

        blocks = new ArrayList<>(); // Initialize the list of blocks

        // Calculate cellSize dynamically based on the screen size
        cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);

        // Initialize the game logic
        game = new KlotskiGame();

        // Create a root table for layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Left side: Grid container
        Table gridTable = new Table();
        gridTable.setFillParent(false);

        // Right side: Button column
        Table buttonTable = new Table();
        String[] buttonNames = { "Restart", "Hint", "Auto", "Undo", "Redo", "Save", "Load", "Exit" };

        // Add buttons with listeners
        for (String name : buttonNames) {
            TextButton button = new TextButton(name, skin);
            buttonTable.add(button).width(100).pad(10);
            buttonTable.row();

            if (name.equals("Auto")) {
                autoButton = button;
            }

            // Add functionality to each button
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    switch (name) {
                        case "Restart":
                            handleRestart(game);
                            break;
                        case "Hint":
                            handleHint(game);
                            break;
                        case "Auto":
                            handleAutoSolve(game, button);
                            break;
                        case "Undo":
                            handleUndo();
                            break;
                        case "Redo":
                            handleRedo();
                            break;
                        case "Save":
                            handleSave();
                            break;
                        case "Load":
                            handleLoad();
                            break;
                        case "Exit":
                            handleExit();
                            break;
                    }
                }
            });
        }

        // Add grid and buttons to the root table
        rootTable.add(gridTable).expand().fill().left().padRight(20); // Grid on the left
        rootTable.add(buttonTable).top().right(); // Buttons on the right

        // Create blocks based on the game pieces
        for (KlotskiGame.KlotskiPiece piece : game.getPieces()) {
            // Convert logical position to graphical position
            float x = piece.position[1] * cellSize; // Column to x-coordinate
            float y = (rows - piece.position[0] - piece.height) * cellSize; // Invert y-axis and adjust for height
            float width = piece.width * cellSize;
            float height = piece.height * cellSize;

            // Create a block with a unique color for each piece
            Color color = getColorForPiece(piece.id);
            RectangleBlockActor block = new RectangleBlockActor(x, y, width, height, color, piece.id, game);

            blocks.add(block); // Add block to the list
            stage.addActor(block); // Add block to the stage
        }

        // Create the congratulations screen
        createCongratulationsScreen();

        moveHistory = new ArrayList<>();
        currentMoveIndex = -1; // No moves yet

        // Add timer label under the buttons
        timerLabel = new Label("Time: 00:00", skin);
        timerLabel.setFontScale(1.2f);
        timerLabel.setAlignment(Align.center);
        buttonTable.add(timerLabel).width(100).pad(10).row();

        // Add moves label under the timer
        movesLabel = new Label("Moves: 0", skin);
        movesLabel.setFontScale(1.2f);
        movesLabel.setAlignment(Align.center);
        buttonTable.add(movesLabel).width(100).pad(10).row();

        // Reset elapsed time
        elapsedTime = 0;

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ESCAPE:
                        handleExit(); // Handle exit when ESC is pressed
                        return true;
                    case Input.Keys.R:
                        handleRestart(game); // Handle restart when R is pressed
                        return true;
                    case Input.Keys.H:
                        handleHint(game); // Handle hint when H is pressed
                        return true;
                    case Input.Keys.U:
                        handleUndo(); // Handle undo when U is pressed
                        return true;
                    case Input.Keys.Y:
                        handleRedo(); // Handle redo when Y is pressed
                        return true;
                    case Input.Keys.A:
                        handleAutoSolve(game, autoButton); // Handle auto-solving when A is pressed
                        return true;
                    case Input.Keys.SPACE:
                        // Handle space key for auto-solving
                        if (isAutoSolving) {
                            stopAutoSolving(); // Stop auto-solving if already active
                            autoButton.setText("Auto"); // Change button text back to "Auto"
                        } else {
                            handleAutoSolve(game, autoButton); // Start auto-solving
                        }
                        return true;
                    case Input.Keys.ENTER:
                        // Handle enter key for auto-solving
                        if (isAutoSolving) {
                            stopAutoSolving(); // Stop auto-solving if already active
                            autoButton.setText("Auto"); // Change button text back to "Auto"
                        } else {
                            handleAutoSolve(game, autoButton); // Start auto-solving
                        }
                        return true;
                    case Input.Keys.LEFT:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[] {0, -1});
                        return true;
                    case Input.Keys.UP:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[] {-1, 0});
                        return true;
                    case Input.Keys.RIGHT:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[] {0, 1});
                        return true;
                    case Input.Keys.DOWN:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[] {1, 0});
                        return true;
                }
                return false;
            }
        });

        broadcastGameState();
    }

    private void handleArrowKeys(int[] direction) {
        List<int[][]> legalMoves = game.getLegalMovesByDirection(direction);
        if (legalMoves.isEmpty()) {
            return;
        }
        int[][] move = legalMoves.get(0);
        int fromRow = move[0][0];
        int fromCol = move[0][1];
        int toRow = move[1][0];
        int toCol = move[1][1];
        for (RectangleBlockActor block : blocks) {
            KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
            if (piece.getRow() == fromRow && piece.getCol() == fromCol) {
                float targetX = toCol * cellSize;
                float targetY = (rows - toRow - piece.height) * cellSize; // Invert y-axis
                block.addAction(Actions.sequence(
                    Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                    Actions.run(() -> {
                        game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                        piece.setPosition(new int[] { toRow, toCol });
                        recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                        isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                    })));
                break;
            }
        }
    }

    // Helper method to assign colors to pieces
    private Color getColorForPiece(int id) {
        switch (id) {
            case 0:
                return Color.RED; // Cao Cao
            case 1:
                return Color.BLUE; // Guan Yu
            case 2:
            case 3:
            case 4:
            case 5:
                return Color.GREEN; // Generals
            case 6:
            case 7:
            case 8:
            case 9:
                return Color.YELLOW; // Soldiers
            default:
                return Color.GRAY; // Default color
        }
    }

    public List<RectangleBlockActor> getBlocks() {
        return blocks;
    }

    public float[] getBoundaryForBlock(RectangleBlockActor block) {
        float minX = 0;
        float minY = 0;
        float maxX = cols * cellSize - block.getWidth();
        float maxY = rows * cellSize - block.getHeight();

        for (RectangleBlockActor other : blocks) {
            if (other == block) {
                continue;
            }

            float x = block.getX();
            float y = block.getY();
            float width = block.getWidth();
            float height = block.getHeight();

            // Check horizontal overlap
            if (y + height > other.getY() && other.getY() + other.getHeight() > y) {
                // Block is to the right of the other block
                if (x >= other.getX() + other.getWidth()) {
                    minX = Math.max(minX, other.getX() + other.getWidth());
                }
                // Block is to the left of the other block
                if (x + width <= other.getX()) {
                    maxX = Math.min(maxX, other.getX() - block.getWidth());
                }

            }

            // Check vertical overlap
            if (x + width > other.getX() && other.getX() + other.getWidth() > x) {
                // Block is above the other block
                if (y >= other.getY() + other.getHeight()) {
                    minY = Math.max(minY, other.getY() + other.getHeight());
                }
                // Block is below the other block
                if (y + height <= other.getY()) {
                    maxY = Math.min(maxY, other.getY() - block.getHeight());
                }
            }
        }

        return new float[] { minX, maxX, minY, maxY };
    }

    private void updateBlocksFromGame(KlotskiGame game) {
        for (RectangleBlockActor block : blocks) {
            KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
            float x = piece.position[1] * cellSize; // Column to x-coordinate
            float y = (rows - piece.position[0] - piece.height) * cellSize; // Invert y-axis and adjust for height
            block.setPosition(x, y);
        }
    }

    @Override
    public void render(float delta) {
        // Update cell size dynamically to ensure the grid stays square
        cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);

        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle 3min-Attack mode
        if (isAttackMode && elapsedTime >= attackModeTimeLimit) {
            showLosingScreen(); // Show losing screen if time limit is exceeded
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
            return;
        }

        // Check if the game is in a terminal state (win condition)
        if (this.isTerminal) {
            showCongratulationsScreen(); // Show the congratulations screen
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
            return;
        }

        // Update elapsed time
        elapsedTime += Gdx.graphics.getDeltaTime();

        // Format elapsed time as MM:SS
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

        // Draw grid lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        for (int row = 0; row <= rows; row++) {
            float y = row * cellSize;
            shapeRenderer.line(0, y, cols * cellSize, y);
        }

        for (int col = 0; col <= cols; col++) {
            float x = col * cellSize;
            shapeRenderer.line(x, 0, x, rows * cellSize);
        }

        shapeRenderer.end();

        // Auto-solving logic
        if (isAutoSolving && solution != null && solutionIndex < solution.size()) {
            // Check if the previous animation has finished
            boolean allAnimationsFinished = true;
            for (RectangleBlockActor block : blocks) {
                if (block.getActions().size > 0) {
                    allAnimationsFinished = false;
                    break;
                }
            }

            if (allAnimationsFinished) {
                // Parse the current move
                String move = solution.get(solutionIndex);
                System.out.println("Auto-solving step: " + move);

                String[] parts = move.split(" ");
                int fromIndex = move.indexOf(" from ");
                String fromPart = move.substring(fromIndex + 6, move.indexOf(" to "));
                String toPart = move.substring(move.indexOf(" to ") + 4);

                int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
                int fromCol = Integer.parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
                int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
                int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));
                System.out.println(game.toString());

                // Find the block at the starting position
                for (RectangleBlockActor block : blocks) {
                    KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                    if (piece.position[0] == fromRow && piece.position[1] == fromCol) {
                        // Animate the block's movement to the target position
                        float targetX = toCol * cellSize;
                        float targetY = (rows - toRow - piece.height) * cellSize; // Invert y-axis
                        block.addAction(Actions.sequence(
                                Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                                Actions.run(() -> {
                                    // Update game logic after animation
                                    // TODO: find a more robust way, letting applyAction to handle at ease
                                    // Maybe we shall add another variable to show whether we finished the update
                                    game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                    piece.setPosition(new int[] { toRow, toCol });
                                    recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                    solutionIndex++;
                                    this.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                                    broadcastGameState();
                                })));
                        break;
                    }
                }

                if (solutionIndex >= solution.size()) {
                    isAutoSolving = false; // Stop auto-solving when all steps are completed
                }
            }
        }

        // Update and draw the stage
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();

        // Cancel the auto-save task when disposing the screen
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
    }

    private void handleRestart(KlotskiGame game) {
        // Stop auto-solving if active
        stopAutoSolving();

        // Reset the timer
        elapsedTime = 0;
        timerLabel.setText("Time: 00:00");
        currentMoveIndex = -1;
        movesLabel.setText("Moves: 0");

        // Stop all animations
        for (RectangleBlockActor block : blocks) {
            block.clearActions(); // Clear all actions for this block
        }

        // Reset the game logic
        game.initialize();

        // Update the blocks to match the game state
        updateBlocksFromGame(game);

        // Reset terminal state
        isTerminal = false;

        broadcastGameState();

        System.out.println("Game restarted.");
    }

    private void handleHint(KlotskiGame game) {
        // Get the solution from the solver
        List<String> solution = KlotskiSolver.solve(game);

        if (solution != null && !solution.isEmpty()) {
            // Parse the first move from the solution
            String move = solution.get(0);
            System.out.println("Hint: " + move);

            String[] parts = move.split(" ");
            int fromIndex = move.indexOf(" from ");
            String fromPart = move.substring(fromIndex + 6, move.indexOf(" to "));
            String toPart = move.substring(move.indexOf(" to ") + 4);

            int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
            int fromCol = Integer.parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
            int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
            int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));

            // Find the block at the starting position
            for (RectangleBlockActor block : blocks) {
                KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                System.out.printf("Block ID: %d, Position: (%d, %d)\n", piece.id, piece.position[0], piece.position[1]);
                if (piece.position[0] == fromRow && piece.position[1] == fromCol) {
                    // Animate the block's movement to the target position
                    float targetX = toCol * cellSize;
                    float targetY = (rows - toRow - piece.height) * cellSize; // Invert y-axis
                    block.addAction(Actions.sequence(
                            Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                            Actions.run(() -> {
                                // Update game logic after animation
                                game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                piece.setPosition(new int[] { toRow, toCol });
                                recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                this.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                                broadcastGameState();
                            })));
                    break;
                }
            }
            System.out.println(game.toString());
        } else {
            System.out.println("No solution found or no hint available.");
        }
    }

    private void handleAutoSolve(KlotskiGame game, TextButton autoButton) {
        if (isAutoSolving()) {
            stopAutoSolving(); // Stop auto-solving if already active
            autoButton.setText("Auto"); // Change button text back to "Auto"
        } else {
            solution = null; // Clear the previous solution
            solutionIndex = 0; // Reset the solution index
            List<String> newSolution = KlotskiSolver.solve(game); // Get the new solution

            if (newSolution != null && !newSolution.isEmpty()) {
                solution = newSolution; // Store the solution
                isAutoSolving = true; // Enable auto-solving mode
                autoButton.setText("Stop"); // Change button text to "Stop"
                System.out.println("Auto-solving started.");
            } else {
                System.out.println("No solution found.");
            }
        }
    }

    private void handleExit() {
        klotski.setScreen(klotski.mainScreen); // Switch back to the main menu
        dispose(); // Dispose of the current screen resources
    }

    public boolean isAutoSolving() {
        return isAutoSolving;
    }

    public void stopAutoSolving() {
        if (isAutoSolving) {
            isAutoSolving = false;
            System.out.println("Auto-solving stopped.");
            updateAutoButtonText(autoButton);
        }
    }

    private void createCongratulationsScreen() {
        // Create a group for the congratulations screen
        congratulationsGroup = new Group();

        // Set position of the group to the center of the stage
        float stageWidth = stage.getWidth();
        float stageHeight = stage.getHeight();
        congratulationsGroup.setPosition(stageWidth / 2f, stageHeight / 2f);

        // Ensure the group is drawn on top
        congratulationsGroup.setZIndex(Integer.MAX_VALUE);

        Table congratsTable = new Table();
        congratsTable.setFillParent(true);
        congratsTable.center(); // Ensure the table is centered

        // Add congratulatory message
        congratsLabel = new Label("Congratulations! You Win!", skin);
        congratsLabel.setFontScale(2); // Make the text larger
        congratsTable.add(congratsLabel).padBottom(20).row();

        // Add time usage placeholder
        Label timerLabelCongrats = new Label("Time: 00:00", skin); // Placeholder for time usage
        timerLabelCongrats.setFontScale(1.5f);
        congratsTable.add(timerLabelCongrats).padBottom(20).row();

        // Store the timeLabel for later updates
        this.timerLabelCongrats = timerLabelCongrats;

        // Add restart button
        TextButton restartButton = new TextButton("Restart", skin);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleRestart(game); // Restart the game
                congratulationsGroup.setVisible(false); // Hide the congratulations screen
            }
        });
        congratsTable.add(restartButton).width(150).height(50);

        // Add the table to the group
        congratulationsGroup.addActor(congratsTable);

        // Initially hide the group
        congratulationsGroup.setVisible(false);

        // Add the group to the stage
        stage.addActor(congratulationsGroup);
    }

    private void showCongratulationsScreen() {
        // Update the time label with the final elapsed time
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        timerLabelCongrats.setText(String.format("Time: %02d:%02d", minutes, seconds));
        congratsLabel.setText("Congratulations! You Win!");

        // Update the moves label with the total moves
        movesLabel.setText("Moves: " + (currentMoveIndex + 1));

        congratulationsGroup.setVisible(true); // Show the congratulations screen
    }

    private void showLosingScreen() {
        // Create a losing screen similar to the congratulations screen
        showCongratulationsScreen();
        congratsLabel.setText("Game Over! You Lose!");
    }

    private void updateAutoButtonText(TextButton autoButton) {
        if (isAutoSolving) {
            autoButton.setText("Stop");
        } else {
            autoButton.setText("Auto");
        }
    }

    public void recordMove(int[] from, int[] to) {
        // Remove any redo history if we are making a new move
        while (moveHistory.size() > currentMoveIndex + 1) {
            moveHistory.remove(moveHistory.size() - 1);
        }

        // Add the move to the history
        moveHistory.add(new int[][] { from, to });
        currentMoveIndex++;

        movesLabel.setText("Moves: " + (currentMoveIndex + 1));
    }

    private void handleUndo() {
        if (currentMoveIndex >= 0) {
            int[][] lastMove = moveHistory.get(currentMoveIndex);
            int[] from = lastMove[1]; // Reverse the move
            int[] to = lastMove[0];

            game.applyAction(from, to); // Apply the reverse move
            updateBlocksFromGame(game); // Update the blocks
            currentMoveIndex--; // Move back in history

            movesLabel.setText("Moves: " + (currentMoveIndex + 1));

            broadcastGameState();

            System.out.println("Undo performed: " + from[0] + "," + from[1] + " to " + to[0] + "," + to[1]);
        } else {
            System.out.println("No moves to undo.");
        }
    }

    private void handleRedo() {
        if (currentMoveIndex < moveHistory.size() - 1) {
            currentMoveIndex++;
            int[][] nextMove = moveHistory.get(currentMoveIndex);
            int[] from = nextMove[0];
            int[] to = nextMove[1];

            game.applyAction(from, to); // Apply the move
            updateBlocksFromGame(game); // Update the blocks

            movesLabel.setText("Moves: " + (currentMoveIndex + 1));

            broadcastGameState();

            System.out.println("Redo performed: " + from[0] + "," + from[1] + " to " + to[0] + "," + to[1]);
        } else {
            System.out.println("No moves to redo.");
        }
    }

    private String getSaveFileName() {
        String username = klotski.getLoggedInUser();
        if (username == null || username.isEmpty()) {
            return "guest_save.dat"; // Default save file for guests
        }
        return username + "_save.dat"; // Unique save file for each user
    }

    private void handleSave() {
        String saveFileName = getSaveFileName();
        File file = new File(Gdx.files.getLocalStoragePath(), saveFileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Save the positions of all pieces, move history, current move index, and elapsed time
            oos.writeObject(new ArrayList<>(List.of(game.getPieces())));
            oos.writeObject(moveHistory);
            oos.writeInt(currentMoveIndex);
            oos.writeFloat(elapsedTime);
            System.out.println("Game saved successfully for user: " + klotski.getLoggedInUser());
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    private void handleLoad() {
        String saveFileName = getSaveFileName();
        File file = new File(Gdx.files.getLocalStoragePath(), saveFileName);
        if (!file.exists()) {
            System.out.println("No save file found for user: " + klotski.getLoggedInUser());
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Restart the game
            handleRestart(game);

            // Load the game state
            List<KlotskiGame.KlotskiPiece> pieces = (List<KlotskiGame.KlotskiPiece>) ois.readObject();
            moveHistory = (List<int[][]>) ois.readObject();
            currentMoveIndex = ois.readInt();
            elapsedTime = ois.readFloat();

            // Update the game state
            game.setPieces(pieces);
            updateBlocksFromGame(game);
            movesLabel.setText("Moves: " + (currentMoveIndex + 1));

            // Update the timer label
            int minutes = (int) (elapsedTime / 60);
            int seconds = (int) (elapsedTime % 60);
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

            System.out.println("Game loaded successfully for user: " + klotski.getLoggedInUser());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
        }
    }

    public KlotskiGame getGame() {
        return game;
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);

        // Cancel the auto-save task when the screen is hidden
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        create();
        handleRestart(game);
        Gdx.input.setInputProcessor(stage);

        // Schedule auto-save every 30 seconds
        autoSaveTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                handleSave(); // Call the save method
                System.out.println("Game auto-saved.");
            }
        }, 30, 30); // Delay of 30 seconds, repeat every 30 seconds
    }

    public void randomShuffle(long seed) {
        game.randomShuffle(seed);
        updateBlocksFromGame(game);
    }

    public void randomShuffle() {
        game.randomShuffle();
        updateBlocksFromGame(game);
    }

    public void broadcastGameState() {
        String gameState = game.toString();
        klotski.getWebSocketServer().broadcastGameState(gameState);
    }
}