/**
 * GameModeBlockedLevelMenuScreen.java
 * 
 * This class represents the game mode blocked level menu screen in the Klotski game.
 * 
 * @author JimZhouZZY
 * @version 1.6
 * @since 2025-05-26
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: Multilevel for blocked
 * 2025-05-27: UI improvement
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: multiple classical level
 */

package io.github.jimzhouzzy.klotski.screen.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.GameScreen;
import io.github.jimzhouzzy.klotski.screen.core.MenuScreen;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;

public class GameModeBlockedLevelMenuScreen extends MenuScreen {
    public GameModeBlockedLevelMenuScreen(Klotski klotski, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
    }

    /**
     * Creates and initializes the UI components for the level selection screen. This method sets up
     * a table-based layout with a title label, five level selection buttons (Level1 to Level5), and
     * a back button. Each level button, when clicked, triggers the creation of a {@link GameScreen}
     * configured for the selected level file (e.g., level1.dat to level5.dat) in "3min-Attack" mode
     * (non-blocked gameplay), switches the active screen to the new game screen, and plays a click sound.
     * The back button navigates to the previous screen and also plays a click sound. All UI elements
     * are styled using the provided skin and include padding, sizing, and font scaling for consistent
     * visual presentation.
     */
    @Override
        protected void create() {
            // Create a table for layout
            Table table = new Table();
            table.setFillParent(true);
            stage.addActor(table);
    
            // Add a title label
            Label titleLabel = new Label("Select Blocked Level", skin);
            titleLabel.setFontScale(1.5f);
            table.add(titleLabel).padBottom(50).row();
    
            // Add 5 level buttons for level1.dat to level5.dat
            for (int i = 1; i <= 5; i++) {
                final int level = i;
                TextButton levelButton = new TextButton("Level " + level, skin);
                levelButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        klotski.playClickSound();
                        String levelString = String.valueOf(level);
                        GameScreen gameScreen = new GameScreen(klotski, levelString);
                        gameScreen.setGameMode(false); // Set to 3min-Attack mode
                        klotski.setGameScreen(gameScreen);
                        klotski.setScreen(gameScreen);
                    }
                });
                table.add(levelButton).width(300).height(50).padBottom(20).row();
            }
    
            // Optionally, add a back button
            TextButton backButton = new TextButton("Back", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    handleBack();
                }
            });
            table.add(backButton).width(300).height(50).padTop(40);
        }
}
