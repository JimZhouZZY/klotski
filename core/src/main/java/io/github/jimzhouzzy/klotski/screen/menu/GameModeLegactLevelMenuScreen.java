/**
 * GameModeLegactLevelMenuScreen.java
 * 
 * This class represents the game mode legacy level menu screen in the Klotski game.
 * 
 * @author JimZhouZZY
 * @version 1.2
 * @since 2025-05-26
 * 
 * Change log:
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

public class GameModeLegactLevelMenuScreen extends MenuScreen {
    public GameModeLegactLevelMenuScreen(Klotski klotski, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
    }

    @Override
    protected void create() {
        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Select Classical Level", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();

        // Add 5 level buttons for level1.dat to level5.dat
        for (int i = 1; i <= 5; i++) {
            final int level = i;
            TextButton levelButton = new TextButton("Classical Level " + level, skin);
            levelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    GameScreen gameScreen = new GameScreen(klotski);
                    gameScreen.setGameMode(false); // Classical mode
                    gameScreen.setLevel(level);    // Set to Level 1~5
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
