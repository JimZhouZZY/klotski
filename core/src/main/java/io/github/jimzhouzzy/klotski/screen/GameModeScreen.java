/**
 * GameModeScreen.java
 * 
 * This class represents the game mode selection screen in the Klotski game.
 *
 * @author JimZhouZZY
 * @version 1.20
 * @since 2025-5-25
 * 
 * Change log:
 * 2025-05-25: generate change log
 */

package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.jimzhouzzy.klotski.Klotski;

public class GameModeScreen extends ProtoScreen {

    public GameModeScreen(final Klotski klotski) {
        super(klotski);
    }

    @Override
    protected void create() {
        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Choose Game Mode", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();

        // Add "Free Game" button
        TextButton freeGameButton = new TextButton("Free-Style", skin);
        freeGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                ;
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
            }
        });
        table.add(freeGameButton).width(300).height(50).padBottom(20).row();

        // Add "3min-Attack" button
        TextButton attackModeButton = new TextButton("3min-Attack", skin);
        attackModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.gameScreen.setGameMode(true); // Set to 3min-Attack mode
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
            }
        });
        table.add(attackModeButton).width(300).height(50).padBottom(20).row();

        // Add "Level 1" button
        TextButton level1Button = new TextButton("Level 1", skin);
        level1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                int p = 2 + (int) (Math.random() * 8);
                System.out.println("Random value generated: " + (int) (Math.random() * 8));
                klotski.gameScreen.blockedPieceId(p); // Select a piece with id 2-9
                System.out.println("Value of p: " + p);
                klotski.gameScreen.randomShuffle(10101L); // Shuffle with seed for Level 1
            }
        });
        table.add(level1Button).width(300).height(50).padBottom(20).row();

        // Add "Level 2" button
        TextButton level2Button = new TextButton("Level 2", skin);
        level2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                ;
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                klotski.gameScreen.randomShuffle(10102L); // Shuffle with seed for Level 2
            }
        });
        table.add(level2Button).width(300).height(50).padBottom(20).row();

        // Add "Spectate" button
        TextButton spectateButton = new TextButton("Spectate", skin);
        spectateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                ;
                klotski.setScreen(new SpectateChoiceScreen(klotski, klotski.webSocketClient)); // Navigate to the game screen
            }
        });
        table.add(spectateButton).width(300).height(50).padBottom(20).row();

        // Add "Back" button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                ;
                handleBack();
            }
        });
        table.add(backButton).width(300).height(50);
    }

    @Override
    protected void handleBack() {
        klotski.setScreen(klotski.mainScreen); // Navigate back to the main screen
        klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
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
