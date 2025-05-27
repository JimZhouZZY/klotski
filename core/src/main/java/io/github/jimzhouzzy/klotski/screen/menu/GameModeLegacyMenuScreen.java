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
 * GameModeLegacyMenuScreen.java
 *
 * This class represents the legacy game mode menu screen in the Klotski game.
 * It allows players to choose between different legacy game modes such as Classical, Shuffled, and 3min-Attack.
 *
 * @author JimZhouZZY
 * @version 1.12
 * @since 2025-5-25
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: Multilevel for blocked
 * 2025-05-27: UI improvement
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: multiple classical level
 * 2025-05-27: make GameScreen seperate
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 */

package io.github.jimzhouzzy.klotski.screen.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.GameScreen;
import io.github.jimzhouzzy.klotski.screen.core.MenuScreen;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;

public class GameModeLegacyMenuScreen extends MenuScreen {

    public GameModeLegacyMenuScreen(final Klotski klotski, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
    }

    /**
     * Creates and configures the UI elements for the Legacy Game Modes menu screen. This method sets up
     * a title label and multiple buttons for navigating between different legacy game modes.
     * <p>
     * The UI includes:
     * - A "Classical" button that navigates to a level selection screen for classic game modes.
     * - A "Shuffled" button that starts a randomly generated game session using a random seed.
     * - A "Shuffle-Attack" button that initiates a time-limited shuffled game session.
     * - A "Blocked" button that navigates to a specialized level selection screen for blocked puzzles.
     * - A "Back" button to return to the previous screen.
     * <p>
     * Each button triggers a click sound effect upon interaction. The layout uses a {@link Table}
     * for organization, with consistent styling, padding, and dimensions for all elements. The method
     * overrides the parent class's {@code create()} method to define screen-specific UI logic.
     */
    @Override
        protected void create() {
            // Create a table for layout
            Table table = new Table();
            table.setFillParent(true);
            stage.addActor(table);
    
            // Add a title label
            Label titleLabel = new Label("Legacy Game Modes", skin);
            titleLabel.setFontScale(1.5f);
            table.add(titleLabel).padBottom(50).row();
    
            // Add "Classical" button
            TextButton classicalButton = new TextButton("Classical", skin);
            classicalButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    klotski.setScreen(new GameModeLegacyLevelMenuScreen(klotski, GameModeLegacyMenuScreen.this));
                }
            });
            table.add(classicalButton).width(300).height(50).padBottom(20).row();
    
            // Add "Random Shuffled" button
            TextButton freeGameButton = new TextButton("Shuffled", skin);
            freeGameButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    GameScreen gameScreen = new GameScreen(klotski, (long) klotski.randomHelper.nextInt(114514), false);
                    klotski.setGameScreen(gameScreen); // Set the game screen
                    klotski.setScreen(gameScreen); // Navigate to the game screen
                }
            });
            table.add(freeGameButton).width(300).height(50).padBottom(20).row();
    
            // Add "3min-Attack" button
            TextButton attackModeButton = new TextButton("Shuffle-Attack", skin);
            attackModeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    GameScreen gameScreen = new GameScreen(klotski, (long) klotski.randomHelper.nextInt(114514), true);
                    klotski.setGameScreen(gameScreen); // Set the game screen
                    klotski.setScreen(gameScreen); // Navigate to the game screen
                }
            });
            table.add(attackModeButton).width(300).height(50).padBottom(20).row();
    
            // Add "blocked" button
            TextButton blockedButton = new TextButton("Blocked", skin);
            blockedButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    klotski.setScreen(new GameModeBlockedLevelMenuScreen(klotski, GameModeLegacyMenuScreen.this));
                    //GameScreen gameScreen = new GameScreen(klotski, 2);
                    //gameScreen.setGameMode(false); // Set to 3min-Attack mode
                    //klotski.setGameScreen(gameScreen); // Set the game screen
                    //klotski.setScreen(gameScreen); // Navigate to the game screen
                }
            });
            table.add(blockedButton).width(300).height(50).padBottom(20).row();
    
            // Add "Back" button
            TextButton backButton = new TextButton("Back", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    handleBack();
                }
            });
            table.add(backButton).width(300).height(50);
        }

    /**
     * Disposes of resources associated with this object to facilitate garbage collection and prevent memory leaks.
     * This method overrides the default dispose behavior to perform the following actions in sequence:
     *1. Triggers an animation revert for the focal length on the dynamic board, resetting any active visual transformations.
     *2. Releases system resources tied to the primary display stage, including its rendering context and input handlers.
     *3. Disposes of the skin resources, freeing textures, fonts, and other UI-related assets from memory.
     * This ensures orderly cleanup when the object is no longer in use or the application is terminated.
     */
    @Override
        public void dispose() {
            klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
            stage.dispose();
            skin.dispose();
        }

    /**
     * Hides the view and performs necessary cleanup operations. This method triggers an animation to revert
     * the focal length of the dynamic board component, restoring it to its default state. It also disables
     * further input processing by setting the input processor to {@code null}, ensuring no interactions
     * occur while the component is hidden. Overrides the parent implementation to handle view-specific teardown.
     */
    @Override
        public void hide() {
            klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
            Gdx.input.setInputProcessor(null);
        }
}
