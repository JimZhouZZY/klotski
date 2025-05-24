/**
 * SpecatateScreen.java
 * 
 * This class represents the spectate game screen in the Klotski game.
 * It allows players to spectate other players' games.
 * The screen connects to a WebSocket server to synchronize game state between players.
 * It is enherited from the {@link GameScreen} class.
 * 
 * @author JimZhouZZY
 * @version 1.11
 * @since 2025-5-25
 * @see {@link GameScreen}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 * 
 * Change log:
 * 2025-05-25: generate change log
 */

package io.github.jimzhouzzy.klotski.screen;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.logic.KlotskiGame;
import io.github.jimzhouzzy.klotski.ui.KlotskiTheme;
import io.github.jimzhouzzy.klotski.ui.component.RectangleBlockActor;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class SpectateScreen extends GameScreen {

    private String username;
    private GameWebSocketClient webSocketClient;

    public SpectateScreen(final Klotski klotski, String username, GameWebSocketClient webSocketClient) {
        super(klotski);
        this.username = username;
        this.webSocketClient = webSocketClient;
        connectWebSocket();
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
                                handleLocalSave(false);
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
}
