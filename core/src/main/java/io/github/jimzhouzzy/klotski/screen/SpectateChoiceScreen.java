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
import io.github.jimzhouzzy.klotski.ui.Dialog;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class SpectateChoiceScreen extends ProtoScreen {

    private final GameWebSocketClient webSocketClient;

    public SpectateChoiceScreen(final Klotski klotski, GameWebSocketClient webSocketClient) {
        super(klotski);
        this.webSocketClient = webSocketClient;
    }

    @Override
    protected void create() {
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
            Dialog.showErrorDialog(klotski, skin, stage, "Unable to connect to the server. Please try again later.", true);
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
            Dialog.showErrorDialog(klotski, skin, stage, "Unable to connect to the server. Please try again later.", true);
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
}
