/*
 * Copyright (C) 2025 Zhiyu Zhou (jimzhouzzy@gmail.com)
 * This file is part of github.com/jimzhouzzy/Klotski.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * LoginScreen.java
 *
 * This class represents the login and registration screen in the Klotski game.
 * It also initializes the user database and handles user authentication when
 * Klotski is starting.
 *
 * @author JimZhouZZY
 * @version 1.34
 * @since 2025-5-25
 * @see {@link Klotski#create()}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: Show error dialog when load-save failed
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
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
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;
import io.github.jimzhouzzy.klotski.ui.component.Dialog;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;

public class LoginScreen extends ProtoScreen {

    private static final String USER_DATA_FILE = ConfigPathHelper.getConfigFilePath("Klotski", "users.dat");
    private static final Map<String, String> userDatabase = new HashMap<>();
    private static TextField usernameField;
    private static TextField passwordField;

    public LoginScreen(final Klotski klotski) {
        super(klotski);
    }

    /**
     * Initializes and configures the UI elements for the login/registration screen. This method sets up
     * input listeners for keyboard events (ESCAPE to return to main screen, ENTER to trigger login),
     * loads existing user data from persistent storage, and constructs a layout using a {@link Table}
     * to organize components. UI elements include username/password text fields (with password masking),
     * a login button (triggers {@link #loginRouter()}), a register button (handles both online and offline
     * registration with error dialogs), and a back button to return to the main screen. All buttons include
     * click sound effects via {@link Klotski#playClickSound()}. The layout is added to the {@link Stage}
     * for rendering and input handling.
     */
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
            titleLabel.setFontScale(1.5f);
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

    /**
     * Handles the user login process by retrieving credentials from input fields and processing based on connectivity.
     * In online mode, delegates authentication to an external service via the {@code login} method. In offline mode,
     * performs local credential validation using {@code authenticate}. On successful offline authentication, sets the
     * logged-in user's name in the application context and navigates to the main screen. Displays an error dialog if
     * offline credentials are invalid. This method does not handle online authentication errors directly.
     */
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

    /**
     * Authenticates a user by validating the provided username and password.
     * <p>
     * This method performs two main checks:
     *1. Basic validation of the username and password (e.g., non-null, non-empty values).
     *2. Verification against the user database to confirm the username exists and the stored password matches the provided password.
     * </p>
     *
     * @param username The username to authenticate.
     * @param password The password associated with the username.
     * @return {@code true} if both basic validation passes and the username-password pair exists in the database;
     * {@code false} if validation fails or no matching credentials are found.
     */
    private boolean authenticate(String username, String password) {
            // Do basic validation
            if (!basicValidation(username, password)) {
                return false;
            }
    
            // Check if the username exists and the password matches
            return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
        }

    /**
     * Registers a new user locally by validating and storing their credentials.
     *
     * This method performs the following steps:
     *1. Checks if the provided username already exists in the user database.
     *2. Validates the username and password using basic criteria (e.g., length, allowed characters).
     *3. If validation passes and the username is unique, adds the user to the database.
     *4. Persists the updated user data to a file.
     *
     * @param username The username to register. Must meet validation criteria from {@link #basicValidation(String, String)}.
     * @param password The password to associate with the username. Must meet validation criteria from {@link #basicValidation(String, String)}.
     * @return {@code true} if registration is successful, {@code false} if the username already exists
     * or validation fails. Returns {@code true} only after successfully updating the database
     * and persisting the changes.
     */
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

    /**
     * Performs basic validation checks on the provided username and password.
     * <p>
     * The method verifies that both the username and password meet the following criteria:
     * <ul>
     * <li>Neither the username nor password is {@code null} or empty.</li>
     * <li>The length of the username does not exceed20 characters.</li>
     * <li>The length of the password does not exceed20 characters.</li>
     * <li>The username contains only alphanumeric characters (a-z, A-Z,0-9) and underscores (_).</li>
     * </ul>
     * If all validations pass, the method returns {@code true}; otherwise, it returns {@code false}.
     *
     * @param username the username to validate
     * @param password the password to validate
     * @return {@code true} if both the username and password satisfy all validation criteria,
     * {@code false} otherwise
     */
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

    /**
     * Loads user data from the specified file (USER_DATA_FILE) into the user database. The method reads
     * each line of the file, expecting lines to be formatted as "key:value". Valid entries (with exactly
     * two segments separated by a colon) are added to the user database. If the file does not exist,
     * the method returns without performing any operations. Malformed lines are silently ignored.
     * In case of an I/O error during reading, an error message is printed to the standard error stream.
     */
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

    /**
     * Saves the user data stored in the userDatabase map to a file specified by USER_DATA_FILE.
     * Each entry in the map is written as a line in the file in the format "key:value". The method
     * uses a BufferedWriter to efficiently write the data and automatically handles resource cleanup
     * via try-with-resources. If an IOException occurs during the write operation, an error message
     * is printed to the standard error stream, but the exception is not propagated further.
     */
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

    /**
     * Attempts to authenticate a user with the provided credentials by sending a POST request to the login endpoint.
     * Performs basic validation of the username and password before proceeding. If validation fails, displays an error dialog.
     * On successful validation, constructs an HTTP request with the credentials as form data and sends it to "http://42.194.132.147:8001/login".
     * Handles the server response asynchronously: if the response starts with "success", extracts the authentication token, updates the logged-in user's
     * state, navigates to the main screen, and initiates a WebSocket connection for the user. If the response indicates failure or the request fails
     * due to network errors, displays appropriate error dialogs. Handles edge cases such as invalid server responses, request cancellation, and exceptions.
     *
     * @param username The username provided for authentication.
     * @param password The password associated with the username.
     * @see HttpResponseListener For handling server response callbacks.
     * @see Net.HttpRequest For details about the HTTP request structure.
     * @see Gdx.app#postRunnable(Runnable) For thread-safe UI updates in the response handler.
     */
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

    /**
     * Registers a new user by sending a POST request to the server with the provided credentials.
     * Validates the username and password using basic validation rules before sending the request.
     * If validation fails, an error dialog is displayed. Upon successful validation, the credentials
     * are URL-encoded and sent to the server's signup endpoint. Handles server responses to display
     * appropriate success or error messages, including cases for invalid input, existing users, server
     * connection issues, and request cancellation. Also handles encoding errors for the request parameters.
     *
     * @param username The username provided by the user for registration.
     * @param password The password provided by the user for registration.
     *
     * @see #basicValidation(String, String) Validates username/password format before submission.
     * @see #showErrorDialog(String) Displays error messages for validation or server response failures.
     * @see #showDialog(String, String) Displays a success message upon successful registration.
     */
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

    /**
     * Displays an error dialog to the user with the specified message. This method utilizes the application's
     * UI components, including the predefined skin and stage, to render a standardized error dialog. The dialog
     * is anchored to the primary application window and styled according to the application's theme.
     *
     * @param message The error message to be displayed in the dialog. This text should clearly communicate
     * the nature of the error to assist the user in resolving the issue.
     */
    private void showErrorDialog(String message) {
            Dialog.showErrorDialog(klotski, skin, stage, message);
        }

    /**
     * Displays a dialog with the specified title and message. The dialog is rendered using the application's
     * current skin and stage configuration, ensuring consistent styling and placement within the UI. This
     * method delegates the creation and display logic to the {@link Dialog#showDialog} method.
     *
     * @param title the text to display as the header or title of the dialog
     * @param message the descriptive content or information to display within the body of the dialog
     */
    private void showDialog(String title, String message) {
            Dialog.showDialog(klotski, skin, stage, title, message);
        }
}
