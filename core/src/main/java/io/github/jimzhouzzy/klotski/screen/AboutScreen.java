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
 * HelpScreen.java
 *
 * This class represents the help screen in the Klotski game.
 * It provides instructions and tips for players on how to play the game.
 *
 * @author Tommy-SUStech
 * @version 1.22
 * @since 2025-5-25
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: Rename HelpScreen to AboutScreen
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Make the sepctsteScreen more pretty.
 * 2025-05-24: Highlight the number of the selected number (#14)
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-24: Add H,J,K,L for changing the selected  block.
 * 2025-05-24: fix: bad init process of HelpScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-22: Fix the HelpScreen's BUG! (#11)
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 */

package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;

public class AboutScreen extends ProtoScreen {

    public AboutScreen(Klotski klotski) {
        super(klotski);
    }

    /**
     * Initializes and configures the UI components for a help or tips screen. Creates a root table
     * that fills the stage and contains a title label, a scrollable text area with game instructions,
     * and a navigation button. The text area displays gameplay tips including control shortcuts
     * (ESCAPE, R, I, U, Y, A, SPACE/ENTER, H/J/K/L), navigation features (Login, Settings, Exit),
     * and hidden background interaction surprises. Configures scroll pane behavior to allow vertical
     * scrolling with adjusted sensitivity and visible scrollbars. Adds a 'Back' button that triggers
     * navigation return logic through {@link #handleBack()} and plays an interface sound on interaction.
     * Uses the application skin for consistent styling of all UI elements.
     */
    @Override
        public void create() {
            Table table = new Table();
            table.setFillParent(true);
            stage.addActor(table);
    
            // Add a title label
            Label titleLabel = new Label("Welcome to our game!", skin, "title");
            titleLabel.setFontScale(1.5f);
            table.add(titleLabel).padBottom(50).row();
    
            Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
            Label textLabel = new Label(
                "Here are some tips for you!\n\n" +
                "You can click the 'Play' and choose the level to start the game!\n\n" +
                "There are also some shortcut keys for you to play games:\n\n" +
                "ESCAPE: Exit  R: Restart  I: Hint  U: Undo  Y: Redo  A: Auto  SPACE/ENTER: first time for Auto and the second time for Stop! \n\n" +
                "H / J / K / L : moving the selected piece in the game! H: Left  J: Down  K: Up  L: Right! \n\n" +
                "You can click the 'Login' to log in and we will store or read your gaming data!\n\n" +
                "You can click the 'Settings' to set your own gaming environment!\n\n" +
                "You can click the 'Exit' to exit the game!\n\n" +
                "Some Surprises for you to explore:\n\n" +
                "You can click the background and you can see the ripples!\n\n" +
                "You can move the background with direction keys to seek your favorite color!\n\n" +
                "You can change the angle of your background: 'Space' for clockwise and 'control' for counterclockwise\n\n\n\n", labelStyle);
            textLabel.setFontScale(1.0f);
    
            textLabel.setWrap(true);
            textLabel.setAlignment(Align.center); // center text
    
            Table textBox = new Table(skin);
            textBox.add(textLabel).width(500).pad(20);
            ScrollPane scrollPane = new ScrollPane(textBox, skin);
            scrollPane.setFadeScrollBars(false);
            scrollPane.setScrollingDisabled(true, false); // allow vertical scrolling only
    
            // Reduce scroll sensitivity and tweak scroll pane behavior
            scrollPane.setScrollY(0); // Optional reset
            scrollPane.setFlickScroll(false); // Disable flick-based fast scrolling
            scrollPane.setScrollbarsOnTop(true);
            scrollPane.setVariableSizeKnobs(false); // Disable proportional scrollbar sizing
    
            TextButton backButton = new TextButton("Back", skin);
            backButton.addListener(event -> {
                if (event.toString().equals("touchDown")) {
                    klotski.playClickSound();
                    handleBack();
                }
                return true;
            });
    
            table.add(scrollPane).width(600).height(400).padBottom(60).row();
            table.add(backButton).width(200).height(50);
        }
}
