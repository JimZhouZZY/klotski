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
 * Dialog.java
 *
 * This class provides static methods to show a dialog box with a message and an OK button.
 * It is used to display error messages or other information to the user.
 * See {@link LoginScreen} for practical usage.
 *
 * Klotski instance is used to play sounds when the dialog is shown and when the OK button is clicked.
 * Skin instance is used to style the dialog elements.
 * Stage instance is used to add the dialog to the screen.
 *
 * Example usage:
 *     Dialog.showErrorDialog(klotski, skin, stage, "An error occurred while loading the game.");
 *     Dialog.showDialog(klotski, skin, stage, "Notification","User logged in successfully.");
 *
 * @author JimZhouZZY
 * @version 1.12
 * @since 2025-5-25
 * @see {@link LoginScreen}
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: modify font
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
 */

package io.github.jimzhouzzy.klotski.ui.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.github.jimzhouzzy.klotski.Klotski;

public class Dialog {
    /**
     * Displays an error dialog with a specified message within the Klotski application. This dialog
     * uses the provided UI skin and is attached to the given stage, ensuring it remains modal and
     * centered. The dialog title is set to "Error", and the content is populated with the provided
     * message. This method delegates to the general-purpose {@code showDialog} method, passing
     * the appropriate error-specific parameters.
     *
     * @param klotski the main Klotski application instance to which the dialog belongs
     * @param skin the UI skin used to style the dialog and its components
     * @param stage the parent stage for the dialog, ensuring proper modality and positioning
     * @param message the error message to display in the dialog body
     */
    public static void showErrorDialog(Klotski klotski, Skin skin, Stage stage, String message) {
            showDialog(klotski, skin, stage, "Error", message);
        }

    /**
     * Displays a modal dialog box on the specified stage with a semi-transparent background, a title, a message,
     * and an "OK" button. The dialog is centered within the stage and styled using the provided skin. When invoked,
     * the dialog plays an alert sound from the {@link Klotski} instance. The dialog includes a title label aligned at the top,
     * a message label with text wrapping positioned below the title, and a centered "OK" button that removes the dialog
     * and plays a click sound when clicked. The dialog's background and components are added to a group that is overlaid
     * on the stage. The dialog's dimensions, positioning, and visual elements (e.g., colors, fonts) are derived from the
     * skin and stage parameters.
     *
     * @param klotski the {@link Klotski} game instance used to trigger alert and click sounds
     * @param skin the {@link Skin} providing UI styling and resources for dialog components
     * @param stage the {@link Stage} on which the dialog will be displayed
     * @param title the text to display as the dialog's title
     * @param message the message content to display within the dialog, wrapped as needed
     */
    public static void showDialog(Klotski klotski, Skin skin, Stage stage, String title, String message) {
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
            Label titleLabel = new Label(title, skin);
            titleLabel.setFontScale(1.0f);
            titleLabel.setAlignment(Align.center); // Align the text to the center
            titleLabel.setSize(background.getWidth(), titleLabel.getHeight()); // Match the width of the background
            titleLabel.setPosition(background.getX(), background.getY() + 180); // Position it relative to the background
            dialogGroup.addActor(titleLabel);
    
            // Create a label for the error message
            Label messageLabel = new Label(message, skin);
            messageLabel.setColor(Color.BLACK);
            messageLabel.setFontScale(1f);
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
                }
            });
            dialogGroup.addActor(okButton);
    
            // Add the dialog group to the stage
            stage.addActor(dialogGroup);
        }
}
