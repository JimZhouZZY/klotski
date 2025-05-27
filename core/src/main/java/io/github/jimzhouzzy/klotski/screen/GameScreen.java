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
 * GameScreen.java
 * <p>
 * This class represents the main game screen of the Klotski game.
 * It is enheritaged by {@link SpectateScreen} and {@link CooperateScreen}.
 *
 * @author JimZhouZZY
 * @version 1.69
 * @since 2025-5-25
 * <p>
 * KNOWN ISSUES:
 * 1. The move count is incorrect when the user dragged a piece
 * across multiple grid.
 * 2. Restart in an leveled (seedly random shuffeled) game won't
 * reset the game to the shuffeled state.
 * <p>
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: filter null user
 * 2025-05-27: resolve co-op restart not synced
 * 2025-05-27: change restart logic to handle cooperate restart
 * 2025-05-27: Multi-threading to avoid delay
 * 2025-05-27: fix restart
 * 2025-05-27: Merge branch thz into main
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: Multilevel for blocked
 * 2025-05-27: fix: arrow key causes crash when selecting blocked pieces
 * 2025-05-27: Change the save screen.
 * 2025-05-27: UI improvement
 * 2025-05-27: Refactor UI in SpectateScreen
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: multiple classical level
 * 2025-05-27: fix white line
 * 2025-05-27: show total moves at the end of the game
 * 2025-05-27: Do not allow unauthorized(guest) users to save and load
 * 2025-05-27: Show error dialog when load-save failed
 * 2025-05-27: Enhance GameScreen block color
 * 2025-05-27: make GameScreen seperate
 * 2025-05-27: Implement Co-op
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: HD-font & UX improvement
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Highlight the number of the selected number (#14)
 * 2025-05-24: Add H,J,K,L for changing the selected  block.
 * 2025-05-24: Add a possible movement of the block if the block is not selected.`
 * 2025-05-24: Highlight the number of the selected number
 * 2025-05-24: refactor spectate
 * 2025-05-24: fix: piece 0 cannot move
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-21: sync
 * 2025-05-21: bug fix: do not show dialog when auto save
 * 2025-05-21: bug fix (#10)
 * 2025-05-21: prevent pop-up window when auto-saving
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 * 2025-05-08: update soundfx
 * 2025-04-30: optimize local storage
 * 2025-04-29: web inspection
 * 2025-04-29: better error log
 * 2025-04-29: offline mode & optimize save-load
 * 2025-04-28: Online server auth & save-load
 * 2025-04-25: Revert 'fix:resize'
 * 2025-04-24: fix resize changed base tile size
 * 2025-04-24: MSAA & Settings
 * 2025-04-23: better main screen
 * 2025-04-23: better main menu
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
 * 2025-04-21: resizable
 * 2025-04-16: Login & Levels
 * 2025-04-16: Login & Game Mode & Save-Load
 * 2025-04-16: Timer & Moves
 * 2025-04-15: refactor & basic undo redo
 * 2025-04-14: Auto solve & unresizable & exit
 * 2025-04-13: feat: restart hint and congratulations
 * 2025-04-09: drag and collide
 * 2025-04-08: libgdx basic GUI
 * 2025-04-08: init libgdx
 */


package io.github.jimzhouzzy.klotski.screen;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.Gson;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.logic.EnhancedKlotskiGame;
import io.github.jimzhouzzy.klotski.logic.GameState;
import io.github.jimzhouzzy.klotski.logic.KlotskiGame;
import io.github.jimzhouzzy.klotski.logic.KlotskiSolver;
import io.github.jimzhouzzy.klotski.ui.component.Dialog;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;
import io.github.jimzhouzzy.klotski.ui.component.RectangleBlockActor;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;


public class GameScreen extends ApplicationAdapter implements Screen {
    public Sound clickRectangularSound;
    public Music winSound;
    public boolean winMusicPlayed = false;
    public Music loseSound;
    public boolean loseMusicPlayed = false;
    public boolean isCooperateMode = false;
    public final String SAVE_FILE = ConfigPathHelper.getConfigFilePath("Klotski", "game_save.dat");

    public Stage stage;
    public Skin skin;
    public ShapeRenderer shapeRenderer;
    public float cellSize;
    public final int rows = 5;
    public final int cols = 4;
    public List<RectangleBlockActor> blocks; // List of all blocks
    public Group congratulationsGroup;
    public RectangleBlockActor selectedBlock = null;

    public int[][] autoMoves;
    public int autoStep;
    public boolean isAutoSolving;
    public boolean isTerminal = false;

    public List<int[][]> moveHistory; // Stores the history of moves
    public int currentMoveIndex; // Tracks the current move in the history
    private int level = -1;

    public float elapsedTime; // Tracks the elapsed time in seconds
    public Label timerLabel; // Label to display the timer
    private Label timerLabelCongrats; // Label to display the timer
    private Label movesLabelCongrats; // Label to display the timer
    public Label movesLabel; // Label to display the total moves

    public KlotskiGame game; // Reference to the game logic
    public Klotski klotski; // Reference to the main game class
    public List<String> solution; // Stores the current solution
    public int solutionIndex; // Tracks the current step in the solution

    public TextButton autoButton;

    public Timer.Task autoSaveTask;

    public boolean isAttackMode; // Flag to track if the game is in 3min-Attack mode
    public float attackModeTimeLimit = 3 * 60; // 3 minutes in seconds
    public Label congratsLabel;
    protected Group badgeGroup;
    private Timer.Task badgeHideTask;
    private long randomSeed = -1L;
    private int blockedLevel;

    public int blockedId = -1; // no piece is blocked at first

    public GameScreen(final Klotski klotski, long seed) {
        this.klotski = klotski;
        create();
        this.randomSeed = seed;
        randomShuffle(seed);
    }
    
    public GameScreen(final Klotski klotski, long seed, boolean isAttackMode) {
        this.klotski = klotski;
        create();
        this.randomSeed = seed;
        this.isAttackMode = isAttackMode;
        randomShuffle(seed);
    }

    public GameScreen(final Klotski klotski, int blockedId) {
        this.klotski = klotski;
        this.blockedId = blockedId;
        System.out.println("GameScreen created with blockedId: " + blockedId);
        // print hash of game
        System.out.println("GameScreen hash: " + this.hashCode());
        create();
    }
    
    public GameScreen(final Klotski klotski) {
        this.klotski = klotski;
        create();
    }

    public GameScreen(final Klotski klotski, String blockedLevel) {
        this.klotski = klotski;
        this.blockedLevel = Integer.parseInt(blockedLevel);
        this.blockedId = EnhancedKlotskiGame.getBlockedIdForLevel(this.blockedLevel);
        create();
    }

    /**
     * Sets the game mode to either attack mode or another specified mode based on the provided parameter.
     * When {@code isAttackMode} is {@code true}, the game switches to attack mode, enabling mechanics and
     * behaviors specific to offensive gameplay. When {@code false}, it switches to the alternate mode
     * (e.g., defense, exploration, or neutral mode), adjusting game rules and interactions accordingly.
     *
     * @param isAttackMode {@code true} to activate attack mode, {@code false} to deactivate it and switch
     * to the alternate mode.
     */
    public void setGameMode(boolean isAttackMode) {
            this.isAttackMode = isAttackMode;
        }

    /**
     * Sets the ID of the blocked piece and disables touch interaction for the corresponding piece.
     * Updates the internal tracking of the blocked piece ID to the specified value and sets the
     * {@code enableTouch} property of the piece associated with the given ID to {@code false}.
     *
     * @param blockedId the ID of the piece to be blocked and have touch interaction disabled
     */
    public void blockedPieceId(int blockedId) {
            this.blockedId = blockedId;
            this.blocks.get(blockedId).enableTouch = false;
        }

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
            game = new EnhancedKlotskiGame(String.valueOf(blockedLevel));

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

        String[] buttonNames = {"Restart", "Hint", "Auto", "Undo", "Redo", "Save", "Load", "Exit"};

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

            // Add functionality to each button
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
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

                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        klotski.playClickSound();
                        switch (buttonName) {
                            case "Up":
                                handleArrowKeys(new int[]{-1, 0});
                                break;
                            case "Down":
                                handleArrowKeys(new int[]{1, 0});
                                break;
                            case "Left":
                                handleArrowKeys(new int[]{0, -1});
                                break;
                            case "Right":
                                handleArrowKeys(new int[]{0, 1});
                                break;
                        }
                    }
                });
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

            blocks.add(block); // Add block to the list
            stage.addActor(block); // Add block to the stage

            block.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Play rectangular block click sound
                    if (clickRectangularSound != null) {
                        clickRectangularSound.play(1.0f);
                    }
                    if (selectedBlock == block) {
                        selectedBlock.setSelected(false);
                        selectedBlock = null;
                    } else {
                        if (selectedBlock != null) {
                            selectedBlock.setSelected(false);
                        }
                        selectedBlock = block;
                        selectedBlock.setSelected(true); // Thicker border
                    }
                }
            });

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
        timerLabel.setFontScale(1.0f);
        timerLabel.setAlignment(Align.center);
        buttonTable.add(timerLabel).width(100).pad(10).row();

        // Add moves label under the timer
        movesLabel = new Label("Moves: 0", timeLabelStyle);
        movesLabel.setFontScale(1.0f);
        movesLabel.setAlignment(Align.center);
        buttonTable.add(movesLabel).width(100).pad(10).row();

        badgeGroup = new Group();
        badgeGroup.setVisible(false);

        Label badgeLabel = new Label("", skin);
        badgeLabel.setName("badgeLabel");
        badgeLabel.setFontScale(1.0f);
        badgeLabel.setAlignment(Align.center);

        Image badgeBg = new Image(skin.newDrawable("white", new Color(1, 1, 1, 0.5f)));
        badgeBg.setSize(470, 50);

        badgeBg.setPosition(-85, 0);

        badgeLabel.setSize(300, 50);
        badgeLabel.setPosition(0, 0);

        badgeGroup.addActor(badgeBg);
        badgeGroup.addActor(badgeLabel);

        badgeGroup.setSize(300, 50);
        badgeGroup.setPosition(Gdx.graphics.getWidth() - 400, 20);

        stage.addActor(badgeGroup);

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
                    case Input.Keys.I:
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
                        handleArrowKeys(new int[]{0, -1});
                        return true;
                    case Input.Keys.UP:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[]{-1, 0});
                        return true;
                    case Input.Keys.RIGHT:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[]{0, 1});
                        return true;
                    case Input.Keys.DOWN:
                        // Handle left arrow key for moving blocks
                        handleArrowKeys(new int[]{1, 0});
                        return true;
                    // Handle number keys 0-9 and numpad 0-9 for block selection
                    case Input.Keys.H: { // Left
                        RectangleBlockActor target = findBlockByOffset(selectedBlock, 0, -1);
                        if (target != null) {
                            if (clickRectangularSound != null) clickRectangularSound.play(1.0f);
                            if (selectedBlock != null) selectedBlock.setSelected(false);
                            selectedBlock = target;
                            selectedBlock.setSelected(true);
                        }
                        return true;
                    }
                    case Input.Keys.L: { // Right
                        RectangleBlockActor target = findBlockByOffset(selectedBlock, 0, 1);
                        if (target != null) {
                            if (clickRectangularSound != null) clickRectangularSound.play(1.0f);
                            if (selectedBlock != null) selectedBlock.setSelected(false);
                            selectedBlock = target;
                            selectedBlock.setSelected(true);
                        }
                        return true;
                    }
                    case Input.Keys.J: { // Down
                        RectangleBlockActor target = findBlockByOffset(selectedBlock, 1, 0);
                        if (target != null) {
                            if (clickRectangularSound != null) clickRectangularSound.play(1.0f);
                            if (selectedBlock != null) selectedBlock.setSelected(false);
                            selectedBlock = target;
                            selectedBlock.setSelected(true);
                        }
                        return true;
                    }
                    case Input.Keys.K: { // Up
                        RectangleBlockActor target = findBlockByOffset(selectedBlock, -1, 0);
                        if (target != null) {
                            if (clickRectangularSound != null) clickRectangularSound.play(1.0f);
                            if (selectedBlock != null) selectedBlock.setSelected(false);
                            selectedBlock = target;
                            selectedBlock.setSelected(true);
                        }
                        return true;
                    }

                    case Input.Keys.NUM_0:
                    case Input.Keys.NUM_1:
                    case Input.Keys.NUM_2:
                    case Input.Keys.NUM_3:
                    case Input.Keys.NUM_4:
                    case Input.Keys.NUM_5:
                    case Input.Keys.NUM_6:
                    case Input.Keys.NUM_7:
                    case Input.Keys.NUM_8:
                    case Input.Keys.NUM_9: {
                        int digitKey = keycode - Input.Keys.NUM_0; // digitKey = 0~9
                        for (RectangleBlockActor block : blocks) {
                            block.setSelected(false);
                        }
                        for (RectangleBlockActor block : blocks) {
                            if (block.pieceId == digitKey) {
                                if (selectedBlock != block) {
                                    selectedBlock = block;
                                    selectedBlock.setSelected(true);
                                    if (clickRectangularSound != null) clickRectangularSound.play(1.0f);
                                } else {
                                    selectedBlock.setSelected(false);
                                    selectedBlock = null;
                                    if (clickRectangularSound != null) clickRectangularSound.play(1.0f);
                                }
                                break;
                            }
                        }
                        return true;
                    }

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

    /**
     * Searches for the first neighboring block in the specified direction from the current block's position.
     * The method iterates through grid positions incrementally based on the provided row and column offsets,
     * checking each step for overlapping blocks within the game's boundaries (rows0-4, columns0-3).
     * Returns the first encountered block whose area contains the calculated target position, excluding
     * the current block itself. If no such block is found within valid grid bounds, returns {@code null}.
     *
     * @param current The starting block from which the search originates.
     * @param rowOffset The directional offset applied to the row per step (e.g., -1,0, or1).
     * @param colOffset The directional offset applied to the column per step (e.g., -1,0, or1).
     * @return The first neighboring block in the specified offset direction, or {@code null}
     * if no valid block is found within the grid bounds.
     */
    private RectangleBlockActor findBlockByOffset(RectangleBlockActor current, int rowOffset, int colOffset) {
            if (current == null) return null;
            KlotskiGame.KlotskiPiece currentPiece = game.getPiece(current.pieceId);
            int targetRow = currentPiece.getRow();
            int targetCol = currentPiece.getCol();
            int k = 0;
            while (targetRow >= 0 && targetCol >= 0 && targetRow <= 4 && targetCol <= 3) {
                targetRow = currentPiece.getRow() + rowOffset * k;
                targetCol = currentPiece.getCol() + colOffset * k;
                k++;
                for (RectangleBlockActor block : blocks) {
                    KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                    int pieceRow = piece.getRow();
                    int pieceCol = piece.getCol();
    
                    // check if targetRow and targetCol is inside the piece rectangle
                    if (targetRow >= pieceRow && targetRow < pieceRow + piece.height &&
                        targetCol >= pieceCol && targetCol < pieceCol + piece.width &&
                        block != current) {
                        return block;
                    }
                }
            }
            return null;
        }

    /**
     * Handles automatic movement of the selected block in the specified direction by applying the first available legal move.
     * Checks if the selected block is blocked or if there are no legal moves in the given direction, exiting early if so.
     * If auto-solving is active, it stops the auto-solving process before proceeding. Applies the move to the game state,
     * updates the block's position, records the move, and checks if the game has reached a terminal state. Broadcasts the
     * updated game state and triggers a smooth animation for the block movement. The direction array determines the legal
     * moves queried from the game logic, and the first valid move is executed immediately.
     *
     * @param direction An array representing the directional input (e.g., keyboard arrow keys) used to determine valid moves.
     */
    private void handleAutoArrowKeys(int[] direction) {
            if (selectedBlock.pieceId == blockedId) return;
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
                    game.applyAction(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
                    piece.setPosition(new int[]{toRow, toCol});
                    recordMove(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
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

    /** * Handles the movement logic for arrow key inputs by attempting to move the selected game piece in the specified direction.
     * If no piece is selected or the selected piece is blocked, delegates to automatic arrow key handling. Checks if the
     * requested move is legal, updates the piece's position, applies the game action, records the move, checks for terminal
     * state, and broadcasts the updated game state. Animates the movement of the selected block to the new position if the move is valid.
     *
     * @param direction A2-element array representing the directional offsets for row and column movement (e.g., [-1,0] for up).
     * The first element modifies the row, and the second modifies the column. */
    private void handleArrowKeys(int[] direction) {
            if (selectedBlock == null || selectedBlock.pieceId == blockedId) {
                handleAutoArrowKeys(direction);
                return;
            }
    
            KlotskiGame.KlotskiPiece piece = game.getPiece(selectedBlock.pieceId);
            int fromRow = piece.getRow();
            int fromCol = piece.getCol();
            int toRow = fromRow + direction[0];
            int toCol = fromCol + direction[1];
    
            if (!game.isLegalMove(new int[]{fromRow, fromCol}, new int[]{toRow, toCol})) return;
    
            float targetX = toCol * cellSize;
            float targetY = (rows - toRow - piece.height) * cellSize;
    
            game.applyAction(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
            piece.setPosition(new int[]{toRow, toCol});
            recordMove(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
            isTerminal = game.isTerminal();
            broadcastGameState();
    
            selectedBlock.addAction(Actions.moveTo(targetX, targetY, 0.1f));
        }

    // Helper method to assign colors to pieces
    /**
     * Returns a color associated with the specified piece ID. The color is determined based on
     * the following ID mappings:
     * - ID0: Soft red (#ff3333) for Cao Cao.
     * - ID1: Soft blue (#3ba776) for Guan Yu.
     * - IDs2-5: Soft purple (#a442b9) for Generals.
     * - IDs6-9: Soft yellow (#fff433) for Soldiers.
     * If the provided ID does not match any predefined cases, a default light gray color (#777773) is returned.
     *
     * @param id The numeric identifier of the piece to determine the color for.
     * @return The {@link Color} corresponding to the piece's ID, or light gray if the ID is unrecognized.
     */
    public Color getColorForPiece(int id) {
            switch (id) {
                case 0:
                    return Color.valueOf("#ff3333"); // Soft red for Cao Cao
                case 1:
                    return Color.valueOf("#3ba776"); // Soft blue for Guan Yu
                case 2:
                case 3:
                case 4:
                case 5:
                    return Color.valueOf("#a442b9"); // Soft purple for Generals
                case 6:
                case 7:
                case 8:
                case 9:
                    return Color.valueOf("#fff433"); // Soft yellow for Soldiers
                default:
                    return Color.valueOf("#777773"); // Light gray for default
            }
        }

    public List<RectangleBlockActor> getBlocks() {
            return blocks;
        }

    /**
     * Calculates the allowable movement boundaries for a specified block to prevent collisions with other blocks
     * and the grid edges. The boundaries define the minimum and maximum X and Y coordinates the block can occupy
     * while avoiding overlaps with existing blocks. The method considers horizontal and vertical overlaps with
     * all other blocks in the grid and adjusts the movement range accordingly.
     *
     * @param block The block for which movement boundaries are calculated.
     * @return An array of four float values: {minX, maxX, minY, maxY}, where:
     * - minX is the minimum allowed X coordinate (left boundary),
     * - maxX is the maximum allowed X coordinate (right boundary),
     * - minY is the minimum allowed Y coordinate (bottom boundary),
     * - maxY is the maximum allowed Y coordinate (top boundary).
     * These values ensure the block remains within the grid and does not intersect with other blocks.
     */
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
    
            return new float[]{minX, maxX, minY, maxY};
        }

    /**
     * Updates the positions of all block actors based on the current state of the provided KlotskiGame.
     * For each block, this method retrieves the corresponding game piece's position, converts the game's
     * grid coordinates to screen coordinates, and sets the block's visual position. The conversion accounts
     * for grid-to-pixel scaling using the cellSize, inverts the y-axis to align with the display's coordinate
     * system (where y=0 is at the top), and adjusts the position vertically based on the block's height to
     * prevent overlap with adjacent grid cells.
     *
     * @param game The KlotskiGame instance from which to retrieve the current piece positions and dimensions.
     */
    public void updateBlocksFromGame(KlotskiGame game) {
            for (RectangleBlockActor block : blocks) {
                KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                float x = piece.position[1] * cellSize; // Column to x-coordinate
                float y = (rows - piece.position[0] - piece.height) * cellSize; // Invert y-axis and adjust for height
                block.setPosition(x, y);
            }
        }

    /**
     * Renders the game state each frame, handling dynamic UI adjustments, game logic, and animations.
     * This method updates the grid cell size to maintain a square aspect ratio, clears the screen,
     * and renders the dynamic board. It checks for terminal states (win/lose conditions) to display
     * appropriate screens (congratulations or losing screen). The elapsed game timer is formatted
     * and displayed as MM:SS. Grid lines are drawn to visualize the game board. In auto-solving mode,
     * the method processes precomputed solution steps, animating block movements between grid positions
     * while ensuring game state synchronization. The stage is updated and drawn to reflect UI changes.
     *
     * @param delta The time in seconds since the last render call, used for frame-rate-independent updates.
     */
    @Override
        public void render(float delta) {
            // Update cell size dynamically to ensure the grid stays square
            cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);
    
            // Clear the screen
            klotski.setGlClearColor();
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    
            // Optional, render dynamic board
            klotski.dynamicBoard.render(delta);
    
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
                            game.applyAction(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
                            piece.setPosition(new int[]{toRow, toCol});
                            recordMove(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
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

    /**
     * Resizes the game stage to the specified dimensions while preserving the current game state.
     * This method saves the current state of the game pieces, move history, move index, and elapsed time,
     * then clears and reinitializes the stage with the new width and height. After reinitialization, the
     * saved state is restored, and UI elements such as blocks, move counter, timer, and badges are updated
     * to reflect the current state. The method ensures visual consistency and proper layout adjustments
     * after resizing. A debug print statement is included to stabilize the resize functionality.
     *
     * @param width the new width of the game stage
     * @param height the new height of the game stage
     */
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
    
            if (badgeGroup != null) {
                badgeGroup.setPosition(width - 320, 20);
            }
    
            // Magically make the resize work without problems
            // I dont't know why, but it works
            // Do not delete this line.
            System.out.println(game.toString());
        }

    /**
     * Disposes all resources associated with this screen to free up memory and prevent leaks.
     * This includes disposing the primary UI components (stage, skin, and shape renderer),
     * canceling any active auto-save tasks to halt background processes, and releasing
     * audio resources (rectangular block click sound, win sound, and lose sound).
     * Each resource is only disposed if it has been initialized (non-null). This method
     * ensures proper cleanup when the screen is no longer in use.
     */
    @Override
        public void dispose() {
            stage.dispose();
            skin.dispose();
            shapeRenderer.dispose();
    
            // Cancel the auto-save task when disposing the screen
            if (autoSaveTask != null) {
                autoSaveTask.cancel();
            }
            // Dispose the rectangular block click sound
            if (clickRectangularSound != null) {
                clickRectangularSound.dispose();
            }
    
            // Dispose the win and lose sounds
            if (winSound != null) {
                winSound.dispose();
            }
            if (loseSound != null) {
                loseSound.dispose();
            }
        }

    /**
     * Restarts the Klotski game by resetting game state, UI elements, and animations. Stops any active auto-solving,
     * resets the timer and move counter, clears block animations, and reinitializes the game logic based on the current
     * configuration (classic levels, blocked levels, cooperative mode, attack mode, or randomized seeds). Updates the
     * game blocks to reflect the reset state, broadcasts the new game state, and recreates the appropriate GameScreen
     * instance for the current game mode (e.g., classical,3-minute attack, or randomized layouts). Handles special
     * cases for cooperative mode, pre-defined levels, and randomized seeds to ensure correct game screen initialization.
     *
     * @param game The KlotskiGame instance to reset and reinitialize. Its type or configuration may be altered based
     * on the current blocked level, cooperative mode status, or attack mode settings.
     */
    public void handleRestart(KlotskiGame game) {
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
    
            // Initialize the game logic
            if (blockedId == -1 && !this.isCooperateMode)
                game = new KlotskiGame();
            else if(!this.isCooperateMode)
                game = new EnhancedKlotskiGame(String.valueOf(blockedLevel));
    
            if (this.level != -1 || !isCooperateMode) {
                this.setLevel(this.level);
            }
    
            if (isCooperateMode)
                game.initialize();
    
            // Update the blocks to match the game state
            updateBlocksFromGame(game);
    
            // Reset terminal state
            isTerminal = false;
            winMusicPlayed = false;
            loseMusicPlayed = false;
    
            broadcastGameState();
    
            System.out.println("Game restarted.");
    
            if (this.isCooperateMode) {
                
            } 
            
            else if (blockedLevel != -1){
                String levelString = String.valueOf(blockedLevel);
                GameScreen gameScreen = new GameScreen(klotski, levelString);
                gameScreen.setGameMode(false); // Set to 3min-Attack mode
                klotski.setGameScreen(gameScreen);
                klotski.setScreen(gameScreen);
            }
            else if (randomSeed != -1L && this.isAttackMode) {
                GameScreen gameScreen = new GameScreen(klotski, (long) klotski.randomHelper.nextInt(114514), true);
                klotski.setGameScreen(gameScreen); // Set the game screen
                klotski.setScreen(gameScreen); // Navigate to the game screen
            }
    
            else if (this.level != -1 || this.randomSeed == -1L) {
                // Currently we just re-create the GameScreen
                GameScreen gameScreen = new GameScreen(klotski);
                gameScreen.setGameMode(false); // Classical mode
                gameScreen.setLevel(level);    // Set to Level 1~5
                klotski.setGameScreen(gameScreen);
                klotski.setScreen(gameScreen);
            }
        
            else if (this.randomSeed != -1L) {
                GameScreen gameScreen = new GameScreen(klotski, (long) klotski.randomHelper.nextInt(114514), false);
                klotski.setGameScreen(gameScreen); // Set the game screen
                klotski.setScreen(gameScreen); // Navigate to the game screen
            }
        }

    /**
     * Handles providing a hint for the current state of the Klotski game by solving the puzzle
     * asynchronously and animating the first move of the solution. This method runs the solver in a
     * background thread to prevent blocking the UI, retrieves the optimal solution path, and executes
     * the initial move visually and logically. The hint includes parsing the solution's first move
     * string to extract source and target coordinates, identifying the corresponding block, animating
     * its movement, and updating the game state to reflect the move. If no solution is found, it logs
     * an appropriate message. After the animation completes, the game state is checked for terminal
     * conditions, and listeners are notified of state changes.
     *
     * @param game The KlotskiGame instance representing the current game state. The solver uses this
     * to compute the solution, and the game is updated with the hinted move.
     */
    public void handleHint(KlotskiGame game) {
            // Get the solution from the solver
            // Run the solver in a separate thread to avoid blocking the UI
            new Thread(() -> {
                List<String> solutionResult = KlotskiSolver.solve(game, blockedId);
                Gdx.app.postRunnable(() -> {
                this.solution = solutionResult;
                if (solution != null && !solution.isEmpty()) {
                    // Parse the first move from the solution
                    String move = solution.get(0);
                    System.out.println("Hint: " + move);
    
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
                            game.applyAction(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
                            piece.setPosition(new int[]{toRow, toCol});
                            recordMove(new int[]{fromRow, fromCol}, new int[]{toRow, toCol});
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
                });
            }).start();
        }

    /**
     * Handles the auto-solving functionality for a Klotski game, toggling between starting and stopping
     * the solver. When activated, this method runs the solver in a background thread to avoid UI blocking,
     * retrieves a solution, and updates the UI to reflect the auto-solving state. If a valid solution is found,
     * the auto-solving mode is enabled, and the provided button's text is updated to "Stop". If auto-solving
     * is already active, it stops the process, resets internal states, and reverts the button text to "Auto".
     *
     * @param game The {@link KlotskiGame} instance to be solved automatically.
     * @param autoButton The {@link TextButton} used to trigger auto-solving, whose text is updated
     * to reflect the current state ("Auto" when inactive, "Stop" when active).
     *
     * @see KlotskiSolver#solve(KlotskiGame, String)
     * @see Gdx.app#postRunnable(Runnable)
     */
    public void handleAutoSolve(KlotskiGame game, TextButton autoButton) {
            if (isAutoSolving()) {
                stopAutoSolving(); // Stop auto-solving if already active
                autoButton.setText("Auto"); // Change button text back to "Auto"
            } else {
                solution = null; // Clear the previous solution
                solutionIndex = 0; // Reset the solution index
                System.out.println("Starting auto-solving..." + blockedId);
    
                // Run the solver in a separate thread to avoid blocking the UI
                new Thread(() -> {
                    List<String> newSolution = KlotskiSolver.solve(game, blockedId); // Get the new solution
    
                    Gdx.app.postRunnable(() -> {
                        if (newSolution != null && !newSolution.isEmpty()) {
                            solution = newSolution; // Store the solution
                            isAutoSolving = true; // Enable auto-solving mode
                            autoButton.setText("Stop"); // Change button text to "Stop"
                            System.out.println("Auto-solving started.");
                        } else {
                            System.out.println("No solution found.");
                        }
                    });
                }).start();
            }
        }

    /**
     * Resets the state of music playback flags and navigates back to the main menu screen.
     * This method sets both {@code winMusicPlayed} and {@code loseMusicPlayed} flags to {@code false},
     * switches the application's active screen to the main menu via {@link Klotski#setScreen(Screen)},
     * and releases resources associated with the current screen by calling {@link #dispose()}.
     * This ensures a clean transition from the current screen to the main menu.
     */
    public void handleExit() {
            // reset
            winMusicPlayed = false;
            loseMusicPlayed = false;
    
            klotski.setScreen(klotski.mainScreen); // Switch back to the main menu
            dispose(); // Dispose of the current screen resources
        }

    /**
     * Indicates whether the auto-solving feature is currently active. This method returns the current
     * status of the auto-solving mode, which determines if automated problem-solving is enabled.
     *
     * @return {@code true} if auto-solving is currently enabled, {@code false} if it is disabled.
     */
    public boolean isAutoSolving() {
            return isAutoSolving;
        }

    /**
     * Stops the auto-solving process if it is currently active. This method checks the {@code isAutoSolving}
     * flag and, when set to {@code true}, sets it to {@code false}, outputs a confirmation message to the console,
     * and updates the text of the auto-solving button via {@code updateAutoButtonText}. If auto-solving is not
     * active, calling this method has no effect.
     */
    public void stopAutoSolving() {
            if (isAutoSolving) {
                isAutoSolving = false;
                System.out.println("Auto-solving stopped.");
                updateAutoButtonText(autoButton);
            }
        }

    /**
     * Creates a congratulations screen overlay displayed upon completing the game. This screen centers
     * on the stage and includes a semi-transparent background, a congratulatory message, a section
     * displaying time and move statistics, and action buttons. The screen is initially hidden and
     * rendered on top of other elements with maximum Z-index. The statistics labels (time and moves)
     * are stored for dynamic updates. Buttons include "Restart" (resets the game and hides the screen)
     * and "Exit" (triggers game closure). The layout uses nested tables for alignment and spacing,
     * and all elements are grouped for collective positioning and visibility control.
     */
    public void createCongratulationsScreen() {
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
            congratsLabel.setFontScale(1.5f); // Make the text larger
            congratsTable.add(congratsLabel).padBottom(20).row();
    
            // Add time usage placeholder
            Label.LabelStyle altStyle = skin.get("alt", Label.LabelStyle.class);
            Table timeMovesTable = new Table();
            Label movesLabelCongrats = new Label("Moves: 0", altStyle);
            Label timerLabelCongrats = new Label("Time: 00:00", altStyle); // Placeholder for time usage
            movesLabelCongrats.setFontScale(1.1f);
            timerLabelCongrats.setFontScale(1.1f);
    
            timeMovesTable.add(timerLabelCongrats).padRight(30);
            timeMovesTable.add(movesLabelCongrats);
    
            congratsTable.add(timeMovesTable).padBottom(20).row();
    
            // Store the timeLabel for later updates
            this.timerLabelCongrats = timerLabelCongrats;
            this.movesLabelCongrats = movesLabelCongrats;
    
            // Create a horizontal table for the buttons
            Table buttonRow = new Table();
    
            // Add restart button
            TextButton restartButton = new TextButton("Restart", skin);
            restartButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    ;
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
                    klotski.playClickSound();
                    ;
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

    /**
     * Displays the congratulations screen upon successful completion of the game. This method
     * plays a win sound effect once, updates the timer label with the final elapsed time formatted
     * as "MM:SS", and displays the total number of moves taken (current move index +1). It sets
     * congratulatory text, adjusts font scales for visibility, and makes the congratulations UI
     * components visible. Additionally, updates the main moves label in the primary interface
     * to reflect the total moves used. The elapsed time and move count are derived from the
     * game's internal state.
     */
    public void showCongratulationsScreen() {
    
            // Play win sound effect
            if (!winMusicPlayed && winSound != null) {
                winSound.play();
                winMusicPlayed = true;
            }
            // Update the time label with the final elapsed time
            int minutes = (int) (elapsedTime / 60);
            int seconds = (int) (elapsedTime % 60);
            timerLabelCongrats.setText(String.format("Time: %02d:%02d", minutes, seconds));
            timerLabelCongrats.setFontScale(1.0f);
            movesLabelCongrats.setText("Moves: " + (currentMoveIndex + 1));
            movesLabelCongrats.setFontScale(1.0f);
            congratsLabel.setText("Congratulations! You Win!");
            congratsLabel.setFontScale(1.0f);
    
            // Update the moves label with the total moves
            // plus one because index starts from 0
            movesLabel.setText("Moves: " + (currentMoveIndex + 1));
    
            congratulationsGroup.setVisible(true); // Show the congratulations screen
        }

    /**
     * Displays the losing screen with relevant game statistics and updates the UI components accordingly.
     * Plays the lose sound effect once if it has not been played already. Updates the timer label to show
     * the final elapsed time formatted as "MM:SS", sets the "Game Over! You Lose!" message, and displays
     * the total number of moves made. Adjusts font scaling for visibility and makes the losing screen UI
     * components visible.
     */
    public void showLosingScreen() {
            // Play lose sound effect
            if (!loseMusicPlayed && loseSound != null) {
                loseSound.play();
                loseMusicPlayed = true;
            }
            // Update the time label with the final elapsed time
            int minutes = (int) (elapsedTime / 60);
            int seconds = (int) (elapsedTime % 60);
            timerLabelCongrats.setText(String.format("Time: %02d:%02d", minutes, seconds));
            timerLabelCongrats.setFontScale(1.2f);
            congratsLabel.setText("Game Over! You Lose!");
            congratsLabel.setFontScale(1.2f);
            // Update the moves label with the total moves
            movesLabel.setText("Moves: " + (currentMoveIndex + 1));
    
            congratulationsGroup.setVisible(true); // Show the losing screen
        }

    /**
     * Updates the text of the provided {@link TextButton} based on the current auto-solving state.
     * If the system is in an auto-solving state ({@code isAutoSolving} is {@code true}), the button text
     * is set to "Stop". Otherwise, the button text is set to "Auto" to indicate the ability to start auto-solving.
     *
     * @param autoButton The button whose text will be dynamically updated to reflect the current auto-solving status.
     */
    public void updateAutoButtonText(TextButton autoButton) {
            if (isAutoSolving) {
                autoButton.setText("Stop");
            } else {
                autoButton.setText("Auto");
            }
        }

    /**
     * Records a move in the move history, managing the undo/redo state and UI updates. When a new move
     * is added, any existing redoable moves (those beyond the current index in the history) are cleared.
     * The move is stored as a pair of source and destination coordinates, and the current move index
     * is incremented to reflect the new state. Updates the displayed move counter in the UI to show
     * the total number of moves made (current index +1).
     *
     * @param from The source coordinates of the move as a two-element array [x, y]
     * @param to The destination coordinates of the move as a two-element array [x, y]
     */
    public void recordMove(int[] from, int[] to) {
            // Remove any redo history if we are making a new move
            while (moveHistory.size() > currentMoveIndex + 1) {
                moveHistory.remove(moveHistory.size() - 1);
            }
    
            // Add the move to the history
            moveHistory.add(new int[][]{from, to});
            currentMoveIndex++;
    
            movesLabel.setText("Moves: " + (currentMoveIndex + 1));
        }

    /**
     * Reverts the game state to the previous move by applying the reverse of the most recent action.
     * Retrieves the last move from the move history, swaps the 'from' and 'to' positions to reverse the move,
     * and updates the game state accordingly. This method also decrements the current move index, refreshes
     * the UI components (e.g., blocks and moves counter), broadcasts the updated game state to listeners,
     * and logs the undone move details. If no moves are left to undo, a message is logged and no changes are made.
     *
     * <p>The method ensures the game state consistency by calling {@code updateBlocksFromGame} to synchronize
     * the UI with the reversed move and triggers {@code broadcastGameState} to notify external components
     * of the state change. The moves counter label is updated to reflect the current number of moves remaining
     * after the undo operation.
     */
    public void handleUndo() {
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

    /**
     * Redoes the next move in the move history if available. This method checks if there are moves to redo by comparing
     * the current move index to the size of the move history. If a redo is possible, it increments the current move index,
     * retrieves the next move from the history, applies the move to the game state, updates the UI components (including
     * the blocks display and moves counter label), broadcasts the updated game state, and logs the redo action. If no moves
     * are available to redo, a message is logged to indicate this state. Each move in the history is represented as a2D array,
     * where the first element contains the "from" coordinates and the second element contains the "to" coordinates of the move.
     */
    public void handleRedo() {
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

    /**
     * Generates and returns the appropriate save file name based on the logged-in user.
     * If no user is currently logged in (i.e., the username is null or empty), the method
     * returns a default save file name "Guest_save.dat". For registered users, it constructs
     * a unique save file name by appending "_save.dat" to the logged-in username.
     *
     * @return A string representing the save file name, either "Guest_save.dat" for guests
     * or "[username]_save.dat" for authenticated users.
     */
    public String getSaveFileName() {
            String username = klotski.getLoggedInUser();
            if (username == null || username.isEmpty()) {
                return "Guest_save.dat"; // Default save file for guests
            }
            return username + "_save.dat"; // Unique save file for each user
        }

    /**
     * Handles the process of saving the current game state for the logged-in user. This method performs
     * several checks and operations: it verifies that a valid user is logged in (non-Guest and non-empty),
     * generates a save file name, serializes the game state (including piece positions, move history,
     * current move index, and elapsed time), writes the data to a local file, and uploads the save to
     * the server. If the save is triggered automatically (auto-save), it displays a corresponding success
     * message. For manual saves, a user-visible badge confirms the save. Errors during file operations
     * or server communication are caught and displayed as dialogs or console messages.
     *
     * @param autoSave If {@code true}, the save operation is identified as an auto-save, and the user
     * notification reflects this. If {@code false}, a standard save confirmation is shown.
     * @implNote The method enforces user authentication before saving. Serialized data includes a
     * copy of the game's pieces, the move history list, and a {@link GameState} object.
     * The local save file is temporarily created and then uploaded to the server.
     * Error messages are displayed via dialogs for user-facing issues (e.g., login checks)
     * and console logs for technical failures (e.g., I/O exceptions).
     */
    public void handleSave(boolean autoSave) {
            if (klotski.getLoggedInUser() == null
                || klotski.getLoggedInUser().isEmpty()
                || klotski.getLoggedInUser().equals("Guest")) {
                Dialog.showDialog(klotski, skin, stage, "Save Error", "You must be logged in to save the game.");
                return;
            }
    
            String saveFileName = getSaveFileName();
            File file = new File(Gdx.files.getLocalStoragePath(), saveFileName);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Save the positions of all pieces, move history, current move index, and
                // elapsed time
                oos.writeObject(new ArrayList<>(List.of(game.getPieces())));
                oos.writeObject(moveHistory);
                oos.writeObject(new GameState(currentMoveIndex, elapsedTime));
                System.out.println("Game saved successfully for user: " + klotski.getLoggedInUser());
    
                // Now I decided to show the save popup every time
                if (!autoSave)
                    showBadge("Game saved successfully for user: " + klotski.getLoggedInUser());
                else
                    showBadge("Game auto-saved for user: " + klotski.getLoggedInUser());
    
                // print save file content
                System.out.println("Save file content: " + new String(Files.readAllBytes(file.toPath())));
    
                // Upload the save to the server
                uploadSaveToServer(Files.readAllBytes(file.toPath()), autoSave);
            } catch (IOException e) {
                System.err.println("Failed to save game: " + e.getMessage());
                Dialog.showDialog(klotski, skin, stage, "Save Error", "Failed to save game: " + e.getMessage());
            }
        }

    /**
     * Uploads game save data to a remote server by encoding the provided raw byte array into Base64,
     * generating a validation hash, and sending it as part of a JSON payload via an HTTP POST request.
     * The method logs the raw and encoded data lengths, the encoded data itself, and server responses.
     * The JSON payload includes the logged-in username, current timestamp in ISO format, the encoded save
     * data, a hash derived from the encoded data's hash code for validation, and the auto-save flag.
     * The request is sent asynchronously, and failures or cancellations are logged to the error stream.
     *
     * @param saveData the raw byte array containing the game save data to be uploaded
     * @param autoSave indicates whether the upload is triggered automatically (true) or manually (false)
     */
    public void uploadSaveToServer(byte[] saveData, boolean autoSave) {
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

    /**
     * Handles the process of loading a saved game state for the currently logged-in user. This method
     * first verifies that a valid user is logged in (non-Guest and non-empty username). If not logged in,
     * it displays an error dialog. If logged in, it fetches the latest saved game data from the server,
     * decodes the Base64-encoded save data, and deserializes the game state. The deserialized data includes
     * piece positions, move history, current move index, and elapsed time. Upon successful loading, it
     * restarts the game with the saved state, updates UI elements (move counter, timer), and refreshes the
     * game board. Displays appropriate error dialogs for missing save data, decoding failures, or
     * deserialization issues. Handles exceptions related to invalid data formats or I/O errors during
     * the loading process.
     */
    public void handleLoad() {
            String username = klotski.getLoggedInUser();
            if (klotski.getLoggedInUser() == null
                || klotski.getLoggedInUser().isEmpty()
                || klotski.getLoggedInUser().equals("Guest")) {
                Dialog.showDialog(klotski, skin, stage, "Load Error", "You must be logged in to load the game.");
                return;
            }
    
            fetchLatestSaveFromServer(username, saveData -> {
                if (saveData == null) {
                    System.out.println("No save file found for user: " + username);
                    Dialog.showDialog(klotski, skin, stage, "Load Error", "No save file found for user: ");
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
                    Dialog.showDialog(klotski, skin, stage, "Load Error", "Failed to load game: " + e.getMessage());
                }
            });
        }

    /**
     * Fetches the latest saved game data for the specified user from the remote server asynchronously.
     * This method sends an HTTP GET request to the server's save endpoint, appending the provided username
     * as a query parameter. The server response is parsed to extract the most recent save data if available.
     *
     * Upon completion, failure, or cancellation of the request, the provided {@link Consumer} callback
     * is invoked with the result. If the server returns a valid response with a200 status code and non-empty
     * saves, the latest save data string is passed to the callback. If no saves exist, the server returns a
     * non-200 status code, or the request fails due to network issues, the callback receives {@code null}.
     *
     * @param username The username associated with the game saves to retrieve. Included in the request URL.
     * @param callback A {@link Consumer} invoked asynchronously with the latest save data (as a string)
     * or {@code null} if the fetch fails, saves are empty, or an error occurs. The callback
     * handles success cases, server errors, network failures, and request cancellation.
     */
    public void fetchLatestSaveFromServer(String username, Consumer<String> callback) {
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

    /**
     * Handles the process of saving the current game state locally for the logged-in user. Validates that
     * the user is authenticated (not a guest or unregistered) before proceeding. Saves the game state to
     * a file in the local filesystem, including piece positions, move history, current move index, and
     * elapsed time. Displays appropriate success or error feedback to the user via dialogs and badges.
     *
     * <p>If the user is not logged in, a "Save Error" dialog is shown, and the save is aborted. On success,
     * a badge notification confirms the save, with distinct messages for manual and auto-save events.
     *
     * <p>The save file is generated using a predefined or dynamically determined filename, stored in the
     * application's configuration directory. Serializes game data using {@link ObjectOutputStream} and
     * handles potential {@link IOException} errors by displaying the error message in a dialog.
     *
     * @param autoSave If {@code true}, indicates the save was triggered automatically (e.g., periodic
     * auto-save), and the success badge reflects this. If {@code false}, confirms a manual
     * user-initiated save.
     */
    public void handleLocalSave(boolean autoSave) {
            // TODO: refactor to math the online method
            if (klotski.getLoggedInUser() == null
                || klotski.getLoggedInUser().isEmpty()
                || klotski.getLoggedInUser().equals("Guest")) {
                Dialog.showDialog(klotski, skin, stage, "Save Error", "You must be logged in to save the game.");
                return;
            }
    
            String saveFileName = getSaveFileName();
            File file = new File(ConfigPathHelper.getConfigFilePath("Klotski", saveFileName));
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Save the positions of all pieces, move history, current move index, and
                // elapsed time
                oos.writeObject(new ArrayList<>(List.of(game.getPieces())));
                oos.writeObject(moveHistory);
                oos.writeInt(currentMoveIndex);
                oos.writeFloat(elapsedTime);
                System.out.println("Game saved successfully for user: " + klotski.getLoggedInUser());
                if (!autoSave)
                    showBadge("Game saved successfully for user: " + klotski.getLoggedInUser());
                else
                    showBadge("Game auto-saved for user: " + klotski.getLoggedInUser());
            } catch (IOException e) {
                System.err.println("Failed to save game: " + e.getMessage());
                Dialog.showDialog(klotski, skin, stage, "Save Error", "Failed to save game: " + e.getMessage());
            }
        }

    /**
     * Handles the process of loading a locally saved game state for the current logged-in user.
     * This method first verifies that a valid user is logged in (non-guest and non-empty), displays
     * an error dialog if not authenticated. Checks for the existence of the user-specific save file,
     * showing an error if missing. If valid, deserializes the saved game state including piece positions,
     * move history, current move index, and elapsed time. Updates the game board, UI labels (moves and timer),
     * and internal state to reflect the loaded data. Displays success/failure notifications via dialogs
     * and logs relevant status messages. Handles file I/O exceptions and class deserialization errors
     * during the load process.
     */
    public void handleLocalLoad() {
            if (klotski.getLoggedInUser() == null
                || klotski.getLoggedInUser().isEmpty()
                || klotski.getLoggedInUser().equals("Guest")) {
                Dialog.showDialog(klotski, skin, stage, "Load Error", "You must be logged in to load the game.");
                return;
            }
    
            String saveFileName = getSaveFileName();
            File file = new File(ConfigPathHelper.getConfigFilePath("Klotski", saveFileName));
            if (!file.exists()) {
                System.out.println("No save file found for user: " + klotski.getLoggedInUser());
                Dialog.showDialog(klotski, skin, stage, "Load Error", "No save file found for user: ");
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
                Dialog.showDialog(klotski, skin, stage, "Load Error", "Failed to load game: " + e.getMessage());
            }
        }

    /**
     * Retrieves the current instance of the KlotskiGame managed by this object.
     * This method provides access to the game's state, configuration, and logic,
     * allowing external interaction with the game board, moves, and other components.
     * The returned object represents the live game instance, and any modifications
     * to it will directly affect the ongoing game session.
     *
     * @return the active {@link KlotskiGame} instance associated with this context.
     */
    public KlotskiGame getGame() {
            return game;
        }

    /**
     * Hides the screen and performs necessary cleanup and state restoration tasks. This method
     * disables input processing by setting the input processor to {@code null}, cancels any active
     * auto-save task to prevent unintended background operations, restores the background music (BGM)
     * volume to full (1.0) if BGM is present, and triggers an animation to revert the focal length
     * of the dynamic board to its default state. This ensures resources are managed correctly and
     * visual/audio settings reset when the screen is no longer visible.
     */
    @Override
        public void hide() {
            Gdx.input.setInputProcessor(null);
    
            // Cancel the auto-save task when the screen is hidden
            if (autoSaveTask != null) {
                autoSaveTask.cancel();
            }
    
            Music bgm = klotski.getBackgroundMusic();
            if (bgm != null) {
                bgm.setVolume(1.0f);
            }
    
            klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
        }

    /**
     * Resumes the operation or activity that was previously paused. This method restarts the processing,
     * execution, or resource utilization associated with the component, transitioning it from a paused
     * state to an active state. If the component was not in a paused state, calling this method may have
     * no effect. Implementations should ensure thread safety and handle any necessary state transitions,
     * error checks, or resource reinitialization required to resume operation correctly. This method
     * typically mirrors the behavior defined in the {@link #pause()} method, reversing its effects.
     */
    @Override
        public void resume() {
        }

    /**
     * Pauses the execution of the current thread or process managed by this instance.
     * This method temporarily halts ongoing operations, allowing them to be resumed later
     * via the {@link #resume()} method. While paused, all internal state and resources are
     * maintained in their current condition. If the thread or process is not in a running
     * state, invoking this method has no effect. Implementations should ensure thread safety
     * and handle potential interruptions or concurrency issues.
     *
     * @throws IllegalStateException if the operation cannot be paused due to the current
     * state of the object (e.g., already paused, terminated, or uninitialized).
     */
    @Override
        public void pause() {
        }

    /**
     * Overrides the show method to configure and initialize the game's display and auto-save functionality.
     * Sets the input processor to the provided stage to handle user input. Triggers an animation for the
     * game board's focal length over a duration of10 seconds, ending with a focal length of1.0. Schedules
     * a recurring auto-save task every30 seconds, which saves the game state either remotely (if online)
     * or locally (if offline). The auto-save task runs indefinitely until canceled. A confirmation message
     * is printed to the console upon each successful auto-save.
     */
    @Override
        public void show() {
            //klotski.dynamicBoard.setStage(stage);
            klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
            Gdx.input.setInputProcessor(stage);
    
            // Schedule auto-save every 30 seconds
            autoSaveTask = Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (!klotski.isOfflineMode())
                        handleSave(true); // Call the save method
                    else
                        handleLocalSave(true);
                    System.out.println("Game auto-saved.");
                }
            }, 30, 30); // Delay of 30 seconds, repeat every 30 seconds
        }

    /**
     * Shuffles the elements within the game using a specified seed to ensure deterministic results,
     * then updates the associated blocks to reflect the shuffled state of the game.
     * The provided seed initializes the random number generator, guaranteeing that the same seed
     * will produce the same shuffle order across executions. After shuffling, this method synchronizes
     * the state of the blocks with the newly shuffled game data.
     *
     * @param seed The seed value used to initialize the random number generator for reproducible shuffling.
     */
    public void randomShuffle(long seed) {
            game.randomShuffle(seed);
            updateBlocksFromGame(game);
        }

    /**
     * Randomly shuffles the elements within the game and updates the associated blocks to reflect
     * the new shuffled state. This method delegates the shuffling logic to the underlying game instance,
     * ensuring the game's internal state is randomized. After shuffling, it synchronizes the block
     * representations with the updated game state to maintain consistency between the game logic
     * and visual or structural components.
     */
    public void randomShuffle() {
            game.randomShuffle();
            updateBlocksFromGame(game);
        }

    /**
     * Broadcasts the current game state to all connected WebSocket clients. This method converts the
     * game state into a string representation using {@link Game#toString()}, then sends it to all
     * clients via the WebSocket server's broadcast mechanism. Additionally, if an external WebSocket
     * client connection exists (e.g., for an external service or UI), it sends the game state directly
     * to that client. If no external client is connected, a message is logged indicating the absence
     * of a connection. Exceptions during broadcasting are caught and logged as errors.
     */
    public void broadcastGameState() {
            String gameState = game.toString();
            try {
                klotski.getWebSocketServer().broadcastGameState(gameState);
                if (klotski.getWebSocketClient() != null)
                    klotski.getWebSocketClient().sendBoardState(gameState);
                else
                    System.out.println("WebSocket client is not connected.");
            } catch (Exception e) {
                System.err.println("Failed to broadcast game state: " + e.getMessage());
            }
        }

    /**
     * Displays a temporary badge message on the screen. The badge is positioned in the bottom-right corner
     * of the screen (offset by390 pixels from the right edge and20 pixels from the bottom). The message
     * text is scaled to80% of the default font size. If a previous badge is still visible, its scheduled
     * hide action is canceled to ensure the new message remains visible for the full duration. The badge
     * automatically hides after3 seconds unless overridden by a subsequent call to this method.
     *
     * @param message The text to display in the badge. The badge will adjust its visibility and position
     * based on the current screen dimensions.
     */
    public void showBadge(String message) {
            if (badgeHideTask != null) {
                badgeHideTask.cancel();
            }
            Label badgeLabel = badgeGroup.findActor("badgeLabel");
            badgeLabel.setText(message);
            badgeLabel.setFontScale(0.8f);
            badgeGroup.setVisible(true);
            badgeGroup.toFront();
    
            badgeGroup.setPosition(Gdx.graphics.getWidth() - 390, 20);
    
            badgeHideTask = Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    badgeGroup.setVisible(false);
                }
            }, 3);
        }

    /**
     * Sets the current game level and initializes the game state based on the specified level data.
     * This method loads the level data file corresponding to the provided {@code level} parameter from
     * the resources directory, parses the data to update the game state, synchronizes the visual block
     * representations with the updated state, and broadcasts the new game state to all listeners.
     * The level data file is expected to be located in the {@code levels/} directory with a name
     * following the format {@code levelX.dat}, where {@code X} is the integer level number.
     *
     * @param level The level number to load. This determines which level data file is read
     * (e.g., {@code level5.dat} for {@code level=5}).
     */
    public void setLevel(int level) {
            this.level = level;
            // get the level data file from the resources
            String levelDataFile = "levels/level" + level + ".dat";
            // read the whole file as a string
            String levelData = Gdx.files.internal(levelDataFile).readString();
            // parse the level data
            game.fromString(levelData);
            // update the blocks from the game state
            updateBlocksFromGame(game);
            broadcastGameState();
        }
}
