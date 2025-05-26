/**
 * GameModeMenuScreen.java
 * 
 * This class represents the game mode selection screen in the Klotski game.
 *
 * @author JimZhouZZY
 * @version 1.25
 * @since 2025-5-25
 * 
 * Change log:
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
                klotski.gameScreen.setGameMode(true); // Set to 3min-Attack mode
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
    
    @Override
    public void show() {
        super.show();
        klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
    }
}
