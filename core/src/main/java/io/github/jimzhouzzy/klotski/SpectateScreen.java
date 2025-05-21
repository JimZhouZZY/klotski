// TODO: Update or refactor this code to enheritage the GameScreen.java

package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
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
import io.github.jimzhouzzy.klotski.KlotskiGame.KlotskiPiece;
import io.github.jimzhouzzy.klotski.KlotskiSolver;
import io.github.jimzhouzzy.klotski.RectangleBlockActor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;

import java.io.*;
import java.net.http.WebSocket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SpectateScreen extends ApplicationAdapter implements Screen {
    private final ConfigPathHelper configPathHelper = new ConfigPathHelper();
    private final String SAVE_FILE = configPathHelper.getConfigFilePath("Klotski", "game_save.dat");

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
    public boolean isTerminal = false;

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
    private String username;
    private GameWebSocketClient webSocketClient;

    public SpectateScreen(final Klotski klotski, String username, GameWebSocketClient webSocketClient) {
        this.klotski = klotski;
        this.username = username;
        this.webSocketClient = webSocketClient;
        connectWebSocket();
    }

    public void setGameMode(boolean isAttackMode) {
        this.isAttackMode = isAttackMode;
    }

    private void connectWebSocket() {
        System.out.println("SpectateScreen is trying to connect the websocket");
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            System.err.println("WebSocket client is not connected.");
            return;
        }

        webSocketClient.setOnMessageListener(message -> {
            System.out.println("Spec Message from server: " + message);
            if (message.startsWith("Board state updated:") && message.contains(username + ":")) {
                String state = message
                        .replace("Board state updated:", "")
                        .replace(username + ":", "")
                        .trim();

                // Parse the board state
                System.out.println("Received and trimmed board state: " + state);
                game.fromString(state);
                updateBlocksFromGame(game);
            }
        });
    }

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        klotski.dynamicBoard.setStage(stage);

        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));
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
            button.getLabel().setFontScale(0.5f);
            buttonTable.add(button).height(30).width(100).pad(10);
            buttonTable.row();

            if (name.equals("Auto")) {
                autoButton = button;
            }

            // Add functionality to each button
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();;
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
                            if (!klotski.isOfflineMode())
                                handleSave(false);
                            else 
                                handleLocalSave();
                            break;
                        case "Load":
                            if (!klotski.isOfflineMode())
                                handleLoad();
                            else
                                handleLocalLoad();
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
            block.enableTouch = false; // Disable touch events for blocks

            blocks.add(block); // Add block to the list
            stage.addActor(block); // Add block to the stage
        }

        // Create the congratulations screen
        createCongratulationsScreen();

        moveHistory = new ArrayList<>();
        currentMoveIndex = -1; // No moves yet

        // Add timer label under the buttons
        Label.LabelStyle timeLabelStyle;
        if (klotski.klotskiTheme == KlotskiTheme.LIGHT)
            timeLabelStyle = skin.get("default", Label.LabelStyle.class);
        else
            timeLabelStyle = skin.get("default-white", Label.LabelStyle.class);
        timerLabel = new Label("Time: 00:00", timeLabelStyle);
        timerLabel.setFontScale(1.2f);
        timerLabel.setAlignment(Align.center);
        buttonTable.add(timerLabel).width(100).pad(10).row();

        // Add moves label under the timer
        movesLabel = new Label("Moves: 0", timeLabelStyle);
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
                }
                return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/clicked.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                        0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                        0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight());

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/cursor.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                        0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                        0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight());

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);
            }
        });

        broadcastGameState();
    }

    private void handleArrowKeys(int[] direction) {
        List<int[][]> legalMoves = game.getLegalMovesByDirection(direction);
        if (legalMoves.isEmpty()) {
            return;
        }
        if (isAutoSolving) {
            stopAutoSolving();
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
                game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                piece.setPosition(new int[] { toRow, toCol });
                recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                broadcastGameState();
                block.addAction(Actions.sequence(
                        Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                        Actions.run(() -> {
                        })));
                break;
            }
        }
    }

    // Helper method to assign colors to pieces
    private Color getColorForPiece(int id) {
        switch (id) {
            case 0:
                return new Color(0.95f, 0.25f, 0.25f, 1); // Soft red for Cao Cao
            case 1:
                return new Color(0.25f, 0.25f, 0.95f, 1); // Soft blue for Guan Yu
            case 2:
            case 3:
            case 4:
            case 5:
                return new Color(204f / 255f, 51f / 255f, 255f / 255f, 1); // Soft purple for Generals
            case 6:
            case 7:
            case 8:
            case 9:
                return new Color(0.95f, 0.95f, 0.25f, 1); // Soft yellow for Soldiers
            default:
                return new Color(0.8f, 0.8f, 0.8f, 1); // Light gray for default
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
        klotski.setGlClearColor();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Optional, render dynamic board
        // klotski.dynamicBoard.render(delta);

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
                        game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                        piece.setPosition(new int[] { toRow, toCol });
                        recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                        solutionIndex++;
                        this.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                        broadcastGameState();
                        block.addAction(Actions.sequence(
                                Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                                Actions.run(() -> {
                                    // Update game logic after animation
                                    // TODO: find a more robust way, letting applyAction to handle at ease
                                    // Maybe we shall add another variable to show whether we finished the update
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
        // Save the current state to memory
        KlotskiGame.KlotskiPiece[] savedPieces = game.getPieces();
        List<int[][]> savedMoveHistory = new ArrayList<>(moveHistory);
        int savedCurrentMoveIndex = currentMoveIndex;
        float savedElapsedTime = elapsedTime;

        // Clear the stage and reinitialize
        stage.clear();
        stage.getViewport().update(width, height, true);
        create();
        // klotski.dynamicBoard = new DynamicBoard(klotski, stage);

        // Restore the saved state
        game.setPieces(savedPieces);
        moveHistory = savedMoveHistory;
        currentMoveIndex = savedCurrentMoveIndex;
        elapsedTime = savedElapsedTime;

        // Update blocks and UI
        updateBlocksFromGame(game);
        movesLabel.setText("Moves: " + (currentMoveIndex + 1));
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

        // Magically make the resize work without problems
        // I dont't know why, but it works
        // Do not delete this line.
        System.out.println(game.toString());
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

        // Add a semi-transparent gray background
        Image background = new Image(skin.newDrawable("white", new Color(0, 0, 0, 0.5f))); // Semi-transparent gray
        background.setSize(stageWidth / 1.7f, stageHeight / 2f); // Half the size of the stage
        background.setPosition(-stageWidth / 3.4f, -stageHeight / 4f); // Center the background
        congratulationsGroup.addActor(background);

        Table congratsTable = new Table();
        congratsTable.setFillParent(true);
        congratsTable.center(); // Ensure the table is centered

        // Add congratulatory message
        Label.LabelStyle narrationStyle = skin.get("narration", Label.LabelStyle.class);
        congratsLabel = new Label("Congratulations! You Win!", narrationStyle);
        congratsLabel.setFontScale(2); // Make the text larger
        congratsTable.add(congratsLabel).padBottom(20).row();

        // Add time usage placeholder
        Label.LabelStyle altStyle = skin.get("alt", Label.LabelStyle.class);
        Label timerLabelCongrats = new Label("Time: 00:00", altStyle); // Placeholder for time usage
        timerLabelCongrats.setFontScale(1.5f);
        congratsTable.add(timerLabelCongrats).padBottom(20).row();

        // Store the timeLabel for later updates
        this.timerLabelCongrats = timerLabelCongrats;

        // Create a horizontal table for the buttons
        Table buttonRow = new Table();

        // Add restart button
        TextButton restartButton = new TextButton("Restart", skin);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                handleRestart(game); // Restart the game
                congratulationsGroup.setVisible(false); // Hide the congratulations screen
            }
        });
        buttonRow.add(restartButton).width(200).height(50).padRight(10); // Add padding between buttons

        // Add exit button
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                handleExit(); // Exit the game
            }
        });
        buttonRow.add(exitButton).width(200).height(50); // Add exit button to the same row

        // Add the button row to the main table
        congratsTable.add(buttonRow).padTop(20).row();

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

    private void handleSave(boolean autoSave) {
        String saveFileName = getSaveFileName();
        File file = new File(Gdx.files.getLocalStoragePath(), saveFileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Save the positions of all pieces, move history, current move index, and
            // elapsed time
            oos.writeObject(new ArrayList<>(List.of(game.getPieces())));
            oos.writeObject(moveHistory);
            oos.writeObject(new GameState(currentMoveIndex, elapsedTime));
            System.out.println("Game saved successfully for user: " + klotski.getLoggedInUser());
            // print save file content
            System.out.println("Save file content: " + new String(Files.readAllBytes(file.toPath())));

            // Upload the save to the server
            uploadSaveToServer(Files.readAllBytes(file.toPath()), autoSave);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    private void uploadSaveToServer(byte[] saveData, boolean autoSave) {
        // print raw data length
        System.out.println("Raw save data length: " + saveData.length);
        // Convert to base 64
        String encodedSaveData = java.util.Base64.getEncoder().encodeToString(saveData);
        // print encoded save data length
        System.out.println("Encoded save data length: " + encodedSaveData.length());
        // print encoded save data
        System.out.println("Encoded save data: " + encodedSaveData);

        // Read the save file content
        // String saveData = new String(Files.readAllBytes(saveFile.toPath()));

        // Create a hash for validation (optional)
        String hash = Integer.toHexString(encodedSaveData.hashCode());

        // Prepare the JSON payload
        String username = klotski.getLoggedInUser();
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
        String payload = new Gson().toJson(Map.of(
                "username", username,
                "date", date,
                "hash", hash,
                "saveData", encodedSaveData,
                "autoSave", autoSave
        ));

        // Send the HTTP POST request
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder
                .newRequest()
                .method(Net.HttpMethods.POST)
                .url("http://42.194.132.147:8001/gameSave/uploadSave")
                .header("Content-Type", "application/json")
                .content(payload)
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();
                System.out.println("Server response: " + response);
            }

            @Override
            public void failed(Throwable t) {
                System.err.println("Failed to upload save: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                System.err.println("Save upload cancelled");
            }
        });
    }

    private void handleLoad() {
        String username = klotski.getLoggedInUser();

        fetchLatestSaveFromServer(username, saveData -> {
            if (saveData == null) {
                System.out.println("No save file found for user: " + username);
                return;
            }

            try {
                byte[] decodedSaveData = java.util.Base64.getDecoder().decode(saveData);
                System.out.println("Decoded save data length: " + decodedSaveData.length);

                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decodedSaveData))) {
                    handleRestart(game);

                    List<KlotskiGame.KlotskiPiece> pieces = (List<KlotskiGame.KlotskiPiece>) ois.readObject();
                    moveHistory = (List<int[][]>) ois.readObject();
                    GameState gameState = (GameState) ois.readObject();
                    currentMoveIndex = gameState.getCurrentMoveIndex();
                    elapsedTime = gameState.getElapsedTime();

                    game.setPieces(pieces);
                    updateBlocksFromGame(game);
                    movesLabel.setText("Moves: " + (currentMoveIndex + 1));

                    int minutes = (int) (elapsedTime / 60);
                    int seconds = (int) (elapsedTime % 60);
                    timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

                    System.out.println("Game loaded successfully for user: " + username);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to decode save data: " + e.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Failed to load game: " + e.getMessage());
            }
        });
    }

    private void fetchLatestSaveFromServer(String username, Consumer<String> callback) {
        // Send the HTTP GET request
        String url = "http://42.194.132.147:8001/gameSave/getSaves?username=" + username;
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder
                .newRequest()
                .method(Net.HttpMethods.GET)
                .url(url)
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                System.out.println("Raw server response: " + httpResponse);
                int statusCode = httpResponse.getStatus().getStatusCode();
                System.out.println("HTTP Status Code: " + statusCode);
                String response = httpResponse.getResultAsString();
                System.out.println("Server response: " + response);
                Map<String, Object> responseMap = new Gson().fromJson(response, Map.class);

                if ((double) responseMap.get("code") == 200) {
                    List<Map<String, String>> saves = (List<Map<String, String>>) responseMap.get("saves");
                    if (!saves.isEmpty()) {
                        String latestSaveData = saves.get(0).get("saveData");
                        System.out.println("Latest save data: " + latestSaveData);
                        callback.accept(latestSaveData);
                    } else {
                        callback.accept(null);
                    }
                } else {
                    System.err.println("Failed to fetch saves: " + responseMap.get("message"));
                    callback.accept(null);
                }
            }

            @Override
            public void failed(Throwable t) {
                System.err.println("Failed to fetch saves: " + t.getMessage());
                callback.accept(null);
            }

            @Override
            public void cancelled() {
                System.err.println("Fetch saves request cancelled");
                callback.accept(null);
            }
        });
    }
    
    private void handleLocalSave() {
        // TODO: refactor to math the online method
        String saveFileName = getSaveFileName();
        File file = new File(Gdx.files.getLocalStoragePath(), saveFileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Save the positions of all pieces, move history, current move index, and
            // elapsed time
            oos.writeObject(new ArrayList<>(List.of(game.getPieces())));
            oos.writeObject(moveHistory);
            oos.writeInt(currentMoveIndex);
            oos.writeFloat(elapsedTime);
            System.out.println("Game saved successfully for user: " + klotski.getLoggedInUser());
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    private void handleLocalLoad() {
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
                if (!klotski.isOfflineMode())
                    handleSave(true); // Call the save method
                else
                    handleLocalSave();
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
        try {
            klotski.getWebSocketServer().broadcastGameState(gameState);
            if(klotski.getWebSocketClient() != null)
                klotski.getWebSocketClient().sendBoardState(gameState);
            else
                System.out.println("WebSocket client is not connected.");
        } catch (Exception e) {
            System.err.println("Failed to broadcast game state: " + e.getMessage());
        }
    }
}