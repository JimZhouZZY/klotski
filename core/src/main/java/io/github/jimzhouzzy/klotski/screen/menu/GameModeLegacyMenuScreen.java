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
 * @version 1.1
 * @since 2025-5-25
 * 
 * Change log:
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
import io.github.jimzhouzzy.klotski.screen.core.MenuScreen;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;

public class GameModeLegacyMenuScreen extends MenuScreen {

    public GameModeLegacyMenuScreen(final Klotski klotski, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
    }

    @Override
    protected void create() {
        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Legacy Game Modes", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();

        // Add "Classical" button
        TextButton classicalButton = new TextButton("Classical", skin);
        classicalButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.setScreen(klotski.gameScreen);
            }
        });
        table.add(classicalButton).width(300).height(50).padBottom(20).row();

        // Add "Random Shuffled" button
        TextButton freeGameButton = new TextButton("Shuffled", skin);
        freeGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                klotski.gameScreen.randomShuffle((long) klotski.randomHelper.nextInt(10000)); // Shuffle with seed for Level 1
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
            }
        });
        table.add(freeGameButton).width(300).height(50).padBottom(20).row();

        // Add "3min-Attack" button
        TextButton attackModeButton = new TextButton("Shuffled-Attack", skin);
        attackModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.gameScreen.setGameMode(true); // Set to 3min-Attack mode
                klotski.gameScreen.randomShuffle((long) klotski.randomHelper.nextInt(10000)); // Shuffle with seed for Level 1
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
            }
        });
        table.add(attackModeButton).width(300).height(50).padBottom(20).row();

        // Add "blocked" button
        TextButton blockedButton = new TextButton("Blocked", skin);
        blockedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                // klotski.gameScreen.setBlocked(true); // Set to Blocked mode
                // klotski reset
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

    @Override
    public void dispose() {
        klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void hide() {
        klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
        Gdx.input.setInputProcessor(null);
    }
}
