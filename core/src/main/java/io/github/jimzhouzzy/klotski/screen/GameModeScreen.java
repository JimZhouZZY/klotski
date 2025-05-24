package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;

public class GameModeScreen implements Screen {

    private final Klotski klotski;
    private final Stage stage;
    private final Skin skin;

    public GameModeScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        klotski.dynamicBoard.setStage(stage);

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/clicked.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                    0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                    0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight());

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/cursor.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                    0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                    0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight());

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);
            }
        });

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ESCAPE:
                        handleBack();
                        return true;
                }
                return false;
            }
        });

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

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
    public void render(float delta) {
        ScreenUtils.clear(klotski.getBackgroundColor());
        klotski.dynamicBoard.render(delta);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // klotski.dynamicBoard = new DynamicBoard(klotski, stage);
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
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private void handleBack() {
        klotski.setScreen(klotski.mainScreen); // Navigate back to the main screen
        klotski.dynamicBoard.triggerAnimateFocalLengthRevert();
    }
}
