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
 * @version 1.5
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-27: Generated comment
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

    /**
     * Overrides the {@code create()} method to set up the stage and UI elements, including keyboard and touch input handling.
     * Registers an {@link InputListener} to the stage to manage the following interactions:
     * <ul>
     * <li>Keyboard events:
     * <ul>
     * <li>{@link Input.Keys#ESCAPE}: Triggers {@link #handleBack()} to handle navigation or exit.</li>
     * <li>{@link Input.Keys#R}, {@link Input.Keys#I}, {@link Input.Keys#U}, {@link Input.Keys#Y}, {@link Input.Keys#A}:
     * Placeholders for restart, hint, undo, redo, and auto-solve actions (commented out).</li>
     * <li>{@link Input.Keys#SPACE} and {@link Input.Keys#ENTER}: Placeholders for toggling auto-solving (commented out).</li>
     * <li>Arrow keys ({@link Input.Keys#LEFT}, {@link Input.Keys#UP}, etc.): Placeholders for directional input handling (commented out).</li>
     * </ul>
     * </li>
     * <li>Touch input events:
     * <ul>
     * <li>Changes the cursor to a custom "clicked" image on {@link #touchDown(InputEvent, float, float, int, int)}.</li>
     * <li>Reverts the cursor to a default image on {@link #touchUp(InputEvent, float, float, int, int)}.</li>
     * <li>Handles Pixmap loading, resizing, hotspot configuration, and resource disposal for cursor changes.</li>
     * </ul>
     * </li>
     * </ul>
     * This method initializes the core interaction layer for the application's UI.
     */
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

    /**
     * Handles the action triggered by pressing the ESC key by default, navigating back to the main screen
     * of the Klotski application. This method is intended to be overridden by subclasses to provide
     * custom behavior for back navigation, but the default implementation sets the application's
     * screen to the predefined main screen. Override this method to modify the navigation logic or
     * add additional actions when the back event occurs.
     */
    protected void handleBack() {
            // Override to handle ESC key press
            // Default navigate back to the main screen
            klotski.setScreen(klotski.mainScreen);
        }

    /**
     * Renders the game elements by performing the following steps each frame:
     * <ol>
     * <li>Clears the screen using the background color defined in the {@code klotski} instance.</li>
     * <li>Renders the dynamic Klotzki game board, updating its state based on the elapsed time.</li>
     * <li>Updates and draws the UI stage, handling actor logic and rendering UI components.</li>
     * </ol>
     * This method is called every frame as part of the game loop to ensure continuous visual updates.
     *
     * @param delta The time in seconds since the last render call, used for frame-rate-independent updates.
     */
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

    /**
     * Updates the viewport dimensions of the associated stage and centers the camera. This method
     * is typically called when the application window is resized, ensuring the viewport adjusts to
     * the new width and height while maintaining proper scaling and aspect ratio. The camera's
     * position is set to the center of the updated viewport, which is critical for rendering
     * content correctly after resizing. This method delegates the update to the stage's viewport
     * and applies the changes immediately.
     *
     * @param width The new width of the viewport in pixels.
     * @param height The new height of the viewport in pixels.
     */
    @Override
        public void resize(int width, int height) {
            stage.getViewport().update(width, height, true);
        }

    /**
     * Disposes of all resources associated with this object, including the {@link Stage}, {@link Skin},
     * and {@link KlotzkiBoard} instances, and resets the input processor to {@code null}. This method
     * ensures proper cleanup of LibGDX-specific resources to prevent memory leaks. It should be called
     * when this object is no longer needed to release internal resources and unregister input handling.
     */
    @Override
        public void dispose() {
            Gdx.input.setInputProcessor(null);
            stage.dispose();
            skin.dispose();
            klotzkiBoard.dispose();
        }

    /**
     * Hides this component and disables input processing by setting the input processor to {@code null}.
     * This ensures that no further input events (e.g., touch, keyboard, or mouse events) will be handled
     * by this component or any underlying components until another input processor is explicitly set.
     * Overrides the parent method to provide specific handling for input termination during component teardown.
     */
    @Override
        public void hide() {
            Gdx.input.setInputProcessor(null);
        }

    /**
     * Resumes the operation or activity that was previously paused. This method restores the state
     * or resources required for normal execution, allowing the process to continue from the point
     * it was suspended. Implementations should ensure thread safety and handle any necessary
     * synchronization or reinitialization of internal components. If the operation is already
     * running or cannot be resumed, this method may have no effect. Overrides should document
     * any additional preconditions, postconditions, or exceptions specific to their context.
     */
    @Override
        public void resume() {
        }

    /**
     * Pauses the current operation or process managed by this instance. When invoked, this method
     * temporarily halts execution or processing, allowing it to be resumed later from the same state.
     * Implementations should ensure that internal state and resources are preserved during the pause.
     * If the operation is already paused or not active, calling this method may have no effect.
     */
    @Override
        public void pause() {
        }

    /**
     * Sets the stage as the input processor for handling user input events (e.g., touch, keyboard)
     * and initializes the {@code klotzkiBoard} component with the provided stage. This method ensures
     * that UI elements within the stage can respond to user interactions and that the game board
     * is properly linked to the stage for rendering and input processing. Overrides the parent class
     * implementation to configure the stage and board during the display lifecycle.
     */
    @Override
        public void show() {
            Gdx.input.setInputProcessor(stage);
            klotzkiBoard.setStage(stage);
        }
}
