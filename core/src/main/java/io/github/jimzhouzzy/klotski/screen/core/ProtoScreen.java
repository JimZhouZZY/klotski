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
 * @version 1.17
 * @since 2025-5-25
 * @see {@link com.badlogic.gdx.Screen}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
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
            /**
             * Handles the touch down event by changing the application cursor to a custom resized "clicked" image.
             * This method loads the "clicked.png" image from the assets directory, resizes it to32x32 pixels,
             * and creates a new cursor with a hotspot offset at (7,1) relative to the cursor's top-left corner.
             * The original and resized pixmaps are disposed after cursor creation to free resources.
             * The newly created cursor is then set as the active cursor for the application.
             *
             * @return {@code true} to indicate the event has been processed and should not propagate further.
             */
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

            /**
             * Handles the touch-up event by creating and setting a custom cursor. This method loads a cursor image
             * from the specified assets path, resizes it to32x32 pixels, defines a hotspot at the coordinates (7,1)
             * relative to the cursor's top-left corner, and sets the newly created cursor as the active system cursor.
             * The original and resized pixmaps are disposed after cursor creation to free up resources.
             *
             * @param event The {@link InputEvent} associated with the touch-up action.
             * @param x The x-coordinate of the touch-up event in screen coordinates.
             * @param y The y-coordinate of the touch-up event in screen coordinates.
             * @param pointer The pointer index for the event.
             * @param button The button that was released (e.g., {@link Buttons#LEFT}).
             */
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
            /**
             * Handles the key down event for specific key codes. This method is invoked when a key is pressed.
             * <p>
             * This method processes the {@link Input.Keys#ESCAPE} key code. When the ESCAPE key is pressed,
             * it triggers the {@link #handleBack()} method to handle navigation or cancellation logic.
             * Other key codes are ignored, allowing event propagation to other listeners.
             * </p>
             *
             * @param event The {@link InputEvent} associated with the key press, containing event details.
             * @param keycode The key code of the pressed key, as defined in {@link Input.Keys}.
             * @return {@code true} if the ESCAPE key was processed (consuming the event),
             * {@code false} otherwise, allowing further processing by other listeners.
             * @Override
             */
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

    /**
     * Initializes and configures the primary stage and core UI components required for the application.
     * This method is intended to be overridden by subclasses to define the specific layout, controls,
     * and event handlers necessary for the application's interface. It is typically invoked during the
     * startup phase to set the scene, configure stage properties (e.g., title, dimensions), and populate
     * the scene graph with root nodes such as layouts, containers, and interactive elements. Implementations
     * should ensure all critical UI elements are properly instantiated and linked before the stage is displayed.
     */
    protected void create() {
            // Override and set up the stage and UI elements here
        }

    /**
     * Handles the back action triggered by pressing the ESC key. This method is intended to be overridden
     * by subclasses to provide custom behavior when navigating back. By default, it navigates back to the
     * application's main screen by setting the current screen of the {@code klotski} instance to the
     * {@code mainScreen}. Implementations should ensure any necessary cleanup or state management is performed
     * before or after the navigation occurs.
     */
    protected void handleBack() {
            // Override to handle ESC key press
            // Default navigate back to the main screen
            klotski.setScreen(klotski.mainScreen);
        }

    /**
     * Renders the game components and updates the UI stage each frame. This method is part of the game loop
     * and is called continuously to refresh the display. It performs the following steps:
     * <ol>
     * <li>Clears the screen using the background color specified in the {@code klotski} configuration.</li>
     * <li>Renders the dynamic board, which includes updating and drawing all moving or interactive game elements
     * (e.g., puzzle pieces, animations) using the provided delta time.</li>
     * <li>Updates the UI stage by processing actor logic (e.g., button clicks, input handling) via {@link #stage#act(float)}.</li>
     * <li>Draws the UI stage to display all static or dynamic UI components (e.g., buttons, labels, menus).</li>
     * </ol>
     *
     * @param delta The time in seconds since the last render call, used to interpolate animations and updates.
     */
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

    /**
     * Resizes the viewport associated with the stage to the specified dimensions and centers the camera.
     * This method is typically called when the application window is resized to ensure proper rendering
     * of the stage's contents. The viewport's dimensions are updated, and the camera is repositioned
     * to the center of the new viewport.
     *
     * @param width The new width (in pixels) to which the viewport should be resized.
     * @param height The new height (in pixels) to which the viewport should be resized.
     */
    @Override
        public void resize(int width, int height) {
            stage.getViewport().update(width, height, true);
        }

    /**
     * Disposes of all resources associated with this object to free up memory and ensure clean destruction.
     * This method explicitly calls {@code dispose()} on the {@code stage} and {@code skin} objects managed
     * by this instance, releasing their underlying resources, closing associated windows, and removing
     * any listeners or native dependencies. After invocation, this object and its components should no
     * longer be used. It overrides the parent method to provide custom cleanup behavior specific to
     * this implementation.
     */
    @Override
        public void dispose() {
            stage.dispose();
            skin.dispose();
        }

    /**
     * Sets the {@link InputProcessor} to {@code null}, disabling all input event handling for this context.
     * This ensures that no further touch, keyboard, mouse, or other input events will be processed by the
     * application until a new input processor is explicitly set. Typically called when the associated
     * component or screen is hidden or deactivated to prevent unintended interaction.
     */
    @Override
        public void hide() {
            Gdx.input.setInputProcessor(null);
        }

    /**
     * Resumes the execution or operation of the component, typically after a prior suspension. This method
     * restores internal state, reactivates necessary resources, or reinitializes processes required for
     * normal functionality. If the component was not in a paused or suspended state, invoking this method
     * may have no observable effect. Implementations should ensure thread safety and handle any state
     * transitions appropriately. Overrides the parent class or interface method to provide specific
     * resume behavior for this component.
     */
    @Override
        public void resume() {
        }

    /**
     * Pauses the current operation or process managed by this instance. This method temporarily halts
     * ongoing activities, such as processing tasks, resource utilization, or background threads,
     * allowing them to be resumed later via a corresponding {@link #resume()} method. Implementations
     * should ensure that internal state is preserved during the pause to enable seamless continuation.
     * If the operation is already paused, invoking this method has no effect. Overrides must adhere
     * to the contract defined by the superclass or interface this method implements.
     *
     * @see #resume()
     */
    @Override
        public void pause() {
        }

    /**
     * Sets the input processor to the provided stage and initializes the dynamic board component
     * with the same stage. This method prepares the UI components to receive input events (e.g., touch,
     * keyboard) and ensures the game board's interactive elements are linked to the current stage.
     * Overrides the parent class's implementation to configure input handling and board state updates
     * specific to the Klotski game's UI requirements.
     */
    @Override
        public void show() {
            Gdx.input.setInputProcessor(stage);
            klotski.dynamicBoard.setStage(stage);
        }
}
