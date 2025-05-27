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
 * SpecatateScreen.java
 * 
 * This class represents the spectate game screen in the Klotski game.
 * It allows players to spectate other players' games.
 * The screen connects to a WebSocket server to synchronize game state between players.
 * It is enherited from the {@link GameScreen} class.
 * 
 * @author JimZhouZZY
 * @version 1.22
 * @since 2025-5-25
 * @see {@link GameScreen}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: Refactor UI in SpectateScreen
 * 2025-05-27: Implement Co-op
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: organize import s
 * 2025-05-24: refactor spectate
 * 2025-05-24: refactor spectate screen to extend GameScreen
 * 2025-05-24: refactor spectate screen to extend GameScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-21: bug fix: do not show dialog when auto save
 * 2025-05-20: Merge branch v1.0.5 into main (#7)
 * 2025-05-14: add fromString method and test cases (#4)
 * 2025-05-07: formal login & prepare in-game spectate
 */

package io.github.jimzhouzzy.klotski.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.logic.EnhancedKlotskiGame;
import io.github.jimzhouzzy.klotski.logic.KlotskiGame;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;
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


    /**
     * Connects the WebSocket client and configures its message listener to handle incoming board state updates
     * specific to the current user. This method verifies if the WebSocket client is initialized and connected
     * before proceeding. If the client is not connected, an error is logged, and the method exits.
     *
     * Once connected, the listener processes messages starting with "Board state updated:" and containing the
     * current user's username. The board state is extracted from the message, parsed into the game model, and
     * triggers a UI update to reflect the new state. The parsed board state is stripped of protocol prefixes
     * and user identifiers before being applied to the game.
     *
     * @implNote The WebSocket message format is expected to follow "Board state updated:username: [state data]",
     * where the username matches the current user. Messages not matching this structure or username
     * are ignored. This method assumes the {@code game} instance and UI components (e.g., blocks) are
     * properly initialized before invocation.
     */
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

    /**
     * Initializes and configures the game screen components including the stage, UI elements, game logic,
     * audio assets, and input handling. Sets up the visual layout with a dynamic grid-based game area and
     * control buttons arranged in a sidebar. Configures background music volume, sound effects, and touch
     * interactions. Creates block actors based on game pieces, handles theme-specific styling, and prepares
     * move history tracking. Implements both touch and keyboard controls (including ESC for exit), and initializes
     * progress tracking elements like timers and move counters. The layout automatically adapts to screen dimensions
     * using calculated cell sizes, and supports both classic and enhanced game modes based on initialization parameters.
     */
    @Override
        public void create() {
            stage = new Stage(new ScreenViewport());
            Gdx.app.postRunnable(() -> {
                Music bgm = klotski.getBackgroundMusic();
                if (bgm != null) {
                    bgm.setVolume(0.3f); // Reduce volume to 30%
                }
            });
            Gdx.input.setInputProcessor(stage);
    
            skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));
            shapeRenderer = new ShapeRenderer();
    
            blocks = new ArrayList<>(); // Initialize the list of blocks
    
            // Load the rectangular block click sound
            clickRectangularSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound_fx/clickRectangular.mp3"));
            // Load the win sound effect
            winSound = Gdx.audio.newMusic(Gdx.files.internal("assets/sound_fx/win.mp3"));
            loseSound = Gdx.audio.newMusic(Gdx.files.internal("assets/sound_fx/lose.mp3"));
    
            // Calculate cellSize dynamically based on the screen size
            cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);
    
            // Initialize the game logic
            if (blockedId == -1)
                game = new KlotskiGame();
            else
                game = new EnhancedKlotskiGame(blockedId);
    
            // Create a root table for layout
            Table rootTable = new Table();
            rootTable.setFillParent(true);
            stage.addActor(rootTable);
    
            // Left side: Grid container
            Table gridTable = new Table();
            gridTable.setFillParent(false);
    
            // Right side: Button column
            // Create a table for buttons, arrange in two columns
            Table buttonTable = new Table();
    
            // Set default button size (1.5x original)
            float buttonWidth = 150;
            float buttonHeight = 45;
    
            String[] buttonNames = { "Restart", "Hint", "Auto", "Undo", "Redo", "Save", "Load", "Exit" };
    
            // Add buttons in two columns
            for (int i = 0; i < buttonNames.length; i++) {
                String name = buttonNames[i];
                TextButton button = new TextButton(name, skin);
                button.getLabel().setFontScale(0.75f); // 1.5x original font scale
                buttonTable.add(button).height(buttonHeight).width(buttonWidth).pad(10);
    
                if (i % 2 == 1) buttonTable.row(); // New row after every two buttons
    
                if (name.equals("Auto")) {
                    autoButton = button;
                }
    
                if (name.equals("Exit")) {
                    button.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            klotski.playClickSound();
                            switch (name) {
                            case "Exit":
                                handleExit();
                                break;
                            }
                        }
                    });
                }
            }
    
            if (klotski.isArrowControlsEnabled()) {
                // Add arrow control buttons
                Label controlLabel = new Label("Move", skin);
                controlLabel.setFontScale(1.5f);
                buttonTable.add(controlLabel).padTop(20).row();
                Map<String, TextButton> directionButtons = new HashMap<>();
                buttonNames = new String[]{"Up", "Down", "Left", "Right"};
                for (String name : buttonNames) {
    
                    final String buttonName = name;
                    TextButton button = new TextButton(buttonName, skin);
                    directionButtons.put(buttonName, button);
                    button.getLabel().setFontScale(0.5f);
                    //buttonTable.add(button).height(30).width(100).pad(10);
                    //buttonTable.row();
                }
    
                Table arrowTable = new Table();
                arrowTable.add().width(30);
                arrowTable.add(directionButtons.get("Up")).width(50).height(40);
                arrowTable.add().width(30).row();
    
                arrowTable.add(directionButtons.get("Left")).width(50).height(40);
                arrowTable.add().width(10);
                arrowTable.add(directionButtons.get("Right")).width(50).height(40).row();
    
                arrowTable.add().width(30);
                arrowTable.add(directionButtons.get("Down")).width(50).height(40);
                arrowTable.add().width(30).row();
    
                buttonTable.add(arrowTable).padTop(20).row();
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
                RectangleBlockActor block = new RectangleBlockActor(x, y, width, height, color, piece.id, game, this);
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
    
            badgeGroup = new Group();
            badgeGroup.setVisible(false);
    
            Label badgeLabel = new Label("", skin);
            badgeLabel.setName("badgeLabel");
            badgeLabel.setFontScale(1.2f);
            badgeLabel.setAlignment(Align.center);
    
            Image badgeBg = new Image(skin.newDrawable("white", new Color(1, 1, 1, 0.5f)));
            badgeBg.setSize(400, 50);
    
            badgeBg.setPosition(-30, 0);
    
            badgeLabel.setSize(300, 50);
            badgeLabel.setPosition(0, 0);
    
            badgeGroup.addActor(badgeBg);
            badgeGroup.addActor(badgeLabel);
    
            badgeGroup.setSize(300, 50);
            badgeGroup.setPosition(Gdx.graphics.getWidth() - 400, 20);
    
            stage.addActor(badgeGroup);
    
            // Reset elapsed time
            elapsedTime = 0;
    
            // broadcastGameState();
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
            });
    
        }
}
