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
 * ProtoScreen.java
 * 
 * Abstract base class for all game screens in the Klotski game.
 * Provides common functionality for screen management, UI rendering, and input handling
 * using LibGDX's {@link Stage} and {@link Skin} system.
 * 
 * Subclasses should override {@link #create()} and {@link #handleBack()} to initialize 
 * their specific UI components, and may optionally override other {@link Screen} interface
 * methods as needed.
 * 
 * @author JimZhouZZY
 * @version 1.13
 * @since 2025-5-25
 * @see {@link com.badlogic.gdx.Screen}
 * 
 * Change log:
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-24: Add H,J,K,L for changing the selected  block.
 * 2025-05-24: fix: bad init process of HelpScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-22: Fix the HelpScreen's BUG! (#11)
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 */

package io.github.jimzhouzzy.klotski.screen.core;

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

public class ProtoScreen implements Screen {
    
    protected final Klotski klotski;
    protected final Stage stage;
    protected final Skin skin;

    public ProtoScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));    

        // Event listener for mouse click
        stage.addListener(new InputListener() {
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

        // Event listener for keyboard stroke
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ESCAPE:
                        handleBack();
                        return true;
                }
                return false;
            }
        });

        create();
    }

    protected void create() {
        // Override and set up the stage and UI elements here
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
        klotski.dynamicBoard.render(delta);

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
        stage.dispose();
        skin.dispose();
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
        klotski.dynamicBoard.setStage(stage);
    }
}
