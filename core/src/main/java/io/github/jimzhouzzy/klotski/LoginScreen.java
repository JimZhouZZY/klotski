package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.MainScreen;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LoginScreen implements Screen {

    private static final String USER_DATA_FILE = "users.dat"; // File to store user data
    private final Klotski klotski;
    private final Stage stage;
    private final Skin skin;
    private final Map<String, String> userDatabase = new HashMap<>();

    public LoginScreen(final Klotski klotski) {
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

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

        // Load user data from file
        loadUserData();

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Login or Register", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();

        // Add username and password fields
        TextField usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        table.add(usernameField).width(300).padBottom(20).row();

        TextField passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        table.add(passwordField).width(300).padBottom(20).row();

        // Add login button
        TextButton loginButton = new TextButton("Login", skin);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (authenticate(username, password)) {
                    klotski.setLoggedInUser(username); // Set the logged-in user's name
                    klotski.setScreen(klotski.mainScreen); // Navigate back to the main screen
                } else {
                    showErrorDialog("Invalid credentials");
                }
            }
        });
        table.add(loginButton).width(200).height(50).padBottom(20).row();

        // Add register button
        TextButton registerButton = new TextButton("Register", skin);
        registerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (register(username, password)) {
                    showErrorDialog("Registration successful! Please log in.");
                } else {
                    showErrorDialog("Registration failed. Username may already exist.");
                }
            }
        });
        table.add(registerButton).width(200).height(50).padBottom(20).row();

        // Add back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(new MainScreen(klotski)); // Navigate back to the main screen
            }
        });
        table.add(backButton).width(200).height(50);
    }

    private boolean authenticate(String username, String password) {
        // Do basic validation
        if (!basicValication(username, password)) {
            return false;
        }

        // Check if the username exists and the password matches
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    private boolean register(String username, String password) {
        // Check if the username already exists
        if (userDatabase.containsKey(username)) {
            return false;
        }

        // Do basic validation
        if (!basicValication(username, password)) {
            return false;
        }

        // Add the new user to the database
        userDatabase.put(username, password);

        // Save the updated user data to the file
        saveUserData();

        return true;
    }

    private boolean basicValication(String username, String password) {
        // Check if the username is valid (not empty)
        if (username == null || username.isEmpty()) {
            return false;
        }
        // Check if the password is valid (not empty)
        if (password == null || password.isEmpty()) {
            return false;
        }
        // Check if the username is too long
        if (username.length() > 20) {
            return false;
        }
        // Check if the password is too long
        if (password.length() > 20) {
            return false;
        }
        // Check if the username contains invalid characters
        if (!username.matches("[a-zA-Z0-9_]+")) {
            return false;
        }

        return true;
    }

    private void loadUserData() {
        File file = new File(Gdx.files.getLocalStoragePath(), USER_DATA_FILE);
        if (!file.exists()) {
            return; // No user data file exists yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userDatabase.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load user data: " + e.getMessage());
        }
    }

    private void saveUserData() {
        File file = new File(Gdx.files.getLocalStoragePath(), USER_DATA_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : userDatabase.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save user data: " + e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        // Create a group to act as the dialog container
        Group dialogGroup = new Group();

        // Create a background for the dialog
        Image background = new Image(skin.newDrawable("white", new Color(1.0f, 1.0f, 1.0f, 0.7f)));
        background.setSize(400, 250);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2, (stage.getHeight() - background.getHeight()) / 2);
        dialogGroup.addActor(background);

        // Create a title label for the dialog
        Label titleLabel = new Label("Error", skin);
        titleLabel.setColor(Color.RED);
        titleLabel.setFontScale(2.0f);
        titleLabel.setPosition(background.getX() + (background.getWidth() - titleLabel.getWidth()) / 2, background.getY() + 180);
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
        okButton.setPosition(background.getX() + (background.getWidth() - okButton.getWidth()) / 2, background.getY() + 20);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogGroup.remove(); // Remove the dialog when OK is clicked
            }
        });
        dialogGroup.addActor(okButton);

        // Add the dialog group to the stage
        stage.addActor(dialogGroup);
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
    }
}