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
 * MenuScreen.java
 * 
 * This class represents the menu screen in the Klotski game.
 * It is used to navigate around the game menus.
 * 
 * It is enherited from the {@link ProtoScreen} class.
 * It should be enherited by other menu screens except {@link MainScreen}.
 *
 * @author JimZhouZZY
 * @version 1.5
 * @since 2025-5-25
 * @see {@link ProtoScreen}
 * @see {@link GameModeMenuScreen}
 * @see {@link MainScreen}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 */

package io.github.jimzhouzzy.klotski.screen.core;

import io.github.jimzhouzzy.klotski.Klotski;

public class MenuScreen extends ProtoScreen {

    public ProtoScreen lastScreen = null; // Store the last screen for back navigation

    public MenuScreen(final Klotski klotski) {
        super(klotski);
        this.lastScreen = klotski.mainScreen; // Default last screen is the main screen
    }

    public MenuScreen(final Klotski klotski, ProtoScreen lastScreen) {
        super(klotski);
        if (lastScreen == null)
            this.lastScreen = klotski.mainScreen; // Fallback to main screen if lastScreen is null
        else 
            this.lastScreen = lastScreen;
    }

    /**
     * Overrides the parent class method to initialize and configure the graphical user interface
     * components specific to the menu screen. This includes setting up layout managers,
     * instantiating UI elements (e.g., buttons, labels, background), attaching event listeners
     * for user interactions, and initializing visual properties such as colors, fonts, and animations.
     * The method is automatically invoked when the menu screen is first created to ensure proper
     * rendering and functionality of the menu interface.
     */
    @Override
        protected void create() {
            // Override the create method to set up the GUI of menu screen
        }

    /**
     * Handles the back navigation action by setting the Klotski application's current screen to the
     * previously stored {@code lastScreen}. This method is triggered when a back navigation event
     * occurs, such as pressing a back button, and updates the display to show the last active screen
     * within the application.
     *
     * @see Klotski#setScreen(Screen)
     * @see #lastScreen
     */
    @Override
        protected void handleBack() {
            klotski.setScreen(lastScreen); // Navigate back to the last screen
        }
}
