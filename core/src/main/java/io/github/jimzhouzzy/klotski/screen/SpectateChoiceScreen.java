package io.github.jimzhouzzy.klotski.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class SpectateChoiceScreen implements Screen {

    private final Klotski klotski;
    private final Stage stage;
    private final Skin skin;
    private final GameWebSocketClient webSocketClient;

    public SpectateChoiceScreen(final Klotski klotski, GameWebSocketClient webSocketClient) {
        this.klotski = klotski;
        this.webSocketClient = webSocketClient;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);

        // Add 10% screen height padding to the top and bottom
        float screenHeight = Gdx.graphics.getHeight();
        table.padTop(screenHeight * 0.1f).padBottom(screenHeight * 0.1f);

        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Choose a Player to Spectate", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();

        // Check if webSocketClient is null or not connected
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            showErrorDialog("Unable to connect to the server. Please try again later.", true);
            return;
        }

        // Request online users from the server
        requestOnlineUsers(table);

        // Add a "Back" button at the bottom
        addBackButton(table);
    }

    private void requestOnlineUsers(Table table) {
        // Check if webSocketClient is null
        if (webSocketClient == null) {
            showErrorDialog("Unable to connect to the server. Please try again later.", true);
            return;
        }

        // Set a callback to handle the server's response
        webSocketClient.setOnMessageListener(message -> {
            System.out.println("Test output");
            if (message.startsWith("Online users: ")) {
                String[] users = message.substring("Online users: ".length()).split(", ");
                Gdx.app.postRunnable(() -> {
                    populateUserButtons(table, users); // Populate user buttons
                    addBackButton(table); // Add the "Back" button at the bottom
                });
            }
        });

        // Send "GetOnlineUsers" request to the server
        webSocketClient.send("GetOnlineUsers");
    }

    private void populateUserButtons(Table table, String[] users) {
        table.clear(); // Clear the table before adding new buttons

        // Add a title label
        Label titleLabel = new Label("Choose a Player to Spectate", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();
        for (String user : users) {
            System.out.println("Adding button for user: " + user);
            Table buttonContainer = new Table(skin);
            buttonContainer.setBackground("white"); // Ensure 'white' drawable exists in skin

            TextButton userButton = new TextButton(user, skin);
            userButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    spectateUser(user);
                }
            });

            buttonContainer.add(userButton).width(300).height(50).pad(10);
            table.add(buttonContainer).padBottom(20).row();
        }
    }

    private void addBackButton(Table table) {
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setScreen(new GameModeScreen(klotski)); // Navigate to the GameModeScreen
                klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
            }
        });
        table.add(backButton).width(300).height(50).padTop(20).expandY().bottom(); // Ensure it's at the bottom
    }

    private void spectateUser(String user) {
        System.out.println("Spectating user: " + user);
        klotski.setScreen(new SpectateScreen(klotski, user, klotski.webSocketClient)); // Navigate to the SpectateScreen
    }

    // TODO: avoid repeated code.
    private void showErrorDialog(String message, boolean existOnClose) {
        // Play alert sound
        klotski.playAlertSound();

        // Create a group to act as the dialog container
        Group dialogGroup = new Group();

        // Create a background for the dialog
        Image background = new Image(skin.newDrawable("white", new Color(1.0f, 1.0f, 1.0f, 0.7f)));
        background.setSize(400, 250);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2,
                (stage.getHeight() - background.getHeight()) / 2);
        dialogGroup.addActor(background);

        // Create a title label for the dialog
        Label titleLabel = new Label("Error", skin);
        titleLabel.setColor(Color.RED);
        titleLabel.setFontScale(2.0f);
        titleLabel.setPosition(background.getX() + (background.getWidth() - titleLabel.getWidth()) / 2,
                background.getY() + 180);
        dialogGroup.addActor(titleLabel);

        // Create a label for the error message
        Label messageLabel = new Label(message, skin);
        messageLabel.setColor(Color.BLACK);
        messageLabel.setFontScale(1.5f);
        messageLabel.setWrap(true);
        messageLabel.setWidth(360);
        messageLabel.setPosition(background.getX() + 20, background.getY() + 100);
        dialogGroup.addActor(messageLabel);

        // Create an OK button
        TextButton okButton = new TextButton("OK", skin);
        okButton.setSize(100, 40);
        okButton.setPosition(background.getX() + (background.getWidth() - okButton.getWidth()) / 2,
                background.getY() + 20);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                dialogGroup.remove(); // Remove the dialog when OK is clicked
                klotski.setScreen(new GameModeScreen(klotski)); // Navigate to the GameModeScreen
                klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
            }
        });
        dialogGroup.addActor(okButton);

        // Add the dialog group to the stage
        stage.addActor(dialogGroup);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(klotski.getBackgroundColor());
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
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
