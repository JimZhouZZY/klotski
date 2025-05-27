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
 * GameModeMenuScreen.java
 *
 * This class represents the game mode selection screen in the Klotski game.
 *
 * @author JimZhouZZY
 * @version 1.33
 * @since 2025-5-25
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: make GameScreen seperate
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
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-24: Highlight the number of the selected number
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 * 2025-05-08: update soundfx
 * 2025-05-07: formal login & prepare in-game spectate
 * 2025-04-29: web inspection
 * 2025-04-29: focal length animation
 * 2025-04-25: Revert 'fix:resize'
 * 2025-04-24: fix resize changed base tile size
 * 2025-04-24: MSAA & Settings
 * 2025-04-23: better main screen
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
 * 2025-04-21: resizable
 * 2025-04-16: Login & Levels
 * 2025-04-16: Login & Game Mode & Save-Load
 */

package io.github.jimzhouzzy.klotski.screen.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.KlotzkiGameScreen;
import io.github.jimzhouzzy.klotski.screen.core.MenuScreen;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;

public class GameModeMenuScreen extends MenuScreen {

    public GameModeMenuScreen(final Klotski klotski, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
    }

    /**
     * Creates and initializes the game mode selection screen UI. This method sets up a table layout
     * as the root container, adds a title label, and configures interactive buttons for different
     * game modes and navigation options. Each button triggers a click sound and navigates to a
     * corresponding screen when pressed:
     * <ul>
     * <li>"Legacy" â Opens the legacy game mode menu via {@link GameModeLegacyMenuScreen}</li>
     * <li>"Klotzki" â Directly launches the Klotzki game via {@link KlotzkiGameScreen}</li>
     * <li>"Spectate" â Navigates to the spectate menu via {@link SpectateMenuScreen}</li>
     * <li>"Cooperate" â Navigates to the cooperation menu via {@link CooperateMenuScreen}</li>
     * <li>"Back" â Triggers {@link #handleBack()} to return to the previous screen</li>
     * </ul>
     * All buttons are styled with a consistent width, height, and padding. The table layout centers
     * elements vertically and horizontally, with the title scaled to1.5x the default font size.
     */
    @Override
        protected void create() {
            // Create a table for layout
            Table table = new Table();
            table.setFillParent(true);
            stage.addActor(table);
    
            // Add a title label
            Label titleLabel = new Label("Choose Game Mode", skin);
            titleLabel.setFontScale(1.5f);
            table.add(titleLabel).padBottom(50).row();
    
            // Add "Legacy" button
            TextButton legacyButton = new TextButton("Legacy", skin);
            legacyButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    klotski.setScreen(new GameModeLegacyMenuScreen(klotski, GameModeMenuScreen.this));
                }
            });
            table.add(legacyButton).width(300).height(50).padBottom(20).row();
    
            // Add "Klotzki" button
            TextButton klotzkiButton = new TextButton("Klotzki", skin);
            klotzkiButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    klotski.setScreen(new KlotzkiGameScreen(klotski)); // Navigate to the game screen
                }
            });
            table.add(klotzkiButton).width(300).height(50).padBottom(20).row();
    
            // Add "Spectate" button
            TextButton spectateButton = new TextButton("Spectate", skin);
            spectateButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    ;
                    klotski.setScreen(new SpectateMenuScreen(klotski, klotski.webSocketClient, GameModeMenuScreen.this));
                }
            });
            table.add(spectateButton).width(300).height(50).padBottom(20).row();
    
            // Add "Cooperate" button
            TextButton cooperateButton = new TextButton("Cooperate", skin);
            cooperateButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    ;
                    klotski.setScreen(new CooperateMenuScreen(klotski, klotski.webSocketClient, GameModeMenuScreen.this));
                }
            });
            table.add(cooperateButton).width(300).height(50).padBottom(20).row();
    
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
     * Disposes of all resources associated with this object. This method triggers the reversion
     * of any active focal length animations on the dynamic board, releases the stage and its
     * associated resources, and cleans up the skin's UI assets. It ensures proper cleanup of
     * graphical components, windowing resources, and skin textures to prevent memory leaks
     * and resource retention after the object is no longer in use.
     */
    @Override
        public void dispose() {
            klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
            stage.dispose();
            skin.dispose();
        }

    /**
     * Hides the view by triggering the focal length reversion animation and disabling input processing.
     * This method first initiates a dynamic animation to revert the focal length of the Klotski game board
     * to its original state, providing a visual transition effect. It then disables all input handling
     * by setting the {@link com.badlogic.gdx.InputProcessor} to {@code null}, ensuring no further user
     * interactions are processed until another input processor is set.
     */
    @Override
        public void hide() {
            klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
            Gdx.input.setInputProcessor(null);
        }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation extends the superclass {@code show()} method by triggering an animation
     * on the dynamic board's focal length. The animation transitions the focal length from its
     * current value to {@code10000.0f} over a duration of {@code1.0f} seconds. This effect can be
     * used to emphasize or de-emphasize visual depth in the Klotski game board dynamically.
     * </p>
     */
    @Override
        public void show() {
            super.show();
            klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
        }
}
