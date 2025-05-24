package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import io.github.jimzhouzzy.klotski.Klotski;

public class HelpScreen extends ProtoScreen {

    public HelpScreen(Klotski klotski) {
        super(klotski);
    }

    @Override
    public void create() {
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
        
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);

        Label textLabel = new Label("Welcome to our game!\n\n"+
            "Here are some surprises for you to explore!\n\n" +
            "You can click the background and you can see the ripples!\n\n" +
            "You can move the background with direction keys to seek your favorite color!\n\n" +
            "You can change the angle of your background: 'Space' for clockwise and 'control' for counterclockwise\n\n\n\n", labelStyle);
        textLabel.setFontScale(2.0f);

        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center); // center text

        Table textBox = new Table(skin);
        textBox.add(textLabel).width(500).pad(20);

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                klotski.playClickSound();
                klotski.setScreen(klotski.mainScreen);
            }
            return true;
        });

        table.add(textBox).width(540).height(250).padBottom(40).row();
        table.add(backButton).width(200).height(50);
    }
}
