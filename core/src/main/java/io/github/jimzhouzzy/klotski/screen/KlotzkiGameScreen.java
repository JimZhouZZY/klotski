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
 * KlotzkiGameScreen.java
 * 
 * This class represents the game screen in the Klotzki game.
 * 
 * @author JimZhouZZY
 * @version 1.4
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: HD-font & UX improvement
 * 2025-05-26: refactor screens & add Kltozki game
 */

package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.KlotzkiBoard;

public class KlotzkiGameScreen extends ApplicationAdapter implements Screen {
    
    protected final Klotski klotski;
    protected final Stage stage;
    protected final Skin skin;
    private final KlotzkiBoard klotzkiBoard;

    public KlotzkiGameScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json")); 

        klotzkiBoard = new KlotzkiBoard(klotski, stage);
        create();
    }

    @Override
    public void create() {
        // Override and set up the stage and UI elements here
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ESCAPE:
                        handleBack(); // Handle exit when ESC is pressed
                        return true;
                    case Input.Keys.R:
                        // handleRestart(game); // Handle restart when R is pressed
                        return true;
                    case Input.Keys.I:
                        // handleHint(game); // Handle hint when H is pressed
                        return true;
                    case Input.Keys.U:
                        // handleUndo(); // Handle undo when U is pressed
                        return true;
                    case Input.Keys.Y:
                        // handleRedo(); // Handle redo when Y is pressed
                        return true;
                    case Input.Keys.A:
                        // handleAutoSolve(game, autoButton); // Handle auto-solving when A is pressed
                        return true;
                    case Input.Keys.SPACE:
                        // Handle space key for auto-solving
                        // if (isAutoSolving) {
                        //     stopAutoSolving(); // Stop auto-solving if already active
                        //     autoButton.setText("Auto"); // Change button text back to "Auto"
                        //} else {
                        //    handleAutoSolve(game, autoButton); // Start auto-solving
                        //}
                        //return true;
                    case Input.Keys.ENTER:
                        // Handle enter key for auto-solving
                        //if (isAutoSolving) {
                        //    stopAutoSolving(); // Stop auto-solving if already active
                        //    autoButton.setText("Auto"); // Change button text back to "Auto"
                        //} else {
                        //    handleAutoSolve(game, autoButton); // Start auto-solving
                        //}
                        //return true;
                    case Input.Keys.LEFT:
                        // Handle left arrow key for moving blocks
                        //handleArrowKeys(new int[]{0, -1});
                        //return true;
                    case Input.Keys.UP:
                        // Handle left arrow key for moving blocks
                        //handleArrowKeys(new int[]{-1, 0});
                        //return true;
                    case Input.Keys.RIGHT:
                        // Handle left arrow key for moving blocks
                        //handleArrowKeys(new int[]{0, 1});
                        //return true;
                    case Input.Keys.DOWN:
                        // Handle left arrow key for moving blocks
                        //handleArrowKeys(new int[]{1, 0});
                        //return true;
                    // Handle number keys 0-9 and numpad 0-9 for block selection
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
    }

    protected void handleBack() {
        // Override to handle ESC key press
        // Default navigate back to the main screen
        klotski.setScreen(klotski.mainScreen);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(klotski.getGlClearColor());

        // Render the dynamic board
        klotzkiBoard.render(delta);

        // Render the stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        stage.dispose();
        skin.dispose();
        klotzkiBoard.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        klotzkiBoard.setStage(stage);
    }
}
