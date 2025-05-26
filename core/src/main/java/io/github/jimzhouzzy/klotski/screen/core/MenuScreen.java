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
 * @version 1.1
 * @since 2025-5-25
 * @see {@link ProtoScreen}
 * @see {@link GameModeMenuScreen}
 * @see {@link MainScreen}
 * 
 * Change log:
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

    @Override
    protected void create() {
        // Override the create method to set up the GUI of menu screen
    }

    @Override
    protected void handleBack() {
        klotski.setScreen(lastScreen); // Navigate back to the last screen
    }
}
