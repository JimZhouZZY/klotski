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
 *
 * This class represents the main game screen of the Klotski game.
 * It is enheritaged by {@link SpectateScreen} and {@link CooperateScreen}.
 *
 * @author JimZhouZZY
 * @version 1.58
 * @since 2025-5-25
 *
 * KNOWN ISSUES:
 * 1. The move count is incorrect when the user dragged a piece
 *    across multiple grid.
 * 2. Restart in an leveled (seedly random shuffeled) game won't
 *    reset the game to the shuffeled state.
 *
 * Change log:
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
    private long randomSeed;

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

    public void setGameMode(boolean isAttackMode) {
        this.isAttackMode = isAttackMode;
    }

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
                                handleArrowKeys(new int[] { -1, 0 });
                                break;
                            case "Down":
                                handleArrowKeys(new int[] { 1, 0 });
                                break;
                            case "Left":
                                handleArrowKeys(new int[] { 0, -1 });
                                break;
                            case "Right":
                                handleArrowKeys(new int[] { 0, 1 });
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
                                }else{
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

    private RectangleBlockActor findBlockByOffset(RectangleBlockActor current, int rowOffset, int colOffset) {
        if (current == null) return null;
        KlotskiGame.KlotskiPiece currentPiece = game.getPiece(current.pieceId);
        int targetRow = currentPiece.getRow();
        int targetCol = currentPiece.getCol();
        int k = 0;
        while (targetRow >= 0 && targetCol >= 0 && targetRow<=4 && targetCol<=3) {
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

    private void handleArrowKeys(int[] direction) {
        if (selectedBlock.pieceId == blockedId) return;
        if (selectedBlock == null || selectedBlock.pieceId == blockedId) {
            handleAutoArrowKeys(direction);
            return;
        }

        KlotskiGame.KlotskiPiece piece = game.getPiece(selectedBlock.pieceId);
        int fromRow = piece.getRow();
        int fromCol = piece.getCol();
        int toRow = fromRow + direction[0];
        int toCol = fromCol + direction[1];

        if (!game.isLegalMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol })) return;

        float targetX = toCol * cellSize;
        float targetY = (rows - toRow - piece.height) * cellSize;

        game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
        piece.setPosition(new int[] { toRow, toCol });
        recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
        isTerminal = game.isTerminal();
        broadcastGameState();

        selectedBlock.addAction(Actions.moveTo(targetX, targetY, 0.1f));
    }

    // Helper method to assign colors to pieces
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

    public void updateBlocksFromGame(KlotskiGame game) {
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

        if (badgeGroup != null) {
            badgeGroup.setPosition(width - 320, 20);
        }

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

        // Reset the game logic
        game.initialize();

        // Update the blocks to match the game state
        updateBlocksFromGame(game);

        // Reset terminal state
        isTerminal = false;
        winMusicPlayed = false;
        loseMusicPlayed = false;

        broadcastGameState();

        System.out.println("Game restarted.");
    }

    public void handleHint(KlotskiGame game) {
        // Get the solution from the solver
        List<String> solution = KlotskiSolver.solve(game, blockedId);

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

    public void handleAutoSolve(KlotskiGame game, TextButton autoButton) {
        if (isAutoSolving()) {
            stopAutoSolving(); // Stop auto-solving if already active
            autoButton.setText("Auto"); // Change button text back to "Auto"
        } else {
            solution = null; // Clear the previous solution
            solutionIndex = 0; // Reset the solution index
            List<String> newSolution = KlotskiSolver.solve(game, blockedId); // Get the new solution

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

    public void handleExit() {
        // reset
        winMusicPlayed = false;
        loseMusicPlayed = false;

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

    public void updateAutoButtonText(TextButton autoButton) {
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

    public String getSaveFileName() {
        String username = klotski.getLoggedInUser();
        if (username == null || username.isEmpty()) {
            return "Guest_save.dat"; // Default save file for guests
        }
        return username + "_save.dat"; // Unique save file for each user
    }

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

        Music bgm = klotski.getBackgroundMusic();
        if (bgm != null) {
            bgm.setVolume(1.0f);
        }

        klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

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

    public void showBadge(String message) {
        if (badgeHideTask != null) {
            badgeHideTask.cancel();
        }
        Label badgeLabel = badgeGroup.findActor("badgeLabel");
        badgeLabel.setText(message);
        badgeGroup.setVisible(true);
        badgeGroup.toFront();

        badgeGroup.setPosition(
            (Gdx.graphics.getWidth() - badgeGroup.getWidth()) / 2f,
            Gdx.graphics.getHeight() * 0.5f
        );

        badgeHideTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                badgeGroup.setVisible(false);
            }
        }, 3);
    }

    public void setLevel(int level) {
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
