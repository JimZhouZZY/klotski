package io.github.jimzhouzzy.klotski.screen;

import io.github.jimzhouzzy.klotski.Klotski;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HelpScreen implements Screen {

    private final Klotski klotski;
    private Stage stage;
    private Skin skin;

    public HelpScreen(Klotski klotski) {
        this.klotski = klotski;
        this.skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));
        create();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);

        Label textLabel = new Label("Welcome to our game!\n\n"+
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
        textLabel.setFontScale(2.0f);

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
                klotski.setScreen(klotski.mainScreen);
            }
            return true;
        });

        table.add(scrollPane).width(600).height(400).padBottom(60).row();
        table.add(backButton).width(200).height(50);
    }
    public void create() {
        stage = new Stage(new ScreenViewport());
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
    }
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        klotski.setGlClearColor();
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        klotski.dynamicBoard.render(delta); // Render the dynamic board

        // Render the stage
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {Gdx.input.setInputProcessor(null);}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
