/**
 * KlotzkiGameScreen.java
 * 
 * This class represents the game screen in the Klotzki game.
 * 
 * Change log:
 * 2025-05-26: refactor screens & add Kltozki game
 */

package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.KlotzkiBoard;

public class KlotzkiGameScreen extends ApplicationAdapter implements Screen {
    
    protected final Klotski klotski;
    protected final Stage stage;
    protected final Skin skin;
    private final KlotzkiBoard klotzkiBoard;

    public KlotzkiGameScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json")); 

        klotzkiBoard = new KlotzkiBoard(klotski, stage);
    }

    public void create() {
        // Override and set up the stage and UI elements here
    }

    protected void handleBack() {
        // Override to handle ESC key press
        // Default navigate back to the main screen
        klotski.setScreen(klotski.mainScreen);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(klotski.getGlClearColor());

        // Render the dynamic board
        klotzkiBoard.render(delta);

        // Render the stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        klotzkiBoard.setStage(stage);
    }
}
