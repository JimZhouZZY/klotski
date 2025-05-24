/**
 * LoginScreen.java
 * 
 * This class represents the login and registration screen in the Klotski game.
 * It also initializes the user database and handles user authentication when 
 * Klotski is starting.
 * 
 * @author JimZhouZZY
 * @version 1.26
 * @since 2025-5-25
 * @see {@link Klotski#create()}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 * 
 * Change log:
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-08: update soundfx
 * 2025-05-07: formal login & prepare in-game spectate
 * 2025-05-06: fix file load problem in windows OS
 * 2025-05-06: fix: wrong error dialog when inputed empty credentials
 * 2025-04-30: optimize local storage
 * 2025-04-29: web inspection
 * 2025-04-29: focal length animation
 * 2025-04-29: better error log
 * 2025-04-29: offline mode & optimize save-load
 * 2025-04-28: Online server auth & save-load
 * 2025-04-25: Revert 'fix:resize'
 * 2025-04-24: fix resize changed base tile size
 * 2025-04-24: MSAA & Settings
 * 2025-04-23: better main screen
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
 * 2025-04-21: resizable
 * 2025-04-16: Login & Game Mode & Save-Load
 */

package io.github.jimzhouzzy.klotski.screen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.Dialog;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;

public class LoginScreen extends ProtoScreen {

    private static final ConfigPathHelper configPathHelper = new ConfigPathHelper();
    private static final String USER_DATA_FILE = configPathHelper.getConfigFilePath("Klotski", "users.dat");
    private static final Map<String, String> userDatabase = new HashMap<>();
    private static TextField usernameField;
    private static TextField passwordField;

    public LoginScreen(final Klotski klotski) {
        super(klotski);
    }

    @Override
    protected void create() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case com.badlogic.gdx.Input.Keys.ESCAPE:
                        klotski.setScreen(new MainScreen(klotski)); // Navigate back to the main screen
                        return true;
                    case com.badlogic.gdx.Input.Keys.ENTER:
                        loginRouter();
                        return true;
                    default:
                        return false;
                }
            }
        });

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
        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        table.add(usernameField).width(300).padBottom(20).row();

        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        table.add(passwordField).width(300).padBottom(20).row();

        // Add login button
        TextButton loginButton = new TextButton("Login", skin);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                loginRouter();
            }
        });
        table.add(loginButton).width(200).height(50).padBottom(20).row();

        // Add register button
        TextButton registerButton = new TextButton("Register", skin);
        registerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (!klotski.isOfflineMode()) register(username, password);
                else {
                    if (registerLocal(username, password)) {
                        showDialog("Complete", "Registration successful! Please log in.");
                    } else {
                        showErrorDialog("Registration failed. Username already exists or invalid input.");
                    }
                }
            }
        });
        table.add(registerButton).width(200).height(50).padBottom(20).row();

        // Add back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setScreen(new MainScreen(klotski)); // Navigate back to the main screen
            }
        });
        table.add(backButton).width(200).height(50);
    }

    private void loginRouter() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (!klotski.isOfflineMode()) {
            login(username, password);
        }
        else {
            if (authenticate(username, password)) {
                klotski.setLoggedInUser(username); // Set the logged-in user's name
                klotski.setScreen(klotski.mainScreen); // Navigate to the main screen
            } else {
                showErrorDialog("Invalid credentials");
            }
        }
    }
    
    private boolean authenticate(String username, String password) {
        // Do basic validation
        if (!basicValidation(username, password)) {
            return false;
        }

        // Check if the username exists and the password matches
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    private boolean registerLocal(String username, String password) {
        // Check if the username already exists
        if (userDatabase.containsKey(username)) {
            return false;
        }

        // Do basic validation
        if (!basicValidation(username, password)) {
            return false;
        }

        // Add the new user to the database
        userDatabase.put(username, password);

        // Save the updated user data to the file
        saveUserData();

        return true;
    }

    private boolean basicValidation(String username, String password) {
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
        File file = new File(USER_DATA_FILE);
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
        File file = new File(USER_DATA_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : userDatabase.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save user data: " + e.getMessage());
        }
    }

    private void login(String username, String password) {
        if (!basicValidation(username, password)) {
            showErrorDialog("Invalid username or password");
            return;
        }

        System.out.println("Attempting to log in with username: " + username + " and password: " + password);
        // Create HTTP request
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder
                .newRequest()
                .method(Net.HttpMethods.POST)
                .url("http://42.194.132.147:8001/login")
                .content("username=" + username + "&password=" + password)
                .build();

        // Send request
        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> {
                    if (response.startsWith("success")) {
                        String[] parts = response.split(":");
                        String token = null;
                        if (parts.length > 1) {
                            token = parts[1];
                        }
                        System.out.println("Got Token: " + token);
                        klotski.setLoggedInUser(username, token); // Set the logged-in user's name
                        klotski.setScreen(klotski.mainScreen); // Navigate to the main screen

                        // Start WebSocket client
                        klotski.webSocketClient.send("login:" + username);
                        System.out.println("WebSocket client started for user: " + username);
                    } else if (response.startsWith("failure")) {
                        showErrorDialog("Invalid credentials");
                    } else {
                        showErrorDialog("Failed to connect to the server");
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                showErrorDialog("Failed to connect to the server: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                showErrorDialog("Request cancelled");
            }
        });
    }
    
    private void register(String username, String password) {
        try {
            if (!basicValidation(username, password)) {
                showErrorDialog("Invalid username or password");
                return;
            }

            // Encode parameters
            String content = "username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8");

            // Create HTTP request
            HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
            Net.HttpRequest request = requestBuilder
                    .newRequest()
                    .method(Net.HttpMethods.POST)
                    .url("http://42.194.132.147:8001/signup") // Ensure this matches your server's URL
                    .content(content)
                    .build();

            // Send request
            Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                    String response = httpResponse.getResultAsString();
                    if ("success".equals(response)) {
                        showDialog("Complete", "Registration successful! Please log in.");
                    } else if ("failure: invalid input".equals(response)) {
                        showErrorDialog("Registration failed. Invalid input.");
                    } else if ("failure: user already exists".equals(response)) {
                        showErrorDialog("Registration failed. Username already exists.");
                    } else {
                        showErrorDialog("Failed to connect to the server");
                    }
                }

                @Override
                public void failed(Throwable t) {
                    showErrorDialog("Failed to connect to the server: " + t.getMessage());
                }

                @Override
                public void cancelled() {
                    showErrorDialog("Request cancelled");
                }
            });
        } catch (UnsupportedEncodingException e) {
            showErrorDialog("Failed to encode request parameters: " + e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        Dialog.showErrorDialog(klotski, skin, stage, message);
    }

    private void showDialog(String title, String message) {
        Dialog.showDialog(klotski, skin, stage, title, message);
    }
}
